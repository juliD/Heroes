package pem.de.heroes;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableWrapper;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ProfileFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private SharedPreferences sharedPreferences;
    private TextView usernameTextView;

    private final Hero[] heroes = {
            new Hero("Wannabe Hero", 100),
            new Hero("Amateur Hero", 1000),
            new Hero("Advanced Hero", 10000),
            new Hero("Master Hero", 100000),
            new Hero("Local Hero", 1000000)
    };

    private final int MIN_COUNT_BRONZE = 10;
    private final int MIN_COUNT_SILVER = 50;
    private final int MIN_COUNT_GOLD = 100;

    public ProfileFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        sharedPreferences = getActivity().getSharedPreferences("pem.de.hero.userid", Context.MODE_PRIVATE);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        String username = sharedPreferences.getString("username", "");
        int karma = sharedPreferences.getInt("karma", 0);

        usernameTextView = (TextView) view.findViewById(R.id.usernameTextView);
        usernameTextView.setText(username);

        setHero(view, 50000);

        setMedal(view, R.id.medal1, 5);
        setMedal(view, R.id.medal2, 20);
        setMedal(view, R.id.medal3, 53);
        setMedal(view, R.id.medal4, 125);

        return view;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.e("Profile", key);
        if (key.equals("username")) {
            usernameTextView.setText(sharedPreferences.getString("username", ""));
        }
    }

    private void setHero(View view, int karma) {
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

        TextView titleTextView = (TextView) view.findViewById(R.id.titleTextView);
        titleTextView.setText(hero.getName());

        TextView  karmaTextView = (TextView) view.findViewById(R.id.karmaTextView);
        karmaTextView.setText(progress + " / " + hero.getRequiredKarmaTillNextLevel() + " Karma");

        ProgressBar karmaProgressBar = (ProgressBar) view.findViewById(R.id.karmaProgressBar);
        karmaProgressBar.setProgress(100 * progress / hero.getRequiredKarmaTillNextLevel()); // percentage
    }

    private void setMedal(View view, int resource, int progress) {
        RelativeLayout medal = (RelativeLayout) view.findViewById(resource);
        ProgressBar medalProgressBar = (ProgressBar) medal.findViewById(R.id.progressBar);
        TextView medalNumber = (TextView) medal.findViewById(R.id.number);

        if (progress < MIN_COUNT_BRONZE) {
            int colorNormal = ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null);
            Drawable progressNormal = ResourcesCompat.getDrawable(getResources(), R.drawable.progress_normal, null);
            medalNumber.setTextColor(colorNormal);
            medalProgressBar.setProgressDrawable(progressNormal);
            medalProgressBar.setMax(MIN_COUNT_BRONZE);
        } else if (progress < MIN_COUNT_SILVER) {
            int colorBronze = ResourcesCompat.getColor(getResources(), R.color.bronze, null);
            Drawable progressBronze = ResourcesCompat.getDrawable(getResources(), R.drawable.progress_bronze, null);
            medalNumber.setTextColor(colorBronze);
            medalProgressBar.setProgressDrawable(progressBronze);
            medalProgressBar.setMax(MIN_COUNT_SILVER);
        } else if (progress < MIN_COUNT_GOLD) {
            int colorSilver = ResourcesCompat.getColor(getResources(), R.color.silver, null);
            Drawable progressSilver = ResourcesCompat.getDrawable(getResources(), R.drawable.progress_silver, null);
            medalNumber.setTextColor(colorSilver);
            medalProgressBar.setProgressDrawable(progressSilver);
            medalProgressBar.setMax(MIN_COUNT_GOLD);
        } else {
            int colorGold = ResourcesCompat.getColor(getResources(), R.color.gold, null);
            Drawable progressGold = ResourcesCompat.getDrawable(getResources(), R.drawable.progress_gold, null);
            medalNumber.setTextColor(colorGold);
            medalProgressBar.setProgressDrawable(progressGold);
            medalProgressBar.setMax(MIN_COUNT_GOLD);
        }

        medalProgressBar.setProgress(progress < MIN_COUNT_GOLD ? progress: MIN_COUNT_GOLD); // maximum
        medalNumber.setText(progress + "");
    }
}
