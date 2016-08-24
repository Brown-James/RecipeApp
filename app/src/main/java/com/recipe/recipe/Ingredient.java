package com.recipe.recipe;

/**
 * Created by james on 24/08/16.
 */
public class Ingredient {

    private String id;
    private String name;

    public Ingredient(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return "id: " + id + "  name: " + name;
    }
}
