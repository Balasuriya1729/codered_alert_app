package com.example.codered.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
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
import com.example.codered.adapter.IndividualListAdapter;
import com.example.codered.model.MemberModel;

import java.util.ArrayList;

public class AlertIndividualFragment extends Fragment {
    private ArrayList<MemberModel> data;
    private boolean isSearch = false, isAnimating = false;
    private TextView titleText;
    private EditText titleEdit;
    private ImageView icon;
    private IndividualListAdapter individualListAdapter;
    public AlertIndividualFragment() {
        // Required empty public constructor
    }

    public static AlertIndividualFragment newInstance(String param1, String param2) {
        AlertIndividualFragment fragment = new AlertIndividualFragment();
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
        return inflater.inflate(R.layout.fragment_alert_individual, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ArrayList<MemberModel> args = (ArrayList<MemberModel>) getArguments().getSerializable("allIndividuals");

        if(Store.haveAUser()) {
            ImageView imageView = view.findViewById(R.id.profileImgIndividual);
            Glide.with(requireContext()).load(Store.getUser().getPhotoUri()).into(imageView);

            imageView.setScaleX(2F);
            imageView.setScaleY(2F);
        }

        try {
            if(args!=null){
                data = args;
                setRecyclerView(view);
            }
        } catch (Exception e){
            Log.e("Caught Error❌","In Getting All Individuals from Intent");
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

        view.findViewById(R.id.backButtonIndividual).setOnClickListener(view1 -> navController.navigateUp());
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
                    }
                    isAnimating = false;
                }
            }
        });
    }

    private void filterList(String text) {
        ArrayList<MemberModel> newMembers = new ArrayList<>();

        for (MemberModel mem :
                data) {
            if (mem.getName().toLowerCase().trim().contains(text.toLowerCase().trim())) {
                newMembers.add(mem);
            }
        }

        individualListAdapter.setNewMembers(newMembers);
    }

    private void setRecyclerView(View view){
        RecyclerView recyclerView = view.findViewById(R.id.individualsList);
        individualListAdapter = new IndividualListAdapter(data, requireContext());
        recyclerView.setAdapter(individualListAdapter);

        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
    }
}