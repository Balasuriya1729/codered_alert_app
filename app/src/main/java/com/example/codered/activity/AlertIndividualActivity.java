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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.codered.R;
import com.example.codered.Store;
import com.example.codered.adapter.IndividualListAdapter;
import com.example.codered.model.MemberModel;

import java.util.ArrayList;

public class AlertIndividualActivity extends AppCompatActivity {
    private ArrayList<MemberModel> data;
    private boolean isSearch = false, isAnimating = false;
    private TextView titleText;
    private EditText titleEdit;
    private ImageView icon;
    private IndividualListAdapter individualListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert_individual);

        Intent intent = getIntent();

        if(Store.haveAUser()) {
            ImageView imageView = findViewById(R.id.profileImgIndividual);
            Glide.with(this).load(Store.getUser().getPhotoUri()).into(imageView);

            imageView.setScaleX(2F);
            imageView.setScaleY(2F);
        }

        try {
            if(intent.hasExtra("allIndividuals")){
                data = (ArrayList<MemberModel>) intent.getExtras().getSerializable("allIndividuals");
                setRecyclerView();
            }
        } catch (Exception e){
          Log.e("Caught Error❌","In Getting All Individuals from Intent");
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
        ArrayList<MemberModel> newMembers = new ArrayList<>();

        for (MemberModel mem :
                data) {
            if (mem.getName().toLowerCase().trim().contains(text.toLowerCase().trim())) {
                newMembers.add(mem);
            }
        }

        individualListAdapter.setNewMembers(newMembers);
    }

    public void goBack(View view){
        onBackPressed();
    }
    private void setRecyclerView(){
        RecyclerView recyclerView = findViewById(R.id.individualsList);
        individualListAdapter = new IndividualListAdapter(data, this);
        recyclerView.setAdapter(individualListAdapter);

        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
    }

    public void startSearch(View view){
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
    }

}
