package com.example.codered.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.codered.R;
import com.example.codered.Store;
import com.example.codered.adapter.TeamListAdapter;
import com.example.codered.model.TeamModel;

import java.util.ArrayList;

public class AlertTeamFragment extends Fragment {
    private ArrayList<TeamModel> data;
    private boolean isSearch = false, isAnimating = false;
    private TextView titleText;
    private EditText titleEdit;
    private ImageView icon;
    private TeamListAdapter teamListAdapter;
    public AlertTeamFragment() {
        // Required empty public constructor
    }
    
    public static AlertTeamFragment newInstance(String param1, String param2) {
        AlertTeamFragment fragment = new AlertTeamFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for requireContext() fragment
        return inflater.inflate(R.layout.fragment_alert_team, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(Store.haveAUser()) {
            ImageView imageView = view.findViewById(R.id.profileImgTeam);
            Glide.with(requireContext()).load(Store.getUser().getPhotoUri()).into(imageView);

            imageView.setScaleX(2F);
            imageView.setScaleY(2F);
        }

        ArrayList<TeamModel> args = (ArrayList<TeamModel>) getArguments().getSerializable("allTeams");

        try {
            if(args!=null){
                data = args;
                setRecyclerView(view);
            }
        } catch (Exception e){
            Log.e("Caught Error❌","In Getting All Teams from Intent");
        }

        titleEdit = view.findViewById(R.id.titleedit);
        titleText = view.findViewById(R.id.titleText);
        icon = view.findViewById(R.id.searchIcon);

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
        NavHostFragment navHostFragment =
                (NavHostFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();

        view.findViewById(R.id.backButtonTeam).setOnClickListener(view1 -> navController.navigateUp());
        view.findViewById(R.id.searchIcon).setOnClickListener(view1 -> {
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
    private void setRecyclerView(View view) {
        RecyclerView recyclerView = view.findViewById(R.id.recycler_parent);
        teamListAdapter = new TeamListAdapter(data, requireContext());
        recyclerView.setAdapter(teamListAdapter);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));
    }

}