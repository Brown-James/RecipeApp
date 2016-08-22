package com.recipe.recipe;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.util.Log;

import com.fernandocejas.arrow.optional.Optional;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Observable;

/**
 * Created by James on 13/08/2016.
 */
public class Recipe extends Observable{

    private static String TAG = "Recipe";

    RecipeRVAdapter adapter;
    String name;
    String description;
    String id;
    Bitmap thumbnail;

    Context context;

    public Recipe(Context context, String id, String name, String description, Optional<Bitmap> thumbnail) {
        this.name = name;
        this.description = description;
        this.id = id;

        if(thumbnail.isPresent()) {
            this.thumbnail = thumbnail.get();
        } else {
            this.thumbnail = BitmapFactory.decodeResource(context.getResources(), R.drawable.loading_thumb);
            downloadThumbnail();
        }

        this.context = context;
    }

    public String getId() {
        return id;
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

        setChanged();
        notifyObservers();
    }

    public void downloadThumbnail() {
        Log.d(TAG, "Downloading thumbnail from recipe_images/" + id + "/1.jpg");

        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        StorageReference pathReference = storageReference.child("recipe_images/" + id + "/1.jpg");

        final long MEGABYTE = 1024 * 1024 * 4;
        pathReference.getBytes(MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
                int imageHeight = options.outHeight;
                int imageWidth = options.outWidth;
                String imageType = options.outMimeType;

                Log.d(TAG, "Height: " + imageHeight + "  Width: " + imageWidth + "  Type: " + imageType);

                // I don't like these random numbers - to be fixed!
                int pxWidth = (int) Math.ceil(Double.parseDouble(String.valueOf(convertDpToPixel(120f, context))));
                int pxHeight = (int) Math.ceil(Double.parseDouble(String.valueOf(convertDpToPixel(100f, context))));

                Log.d(TAG, "Px Width: " + pxWidth + "  Height: " + pxHeight);

                options.inSampleSize = calculateInSampleSize(options, pxWidth, pxHeight);

                Log.d(TAG, "Sample Size: " + options.inSampleSize);

                options.inJustDecodeBounds = false;
                Bitmap b = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);

                Log.d(TAG, "Compressed Height: " + b.getHeight() + "  Width: " + b.getWidth());
                setThumbnail(b);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "Something went wrong downloading image", e);
            }
        });
    }

    // From https://developer.android.com/training/displaying-bitmaps/load-bitmap.html
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    // From http://stackoverflow.com/questions/4605527/converting-pixels-to-dp
    public static float convertDpToPixel(float dp, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }

}
