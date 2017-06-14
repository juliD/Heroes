package pem.de.heroes;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class HelpFragment extends Fragment {

    private static final String ARG_TYPE = "fragment_type";
    String fragment_type = "ask";
    LatLng home;

    public HelpFragment() {
        // Required empty public constructor
    }

    public static HelpFragment newInstance(String type) {
        HelpFragment fragment = new HelpFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TYPE, type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            fragment_type = getArguments().getString(ARG_TYPE);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_help, container, false);

        //Load Home preference
        SharedPreferences sharedPref = getActivity().getSharedPreferences("pem.de.hero.userid", Context.MODE_PRIVATE);
        home = Helper.getLocationFromAddress(sharedPref.getString("home","Oettingenstraße 67, München"),getActivity());


        // ListView
        ListView listView = (ListView) rootView.findViewById(R.id.listView);
        if (!fragment_type.equals("ask") && !fragment_type.equals("offer")) {
            // add profile header
            View header = getActivity().getLayoutInflater().inflate(R.layout.profile_header, null);
            listView.addHeaderView(header);
        }


        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(fragment_type);

        ListAdapter adapter = new FirebaseListAdapter<ListItem>(this.getActivity(), ListItem.class, R.layout.item, ref) {
            protected void populateView(View view, ListItem item, int position)
            {
                TextView titleView = (TextView) view.findViewById(R.id.item_title);
                TextView infosView = (TextView) view.findViewById(R.id.item_description);
                TextView distView = (TextView) view.findViewById(R.id.distance);
                titleView.setText(item.getTitle());
                infosView.setText(item.getDescription());

                float distance = Helper.calculateDistance(home,Helper.getLocationFromAddress(item.getAddress(),getActivity()));
                distView.setText(Helper.distanceToString(distance));
            }
        };

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                ListItem selected = (ListItem) parent.getItemAtPosition(position);
                Intent intent = new Intent(getActivity(), DetailItemActivity.class);
                intent.putExtra("selected", selected);
                startActivity(intent);
            }
        });

        return rootView;
    }


}