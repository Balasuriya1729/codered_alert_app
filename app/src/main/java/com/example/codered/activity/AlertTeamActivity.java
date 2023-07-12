package com.example.codered.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.codered.R;
import com.example.codered.Store;
import com.example.codered.adapter.TeamListAdapter;
import com.example.codered.model.TeamModel;

import java.util.ArrayList;

public class AlertTeamActivity extends AppCompatActivity {
    private ArrayList<TeamModel> data;
    private boolean isSearch = false, isAnimating = false;
    private TextView titleText;
    private EditText titleEdit;
    private ImageView icon;
    private TeamListAdapter teamListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert_team);

        if(Store.haveAUser()) {
            ImageView imageView = findViewById(R.id.profileImgTeam);
            Glide.with(this).load(Store.getUser().getPhotoUri()).into(imageView);

            imageView.setScaleX(2F);
            imageView.setScaleY(2F);
        }

        Intent intent = getIntent();
        try {
            if(intent.hasExtra("allTeams")){
                data = (ArrayList<TeamModel>) intent.getExtras().getSerializable("allTeams");
                setRecyclerView();
            }
        } catch (Exception e){
            Log.e("Caught Error❌","In Getting All Teams from Intent");
        }

        titleEdit = findViewById(R.id.titleedit);
        titleText = findViewById(R.id.titleText);
        icon = findViewById(R.id.searchIcon);

        titleEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                filterList(editable.toString());
            }
        });
    }

    private void filterList(String text) {
        ArrayList<TeamModel> newTeams = new ArrayList<>();

        for (TeamModel team :
                data) {
            if (team.getTeam_name().toLowerCase().trim().contains(text.toLowerCase().trim())) {
                newTeams.add(team);
            }
        }

        teamListAdapter.setNewTeams(newTeams);
    }

    private void setRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recycler_parent);
        teamListAdapter = new TeamListAdapter(data, this);
        recyclerView.setAdapter(teamListAdapter);

        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
    }
    public void goBack(View view){
        onBackPressed();
    }
    public void startSearch(View view){
//        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if(!isAnimating){
            isAnimating = true;
            if(!isSearch){
                titleText
                        .animate()
                        .translationX((titleText.getWidth() * -1)-50)
                        .alpha(0)
                        .setDuration(300);

                new android.os.Handler(Looper.getMainLooper()).postDelayed(() -> {
                    titleEdit
                            .animate()
                            .alpha(1);


                    isSearch = true;

                    isAnimating = false;
                    icon.setImageDrawable(getResources().getDrawable(R.drawable.baseline_add_24));
                    icon.setRotation(45);
                }, 300);

                titleEdit.requestFocus();
//                imm.showSoftInput(titleEdit, InputMethodManager.SHOW_IMPLICIT);

            }
            else{
                if(!titleEdit.getText().toString().equals(""))
                    titleEdit.setText("");
                else {
                    titleEdit
                            .animate()
                            .alpha(0)
                            .setDuration(300);

                    new android.os.Handler(Looper.getMainLooper()).postDelayed(() -> {
                        titleText
                                .animate()
                                .translationX(0)
                                .alpha(1);
                        isSearch = false;
                        icon.setImageDrawable(getResources().getDrawable(R.drawable.baseline_search_24));
                        icon.setRotation(0);
                    }, 300);
//                    imm.showSoftInput(titleEdit, InputMethodManager.HIDE_IMPLICIT_ONLY);
                }
                isAnimating = false;
            }
        }
    }
}
