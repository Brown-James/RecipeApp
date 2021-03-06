package com.recipe.recipe;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by James on 13/08/2016.
 */
public class RecipeRVAdapter extends RecyclerView.Adapter<RecipeRVAdapter.RecipeViewHolder>
    implements Observer {

    private static String TAG = "RecipeRVAdapter";

    Context context;
    List<Recipe> recipes;

    public RecipeRVAdapter(List<Recipe> recipes) {
        this.recipes = recipes;
    }

    @Override
    public RecipeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recipe_menu_layout, parent, false);
        RecipeViewHolder rvh = new RecipeViewHolder(v);
        return rvh;
    }

    @Override
    public void onBindViewHolder(RecipeViewHolder holder, int i) {
        final Recipe r = recipes.get(i);
        r.addObserver(this);

        holder.recipeName.setText(r.getName());
        holder.recipeDescription.setText(r.getDescription());
        holder.thumbnail.setImageBitmap(r.getThumbnail());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN &&
                !r.thumbnailDownloaded) {
            r.downloadThumbnail(holder.thumbnail.getMaxWidth(), holder.thumbnail.getMaxHeight());
        } else {
            // Write some code here to deal with lower APIs
        }

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, RecipeInfoActivity.class);
                i.putExtra("name", r.getName());
                i.putExtra("description", r.getDescription());
                i.putExtra("id", r.getId());

                context.startActivity(i);
            }
        });
    }

    @Override
    public void update(Observable observable, Object data) {
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView view) {
        super.onAttachedToRecyclerView(view);
    }

    public static class RecipeViewHolder extends RecyclerView.ViewHolder{
        CardView cardView;
        TextView recipeName;
        TextView recipeDescription;
        ImageView thumbnail;

        public RecipeViewHolder(View itemView){
            super(itemView);
            cardView = (CardView) itemView.findViewById(R.id.crdRecipeCardView);
            recipeName = (TextView) itemView.findViewById(R.id.txtRecipeName);
            recipeDescription = (TextView) itemView.findViewById(R.id.txtRecipeDescription);
            thumbnail = (ImageView) itemView.findViewById(R.id.imgRecipeImage);
        }
    }
}
