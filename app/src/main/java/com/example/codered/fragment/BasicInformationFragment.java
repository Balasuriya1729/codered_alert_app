package com.example.codered.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.os.Looper;
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

import com.bumptech.glide.Glide;
import com.example.codered.MainActivity;
import com.example.codered.R;
import com.example.codered.Store;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

public class BasicInformationFragment extends Fragment {
    private final RelativeLayout[] sections = new RelativeLayout[4];
    private final ImageView[] dots = new ImageView[4];
    private TextView next;
    private TextView phNoInputText;
    private TextInputEditText editTextPhNo;
    private int currentPage = 0;
    private final CardView[] teamCards = new CardView[4], positionCards = new CardView[5];
    public BasicInformationFragment() {
        // Required empty public constructor
    }
    public static BasicInformationFragment newInstance(String param1, String param2) {
        BasicInformationFragment fragment = new BasicInformationFragment();
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
        return inflater.inflate(R.layout.fragment_basic_infomation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sections[0] = view.findViewById(R.id.section0);
        sections[1] = view.findViewById(R.id.section1);
        sections[2] = view.findViewById(R.id.section2);
        sections[3] = view.findViewById(R.id.section3);

        dots[0] = view.findViewById(R.id.sectionDot0);
        dots[1] = view.findViewById(R.id.sectionDot1);
        dots[2] = view.findViewById(R.id.sectionDot2);
        dots[3] = view.findViewById(R.id.sectionDot3);

        teamCards[0] = view.findViewById(R.id.teamRainbowCard);
        teamCards[1] = view.findViewById(R.id.teamEvolutionCard);
        teamCards[2] = view.findViewById(R.id.teamAvatarCard);
        teamCards[3] = view.findViewById(R.id.teamMatrixCard);

        positionCards[0] = view.findViewById(R.id.managerCard);
        positionCards[1] = view.findViewById(R.id.teamLeadCard);
        positionCards[2] = view.findViewById(R.id.scrumMasterCard);
        positionCards[3] = view.findViewById(R.id.devCard);
        positionCards[4] = view.findViewById(R.id.internCard);

        TextView prev = view.findViewById(R.id.prevButton);
        next = view.findViewById(R.id.nextButton);

        ImageView profileEdit = view.findViewById(R.id.profile_img_holder);
        phNoInputText = view.findViewById(R.id.textInInput);
        editTextPhNo = view.findViewById(R.id.editPhoneNo);
        CardView editProfileButton = view.findViewById(R.id.editProfileButton);

        Glide.with(requireContext()).load(Store.getUser().getPhotoUri()).into(profileEdit);

        editTextPhNo.setOnFocusChangeListener((view1, b) -> {
            if(b){
                Animation a = new Animation() {
                    @Override
                    protected void applyTransformation(float interpolatedTime, Transformation t) {
                    phNoInputText.setScaleX((float) (1 - (0.4*interpolatedTime)));
                    phNoInputText.setScaleY((float) (1 - (0.4*interpolatedTime)));
                    phNoInputText.setTranslationX(-70*interpolatedTime);
                    phNoInputText.setTranslationY(-35*interpolatedTime);
                    }
                };
                a.setDuration(500);
                phNoInputText.startAnimation(a);
            } else{
                if(Objects.requireNonNull(editTextPhNo.getText()).toString().equals("")){
                    Log.i("Basic Info Act Says: ", "Focus Lost");
                    Animation a = new Animation() {
                        @Override
                        protected void applyTransformation(float interpolatedTime, Transformation t) {
                        phNoInputText.setScaleX((float) (0.6 + (0.4*interpolatedTime)));
                        phNoInputText.setScaleY((float) (0.6 + (0.4*interpolatedTime)));
                        phNoInputText.setTranslationX(-70*(1-interpolatedTime));
                        phNoInputText.setTranslationY(-35*(1-interpolatedTime));
                        }
                    };
                    a.setDuration(500);
                    phNoInputText.startAnimation(a);
                }
            }
        });
        prev.setOnClickListener(view1 -> {
            if(currentPage!=0 && !Store.isAnimating){
                Store.isAnimating = true;
                currentPage-=1;
                sections[currentPage].setVisibility(View.VISIBLE);

                Animation a = new Animation() {
                    @Override
                    protected void applyTransformation(float interpolatedTime, Transformation t) {
                        sections[currentPage+1].setAlpha(1-interpolatedTime);
                        sections[currentPage].setAlpha(interpolatedTime);
                    }
                };

                a.setDuration(500);
                sections[currentPage].startAnimation(a);

                new android.os.Handler(Looper.myLooper()).postDelayed(() -> {
                    dots[currentPage].setBackground(AppCompatResources.getDrawable(requireContext(), R.drawable.circle_selected_indicator));
                    dots[currentPage+1].setBackground(AppCompatResources.getDrawable(requireContext(), R.drawable.circle_indicator));
                }, 500);

                new android.os.Handler(Looper.myLooper()).postDelayed(() -> {
                    Store.isAnimating = false;
                    sections[currentPage+1].setVisibility(View.GONE);
                }, 1000);
            }
            if(next.getText().equals("Continue") && currentPage != 3) next.setText("Next");
        });
        next.setOnClickListener(view1 -> {
            StringBuilder notSatisfiedField = new StringBuilder("");
            switch (currentPage){
                case 0:
                    if(Objects.requireNonNull(editTextPhNo.getText()).toString().equals("")) notSatisfiedField.append("Phone Number");
                    else Store.getUser().setPhNo(editTextPhNo.getText().toString());
                    break;
                case 1:
                    if(Store.getUser().getTeamName().equals("")) notSatisfiedField.append("Team Name");
                    break;
                case 2:
                    if(Store.getUser().getTeamPosition().equals("")) notSatisfiedField.append("Team Position");
                    break;
            }

            if(notSatisfiedField.toString().equals("")){
                if(currentPage<3 && !Store.isAnimating){
                    Store.isAnimating = true;
                    currentPage+=1;
                    sections[currentPage].setVisibility(View.VISIBLE);

                    Animation a = new Animation() {
                        @Override
                        protected void applyTransformation(float interpolatedTime, Transformation t) {
                            sections[currentPage-1].setAlpha(1-interpolatedTime);
                            sections[currentPage].setAlpha(interpolatedTime);
                        }
                    };

                    a.setDuration(500);
                    sections[currentPage].startAnimation(a);

                    new android.os.Handler(Looper.myLooper()).postDelayed(() -> {
                        dots[currentPage].setBackground(AppCompatResources.getDrawable(requireContext(), R.drawable.circle_selected_indicator));
                        dots[currentPage-1].setBackground(AppCompatResources.getDrawable(requireContext(), R.drawable.circle_indicator));
                    }, 500);

                    new android.os.Handler(Looper.myLooper()).postDelayed(() -> {
                        Store.isAnimating = false;
                        sections[currentPage-1].setVisibility(View.GONE);
                    }, 1000);

                    if(currentPage == 3) next.setText("Continue");
                }
                else {
                    Store.getUser().addMeToFirebase();
                    NavHostFragment navHostFragment =
                            (NavHostFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
                    NavController navController = navHostFragment.getNavController();
                    Bundle args = new Bundle();
                    args.putString("signInType", "New Sign");
                    navController.navigate(R.id.homeFragment, args);
                }
            } else {
                Toast.makeText(requireContext(), "Required Field: "+notSatisfiedField, Toast.LENGTH_SHORT).show();
            }
        });

        ActivityResultLauncher<Intent> startActivityForResult = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == AppCompatActivity.RESULT_OK && result.getData()!=null) {
                        Uri uri = result.getData().getData();
                        Glide.with(this).load(uri).into(profileEdit);

                        Store.getUser().setPhotoUrl(uri.toString());
                    }
                }
        );

        editProfileButton.setOnClickListener(view1 -> {
            Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
            chooseFile.setType("image/jpeg");
            chooseFile = Intent.createChooser(chooseFile, "Choose a file");
            startActivityForResult.launch(chooseFile);
        });

        setListenersForCards(teamCards, 1);
        setListenersForCards(positionCards, 2);
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
            if(i==j) cardViews[j].setForeground(AppCompatResources.getDrawable(requireContext(), R.drawable.selected_border_style));
            else cardViews[j].setForeground(AppCompatResources.getDrawable(requireContext(), R.drawable.not_selected_border_style));
        }
    }
}