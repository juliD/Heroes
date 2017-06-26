package pem.de.heroes;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import pem.de.heroes.R;

public class ProfileFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private SharedPreferences sharedPreferences;
    private TextView usernameTextView;
    private TextView titleTextView;
    private TextView karmaTextView;
    private ProgressBar karmaProgressBar;

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

        titleTextView = (TextView) view.findViewById(R.id.titleTextView);
        titleTextView.setText("Local Hero");

        karmaTextView = (TextView) view.findViewById(R.id.karmaTextView);
        karmaTextView.setText("250 / 1000 Karma");

        karmaProgressBar = (ProgressBar) view.findViewById(R.id.karmaProgressBar);
        karmaProgressBar.setProgress(25);

        return view;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.e("Profile", key);
        if (key.equals("username")) {
            usernameTextView.setText(sharedPreferences.getString("username", ""));
        }
    }
}
