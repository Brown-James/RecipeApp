package com.recipe.recipe;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.recipe.recipe.LoginActivity;

/**
 * Created by James on 30/07/2016.
 */
public class RecipeUser {

    // This is kind of hacky but it works ¯\_(ツ)_/¯
    private static boolean isPremium = true;

    private static String TAG = "RecipeUser";

    public static boolean isCurrentUserPremium() {
        final String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        Query query = ref.child("users").child(userID).child("groups").child("premium");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    isPremium = Boolean.parseBoolean(dataSnapshot.getValue().toString());
                } catch (NullPointerException e) {
                    Log.w(TAG, "This user doesn't have a premium entry: " + userID, e);
                    isPremium = false;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "Failed to find if user is premium", databaseError.toException());
            }
        });

        return isPremium;
    }
}
