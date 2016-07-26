package com.recipe.recipe;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.TwitterAuthProvider;
import com.twitter.sdk.android.Twitter;
import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;

import java.util.Arrays;

import io.fabric.sdk.android.Fabric;

public class TestActivity extends AppCompatActivity {

    private static String TAG = "TestActivity";

    private TwitterAuthClient twitterAuthClient;
    private CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_test);
        MobileAds.initialize(getApplicationContext(), getResources().getString(R.string.banner_bottom_home_unit_id));

        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().addTestDevice("9F2A3EB551323664824809D9EA0B76ED").build();
        mAdView.loadAd(adRequest);

        twitterAuthClient = new TwitterAuthClient();
        callbackManager = new CallbackManager.Factory().create();

        Log.d(TAG, FirebaseAuth.getInstance().getCurrentUser().getUid());
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
            case R.id.menu_main_link_email:
                break;
            case R.id.menu_main_link_facebook:
                Log.d(TAG, "Starting Facebook account link");

                FacebookSdk.sdkInitialize(getApplicationContext());

                LoginManager.getInstance().registerCallback(callbackManager,
                        new FacebookCallback<LoginResult>() {
                            @Override
                            public void onSuccess(LoginResult loginResult) {
                                Log.d(TAG, "Facebook login successful");

                                AuthCredential credential = FacebookAuthProvider.getCredential(loginResult.getAccessToken().getToken());

                                Log.d(TAG, credential.toString());

                                FirebaseAuth.getInstance().getCurrentUser().linkWithCredential(credential)
                                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<AuthResult> task) {
                                                Log.d(TAG, "linkWithCredential:onComplete:" + task.isSuccessful());

                                                if(!task.isSuccessful()) {
                                                    Log.w(TAG, "Linking with facebook was unsuccessful.", task.getException());
                                                }
                                            }
                                        });
                            }

                            @Override
                            public void onCancel() {
                                Log.d(TAG, "Facebook login cancelled");
                            }

                            @Override
                            public void onError(FacebookException error) {
                                Log.w(TAG, "Error while logging in to Facebook", error);
                            }
                        });

                LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email", "public_profile"));

                Log.d(TAG, "Facebook link done");
                break;

            case R.id.menu_main_link_twitter:
                Log.d(TAG, "Trying to link twitter");

                twitterAuthClient.authorize(this, new Callback<TwitterSession>() {
                    @Override
                    public void success(Result<TwitterSession> result) {

                        Log.d(TAG, "Twitter Log in successful");

                        TwitterSession session = result.data;

                        AuthCredential credential = TwitterAuthProvider.getCredential(
                                session.getAuthToken().token,
                                session.getAuthToken().secret);

                        FirebaseAuth.getInstance().getCurrentUser().linkWithCredential(credential)
                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        Log.d(TAG, "linkWithCredential:onComplete:" + task.isSuccessful());

                                        if(!task.isSuccessful()) {
                                            Log.d(TAG, "Linking failed");
                                            Toast.makeText(getApplicationContext(), "Linking failed.",
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                    }

                    @Override
                    public void failure(TwitterException exception) {
                        Log.w(TAG, "Log in failed", exception);
                    }
                });

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void signOut() {
        FirebaseAuth.getInstance().signOut();

        Twitter.logOut();
        LoginManager.getInstance().logOut();

        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
    }

    @Override
    protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
        super.onActivityResult(requestCode, responseCode, intent);
        twitterAuthClient.onActivityResult(requestCode, responseCode, intent);
        callbackManager.onActivityResult(requestCode, responseCode, intent);
    }
}
