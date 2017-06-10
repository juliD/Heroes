package pem.de.heroes;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class AskForHelpFragment extends Fragment {



    public AskForHelpFragment() {
        // Required empty public constructor
    }


    public static AskForHelpFragment newInstance(String param1, String param2) {
        AskForHelpFragment fragment = new AskForHelpFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_ask_for_help, container, false);
    }


}
