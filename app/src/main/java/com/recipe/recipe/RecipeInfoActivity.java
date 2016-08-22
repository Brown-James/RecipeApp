package com.recipe.recipe;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.fernandocejas.arrow.optional.Optional;

public class RecipeInfoActivity extends AppCompatActivity {

    Recipe r;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent i = getIntent();
        String name = i.getStringExtra("name");
        String description = i.getStringExtra("description");
        String id = i.getStringExtra("id");

        this.r = new Recipe(getApplicationContext(), id, name, description, Optional.<Bitmap>absent());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_info);
    }

    public Recipe getRecipe() {
        return r;
    }
}
