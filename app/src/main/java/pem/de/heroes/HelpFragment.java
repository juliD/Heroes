package pem.de.heroes;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.firebase.geofire.util.GeoUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class HelpFragment extends Fragment {

    private static final String ARG_TYPE = "fragment_type";
    private static final String ITEM_ID = "item_id";
    String fragment_type = "ask";

    private List<ListItem> list = new ArrayList<>();
    private Map<String, Integer> itemToDistance = new HashMap<>();
    private List<String> keys = new ArrayList<>();

    private RecyclerView recyclerView;
    public Adapter adapter;
    DatabaseReference ref;
    GeoFire geoFire;
    GeoLocation home;
    private Set<String> itemWithListeners = new HashSet<>();
    private ValueEventListener itemValueListener;
    GeoQuery geoQuery;
    private int initialListSize;
    private int iterationCount;
    private boolean fetchedItemIds;

    public HelpFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            fragment_type = getArguments().getString(ARG_TYPE);
        }
        ref = FirebaseDatabase.getInstance().getReference(fragment_type);
        ref.orderByChild("date");
        DatabaseReference georef = FirebaseDatabase.getInstance().getReference("geofire/"+fragment_type);
        geoFire = new GeoFire(georef);
        setupListeners();

    }

    public static HelpFragment newInstance(String type) {
        HelpFragment fragment = new HelpFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TYPE, type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_help, container, false);
    }



    @Override
    public void onStart(){
        super.onStart();

        //Load Preferences
        SharedPreferences sharedPref = getActivity().getSharedPreferences("pem.de.hero.userid", Context.MODE_PRIVATE);
        home = new GeoLocation(Helper.getDouble(sharedPref,"homelat",0),Helper.getDouble(sharedPref,"homelong",0));
        final String userid = sharedPref.getString("userid","No UserID");
        final int radius = sharedPref.getInt("radius", 500);



        //Set up recyclerView
        recyclerView = (RecyclerView) getView().findViewById(R.id.recycler_view);
        adapter = new Adapter(list,userid,getActivity());

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getActivity(), recyclerView, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                String itemID = list.get(position).getid();
                Intent intent = new Intent(getActivity(), DetailItemActivity.class);
                intent.putExtra("selected", list.get(position));
                if(fragment_type.equals("offer")){

                    intent.putExtra("fragment_type","offer");
                }else{
                    intent.putExtra("fragment_type","ask");
                }
                intent.putExtra(ITEM_ID, itemID);
                startActivity(intent);
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

        fetchListItems(radius);

    }

    private int getUserPosition(String id) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getid().equals(id)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Add GeoQuery to Firebase Data
     * @param radius max Distance for Query Items to be shown
     */
    public void fetchListItems(int radius){

        geoQuery = geoFire.queryAtLocation(home,radius/1000);
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                int distance = (int)(GeoUtils.distance(location,home));

                itemToDistance.put(key,distance);
                addItemListener(key);

            }

            public void addItemListener(String id){
                ref.child(id).addValueEventListener(itemValueListener);
            }

            @Override
            public void onKeyExited(String key) {
                Log.d("Fragment", "onKeyExited: ");
                if (itemWithListeners.contains(key)) {
                    int position = getUserPosition(key);
                    list.remove(position);
                    keys.remove(position);
                    adapter.notifyItemRemoved(position);
                }
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                Log.d("Fragment", "onKeyMoved: ");
            }

            @Override
            public void onGeoQueryReady() {
                Log.d("Fragment", "onGeoQueryReady: ");

                initialListSize = itemToDistance.size();
                if (initialListSize == 0) {
                    fetchedItemIds = true;
                }
                iterationCount = 0;

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                Log.e("Fragment", "onGeoQueryError: ", error.toException());
            }
        });


    }
    private int getIndexOfNewItem(ListItem u) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getid().equals(u.getid())) {
                Log.d("Fragment", "getIndexOfNewUser: " + i);
                return i;
            }
        }
        throw new RuntimeException();
    }

    private void setupListeners(){
        itemValueListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("Fragment", "datasnapshot Key: "+ dataSnapshot.getKey());
                ListItem listItem = dataSnapshot.getValue(ListItem.class);
                Log.d("Fragment", "Item Title: "+listItem.getTitle());
                listItem.setid(dataSnapshot.getKey());
                listItem.setDistance(itemToDistance.get(dataSnapshot.getKey()));

                if(itemToDistance.containsKey(dataSnapshot.getKey())){
                    if(keys.contains(dataSnapshot.getKey())){
                        itemUpdated(listItem);
                    }else{
                        newItem(listItem);
                    }
                }

            }

            private void newItem(ListItem listitem){
                iterationCount++;
                list.add(0,listitem);
                keys.add(0,listitem.getid());
                if(!fetchedItemIds&&iterationCount==initialListSize){
                    fetchedItemIds=true;
                    Collections.sort(list);
                    adapter.setItems(list);
                }else{
                    Collections.sort(list);
                    adapter.notifyItemInserted(getIndexOfNewItem(listitem));
                }



            }

            private void itemUpdated(ListItem listItem){
                int position = getUserPosition(listItem.getid());
                list.set(position,listItem);
                adapter.notifyItemChanged(position);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Fragment", "onCancelled: ", databaseError.toException());
            }




        };
    }

    class RecyclerTouchListener implements RecyclerView.OnItemTouchListener{
        private GestureDetector mGestureDetector;
        private ClickListener mClickListener;


        public RecyclerTouchListener(final Context context, final RecyclerView recyclerView, final ClickListener clickListener) {
            this.mClickListener = clickListener;
            mGestureDetector = new GestureDetector(context,new GestureDetector.SimpleOnGestureListener(){
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child = recyclerView.findChildViewUnder(e.getX(),e.getY());
                    if (child!=null && clickListener!=null){
                        clickListener.onLongClick(child,recyclerView.getChildAdapterPosition(child));
                    }
                    super.onLongPress(e);
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            View child = rv.findChildViewUnder(e.getX(), e.getY());
            if (child!=null && mClickListener!=null && mGestureDetector.onTouchEvent(e)){
                mClickListener.onClick(child,rv.getChildAdapterPosition(child));
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {

        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }

    public static interface ClickListener{
        public void onClick(View view, int position);
        public void onLongClick(View view, int position);
    }





}
