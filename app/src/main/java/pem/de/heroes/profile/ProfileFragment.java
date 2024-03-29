package pem.de.heroes.profile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import pem.de.heroes.R;
import pem.de.heroes.main.MainActivity;

public class ProfileFragment extends Fragment {

    // predefined levels a user can achieve with name, karma and hero image
    private final Hero[] heroes = {
            new Hero("Wannabe Hero", 100, R.drawable.hero1),
            new Hero("Amateur Hero", 1000, R.drawable.hero2),
            new Hero("Advanced Hero", 10000, R.drawable.hero3),
            new Hero("Master Hero", 100000, R.drawable.hero4),
            new Hero("Local Hero", 1000000, R.drawable.hero5)
    };

    public ProfileFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        final TextView username = (TextView) view.findViewById(R.id.username);
        final ImageView heroImage = (ImageView) view.findViewById(R.id.heroImage);
        final View hero = view.findViewById(R.id.hero);
        final View medal1 = view.findViewById(R.id.medal1);
        final View medal2 = view.findViewById(R.id.medal2);
        final View medal3 = view.findViewById(R.id.medal3);
        final View medal4 = view.findViewById(R.id.medal4);

        // reset all views
        setHero(hero, heroImage, 0);
        setMedal(medal1, 0);
        setMedal(medal2, 0);
        setMedal(medal3, 0);
        setMedal(medal4, 0);

        SharedPreferences sharedPref = getActivity().getSharedPreferences("pem.de.hero.userid", Context.MODE_PRIVATE);
        String userId = sharedPref.getString("userid", "");
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users").child(userId);

        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                onChildChanged(dataSnapshot, s);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                switch (dataSnapshot.getKey()) {
                    case "karma":
                        setHero(hero, heroImage, dataSnapshot.getValue(Integer.class));
                        break;
                    case "asksCreated":
                        setMedal(medal1, dataSnapshot.getValue(Integer.class));
                        break;
                    case "asksDone":
                        setMedal(medal2, dataSnapshot.getValue(Integer.class));
                        break;
                    case "offersCreated":
                        setMedal(medal3, dataSnapshot.getValue(Integer.class));
                        break;
                    case "offersDone":
                        setMedal(medal4, dataSnapshot.getValue(Integer.class));
                        break;
                    case "username":
                        username.setText(dataSnapshot.getValue(String.class));
                        break;
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return view;
    }

    /**
     * Updates all information concerning the current hero (depending on the karma count)
     * @param heroView the view where the hero title, the karma progress bar and the karma label is shown
     * @param heroImage the view where the hero image is shown
     * @param karma current karma count of the user
     */
    private void setHero(View heroView, ImageView heroImage, int karma) {
        Hero hero = null;
        int progress = karma;
        for (Hero h : heroes) {
            if (progress - h.getRequiredKarmaTillNextLevel() < 0) {
                hero = h;
                break;
            } else {
                progress -= h.getRequiredKarmaTillNextLevel();
            }
        }

        if (hero == null) {
            hero = heroes[heroes.length - 1];
            progress = hero.getRequiredKarmaTillNextLevel(); // maximum
        }

        TextView heroTitle = (TextView) heroView.findViewById(R.id.heroTitle);
        heroTitle.setText(hero.getName());

        Drawable drawable = ResourcesCompat.getDrawable(getResources(), hero.getResource(), null);
        heroImage.setImageDrawable(drawable);

        TextView karmaText = (TextView) heroView.findViewById(R.id.karmaText);
        karmaText.setText(progress + " / " + hero.getRequiredKarmaTillNextLevel() + " Karma");

        ProgressBar karmaProgress = (ProgressBar) heroView.findViewById(R.id.karmaProgress);
        karmaProgress.setProgress(100 * progress / hero.getRequiredKarmaTillNextLevel()); // percentage
    }

    /**
     * Updates all information concerning one medal
     * @param medalView the view where the medal is shown
     * @param progress current count of successes of the user (of different types, e.g. created offers)
     */
    private void setMedal(View medalView, int progress) {
        ProgressBar medalProgressBar = (ProgressBar) medalView.findViewById(R.id.progressBar);
        TextView medalNumber = (TextView) medalView.findViewById(R.id.number);

        final int MIN_COUNT_GOLD = 100;
        final int MIN_COUNT_SILVER = 50;
        final int MIN_COUNT_BRONZE = 10;

        int color = 0, max = 0;
        Drawable progressDrawable = null;

        if (progress < MIN_COUNT_BRONZE) {
            // medal with blue color, set min bronze count as next step
            color = ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null);
            progressDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.progress_normal, null);
            max = MIN_COUNT_BRONZE;
        } else if (progress < MIN_COUNT_SILVER) {
            // medal with bronze color, set min silver count as next step
            color = ResourcesCompat.getColor(getResources(), R.color.bronze, null);
            progressDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.progress_bronze, null);
            max = MIN_COUNT_SILVER;
        } else if (progress < MIN_COUNT_GOLD) {
            // medal with silver color, set min gold count as next step
            color = ResourcesCompat.getColor(getResources(), R.color.silver, null);
            progressDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.progress_silver, null);
            max = MIN_COUNT_GOLD;
        } else {
            // medal with gold color, set max to min gold count, we are done
            color = ResourcesCompat.getColor(getResources(), R.color.gold, null);
            progressDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.progress_gold, null);
            max = MIN_COUNT_GOLD;
        }

        medalNumber.setTextColor(color);
        medalNumber.setText(progress + "");

        progress = progress < MIN_COUNT_GOLD ? progress: MIN_COUNT_GOLD; // maximum
        if (medalProgressBar.getProgress() != progress || progress == 0) {
            medalProgressBar.setProgressDrawable(progressDrawable);
            medalProgressBar.setMax(max);
            medalProgressBar.setProgress(progress); // maximum
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();

        // inflate menu and get edit item
        inflater.inflate(R.menu.menu_profile, menu);
        MenuItem edit = menu.findItem(R.id.edit);

        // listener for when edit icon will be pressed
        edit.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent i = new Intent(getContext(), EditSettingsActivity.class);
                startActivityForResult(i,1);
                return true;
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        Intent i = new Intent(getActivity(), MainActivity.class);
        i.putExtra("tab",0);
        startActivity(i);
        getActivity().finish();
    }

}
