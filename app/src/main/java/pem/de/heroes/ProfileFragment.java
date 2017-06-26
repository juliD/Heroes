package pem.de.heroes;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ProfileFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private SharedPreferences sharedPreferences;
    private TextView usernameTextView;
    private TextView titleTextView;
    private TextView karmaTextView;
    private ProgressBar karmaProgressBar;

    private Hero[] heroes = {
            new Hero("Wannabe Hero", 100),
            new Hero("Amateur Hero", 1000),
            new Hero("Advanced Hero", 10000),
            new Hero("Local Hero", 100000)
    };

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

        Pair<Hero, Integer> heroAndProgress = getCurrentHeroAndProgress(karma);
        Hero hero = heroAndProgress.first;
        int progress = heroAndProgress.second;

        usernameTextView = (TextView) view.findViewById(R.id.usernameTextView);
        usernameTextView.setText(username);

        titleTextView = (TextView) view.findViewById(R.id.titleTextView);
        titleTextView.setText(hero.getName());

        karmaTextView = (TextView) view.findViewById(R.id.karmaTextView);
        karmaTextView.setText(progress + " / " + hero.getRequiredKarma() + " Karma");

        karmaProgressBar = (ProgressBar) view.findViewById(R.id.karmaProgressBar);
        karmaProgressBar.setProgress(100 * progress / hero.getRequiredKarma()); // percentage

        return view;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.e("Profile", key);
        if (key.equals("username")) {
            usernameTextView.setText(sharedPreferences.getString("username", ""));
        }
    }

    private Pair<Hero, Integer> getCurrentHeroAndProgress(int karma) {
        for (Hero h : heroes) {
            if (karma - h.getRequiredKarma() < 0) {
                return new Pair<>(h, karma);
            } else {
                karma -= h.getRequiredKarma();
            }
        }
        Hero lastHero = heroes[heroes.length - 1];
        return new Pair<>(lastHero, lastHero.getRequiredKarma()); // maximum
    }
}
