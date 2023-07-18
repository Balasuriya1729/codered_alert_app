package com.example.codered.fragment;

import static com.example.codered.Store.isPermissionGranted;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

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

public class LoginFragment extends Fragment {
    private SignInButton signInButton;
    private GoogleSignInClient mGoogleSignInClient;
    private final Scope calendarScope = new Scope("https://www.googleapis.com/auth/calendar");

    public LoginFragment() {
        // Required empty public constructor
    }

    public static LoginFragment newInstance(String param1, String param2) {
        LoginFragment fragment = new LoginFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("Login Fragment Says", "On Create");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i("Login Fragment Says", "On Create View");
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.i("Useful Info📄","Permission Requesting");
            requestSMSPermissionLauncher.launch(new String[]{
                    Manifest.permission.READ_SMS,
                    Manifest.permission.RECEIVE_SMS,
                    Manifest.permission.SEND_SMS,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            });
        } else isPermissionGranted = true;

        signInButton = requireView().findViewById(R.id.googleSignIn);
        setGooglePlusButtonText();

        if(!Store.isPlayingAlert()) {
            try {
                setGoogleSignIn();
            } catch (JSONException e) {
                Log.i("Sign In Call Says", e.getMessage());
            }
        } else {
            requireView().findViewById(R.id.stopButton).setVisibility(View.VISIBLE);
            requireView().findViewById(R.id.loginFragment).setVisibility(View.GONE);

            requireView().findViewById(R.id.stopButton).setOnClickListener(view1 -> {
                sendStopBroadCast();
                requireView().findViewById(R.id.stopButton).setVisibility(View.GONE);
                requireView().findViewById(R.id.loginFragment).setVisibility(View.VISIBLE);
                try {
                    setGoogleSignIn();
                } catch (JSONException e) {
                    Log.i("Sign In Call Says", e.getMessage());
                }
            });
        }
    }

    private final ActivityResultLauncher<String[]> requestSMSPermissionLauncher = registerForActivityResult(
        new ActivityResultContracts.RequestMultiplePermissions(),
        result -> {
            Log.i("Login F Says", result+"");
            if(
                Boolean.TRUE.equals(result.get("android.permission.READ_SMS")) &&
                Boolean.TRUE.equals(result.get("android.permission.READ_EXTERNAL_STORAGE")) &&
                Boolean.TRUE.equals(result.get("android.permission.RECEIVE_SMS")) &&
                Boolean.TRUE.equals(result.get("android.permission.SEND_SMS")) &&
                Boolean.TRUE.equals(result.get("android.permission.WRITE_EXTERNAL_STORAGE"))
            ){
                isPermissionGranted = true;
                Log.i("Login Fragment Says", "Calling Loader Animation After Permission Reqs");
                startLoaderAnimation();
            }
            else {
                isPermissionGranted = false;
                Toast.makeText(getContext(), "SMS Permission Denied, You Cannot Proceed👮‍♂️", Toast.LENGTH_SHORT) .show();
            }
        }
    );

    public void sendStopBroadCast(){
        Intent i= new Intent();
        i.putExtra("data", "Stop Playing");
        i.setAction("stopPlayer");
        i.setComponent(new ComponentName("com.example.codered", "com.example.codered.receiver.AlertReceiver"));
        i.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        requireContext().sendBroadcast(i);

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

        mGoogleSignInClient = GoogleSignIn.getClient(requireContext(), gso);
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(requireContext());

        Log.i("Login Activity Says", "Fetching Started");

        try {
            SharedPreferences preferences = requireContext().getSharedPreferences(getString(R.string.preferences_name), 0);
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

                    requireView().findViewById(R.id.InitialLoading).setVisibility(View.GONE);
                    requireView().findViewById(R.id.appSlogan).setVisibility(View.VISIBLE);
                    ImageView loginBanner = requireView().findViewById(R.id.loginBanner);
                    Animation a = new Animation() {
                        @Override
                        protected void applyTransformation(float interpolatedTime, Transformation t) {
                            super.applyTransformation(interpolatedTime, t);
                            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) loginBanner.getLayoutParams();
                            layoutParams.topMargin = (int) (Store.dpTopx(requireContext(), 100) * (1-interpolatedTime));
                            requireView().findViewById(R.id.appSlogan).setAlpha(interpolatedTime);
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
            if(isPermissionGranted) {
                Log.i("Login Fragment Says", "Calling Loader Animation Since Account is NULL");
                startLoaderAnimation();
            }
        }

        ActivityResultLauncher<Intent> startActivityForResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                    handleSignInResult(task);
                }
                else if(result.getResultCode() == Activity.RESULT_CANCELED) {
                    Log.i("SSO", "Cancelled");
                } else {
                    Toast.makeText(getContext(), "Only Rently Employees", Toast.LENGTH_SHORT).show();
                }
            }
        );

        signInButton.setOnClickListener(view -> {
            if(isPermissionGranted) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult.launch(signInIntent);
            } else{
                Toast.makeText(requireContext(), "Permissions Denied, Restart the App😅", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void startLoaderAnimation(){
        new android.os.Handler(Looper.myLooper()).postDelayed(() -> {
            requireView().findViewById(R.id.InitialLoading).setVisibility(View.GONE);
            ImageView loginBanner = requireView().findViewById(R.id.loginBanner);
            Animation a = new Animation() {
                @Override
                protected void applyTransformation(float interpolatedTime, Transformation t) {
                    super.applyTransformation(interpolatedTime, t);
                    LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) loginBanner.getLayoutParams();
                    layoutParams.topMargin = (int) (Store.dpTopx(requireContext(), 100) * (1-interpolatedTime));
                    loginBanner.requestLayout();
                }
            };
            a.setDuration(1000);
            loginBanner.startAnimation(a);

            new android.os.Handler(Looper.myLooper()).postDelayed(() -> {
                Log.i("Login Says", "Starting Animation");

                requireView().findViewById(R.id.buttonLayout).setVisibility(View.VISIBLE);
            }, 1000);
        }, 2000);
    }
    private void navigateToMainActivity(GoogleSignInAccount account, String intent_msg) {
        NavHostFragment navHostFragment =
                (NavHostFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();

        if(intent_msg.equals("Last Sign")) {
            Bundle args = new Bundle();
            args.putString("signInType", intent_msg);

            navController.navigate(R.id.homeFragment, args);
        } else{
            Store.setUser(account);
            navController.navigate(R.id.basicInformationFragment);
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