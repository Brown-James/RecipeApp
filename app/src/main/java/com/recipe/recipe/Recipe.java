package com.recipe.recipe;

import android.graphics.Bitmap;

/**
 * Created by James on 13/08/2016.
 */
public class Recipe {

    String name;
    String description;
    String id;
    Bitmap thumbnail;

    public Recipe(String id, String name, String description, Bitmap thumbnail) {
        this.name = name;
        this.description = description;
        this.id = id;
        this.thumbnail = thumbnail;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Bitmap getThumbnail() {
        return thumbnail;
    }
}
