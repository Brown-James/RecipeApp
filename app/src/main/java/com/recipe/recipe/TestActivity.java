package com.recipe.recipe;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.fernandocejas.arrow.optional.Optional;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.TwitterAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.twitter.sdk.android.Twitter;
import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import io.fabric.sdk.android.Fabric;

public class TestActivity extends AppCompatActivity {

    private static String TAG = "TestActivity";
    private ArrayList<Recipe> recipes;

    private RecyclerView recipeRecyclerView;
    private RecipeRVAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_test);

        recipes = new ArrayList<Recipe>();

        recipeRecyclerView = (RecyclerView) findViewById(R.id.main_recipe_list);
        recipeRecyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getApplicationContext());
        llm.setOrientation(LinearLayout.VERTICAL);
        recipeRecyclerView.setLayoutManager(llm);
        adapter = new RecipeRVAdapter(recipes);
        recipeRecyclerView.setAdapter(adapter);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        ref.child("recipes")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        Iterator<DataSnapshot> children = dataSnapshot.getChildren().iterator();

                        while(children.hasNext()) {
                            DataSnapshot recipe = children.next();

                            Log.d(TAG, recipe.toString());

                            final String id = recipe.getKey();
                            final String name = recipe.child("name").getValue().toString();
                            final String description = recipe.child("description").getValue().toString();
                            final Optional<Bitmap> thumbnail = Optional.absent();
                            addRecipeToList(new Recipe(getApplicationContext(), id, name, description, thumbnail, adapter));
                    }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(getApplicationContext(), "Failed to get recipes", Toast.LENGTH_SHORT).show();
                    }
                });

        if(!RecipeUser.isCurrentUserPremium()){
            Log.d(TAG, "Current user isn't premium");

            MobileAds.initialize(getApplicationContext(), getResources().getString(R.string.banner_bottom_home_unit_id));

            AdView mAdView = (AdView) findViewById(R.id.adView);
            AdRequest adRequest = new AdRequest.Builder().addTestDevice("9F2A3EB551323664824809D9EA0B76ED").build();
            mAdView.loadAd(adRequest);
        }

        Log.d(TAG, FirebaseAuth.getInstance().getCurrentUser().getUid());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_main_settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                break;
            case R.id.menu_main_sign_out:
                signOut();
                break;
            case R.id.menu_main_add_ingredient:
                Intent addIngredientIntent = new Intent(this, AddIngredientActivity.class);
                startActivity(addIngredientIntent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void signOut() {
        FirebaseAuth.getInstance().signOut();

        Twitter.logOut();
        LoginManager.getInstance().logOut();

        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
    }

    private void addRecipeToList(Recipe r) {
        recipes.add(r);
        adapter.notifyDataSetChanged();
    }
}
