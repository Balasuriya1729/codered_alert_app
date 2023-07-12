package com.example.codered.activity;

import static com.example.codered.Store.dpTopx;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.codered.R;
import com.example.codered.Store;
import com.example.codered.adapter.EventListAdapter;
import com.example.codered.model.EventModel;
import com.example.codered.model.MemberModel;
import com.example.codered.model.TeamModel;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private ArrayList<MemberModel> members;
    private ArrayList<TeamModel> teams;
    private GoogleSignInClient client;
    private Store.User userAccount;
    private JSONObject jsonObject;
    private EventListAdapter eventListAdapter;
    private int selected = 1;
    private View previousSelectedView;
    private View bottomCards;
    private TextView teamT, individualT, allT;
    private ShimmerFrameLayout mShimmerViewContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        previousSelectedView = findViewById(R.id.alertAll);
        bottomCards = findViewById(R.id.bottomCards);
        teamT = findViewById(R.id.teamIText);
        individualT = findViewById(R.id.indivIText);
        allT = findViewById(R.id.allIText);
        mShimmerViewContainer = findViewById(R.id.shimmer_view_container);

        Intent intent = getIntent();
        if(Store.haveAUser()) {
            userAccount = Store.getUser();
            ImageView imageView = findViewById(R.id.profileImg);
            Glide.with(this).load(userAccount.getPhotoUri()).into(imageView);

            imageView.setScaleX(2F);
            imageView.setScaleY(2F);

            if(intent.getExtras().getString("signInType").equals("New Sign")) {
                Log.i("Useful Info📜","Going with Newly Signed In Account and getting Token");
                getAccessToken();
            }
            else {
                Log.i("Useful Info📜","Going with Already Signed In Account");

                new Thread(() -> {
                    try {
                        SharedPreferences preferences = getApplication().getSharedPreferences(getString(R.string.preferences_name), 0);
                        jsonObject = new JSONObject(preferences.getString(getString(R.string.JSONObject), "NULL"));
                        Log.i("API says 🔊JSON Value", jsonObject.toString());


                        getEvents();
                    } catch (JSONException | GeneralSecurityException | IOException e) {
                        Log.e("Caught Error❌","Token Error From Prefs "+e.getMessage()+ "\n So, Refreshing Token");
                        try {
                            refreshToken();
                        } catch (JSONException ex){
                            Log.e("Caught Error❌","While Refreshing Token Try Again -> \n"+ e.getMessage());
                        }
                    } catch (ParseException e) {
                        Log.e("Events Says", "Parsing Exception");
                    }
                }).start();
            }
        }

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        client = GoogleSignIn.getClient(this, gso);

        setVisible(1);
        setDateAndDay();
        settingDummyData();
        setEventRecyclerView();
    }
    @Override
    protected void onResume() {
        super.onResume();
        Store.inTeamSelectionPage = false;
        mShimmerViewContainer.startShimmerAnimation();
    }
    @Override
    protected void onPause() {
        super.onPause();
        mShimmerViewContainer.stopShimmerAnimation();
    }

    //API Handlers
    private void getAccessToken() {
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = new FormEncodingBuilder()
                .add("grant_type", "authorization_code")
                .add("client_id", getString(R.string.server_client_id))
                .add("client_secret", getString(R.string.client_secret))
                .add("redirect_uri","")
                .add("code", Objects.requireNonNull(userAccount.getServerAuthCode()))
                .build();
        final Request request = new Request.Builder()
                .url("https://www.googleapis.com/oauth2/v4/token")
                .post(requestBody)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(final Request request, final IOException e) {
                Log.e("Error in API", e.toString());
            }

            @Override
            public void onResponse(Response response) throws IOException {
                try {
                    jsonObject = new JSONObject(response.body().string());
                    final String message = jsonObject.toString(5);
                    writeToSharedPreferences(message);
                    getEvents();
                    Log.i("Useful Info📄", "Token Got and Saving");
                } catch (JSONException | GeneralSecurityException e) {
                    Log.e("Caught Error❌","In Getting Token Response"+ e.getMessage());
                } catch (ParseException e) {
                    Log.e("Events Says", "Parsing Exception");

                }
            }

            private void writeToSharedPreferences(String value) {
                SharedPreferences sharedPreferences = getApplication().getSharedPreferences(getString(R.string.preferences_name), Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(getString(R.string.JSONObject), value);
                Log.i("API says 🔊JSON Value", value);
                editor.apply();
            }
        });
    }
    private void refreshToken() throws JSONException {

        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = new FormEncodingBuilder()
                .add("client_id", getString(R.string.server_client_id))
                .add("client_secret", getString(R.string.client_secret))
                .add("grant_type", "refresh_token")
                .add("refresh_token", jsonObject.getString("refresh_token"))
                .build();
        Log.e("Caught Error❌", "In Refresh Token "+jsonObject.getString("refresh_token"));
        final Request request = new Request.Builder()
                .url("https://oauth2.googleapis.com/token")
                .post(requestBody)
                .build();


        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(final Request request, final IOException e) {
                Log.e("Error in API❌", e.toString());
            }

            @Override
            public void onResponse(Response response) throws IOException {
                try {
                    JSONObject jsonObject1 = new JSONObject(response.body().string());
                    Log.i("API Says", jsonObject1.getString("access_token"));
                    jsonObject.put("access_token", jsonObject1.get("access_token"));
                    final String jsonString = jsonObject.toString();
                    writeToSharedPreferences(jsonString);
                    getEvents();
                    Log.i("Useful Info📄", "Token Refreshed and Saving");
                } catch (JSONException | GeneralSecurityException e) {
                    Log.e("Caught Error❌","In Getting Token Response"+ e.getMessage());
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "Error In Getting the Creds, \nPlease Sign in Again", Toast.LENGTH_LONG).show();
                        signOut(new View(MainActivity.this));
                    });
                } catch (ParseException e) {
                    Log.e("Events Says", "Parsing Exception");

                }
            }

            private void writeToSharedPreferences(String value) {
                SharedPreferences sharedPreferences = getApplication().getSharedPreferences(getString(R.string.preferences_name), Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(getString(R.string.JSONObject), value);
                Log.i("API says 🔊JSON Value", value);
                editor.apply();
            }
        });
    }
    private void getEvents() throws GeneralSecurityException, IOException, JSONException, ParseException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Credential credentials = new Credential(BearerToken.authorizationHeaderAccessMethod()).setAccessToken(jsonObject.getString("access_token"));
        Calendar service =
                new Calendar.Builder(HTTP_TRANSPORT, GsonFactory.getDefaultInstance(), credentials)
                        .setApplicationName("SMS Alert")
                        .build();

        DateTime now = new DateTime(System.currentTimeMillis());
        int todayDate = Integer.parseInt(now.toString().substring(8, 10));
        Events events = service.events().list("primary")
                .setTimeMin(now)
                .setMaxResults(6)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();

        List<Event> items = events.getItems();
        List<EventModel> eventModels = new ArrayList<>();
        Event firstEvent = null;

        LocalDateTime firstEventLDT = null;
        if (items.isEmpty()) {
            Log.w("Warning📄", "No upcoming events found.");
        } else {
            Log.i("Useful Info📄", "Upcoming events Found");

            boolean isFirst = true;

            for (Event event : items) {
                DateTime start = event.getStart().getDateTime();
                StringBuilder time = new StringBuilder();
                LocalDateTime l = null;
                String dateString;

                if (start == null) {
                    start = event.getStart().getDate();
                    time.append("All Day");
                } else {
                    dateString = start.toString();
                    if(!(start.toString().charAt(23) == 'Z'))
                        dateString = start.toString().substring(0, 23).concat("Z");

                    DateTimeFormatter formatter =
                            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                    l = LocalDateTime.parse(dateString, formatter);


                    if (l.getMinute() < 10)
                        time.append(String.valueOf(l.getHour()).concat(":0").concat(String.valueOf(l.getMinute())));
                    else
                        time.append(String.valueOf(l.getHour()).concat(":").concat(String.valueOf(l.getMinute())));
                }

                if(!time.toString().equals("All Day")){
                    if (isFirst) {
                        firstEventLDT = l;
                        firstEvent = event;
                        isFirst = false;
                    } else {
                        String status;
                        int eventStartDate = Integer.parseInt(event.getStart().getDateTime().toString().substring(8,10));

                        if(eventStartDate == todayDate) status = "Today";
                        else if (eventStartDate == todayDate+1) status = "Tommorrow";
                        else status = event.getStart().getDateTime().toString().substring(0,10);
                        eventModels.add(new EventModel(event.getSummary(), time.toString(), status, getColor(R.color.sky_blue)));
                        System.out.printf("%s (%s) - (%s)\n", event.getSummary(), start, event.getEventType());
                    }
                }
            }
        }

        TextView attendeeCountText = findViewById(R.id.mainCardAttendeeCountText);
        TextView eventLocText = findViewById(R.id.mainCardLocationText);
        TextView t1 = findViewById(R.id.dayAndYearText);

        if(!(firstEvent == null)) {
            Event finalFirstEvent = firstEvent;
            LocalDateTime finalFirstEventLDT = firstEventLDT;
            runOnUiThread(() -> {
                String title = finalFirstEvent.getSummary();
                if(!(finalFirstEventLDT == null)) {
                    if (finalFirstEventLDT.getMinute() < 10)
                        title = title.concat(" on ")
                                .concat(
                                        String.valueOf(finalFirstEventLDT.getHour())
                                                .concat(":0")
                                                .concat(String.valueOf(finalFirstEventLDT.getMinute()))
                                );
                    else
                        title = title.concat(" on ")
                                .concat(
                                        String.valueOf(finalFirstEventLDT.getHour())
                                                .concat(":")
                                                .concat(String.valueOf(finalFirstEventLDT.getMinute()))
                                );
                }
                t1.setText(title);
                if(!(finalFirstEventLDT == null)){
                    attendeeCountText.setText(String.valueOf(finalFirstEvent.getAttendees().size()).concat("+"));

                    StringBuilder stringBuilder = new StringBuilder();
                    boolean startedGettingInitials = false;
                    for (char letter :
                            finalFirstEvent.getOrganizer().getEmail().toCharArray()) {
                        if(letter == '@') break;
                        if(letter == '.') {
                            stringBuilder.append(" ");
                            startedGettingInitials = true;
                            continue;
                        }

                        stringBuilder.append(letter);
                        if(startedGettingInitials) stringBuilder.append(" ");
                    }

                    eventLocText.setText(stringBuilder.toString().toUpperCase());
                }
                else Log.i("Useful Info📄",finalFirstEvent.toString());
            });
        } else {
            t1.setText("No Event For you Today");
            eventLocText.setText("-");
        }
        runOnUiThread(() -> {
            findViewById(R.id.progressForEvents).setVisibility(View.GONE);

            if(eventModels.size()>0){
                findViewById(R.id.eventCards).setVisibility(View.VISIBLE);
                eventListAdapter.setEvents(eventModels);
            } else findViewById(R.id.noEventsText).setVisibility(View.VISIBLE);

            mShimmerViewContainer.stopShimmerAnimation();
            mShimmerViewContainer.setVisibility(View.GONE);
            findViewById(R.id.originalContent).setVisibility(View.VISIBLE);
        });
    }

    //Handlers
    public void goBack(View view){
        onBackPressed();
    }
    public void signOut(View view) {
        client.revokeAccess();
        client.signOut();

        SharedPreferences preferences = getApplication().getSharedPreferences(getString(R.string.preferences_name), 0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(getString(R.string.JSONObject), "NULL");
        editor.apply();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivity(intent);
    }
    public void alertAll(View view) {

        if (selected == 1) {
            Dialog dialog = createDialog();
            dialog.show();
        } else {
            animateSelectedWidth(view, 1);
            animateSelectedWidth(previousSelectedView, -1);

            if(selected == 0) animateSelectedMargin(bottomCards, false, false, true, false);
            if(selected == 2) animateSelectedMargin(bottomCards, false, false, false, true);

            setVisible(1);

            previousSelectedView = view;
            selected = 1;
        }
    }
    public void alertTeam(View view) {
        if (selected == 2) {
            Store.inTeamSelectionPage = true;
            Intent intent = new Intent(this, AlertTeamActivity.class);
            intent.putExtra("allTeams", teams);

            startActivity(intent);
        } else {
            animateSelectedWidth(view, 1);
            animateSelectedWidth(previousSelectedView, -1);

            if(selected == 0) animateSelectedMargin(bottomCards, false, true, true, false);
            if(selected == 1) animateSelectedMargin(bottomCards, false, true, false, false);

            setVisible(2);

            previousSelectedView = view;
            selected = 2;
        }
    }
    public void alertIndividual(View view) {
        if (selected == 0) {
            Intent intent = new Intent(this, AlertIndividualActivity.class);
            intent.putExtra("allIndividuals", members);

            startActivity(intent);
        } else {
            animateSelectedWidth(view, 1);
            animateSelectedWidth(previousSelectedView, -1);

            if(selected == 2) animateSelectedMargin(bottomCards, true, false, false, true);
            if(selected == 1) animateSelectedMargin(bottomCards, true, false, false, false);

            setVisible(0);

            previousSelectedView = view;
            selected = 0;
        }
    }

    //Utils
    private void setEventRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.eventCards);
        eventListAdapter = new EventListAdapter(new ArrayList<>(), this);

        recyclerView.setAdapter(eventListAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
    }
    private void setDateAndDay() {
        DateTime now = new DateTime(System.currentTimeMillis());
        TextView t2 = findViewById(R.id.dateText);

        StringBuilder DateString = new StringBuilder();
        DateString.append(now.toString().substring(8, 10));
        switch (Integer.parseInt(now.toString().substring(5, 7))){
            case 12:
                DateString.append(" DEC ");break;
            case 11:
                DateString.append(" NOV ");break;
            case 10:
                DateString.append(" OCT ");break;
            case 9:
                DateString.append(" SEP ");break;
            case 8:
                DateString.append(" AUG ");break;
            case 7:
                DateString.append(" JUL ");break;
            case 6:
                DateString.append(" JUN ");break;
            case 5:
                DateString.append(" MAY ");break;
            case 4:
                DateString.append(" APR ");break;
            case 3:
                DateString.append(" MAR ");break;
            case 2:
                DateString.append(" FEB ");break;
            case 1:
                DateString.append(" JAN ");break;
        }
        DateString.append(now.toString().substring(0, 4));

        t2.setText(DateString.toString());
    }
    private void settingDummyData() {
        members = new ArrayList<>();
        teams = new ArrayList<>();

        members.add(new MemberModel("Ari Prasath","Male" ,"null", "Developer", "Rainbow"));
        members.add(new MemberModel("Aathika", "Female","null", "Developer", "Rainbow"));
        members.add(new MemberModel("Rohinth","Male", "null", "Developer", "Rainbow"));
        members.add(new MemberModel("Suhas Siripole", "Male","null", "Developer", "Rainbow"));
        members.add(new MemberModel("Raagavendiran", "Male","null", "Intern-Developer", "Rainbow"));
        members.add(new MemberModel("Team Lead", "Male","null", "Team Lead", "Rainbow"));


        members.add(new MemberModel("Divagar", "Male", "null", "Developer", "Evolution"));
        members.add(new MemberModel("Sriharsha", "Male","null", "Developer", "Evolution"));
        members.add(new MemberModel("Harish", "Male", "null", "Developer", "Evolution"));
        members.add(new MemberModel("Team Lead", "Male", "null", "Team Lead", "Evolution"));

        teams.add(new TeamModel(new ArrayList<>(), members.get(5), "Rainbow"));
        teams.add(new TeamModel(new ArrayList<>(), members.get(9), "Evolution"));

        for (MemberModel mem : members) {
            if (mem.getTeam_name().equals("Rainbow")) {
                teams.get(0).setTeamMember(mem);
            } else if (mem.getTeam_name().equals("Evolution")) {
                teams.get(1).setTeamMember(mem);
            }
        }

    }
    private Dialog createDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure?")
                .setPositiveButton("Yes", (dialog, id) -> {
                    for (MemberModel mem :
                            members) {
                        if(!mem.getMobileNumber().equals("null")) {
                            SmsManager.getDefault().sendTextMessage(mem.getMobileNumber(), null, "CODE RED: Server Down", null, null);
                            Log.i("Useful Info📜","Message Sent: "+mem.getName());
                        }
                    }
                })
                .setNegativeButton("No", (dialog, id) -> dialog.cancel());
        return builder.create();
    }
    private void animateSelectedWidth(View view, int direction){
        if(view != null && direction!=0) {
            Animation a = new Animation() {
                @Override
                protected void applyTransformation(float interpolatedTime, Transformation t) {
                    RelativeLayout.LayoutParams viewParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                    if (direction > 0)
                        viewParams.width = (int) (dpTopx(MainActivity.this, 60) * interpolatedTime) + dpTopx(MainActivity.this, 60);
                    else
                        viewParams.width = (int) (dpTopx(MainActivity.this, 60) * (1 - interpolatedTime)) + dpTopx(MainActivity.this, 60);

                    view.requestLayout();
                }
            };

            a.setDuration(500);
            view.startAnimation(a);
        }
    }
    private void animateSelectedMargin(View view, boolean incLeft, boolean incRight, boolean decLeft, boolean decRight){
        if(view != null) {
            Animation a = new Animation() {
                @Override
                protected void applyTransformation(float interpolatedTime, Transformation t) {
                    RelativeLayout.LayoutParams viewParams = (RelativeLayout.LayoutParams) view.getLayoutParams();

                    if(incLeft) viewParams.leftMargin = (int) (dpTopx(MainActivity.this, 30) * interpolatedTime) + dpTopx(MainActivity.this, 15);
                    if(decRight) viewParams.rightMargin = (int) (dpTopx(MainActivity.this, 30) * (1 - interpolatedTime)) + dpTopx(MainActivity.this, 15);

                    if(incRight) viewParams.rightMargin = (int) (dpTopx(MainActivity.this, 30) * interpolatedTime) + dpTopx(MainActivity.this, 15);
                    if(decLeft) viewParams.leftMargin = (int) (dpTopx(MainActivity.this, 30) * (1 - interpolatedTime)) + dpTopx(MainActivity.this, 15);

                    view.requestLayout();
                }
            };

            a.setDuration(500);
            view.startAnimation(a);
        }
    }
    private void setVisible(int i) {
        allT.setVisibility(View.GONE);
        individualT.setVisibility(View.GONE);
        teamT.setVisibility(View.GONE);

        switch (i){
            case 0: individualT.setVisibility(View.VISIBLE);break;
            case 1: allT.setVisibility(View.VISIBLE);break;
            case 2: teamT.setVisibility(View.VISIBLE);break;
        }
    }
}