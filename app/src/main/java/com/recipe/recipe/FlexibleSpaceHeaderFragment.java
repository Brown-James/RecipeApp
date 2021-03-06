package com.recipe.recipe;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.InjectView;

import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.nineoldandroids.view.ViewHelper;

import java.util.Observable;
import java.util.Observer;

public class FlexibleSpaceHeaderFragment extends Fragment implements Observer, ObservableScrollViewCallbacks {
    public static final String TAG = "FlexibleSpaceHeaderFrag";

    @InjectView (R.id.observable_sv)
    ObservableScrollViewWithFling mScrollView;

    @InjectView(R.id.title)
    TextView mTitle; // Title used instead of Toolbar.title

    @InjectView(R.id.tv_data)
    TextView mDescription;

    @InjectView(R.id.toolbar_view)
    Toolbar mToolbarView;

    @InjectView(R.id.ll_above_photo)
    protected LinearLayout llTintLayer; //Layout that we're tinting when scrolling

    @InjectView(R.id.fl_image)
    protected FrameLayout flImage; // Layout that hosts the header image

    @InjectView(R.id.imgRecipeInfoImage)
    ImageView image;

    private int mParralaxImageHeight;
    private int mScrollY;
    private boolean mIsToolbarShown = true;
    private int mToolbarHeight;
    private boolean goingUp = false;

    private int mToolbarBackgroundColour;

    private Recipe recipe;

    public FlexibleSpaceHeaderFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_flexible_space_header, container, false);
        ButterKnife.inject(this, view);

        RecipeInfoActivity activity = (RecipeInfoActivity) getActivity();
        recipe = activity.getRecipe();

        mTitle.setText(recipe.getName());
        image.setImageBitmap(recipe.getThumbnail());
        mDescription.setText(recipe.getDescription() + "\n\n" + getString(R.string.lorem_ipsum));

        // Observe the recipe - will see change in bitmap as its proper picture downloads fully
        recipe.addObserver(this);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Store flexible space height
        mParralaxImageHeight = getResources().getDimensionPixelSize(R.dimen.parallax_image_height);

        flImage.post(new Runnable() {

            @Override
            public void run() {
                Log.d(TAG, "Width: " + flImage.getWidth() + " Height: " + flImage.getHeight());
                recipe.downloadThumbnail(flImage.getWidth(), flImage.getHeight() + mParralaxImageHeight);
            }
        });

        configureToolbarView();
        configureScrollView();
    }

    private void configureScrollView() {
        mScrollView.setScrollViewCallbacks(this);
        mScrollView.setOverScrollMode(View.OVER_SCROLL_NEVER);

        mScrollView.setOnFlingListener(new ObservableScrollViewWithFling.OnFlingListener() {
            @Override
            public void onFlingStarted() {
                if(goingUp && !mIsToolbarShown) {
                    showFullToolbar(50);
                }
            }

            @Override
            public void onFlingStopped() {

            }
        });

        ViewTreeObserver vto = mTitle.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    mTitle.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    mTitle.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }

                updateFlexibleSpaceText(0);
            }
        });
    }

    private void configureToolbarView() {
        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbarView);

        // Remove toolbars title, as we have our own title implementation
        mToolbarView.post(new Runnable() {
            @Override
            public void run() {
                mToolbarView.setTitle("");
            }
        });

        mToolbarBackgroundColour = getResources().getColor(R.color.colorPrimary);
        TypedValue tv = new TypedValue();
        if(getActivity().getTheme().resolveAttribute(R.attr.actionBarSize, tv, true)) {
            mToolbarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
        }

        setBackgroundAlpha(mToolbarView, 0.0f, mToolbarBackgroundColour);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.recipe_info_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_recipe_info_share:
                Toast.makeText(getActivity(), "Coming soon.", Toast.LENGTH_SHORT).show();
                break;

            case R.id.menu_recipe_info_favourite:
                Toast.makeText(getActivity(), "Coming soon.", Toast.LENGTH_SHORT).show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {
        // Store actual scroll state:
        if(mScrollY > scrollY) {
            goingUp = true;
        } else if (mScrollY < scrollY){
            goingUp = false;
        }

        // If we're close to edge, show toolbar faster
        if(mScrollY - scrollY > 50 && !mIsToolbarShown) {
            showFullToolbar(50); // speed up
        } else if (mScrollY - scrollY > 0 && scrollY <= mParralaxImageHeight && !mIsToolbarShown) {
            showFullToolbar(250);
        }

        // Show or hide full toolbar colour, so it will become visible over scrollable
        if (scrollY >= mParralaxImageHeight - mToolbarHeight) {
            setBackgroundAlpha(mToolbarView, 1, mToolbarBackgroundColour);
        } else {
            setBackgroundAlpha(mToolbarView, 0, mToolbarBackgroundColour);
        }

        // Translate flexible image in Y axis
        ViewHelper.setTranslationY(flImage, scrollY / 2);

        // Calculate flexible space alpha based on scroll state
        float alpha = 1 - (float) Math.max(0, mParralaxImageHeight - (mToolbarHeight) - scrollY) / (mParralaxImageHeight - (mToolbarHeight * 1.5f));
        setBackgroundAlpha(llTintLayer, alpha, mToolbarBackgroundColour);

        // Store last scroll state
        mScrollY = scrollY;

        // Move flexible text
        updateFlexibleSpaceText((scrollY));
    }

    @Override
    public void onDownMotionEvent() {

    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {
        // If we're scrolling up, and are too far away from toolbar, hide it:
        if (scrollState == ScrollState.UP) {
            if(mScrollY > mParralaxImageHeight) {
                if (mIsToolbarShown) {
                    hideFullToolbar();
                }
            } else {
                // Don't hide toolbar yet
            }
        } else if (scrollState == ScrollState.DOWN) {
            // Show toolbar as fast as we're starting to scroll down
            if (!mIsToolbarShown) {
                showFullToolbar(250);
            }
        }
    }

    private void setBackgroundAlpha(View view, float alpha, int baseColor) {
        int a = Math.min(255, Math.max(0, (int) (alpha * 255))) << 24;
        int rgb = 0x00ffffff & baseColor;
        view.setBackgroundColor(a + rgb);
    }

    public void showFullToolbar(int duration) {
        mIsToolbarShown = true;

        final AnimatorSet animatorSet = buildAnimationSet(duration,
                buildAnimation(mToolbarView, -mToolbarHeight, 0),
                buildAnimation(mTitle, -mToolbarHeight, 0));

        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                updateFlexibleSpaceText(mScrollY); // dirty update fling-fix
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                updateFlexibleSpaceText(mScrollY); // dirty update fling-fix
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        animatorSet.start();
    }

    private ObjectAnimator buildAnimation(View view, float from, float to) {
        return ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, from, to);
    }

    public void hideFullToolbar() {
        mIsToolbarShown = false;
        final AnimatorSet animatorSet = buildAnimationSet(250,
                buildAnimation(mToolbarView, 0, -mToolbarHeight),
                buildAnimation(mTitle, 0, -mToolbarHeight));
        animatorSet.start();
    }

    private AnimatorSet buildAnimationSet(int duration, ObjectAnimator... objectAnimators) {
        AnimatorSet a = new AnimatorSet();
        a.playTogether(objectAnimators);
        a.setInterpolator(AnimationUtils.loadInterpolator(getActivity(), android.R.interpolator.accelerate_decelerate));
        a.setDuration(duration);

        return a;
    }

    /**
     * Scale title view and move it in Flexible space
     * @param scrollY
     */
    private void updateFlexibleSpaceText(final int scrollY){
        if (!mIsToolbarShown) return;

        int adjustedScrollY = scrollY;
        if (scrollY < 0) {
            adjustedScrollY = 0;
        } else if (scrollY > mParralaxImageHeight) {
            adjustedScrollY = mParralaxImageHeight;
        }

        float maxScale = 0.75f;
        float scale = maxScale * ((float) (mParralaxImageHeight - mToolbarHeight) - adjustedScrollY) / (mParralaxImageHeight - mToolbarHeight);
        if(scale < 0 ) {
            scale = 0;
        }

        ViewHelper.setPivotX(mTitle, 0);
        ViewHelper.setPivotY(mTitle, 0);
        ViewHelper.setScaleX(mTitle, 1 + scale);
        ViewHelper.setScaleY(mTitle, 1 + scale);

        int maxTitleTranslation = (int) (mParralaxImageHeight * 0.4f);
        int titleTranslation = (int) (maxTitleTranslation * ((float) scale / maxScale));
        ViewHelper.setTranslationY(mTitle, titleTranslation);
    }

    @Override
    public void update(Observable observable, Object data) {
        // Update to the newest bitmap
        image.setImageBitmap(recipe.getThumbnail());
    }
}
