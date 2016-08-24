package com.recipe.recipe;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;

public class SuggestRecipeActivity extends AppCompatActivity {

    private static String TAG = "SuggestRecipe";

    private ArrayList<Ingredient> ingredients;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggest_recipe);

        Log.d(TAG, "Starting to download ingredients");

        ingredients = new ArrayList<Ingredient>();

        // Download all ingredients from the database
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        ref.child("ingredients").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterator<DataSnapshot> children = dataSnapshot.getChildren().iterator();

                while(children.hasNext()) {
                    DataSnapshot child = children.next();

                    String id = child.getKey().toString();
                    String name = child.child("name").getValue().toString();

                    Ingredient i = new Ingredient(id, name);
                    ingredients.add(i);
                }

                Log.d(TAG, "Done downloading ingredients");

                for(Ingredient i : ingredients) {
                    System.out.println(i);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "Something has gone wrong downloading all ingredients", databaseError.toException());
            }
        });
    }
}
