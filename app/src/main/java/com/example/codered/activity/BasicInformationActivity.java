package com.example.codered.activity;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.codered.R;
import com.example.codered.Store;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

public class BasicInformationActivity extends AppCompatActivity{
    private RelativeLayout[] sections = new RelativeLayout[4];
    private ImageView[] dots = new ImageView[4];
    private ImageView profileEdit;
    private TextView prev, next, phNoInputText;
    private TextInputEditText editTextPhNo;
    private int currentPage = 0;
    private CardView editProfileButton;
    private CardView[] teamCards = new CardView[4], positionCards = new CardView[5];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic_infomation);

//        sections[0] = findViewById(R.id.section0);
//        sections[1] = findViewById(R.id.section1);
//        sections[2] = findViewById(R.id.section2);
//        sections[3] = findViewById(R.id.section3);
//
//        dots[0] = findViewById(R.id.sectionDot0);
//        dots[1] = findViewById(R.id.sectionDot1);
//        dots[2] = findViewById(R.id.sectionDot2);
//        dots[3] = findViewById(R.id.sectionDot3);
//
//        teamCards[0] = findViewById(R.id.teamRainbowCard);
//        teamCards[1] = findViewById(R.id.teamEvolutionCard);
//        teamCards[2] = findViewById(R.id.teamAvatarCard);
//        teamCards[3] = findViewById(R.id.teamMatrixCard);
//
//        positionCards[0] = findViewById(R.id.managerCard);
//        positionCards[1] = findViewById(R.id.teamLeadCard);
//        positionCards[2] = findViewById(R.id.scrumMasterCard);
//        positionCards[3] = findViewById(R.id.devCard);
//        positionCards[4] = findViewById(R.id.internCard);
//
//        prev = findViewById(R.id.prevButton);
//        next = findViewById(R.id.nextButton);
//
//        profileEdit = findViewById(R.id.profile_img_holder);
//        phNoInputText = findViewById(R.id.textInInput);
//        editTextPhNo = findViewById(R.id.editPhoneNo);
//        editProfileButton = findViewById(R.id.editProfileButton);

