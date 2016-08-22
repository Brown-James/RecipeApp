package com.recipe.recipe;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/**
 * Created by James on 13/08/2016.
 */
public class RecipeRVAdapter extends RecyclerView.Adapter<RecipeRVAdapter.RecipeViewHolder> {

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

        holder.recipeName.setText(r.getName());
        holder.recipeDescription.setText(r.getDescription());
        holder.thumbnail.setImageBitmap(r.getThumbnail());

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
