package com.example.codered.activity;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.codered.R;
import com.example.codered.Store;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;


public class LoginActivity extends AppCompatActivity {
    private SignInButton signInButton;
    private GoogleSignInClient mGoogleSignInClient;
    private final Scope calendarScope = new Scope("https://www.googleapis.com/auth/calendar");
    private boolean isPermissionGranted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        if(
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.i("Useful Info📄","Permission Requesting");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS, android.Manifest.permission.SEND_SMS, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 103);
        } else isPermissionGranted = true;

        signInButton = findViewById(R.id.googleSignIn);
        setGooglePlusButtonText();

        if(!Store.isPlayingAlert()) {
            try {
                setGoogleSignIn();
            } catch (JSONException e) {
                Log.i("Sign In Call Says", e.getMessage());
            }
        } else {
            findViewById(R.id.stopButton).setVisibility(View.VISIBLE);
            findViewById(R.id.loginFragment).setVisibility(View.GONE);

            findViewById(R.id.stopButton).setOnClickListener(view -> {
                sendStopBroadCast();
                findViewById(R.id.stopButton).setVisibility(View.GONE);
                findViewById(R.id.loginFragment).setVisibility(View.VISIBLE);
                try {
                    setGoogleSignIn();
                } catch (JSONException e) {
                    Log.i("Sign In Call Says", e.getMessage());
                }
            });
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode,
                permissions,
                grantResults);

        if (requestCode == 103) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                isPermissionGranted = true;
                startLoaderAnimation();
            }
            else
                Toast.makeText(LoginActivity.this, "SMS Permission Denied, You Cannot Proceed👮‍♂️", Toast.LENGTH_SHORT) .show();
        }

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        } else if(resultCode == RESULT_CANCELED) {
          Log.i("SSO", "Cancelled");
        } else {
            Toast.makeText(this, "Only Rently Employees", Toast.LENGTH_SHORT).show();
        }
    }

    //handlers
    public void sendStopBroadCast(){
        Intent i= new Intent();
        i.putExtra("data", "Stop Playing");
        i.setAction("stopPlayer");
        i.setComponent(new ComponentName("com.example.codered", "com.example.codered.receiver.AlertReceiver"));
        i.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        this.sendBroadcast(i);

        Log.i("Useful Info📜","Send Broadcast");
    }
    protected void setGooglePlusButtonText() {
        for (int i = 0; i < signInButton.getChildCount(); i++) {
            View v = signInButton.getChildAt(i);

            if (v instanceof TextView) {
                TextView tv = (TextView) v;
                tv.setText("Continue with Google");
                tv.setTextColor(Color.rgb(255, 79, 90));
                return;
            }
        }
    }
    private void setGoogleSignIn() throws JSONException {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.server_client_id))
            .requestServerAuthCode(getString(R.string.server_client_id))
            .requestScopes(calendarScope)
            .requestEmail()
            .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

        Log.i("Login Activity Says", "Fetching Started");

        try {
            SharedPreferences preferences = getApplication().getSharedPreferences(getString(R.string.preferences_name), 0);
            if(!preferences.getString(getString(R.string.JSONObject), "NULL").equals("NULL")){
                JSONObject jsonObject = new JSONObject(preferences.getString(getString(R.string.JSONObject), "NULL"));
                Log.i("Login Activity Says", jsonObject.toString());

                if(jsonObject.getString("refresh_token").equals("")){
                    Log.e("Login Activity Says", "No Refresh Token Revoke Access");
                }
            }
        } catch (Exception ex){
            Log.e("Login Activity Says", ex.getMessage());
        }

        if(account!=null) {
            Store.setUser(account);
            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

            Query query = mDatabase.child("users")
                    .orderByChild("UserID")
                    .equalTo(account.getId());

            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot ds : snapshot.getChildren())
                        Store.getUser().setSnapShotToData(ds);

                    findViewById(R.id.InitialLoading).setVisibility(View.GONE);
                    findViewById(R.id.appSlogan).setVisibility(View.VISIBLE);
                    ImageView loginBanner = findViewById(R.id.loginBanner);
                    Animation a = new Animation() {
                        @Override
                        protected void applyTransformation(float interpolatedTime, Transformation t) {
                            super.applyTransformation(interpolatedTime, t);
                            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) loginBanner.getLayoutParams();
                            layoutParams.topMargin = (int) (Store.dpTopx(LoginActivity.this, 100) * (1-interpolatedTime));
                            findViewById(R.id.appSlogan).setAlpha(interpolatedTime);
                            loginBanner.requestLayout();
                        }
                    };
                    a.setDuration(1000);
                    loginBanner.startAnimation(a);
                    new android.os.Handler(Looper.myLooper()).postDelayed(() -> {
                        navigateToMainActivity(getScopedAccount(account), "Last Sign");
                    }, 4000);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });

        }
        else{
            if(isPermissionGranted) startLoaderAnimation();
        }
        
        ActivityResultLauncher<Intent> startActivityForResult = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> Log.e("Google Sign in error", result.getResultCode()+"")

        );

        signInButton.setOnClickListener(view -> {
            if(isPermissionGranted) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult.launch(signInIntent);
            } else{
                Toast.makeText(this, "Permissions Denied, Restart the App😅", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startLoaderAnimation(){
        new android.os.Handler(Looper.myLooper()).postDelayed(() -> {
            findViewById(R.id.InitialLoading).setVisibility(View.GONE);
            ImageView loginBanner = findViewById(R.id.loginBanner);
            Animation a = new Animation() {
                @Override
                protected void applyTransformation(float interpolatedTime, Transformation t) {
                    super.applyTransformation(interpolatedTime, t);
                    LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) loginBanner.getLayoutParams();
                    layoutParams.topMargin = (int) (Store.dpTopx(LoginActivity.this, 100) * (1-interpolatedTime));
                    loginBanner.requestLayout();
                }
            };
            a.setDuration(1000);
            loginBanner.startAnimation(a);

            new android.os.Handler(Looper.myLooper()).postDelayed(() -> {
                Log.i("Login Says", "Starting Animation");

                findViewById(R.id.buttonLayout).setVisibility(View.VISIBLE);
            }, 1000);
        }, 2000);
    }
    private void navigateToMainActivity(GoogleSignInAccount account, String intent_msg) {
        if(intent_msg.equals("Last Sign")) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("signInType", intent_msg);

            startActivity(intent);
        } else{
            Store.setUser(account);
            Intent intent2 = new Intent(this, BasicInformationActivity.class);
            startActivity(intent2);
        }
    }
    private void handleSignInResult(@NonNull Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            navigateToMainActivity(getScopedAccount(account), "New Sign");
        } catch (ApiException e) {
            Log.e("Caught Error❌" , "signInResult:failed code="+e.getStatusCode());
        }
    }
    @NonNull
    private GoogleSignInAccount getScopedAccount(GoogleSignInAccount account) {
        return account.requestExtraScopes(calendarScope);
    }
}