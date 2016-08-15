package com.recipe.recipe;

import android.content.DialogInterface;
import android.content.Intent;
import android.preference.DialogPreference;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;

import com.facebook.FacebookSdk;
import com.google.firebase.auth.TwitterAuthProvider;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import io.fabric.sdk.android.Fabric;

public class LoginActivity extends AppCompatActivity {

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    public static final String TWITTER_KEY = "lOBkSO8Wj6G8gZvGJirr0wgb4";
    public static final String TWITTER_SECRET = "H3SBdwkoDF6x2Cp004lm6K1VaKnBEGA7rd8i3trTYGhrXCRD53";

    private static String TAG = "LOGIN";

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private EditText email;
    private EditText password;

    private TwitterLoginButton twitterLoginButton;
    private LoginButton facebookLoginButton;
    private CallbackManager mCallbackManager;
    private TextView logInAnonymously;

    private Button forgotPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));

        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_login);

        email = (EditText) findViewById(R.id.etxtEmail);
        password = (EditText) findViewById(R.id.etxtPassword);
        facebookLoginButton = (LoginButton) findViewById(R.id.btnLoginWithFacebook);
        twitterLoginButton = (TwitterLoginButton) findViewById(R.id.btnLoginWithTwitter);

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {

            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null) {
                    // User is signed in
                    Log.d("Tag", "onAuthStateChanged:signed_in" + user.getUid());

                    final DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

                    final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    Log.d(TAG, ref.child("users").child(uid).toString());

                   ref.child("users").addListenerForSingleValueEvent(
                           new ValueEventListener() {
                               @Override
                               public void onDataChange(DataSnapshot dataSnapshot) {
                                   if (!dataSnapshot.hasChild(uid)) {
                                       ref.child("users").child(uid).child("groups").child("premium")
                                               .setValue(true);
                                   }
                               }

                               @Override
                               public void onCancelled(DatabaseError databaseError) {
                                    Log.w(TAG, "Error creating database entry for user " + uid,
                                            databaseError.toException());
                               }
                           }
                   );

                    Intent i = new Intent(getApplicationContext(), TestActivity.class);
                    startActivity(i);

                } else {
                    // User is signed out
                    Log.d("Tag", "onAuthStateChanged:signed_out");
                }
            }
        };

        mCallbackManager = CallbackManager.Factory.create();
        facebookLoginButton.setReadPermissions("email", "public_profile");
        facebookLoginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
            }
        });

        twitterLoginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                Log.d(TAG, "twitter:success:"+result);
                handleTwitterSession(result.data);
            }

            @Override
            public void failure(TwitterException exception) {
                Log.w(TAG, "twitterLogin:failure", exception);
            }
        });

        logInAnonymously = (TextView) findViewById(R.id.txtSignUpLater);
        logInAnonymously.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(getApplicationContext(), getString(R.string.login_anon_sign_in), Toast.LENGTH_SHORT).show();

                mAuth.signInAnonymously()
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                Log.d(TAG, "logInAnonymously:" + task.isSuccessful());

                                if(!task.isSuccessful()) {
                                    task.getException().printStackTrace();
                                    Toast.makeText(getApplicationContext(), getString(R.string.login_anon_sign_in_failed), Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            }
        });

        forgotPassword = (Button) findViewById(R.id.btnLoginForgotPassword);
        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                builder.setTitle(getString(R.string.login_forgot_pass_enter_email));

                final EditText input = new EditText(LoginActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                builder.setView(input);

                builder.setPositiveButton(getString(R.string.login_enter_forgot_password), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FirebaseAuth.getInstance().sendPasswordResetEmail(input.getText().toString())
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Log.d(TAG, "onClick:onClick:onComplete:" + task.isSuccessful());

                                        if(!task.isSuccessful()) {
                                            Log.d(TAG, "Failed to send password reset email");
                                            task.getException().printStackTrace();

                                            Toast.makeText(getApplicationContext(), getString(R.string.login_failed_send_password_reset_email), Toast.LENGTH_LONG).show();
                                        } else {
                                            String email = input.getText().toString();
                                            String s = getString(R.string.login_password_reset_email_sent, email);
                                            Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                    }
                });

                builder.setNegativeButton(getString(R.string.login_cancel_forgot_password), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing
                        Log.d(TAG, "User cancelled forgot password operation");
                    }
                });

                builder.show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    public void signUpPressed(View view) {

        Log.d("Tag", "Pressed sign up button");

        if(email.length() == 0 || password.length() == 0) {
            Toast.makeText(getApplicationContext(), getString(R.string.login_email_or_pass_empty), Toast.LENGTH_LONG).show();
        } else {

            String emailStr = email.getText().toString();
            String passwordStr = password.getText().toString();

            mAuth.createUserWithEmailAndPassword(emailStr, passwordStr)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            Log.d("Tag", "createUserWithEmail:onComplete:" + task.isSuccessful());

                            // If sign in fails, display a message to the user. If sign in succeeds
                            // the auth state listener will be notified and logic to handle the
                            // signed in user can be handled in the listener.
                            if(!task.isSuccessful()) {
                                Toast.makeText(LoginActivity.this, getString(R.string.login_sign_up_failed), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }
    }

    public void loginPressed(View view) {

        String emailStr = email.getText().toString();
        String passwordStr = password.getText().toString();

        if(emailStr.length() == 0 || passwordStr.length() == 0) {
            Toast.makeText(getApplicationContext(), getString(R.string.login_email_or_pass_empty), Toast.LENGTH_LONG).show();
        } else {
            mAuth.signInWithEmailAndPassword(emailStr, passwordStr)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            Log.d("Tag", "signInWithEmailAndPassword:onComplete:" + task.isSuccessful());

                            if (!task.isSuccessful()) {
                                Toast.makeText(LoginActivity.this, getString(R.string.login_login_failed), Toast.LENGTH_LONG).show();
                                Log.w(TAG, "Failed to login with email/password", task.getException());
                            }
                        }
                    });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
        twitterLoginButton.onActivityResult(requestCode, resultCode, data);
    }

    private void handleFacebookAccessToken(AccessToken accessToken) {
        Log.d(TAG, "handleFacebookAccessToken:" + accessToken);

        AuthCredential credential = FacebookAuthProvider.getCredential(accessToken.getToken());
            mAuth.signInWithCredential(credential)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                            // If sign in fails, display a message to the user. If sign in succeeds
                            // the auth state listener will be notified and logic to handle the
                            // signed in user can be handled in the listener.
                            if (!task.isSuccessful()) {
                                Log.w(TAG, "signInWithCredential", task.getException());

                                if(task.getException() instanceof FirebaseAuthUserCollisionException) {
                                    Toast.makeText(getApplicationContext(), getString(R.string.login_facebook_user_email_already_exists), Toast.LENGTH_LONG).show();
                                }
                                else {
                                    Toast.makeText(LoginActivity.this, getString(R.string.login_login_failed),
                                            Toast.LENGTH_SHORT).show();
                                }
                            }

                        }
                    });
    }

    private void handleTwitterSession(TwitterSession session) {
        Log.d(TAG, "handTwitterSession:" + session);

        TwitterAuthClient twitterAuthClient = new TwitterAuthClient();

        AuthCredential credential = TwitterAuthProvider.getCredential(
                session.getAuthToken().token,
                session.getAuthToken().secret);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task);

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(LoginActivity.this, getString(R.string.login_login_failed),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
