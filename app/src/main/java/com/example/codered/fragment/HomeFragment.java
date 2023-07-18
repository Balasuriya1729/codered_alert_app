package com.example.codered.fragment;

import static com.example.codered.Store.dpTopx;
import static com.example.codered.Store.getCurrentIndexBottomNav;
import static com.example.codered.Store.getFirstEvent;
import static com.example.codered.Store.getFirstEventLDT;
import static com.example.codered.Store.setCurrentIndexBottomNav;
import static com.example.codered.Store.setEvents;
import static com.example.codered.Store.setFirstEvent;
import static com.example.codered.Store.setFirstEventLDT;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
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

public class HomeFragment extends Fragment {
    private ArrayList<MemberModel> members;
    private ArrayList<TeamModel> teams;
    private GoogleSignInClient client;
    private Store.User userAccount;
    private JSONObject jsonObject;
    private EventListAdapter eventListAdapter;
    private View previousSelectedView;
    private View bottomCards;
    private TextView teamT, individualT, allT;
    private ShimmerFrameLayout mShimmerViewContainer;
    private final CardView[] cardViews = new CardView[5];

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Store.haveAUser()) {
            userAccount = Store.getUser();
            Log.i("Home Fragment Says", "View On Create");

            assert getArguments() != null;
            String signInType = getArguments().getString("signInType");

            if (signInType.equals("New Sign")) {
                Log.i("Useful Info📜", "Going with Newly Signed In Account and getting Token");
                getAccessToken();
            } else {
                Log.i("Useful Info📜", "Going with Already Signed In Account");

                new Thread(() -> {
                    try {
                        SharedPreferences preferences = requireContext().getSharedPreferences(getString(R.string.preferences_name), 0);
                        jsonObject = new JSONObject(preferences.getString(getString(R.string.JSONObject), "NULL"));
                        Log.i("API says 🔊JSON Value", jsonObject.toString());

                        getEvents();
                    } catch (JSONException | GeneralSecurityException | IOException e) {
                        Log.e("Caught Error❌", "Token Error From Prefs " + e.getMessage() + "\n So, Refreshing Token");
                        try {
                            refreshToken();
                        } catch (JSONException ex) {
                            Log.e("Caught Error❌", "While Refreshing Token Try Again -> \n" + e.getMessage());
                        }
                    } catch (ParseException e) {
                        Log.e("Events Says", "Parsing Exception");
                    }
                }).start();
            }
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for requireContext() fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.i("Home Fragment Says", "View Created");

        previousSelectedView = view.findViewById(R.id.alertAll);
        bottomCards = view.findViewById(R.id.bottomCards);
        teamT = view.findViewById(R.id.teamIText);
        individualT = view.findViewById(R.id.indivIText);
        allT = view.findViewById(R.id.allIText);
        mShimmerViewContainer = view.findViewById(R.id.shimmer_view_container);

        if (Store.haveAUser()) {
            userAccount = Store.getUser();
            ImageView imageView = view.findViewById(R.id.profileImg);
            Glide.with(requireContext()).load(userAccount.getPhotoUri()).into(imageView);

            imageView.setScaleX(2F);
            imageView.setScaleY(2F);
        }

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        client = GoogleSignIn.getClient(requireContext(), gso);

        setVisible(getCurrentIndexBottomNav());
        setDateAndDay();
        settingDummyData();
        setEventRecyclerView();
        setOnClickListeners(view);
    }
    @Override
    public void onResume() {
        super.onResume();
        Log.i("Login Says", "Resumed");
        Store.inTeamSelectionPage = false;

        if (0 == Store.getEvents().size()) mShimmerViewContainer.startShimmerAnimation();
        else setEventsForTheUI();

        Log.i("Index -> ", ""+getCurrentIndexBottomNav());
        if(getCurrentIndexBottomNav()==0) {
            animateSelectedWidth(cardViews[2], 1);
            animateSelectedWidth(cardViews[4], -1);

            animateSelectedMargin(bottomCards, true, false, false, false);

            setVisible(0);

            previousSelectedView = cardViews[2];
        }
        else if(getCurrentIndexBottomNav()==2) {
            animateSelectedWidth(cardViews[3], 1);
            animateSelectedWidth(cardViews[4], -1);
            animateSelectedMargin(bottomCards, false, true, false, false);

            setVisible(2);
            previousSelectedView = cardViews[3];
        }

    }
    @Override
    public void onPause() {
        Log.i("Login Says", "Paused");
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
                .add("redirect_uri", "")
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
                    Log.e("Caught Error❌", "In Getting Token Response" + e.getMessage());
                } catch (ParseException e) {
                    Log.e("Events Says", "Parsing Exception");

                }
            }

            private void writeToSharedPreferences(String value) {
                SharedPreferences sharedPreferences = requireContext().getSharedPreferences(getString(R.string.preferences_name), Context.MODE_PRIVATE);
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
        Log.e("Caught Error❌", "In Refresh Token " + jsonObject.getString("refresh_token"));
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
                    Log.e("Caught Error❌", "In Getting Token Response" + e.getMessage());
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Error In Getting the Creds, \nPlease Sign in Again", Toast.LENGTH_LONG).show();
                        signOut();
                    });
                } catch (ParseException e) {
                    Log.e("Events Says", "Parsing Exception");

                }
            }

            private void writeToSharedPreferences(String value) {
                SharedPreferences sharedPreferences = requireContext().getSharedPreferences(getString(R.string.preferences_name), Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(getString(R.string.JSONObject), value);
                Log.i("API says 🔊JSON Value", value);
                editor.apply();
            }
        });
    }
    private void getEvents() throws GeneralSecurityException, IOException, JSONException, ParseException {
        Log.i("Events Says", "Started Getting the Events");

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
        ArrayList<EventModel> eventModels = new ArrayList<>();

        if (items.isEmpty())
            Log.w("Warning📄", "No upcoming events found.");

        else {
            Log.i("Useful Info📄", "Upcoming events Found");

            boolean isFirst = true;
            StringBuilder time = new StringBuilder();
            DateTime start;
            LocalDateTime l;

            for (Event event : items) {
                time.delete(0, time.capacity());
                start = event.getStart().getDateTime();
                String dateString;

                if (start != null) { //If Start Null => It should be a All Day Event
                    dateString = start.toString();
                    if (!(start.toString().charAt(23) == 'Z'))
                        dateString = start.toString().substring(0, 23).concat("Z");

                    DateTimeFormatter formatter =
                            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                    l = LocalDateTime.parse(dateString, formatter);

                    if (l.getMinute() < 10)
                        time.append(String.valueOf(l.getHour()).concat(":0").concat(String.valueOf(l.getMinute())));
                    else
                        time.append(String.valueOf(l.getHour()).concat(":").concat(String.valueOf(l.getMinute())));

                    String status;
                    int eventStartDate = Integer.parseInt(event.getStart().getDateTime().toString().substring(8, 10));

                    if (eventStartDate == todayDate) status = "Today";
                    else if (eventStartDate == todayDate + 1) status = "Tommorrow";
                    else status = event.getStart().getDateTime().toString().substring(0, 10);

                    if (isFirst && status.equals("Today")) {
                        setFirstEvent(event);
                        setFirstEventLDT(l);
                        isFirst = false;
                    } else {
                        eventModels.add(new EventModel(event.getSummary(), time.toString(), status, requireContext().getColor(R.color.sky_blue)));
                        System.out.printf("%s (%s) - (%s)\n", event.getSummary(), start, event.getEventType());
                    }
                }

            }
        }

        setEvents(eventModels);
        requireActivity().runOnUiThread(this::setEventsForTheUI);
    }

    //Handlers
    public void signOut() {
        client.revokeAccess();
        client.signOut();

        SharedPreferences preferences = requireContext().getSharedPreferences(getString(R.string.preferences_name), 0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(getString(R.string.JSONObject), "NULL");
        editor.apply();

        NavHostFragment navHostFragment =
                (NavHostFragment) requireActivity().getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        assert navHostFragment != null;
        NavController navController = navHostFragment.getNavController();

        navController.navigateUp();
    }



    //Utils
    private void setFirstEventForTheUI(Event firstEvent, LocalDateTime firstEventLDT) {
        TextView attendeeCountText = requireView().findViewById(R.id.mainCardAttendeeCountText);
        TextView eventLocText = requireView().findViewById(R.id.mainCardLocationText);
        TextView t1 = requireView().findViewById(R.id.dayAndYearText);

        if (firstEvent != null) {
            String title = firstEvent.getSummary();
            if (!(firstEventLDT == null)) {
                if (firstEventLDT.getMinute() < 10)
                    title = title.concat(" on ")
                            .concat(
                                    String.valueOf(firstEventLDT.getHour())
                                            .concat(":0")
                                            .concat(String.valueOf(firstEventLDT.getMinute()))
                            );
                else
                    title = title.concat(" on ")
                            .concat(
                                    String.valueOf(firstEventLDT.getHour())
                                            .concat(":")
                                            .concat(String.valueOf(firstEventLDT.getMinute()))
                            );
            }
            t1.setText(title);
            if (!(firstEventLDT == null)) {
                attendeeCountText.setText(String.valueOf(firstEvent.getAttendees().size()).concat("+"));

                StringBuilder stringBuilder = new StringBuilder();
                boolean startedGettingInitials = false;
                for (char letter :
                        firstEvent.getOrganizer().getEmail().toCharArray()) {
                    if (letter == '@') break;
                    if (letter == '.') {
                        stringBuilder.append(" ");
                        startedGettingInitials = true;
                        continue;
                    }

                    stringBuilder.append(letter);
                    if (startedGettingInitials) stringBuilder.append(" ");
                }

                eventLocText.setText(stringBuilder.toString().toUpperCase());
            } else Log.i("Useful Info📄", firstEvent.toString());

        } else {
            t1.setText("No Events Today");
            eventLocText.setText("-");
            attendeeCountText.setText("🤗");
        }
    }

    private void setEventsForTheUI() {
        setFirstEventForTheUI(getFirstEvent(), getFirstEventLDT());
        requireView().findViewById(R.id.progressForEvents).setVisibility(View.GONE);

        if (Store.getEvents().size() > 0) {
            requireView().findViewById(R.id.eventCards).setVisibility(View.VISIBLE);
            eventListAdapter.setEvents(Store.getEvents());
        } else requireView().findViewById(R.id.noEventsText).setVisibility(View.VISIBLE);

        mShimmerViewContainer.stopShimmerAnimation();
        mShimmerViewContainer.setVisibility(View.GONE);
        requireView().findViewById(R.id.originalContent).setVisibility(View.VISIBLE);
    }

    private void setOnClickListeners(View view) {
        NavHostFragment navHostFragment =
                (NavHostFragment) requireActivity().getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        assert navHostFragment != null;
        NavController navController = navHostFragment.getNavController();
        cardViews[0] = view.findViewById(R.id.backButton);
        cardViews[1] = view.findViewById(R.id.profileButton);
        cardViews[2] = view.findViewById(R.id.alertIndividualButton);
        cardViews[3] = view.findViewById(R.id.alertTeam);
        cardViews[4] = view.findViewById(R.id.alertAll);

        cardViews[0].setOnClickListener(view1 -> navController.navigateUp());
        cardViews[1].setOnClickListener(view1 -> view.findViewById(R.id.model).setVisibility(View.VISIBLE));
        cardViews[4].setOnClickListener(view1 -> { //ALL
            if (getCurrentIndexBottomNav() == 1) {
                Dialog dialog = createDialog();
                dialog.show();
            } else {
                animateSelectedWidth(view1, 1);
                animateSelectedWidth(previousSelectedView, -1);

                if (getCurrentIndexBottomNav() == 0) animateSelectedMargin(bottomCards, false, false, true, false);
                if (getCurrentIndexBottomNav() == 2) animateSelectedMargin(bottomCards, false, false, false, true);

                setVisible(1);

                previousSelectedView = view1;
            }
        });
        cardViews[3].setOnClickListener(view1 -> { //TEAM
            if (getCurrentIndexBottomNav() == 2) {
                Store.inTeamSelectionPage = true;

                Bundle args = new Bundle();
                args.putSerializable("allTeams", teams);
                navController.navigate(R.id.alertTeamFragment, args);
            } else {
                animateSelectedWidth(view1, 1);
                animateSelectedWidth(previousSelectedView, -1);

                if (getCurrentIndexBottomNav() == 0) animateSelectedMargin(bottomCards, false, true, true, false);
                if (getCurrentIndexBottomNav() == 1) animateSelectedMargin(bottomCards, false, true, false, false);

                setVisible(2);

                previousSelectedView = view1;
            }
        });
        cardViews[2].setOnClickListener(view1 -> {
            if (getCurrentIndexBottomNav() == 0) {
                Bundle args = new Bundle();
                args.putSerializable("allIndividuals", members);
                navController.navigate(R.id.alertIndividualFragment, args);
            } else {
                animateSelectedWidth(view1, 1);
                animateSelectedWidth(previousSelectedView, -1);

                if (getCurrentIndexBottomNav() == 2) animateSelectedMargin(bottomCards, true, false, false, true);
                if (getCurrentIndexBottomNav() == 1) animateSelectedMargin(bottomCards, true, false, false, false);

                setVisible(0);

                previousSelectedView = view1;
            }
        });

        view.findViewById(R.id.model).setOnClickListener(view1 -> closeSignIn(view));
        view.findViewById(R.id.signOutText).setOnClickListener(view1 -> signOut());
    }

    private void closeSignIn(View view){
        if(view.findViewById(R.id.model).getVisibility()==View.VISIBLE)
            view.findViewById(R.id.model).setVisibility(View.GONE);
    }

    private void setEventRecyclerView() {
        RecyclerView recyclerView = requireView().findViewById(R.id.eventCards);
        eventListAdapter = new EventListAdapter(new ArrayList<>(), requireContext());

        recyclerView.setAdapter(eventListAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
    }

    private void setDateAndDay() {
        DateTime now = new DateTime(System.currentTimeMillis());
        TextView t2 = requireView().findViewById(R.id.dateText);

        StringBuilder DateString = new StringBuilder();
        DateString.append(now.toString().substring(8, 10));
        switch (Integer.parseInt(now.toString().substring(5, 7))) {
            case 12:
                DateString.append(" DEC ");
                break;
            case 11:
                DateString.append(" NOV ");
                break;
            case 10:
                DateString.append(" OCT ");
                break;
            case 9:
                DateString.append(" SEP ");
                break;
            case 8:
                DateString.append(" AUG ");
                break;
            case 7:
                DateString.append(" JUL ");
                break;
            case 6:
                DateString.append(" JUN ");
                break;
            case 5:
                DateString.append(" MAY ");
                break;
            case 4:
                DateString.append(" APR ");
                break;
            case 3:
                DateString.append(" MAR ");
                break;
            case 2:
                DateString.append(" FEB ");
                break;
            case 1:
                DateString.append(" JAN ");
                break;
        }
        DateString.append(now.toString().substring(0, 4));

        t2.setText(DateString.toString());
    }

    private void settingDummyData() {
        members = new ArrayList<>();
        teams = new ArrayList<>();

        members.add(new MemberModel("Ari Prasath", "Male", "null", "Developer", "Rainbow"));
        members.add(new MemberModel("Aathika", "Female", "null", "Developer", "Rainbow"));
        members.add(new MemberModel("Rohinth", "Male", "null", "Developer", "Rainbow"));
        members.add(new MemberModel("Suhas Siripole", "Male", "null", "Developer", "Rainbow"));
        members.add(new MemberModel("Raagavendiran", "Male", "null", "Intern-Developer", "Rainbow"));
        members.add(new MemberModel("Team Lead", "Male", "null", "Team Lead", "Rainbow"));


        members.add(new MemberModel("Divagar", "Male", "null", "Developer", "Evolution"));
        members.add(new MemberModel("Sriharsha", "Male", "null", "Developer", "Evolution"));
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

    private Dialog createDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setMessage("Are you sure?")
                .setPositiveButton("Yes", (dialog, id) -> {
                    for (MemberModel mem :
                            members) {
                        if (!mem.getMobileNumber().equals("null")) {
                            SmsManager.getDefault().sendTextMessage(mem.getMobileNumber(), null, "CODE RED: Server Down", null, null);
                            Log.i("Useful Info📜", "Message Sent: " + mem.getName());
                        }
                    }
                })
                .setNegativeButton("No", (dialog, id) -> dialog.cancel());
        return builder.create();
    }

    private void animateSelectedWidth(View view, int direction) {
        if (view != null && direction != 0) {
            Animation a = new Animation() {
                @Override
                protected void applyTransformation(float interpolatedTime, Transformation t) {
                    RelativeLayout.LayoutParams viewParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                    if (direction > 0)
                        viewParams.width = (int) (dpTopx(requireContext(), 60) * interpolatedTime) + dpTopx(requireContext(), 60);
                    else
                        viewParams.width = (int) (dpTopx(requireContext(), 60) * (1 - interpolatedTime)) + dpTopx(requireContext(), 60);

                    view.requestLayout();
                }
            };

            a.setDuration(500);
            view.startAnimation(a);
        }
    }

    private void animateSelectedMargin(View view, boolean incLeft, boolean incRight, boolean decLeft, boolean decRight) {
        if (view != null) {
            Animation a = new Animation() {
                @Override
                protected void applyTransformation(float interpolatedTime, Transformation t) {
                    RelativeLayout.LayoutParams viewParams = (RelativeLayout.LayoutParams) view.getLayoutParams();

                    if (incLeft)
                        viewParams.leftMargin = (int) (dpTopx(requireContext(), 30) * interpolatedTime) + dpTopx(requireContext(), 15);
                    if (decRight)
                        viewParams.rightMargin = (int) (dpTopx(requireContext(), 30) * (1 - interpolatedTime)) + dpTopx(requireContext(), 15);

                    if (incRight)
                        viewParams.rightMargin = (int) (dpTopx(requireContext(), 30) * interpolatedTime) + dpTopx(requireContext(), 15);
                    if (decLeft)
                        viewParams.leftMargin = (int) (dpTopx(requireContext(), 30) * (1 - interpolatedTime)) + dpTopx(requireContext(), 15);

                    view.requestLayout();
                }
            };

            a.setDuration(500);
            view.startAnimation(a);
        }
    }

    private void setVisible(int i) {
        setCurrentIndexBottomNav(i);

        allT.setVisibility(View.GONE);
        individualT.setVisibility(View.GONE);
        teamT.setVisibility(View.GONE);

        switch (i) {
            case 0:
                individualT.setVisibility(View.VISIBLE);
                break;
            case 1:
                allT.setVisibility(View.VISIBLE);
                break;
            case 2:
                teamT.setVisibility(View.VISIBLE);
                break;
        }
    }
}