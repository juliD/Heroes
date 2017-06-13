package pem.de.heroes;

import android.content.Intent;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;

public class HelpFragment extends Fragment {

    private static final String ARG_TYPE = "fragment_type";
    String fragment_type = "ask";

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
                TextView userView = (TextView) view.findViewById(R.id.by_user);
                titleView.setText(item.getTitle());
                infosView.setText(item.getDescription());


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