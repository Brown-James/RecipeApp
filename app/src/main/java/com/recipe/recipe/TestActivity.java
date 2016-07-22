package com.recipe.recipe;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

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
            case R.id.settings:
                break;
            case R.id.sign_out:
                signOut();
        }
        return super.onOptionsItemSelected(item);
    }

    public void signOut() {
        FirebaseAuth.getInstance().signOut();

        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
    }
}
