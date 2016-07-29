package com.recipe.recipe;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.TwitterAuthProvider;
import com.google.firebase.auth.UserInfo;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.fabric.sdk.android.Fabric;

public class SettingsActivity extends AppCompatActivity {

    private static String TAG = "Settings";

    private CallbackManager callbackManager;
    private TwitterAuthClient twitterAuthClient;

    private Button linkEmail;
    private Button linkFacebook;
    private Button linkTwitter;
    private Button deleteAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(getApplicationContext());
        TwitterAuthConfig twitterAuthConfig = new TwitterAuthConfig(LoginActivity.TWITTER_KEY,
                LoginActivity.TWITTER_SECRET);
        Fabric.with(this, new TwitterCore(twitterAuthConfig));

        setContentView(R.layout.activity_settings);

        callbackManager = new CallbackManager.Factory().create();
        twitterAuthClient = new TwitterAuthClient();

        linkEmail = (Button)findViewById(R.id.btnSettingsLinkEmail);
        linkEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                linkEmail();
            }
        });

        linkFacebook = (Button)findViewById(R.id.btnSettingsLinkFacebook);
        linkFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                linkFacebook();
            }
        });

        linkTwitter = (Button) findViewById(R.id.btnSettingsLinkTwitter);
        linkTwitter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                linkTwitter();
            }
        });

        deleteAccount = (Button) findViewById(R.id.btnSettingsDeleteAccount);
        deleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        // Create on complete listener for re-use
                        OnCompleteListener<Void> onCompleteListener = new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()) {
                                    Toast.makeText(getApplicationContext(), "Account successfully deleted.", Toast.LENGTH_LONG).show();
                                    Log.d(TAG, "User account deleted");

                                    // Take the user back to the login screen and end this activity.
                                    Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                                    startActivity(i);
                                    SettingsActivity.this.finish();
                                } else {
                                    if (task.getException() instanceof FirebaseAuthRecentLoginRequiredException) {
                                        Log.d(TAG, "User needs to log in again");
                                    } else {
                                        task.getException().printStackTrace();
                                        Log.d(TAG, "Failed to delete user account");
                                        Toast.makeText(getApplicationContext(), "Account deletion failed", Toast.LENGTH_LONG).show();
                                    }
                                }
                            }
                        };

                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:

                                Log.d(TAG, "Deleting user account");

                                FirebaseAuth.getInstance().getCurrentUser().delete()
                                        .addOnCompleteListener(onCompleteListener);
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                // Do nothing.
                                Log.d(TAG, "User pressed no, doing nothing");

                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                builder.setMessage("Are you sure you want to delete your account?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();
            }
        });

        List<? extends UserInfo> providerData = FirebaseAuth.getInstance().getCurrentUser().getProviderData();
        ArrayList<String> providers = new ArrayList<String>();
        for(UserInfo info : providerData) {
            providers.add(info.getProviderId());
        }

        if(providers.contains("twitter.com")){
            linkTwitter.setEnabled(false);
        }
        if(providers.contains("facebook.com")) {
            linkFacebook.setEnabled(false);
        }

    }

    private void linkEmail() {
        Toast.makeText(getApplicationContext(), "Coming soon.", Toast.LENGTH_SHORT).show();
    }

    private void linkTwitter() {
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

    }

    private void linkFacebook() {
        Log.d(TAG, "Starting Facebook account link");

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
    }

    @Override
    protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
        super.onActivityResult(requestCode, responseCode, intent);
        twitterAuthClient.onActivityResult(requestCode, responseCode, intent);
        callbackManager.onActivityResult(requestCode, responseCode, intent);
    }
}
