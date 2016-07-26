package com.recipe.recipe;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AddIngredientActivity extends AppCompatActivity {

    private static String TAG = "AddIngredientActivity";

    EditText ingredient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_ingredient);

        ingredient = (EditText) findViewById(R.id.etxtIngredientName);
    }

    public void saveIngredient(View view) {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        String key = database.child("ingredients").push().getKey();
        database.child("ingredients").child(key).child("name").setValue(ingredient.getText().toString());

        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This is called once initially with the initial value and
                // again whenever data at this location is updated
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", databaseError.toException());
            }
        });

        Intent i = new Intent(getApplicationContext(), TestActivity.class);
        startActivity(i);
    }
}
