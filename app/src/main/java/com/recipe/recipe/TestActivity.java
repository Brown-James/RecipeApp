package com.recipe.recipe;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.twitter.sdk.android.Twitter;
import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import io.fabric.sdk.android.Fabric;

public class TestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_test);
        MobileAds.initialize(getApplicationContext(), getResources().getString(R.string.banner_bottom_home_unit_id));

        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().addTestDevice("9F2A3EB551323664824809D9EA0B76ED").build();
        mAdView.loadAd(adRequest);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_main_settings:
                break;
            case R.id.menu_main_sign_out:
                signOut();
            case R.id.menu_main_add_ingredient:
                Intent i = new Intent(this, AddIngredientActivity.class);
                startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }

    public void signOut() {
        FirebaseAuth.getInstance().signOut();

        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
    }
}