//        Glide.with(this).load(Store.getUser().getPhotoUri()).into(profileEdit);
//
//        editTextPhNo.setOnFocusChangeListener((view, b) -> {
//            if(b){
//                Animation a = new Animation() {
//                    @Override
//                    protected void applyTransformation(float interpolatedTime, Transformation t) {
//                        phNoInputText.setScaleX((float) (1 - (0.4*interpolatedTime)));
//                        phNoInputText.setScaleY((float) (1 - (0.4*interpolatedTime)));
//                        phNoInputText.setTranslationX(-70*interpolatedTime);
//                        phNoInputText.setTranslationY(-35*interpolatedTime);
//                    }
//                };
//                a.setDuration(500);
//                phNoInputText.startAnimation(a);
//            } else{
//                if(Objects.requireNonNull(editTextPhNo.getText()).toString().equals("")){
//                    Log.i("Basic Info Act Says: ", "Focus Lost");
//                    Animation a = new Animation() {
//                        @Override
//                        protected void applyTransformation(float interpolatedTime, Transformation t) {
//                            phNoInputText.setScaleX((float) (0.6 + (0.4*interpolatedTime)));
//                            phNoInputText.setScaleY((float) (0.6 + (0.4*interpolatedTime)));
//                            phNoInputText.setTranslationX(-70*(1-interpolatedTime));
//                            phNoInputText.setTranslationY(-35*(1-interpolatedTime));
//                        }
//                    };
//                    a.setDuration(500);
//                    phNoInputText.startAnimation(a);
//                }
//            }
//        });
//        prev.setOnClickListener(view -> {
//            if(currentPage!=0 && !Store.isAnimating){
//                Store.isAnimating = true;
//                currentPage-=1;
//                sections[currentPage].setVisibility(View.VISIBLE);
//
//                Animation a = new Animation() {
//                    @Override
//                    protected void applyTransformation(float interpolatedTime, Transformation t) {
//                        sections[currentPage+1].setAlpha(1-interpolatedTime);
//                        sections[currentPage].setAlpha(interpolatedTime);
//                    }
//                };
//
//                a.setDuration(500);
//                sections[currentPage].startAnimation(a);
//
//                new android.os.Handler(Looper.myLooper()).postDelayed(() -> {
//                    dots[currentPage].setBackground(AppCompatResources.getDrawable(this, R.drawable.circle_selected_indicator));
//                    dots[currentPage+1].setBackground(AppCompatResources.getDrawable(this, R.drawable.circle_indicator));
//                }, 500);
//
//                new android.os.Handler(Looper.myLooper()).postDelayed(() -> {
//                    Store.isAnimating = false;
//                    sections[currentPage+1].setVisibility(View.GONE);
//                }, 1000);
//            }
//            if(next.getText().equals("Continue") && currentPage != 3) next.setText("Next");
//        });
//        next.setOnClickListener(view -> {
//            StringBuilder notSatisfiedField = new StringBuilder("");
//            switch (currentPage){
//                case 0:
//                    if(Objects.requireNonNull(editTextPhNo.getText()).toString().equals("")) notSatisfiedField.append("Phone Number");
//                    else Store.getUser().setPhNo(editTextPhNo.getText().toString());
//                    break;
//                case 1:
//                    if(Store.getUser().getTeamName().equals("")) notSatisfiedField.append("Team Name");
//                    break;
//                case 2:
//                    if(Store.getUser().getTeamPosition().equals("")) notSatisfiedField.append("Team Position");
//                    break;
//            }
//
//            if(notSatisfiedField.toString().equals("")){
//                if(currentPage<3 && !Store.isAnimating){
//                    Store.isAnimating = true;
//                    currentPage+=1;
//                    sections[currentPage].setVisibility(View.VISIBLE);
//
//                    Animation a = new Animation() {
//                        @Override
//                        protected void applyTransformation(float interpolatedTime, Transformation t) {
//                            sections[currentPage-1].setAlpha(1-interpolatedTime);
//                            sections[currentPage].setAlpha(interpolatedTime);
//                        }
//                    };
//
//                    a.setDuration(500);
//                    sections[currentPage].startAnimation(a);
//
//                    new android.os.Handler(Looper.myLooper()).postDelayed(() -> {
//                        dots[currentPage].setBackground(AppCompatResources.getDrawable(this, R.drawable.circle_selected_indicator));
//                        dots[currentPage-1].setBackground(AppCompatResources.getDrawable(this, R.drawable.circle_indicator));
//                    }, 500);
//
//                    new android.os.Handler(Looper.myLooper()).postDelayed(() -> {
//                        Store.isAnimating = false;
//                        sections[currentPage-1].setVisibility(View.GONE);
//                    }, 1000);
//
//                    if(currentPage == 3) next.setText("Continue");
//                }
//                else {
//                    Store.getUser().addMeToFirebase();
//                    Intent intent = new Intent(this, MainActivity.class);
//                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    intent.putExtra("signInType", "New Sign");
//
//                    startActivity(intent);
//                }
//            } else {
//                Toast.makeText(this, "Required Field: "+notSatisfiedField, Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        ActivityResultLauncher<Intent> startActivityForResult = registerForActivityResult(
//                new ActivityResultContracts.StartActivityForResult(),
//                result -> {}
//        );
//        editProfileButton.setOnClickListener(view -> {
//            Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
//            chooseFile.setType("image/jpeg");
//            chooseFile = Intent.createChooser(chooseFile, "Choose a file");
//            startActivityForResult.launch(chooseFile);
//        });
//
//        setListenersForCards(teamCards, 1);
//        setListenersForCards(positionCards, 2);
    }

    private void setListenersForCards(CardView[] cards, int flag) {
        for (int i = 0; i < cards.length; i++) {
            int finalI = i;
            cards[i].setOnClickListener(view -> {
                selectCard(finalI, cards);
                TextView child = (TextView) cards[finalI].getChildAt(0);

                if(flag == 1)
                    Store.getUser().setTeamName((String) child.getText());
                else
                    Store.getUser().setTeamPosition((String) child.getText());
            });
        }
    }

    private void selectCard(int i, CardView[] cardViews) {
        for (int j = 0; j < cardViews.length; j++) {
            if(i==j) cardViews[j].setForeground(AppCompatResources.getDrawable(this, R.drawable.selected_border_style));
            else cardViews[j].setForeground(AppCompatResources.getDrawable(this, R.drawable.not_selected_border_style));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == AppCompatActivity.RESULT_OK && data!=null) {
            Uri uri = data.getData();
            Glide.with(this).load(uri).into(profileEdit);

            Store.getUser().setPhotoUrl(uri.toString());
        }
    }
}