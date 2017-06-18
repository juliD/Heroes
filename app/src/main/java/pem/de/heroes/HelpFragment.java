package pem.de.heroes;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HelpFragment extends Fragment implements GeoQueryEventListener {

    private static final String ARG_TYPE = "fragment_type";
    private static final String ITEM_ID = "item_id";
    String fragment_type = "ask";
    LatLng home;
    ListView listView;
    pem.de.heroes.FirebaseListAdapter<ListItem> adapter;
    ArrayList<ListItem> items;
    DatabaseReference ref;
    GeoFire geoFire;


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
        ref = FirebaseDatabase.getInstance().getReference(fragment_type);
        geoFire = new GeoFire(FirebaseDatabase.getInstance().getReference("geofire/"+fragment_type));





    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_help, container, false);



        return rootView;
    }

    @Override
    public void onStart(){
        super.onStart();
        //Load Home preference
        SharedPreferences sharedPref = getActivity().getSharedPreferences("pem.de.hero.userid", Context.MODE_PRIVATE);
        home =new LatLng(Helper.getDouble(sharedPref,"homelat",0),Helper.getDouble(sharedPref,"homelong",0));


        // ListView
        listView = (ListView) getView().findViewById(R.id.listView);


        adapter = new pem.de.heroes.FirebaseListAdapter<ListItem>(ref.equalTo("geofire"),ListItem.class,R.layout.item,getActivity()) {
            @Override
            protected void populateView(View view, ListItem item) {
                TextView titleView = (TextView) view.findViewById(R.id.item_title);
                TextView infosView = (TextView) view.findViewById(R.id.item_description);
                TextView distView = (TextView) view.findViewById(R.id.distance);
                titleView.setText(item.getTitle());
                infosView.setText(item.getDescription());

                float distance = Helper.calculateDistance(home,Helper.getLocationFromAddress(item.getAddress(),getActivity()));
                Log.d("HelpFragment","Distance= "+distance);
                if(distance<1000) {
                    distView.setText(Math.round(distance) + "m");
                }else{
                    distView.setText(Math.round(distance/1000) + "km");
                }
            }

            @Override
            protected List<ListItem> filters(List<ListItem> models, CharSequence constraint) {
                return null;
            }

            @Override
            protected Map<String, ListItem> filterKeys(List<ListItem> mModels) {
                return null;
            }
        };
        listView.setAdapter(adapter);


        GeoQuery query = geoFire.queryAtLocation(new GeoLocation(home.latitude, home.longitude),2);
        query.addGeoQueryEventListener(this);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {

                //String itemID = firebaseadapter.getRef(position).getKey();      //get the key of our firebase item
                ListItem selected = (ListItem) parent.getItemAtPosition(position);
                Intent intent = new Intent(getActivity(), DetailItemActivity.class);
                intent.putExtra("selected", selected);
                if(fragment_type.equals("offer")){
                    intent.putExtra(ARG_TYPE,"offer");
                }else{
                    intent.putExtra(ARG_TYPE,"ask");
                }
                //intent.putExtra(ITEM_ID, itemID);
                startActivity(intent);
            }
        });




    }

    @Override
    public void onKeyEntered(String key, GeoLocation location) {
        DatabaseReference tempRef = ref.child(key);
        tempRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String key = dataSnapshot.getKey();
                if (!adapter.exists(key)) {
                    //add new item
                    Log.d("HelpFragment", "item added " + key);
                    adapter.addSingle(dataSnapshot);
                    adapter.notifyDataSetChanged();
                } else {
                    //update item
                    Log.d("HelpFragment", "item updated: " + key);
                    adapter.update(dataSnapshot, key);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("HelpFragment", "cancelled with error:" + databaseError.getMessage());
            }
        });
    }

    @Override
    public void onKeyExited(String key) {
        adapter.remove(key);
    }

    @Override
    public void onKeyMoved(String key, GeoLocation location) {

    }

    @Override
    public void onGeoQueryReady() {

    }

    @Override
    public void onGeoQueryError(DatabaseError error) {
        Log.e("HelpFragment", "There was an error with this query: " + error);
    }


}