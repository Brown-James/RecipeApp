package com.recipe.recipe;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.util.Log;

import com.fernandocejas.arrow.optional.Optional;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * Created by James on 13/08/2016.
 */
public class Recipe {

    private static String TAG = "Recipe";

    RecipeRVAdapter adapter;
    String name;
    String description;
    String id;
    Bitmap thumbnail;

    public Recipe(Context context, String id, String name, String description, Optional<Bitmap> thumbnail, RecipeRVAdapter adapter) {
        this.name = name;
        this.description = description;
        this.id = id;

        if(thumbnail.isPresent()) {
            this.thumbnail = thumbnail.get();
        } else {
            this.thumbnail = BitmapFactory.decodeResource(context.getResources(), R.drawable.loading_thumb);
            downloadThumbnail();
        }

        this.adapter = adapter;
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

    private void setThumbnail(Bitmap b) {
        this.thumbnail = b;
        adapter.notifyDataSetChanged();
    }

    public void downloadThumbnail() {
        Log.d(TAG, "Downloading thumbnail from recipe_images/" + id + "/1.jpg");

        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        StorageReference pathReference = storageReference.child("recipe_images/" + id + "/1.jpg");

        final long MEGABYTE = 1024 * 1024 * 4;
        pathReference.getBytes(MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                setThumbnail(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "Something went wrong downloading image", e);
            }
        });
    }
}
