package pem.de.heroes.main;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.CursorAdapter;
import android.widget.SimpleCursorAdapter;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.firebase.geofire.util.GeoUtils;
import com.google.firebase.database.ChildEventListener;
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

import pem.de.heroes.detail.DetailItemActivity;
import pem.de.heroes.shared.Helper;
import pem.de.heroes.R;
import pem.de.heroes.model.ListItem;

import android.widget.ImageButton;
import android.widget.SearchView;

public class HelpFragment extends Fragment {

    private static final String ARG_TYPE = "fragment_type";
    private static final String ITEM_ID = "item_id";
    private static final String TAG = "HelpFragment";
    String fragment_type = "ask";

    private List<ListItem> list = new ArrayList<>();
    private Map<String, Integer> itemToDistance = new HashMap<>();
    private List<String> keys = new ArrayList<>();

    private RecyclerView recyclerView;
    public Adapter adapter;
    DatabaseReference ref;
    GeoFire geoFire;
    GeoLocation home;
    private ValueEventListener itemValueListener;
    GeoQuery geoQuery;
    private int initialListSize;
    private int iterationCount;
    private boolean fetchedItemIds;
    String userid;
    private SimpleCursorAdapter suggestionadapter;
    SearchView searchView;

    private String[] SUGGESTIONS;

    public HelpFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        SUGGESTIONS = getResources().getStringArray(R.array.suggestions);
        if (getArguments() != null) {
            fragment_type = getArguments().getString(ARG_TYPE);
        }
        ref = FirebaseDatabase.getInstance().getReference(fragment_type);

        ref.orderByChild("date");
        DatabaseReference georef = FirebaseDatabase.getInstance().getReference("geofire/"+fragment_type);
        geoFire = new GeoFire(georef);
        setupListeners();

        final String[] from = new String[] {"tags"};
        final int[] to = new int[] {android.R.id.text1};
        suggestionadapter = new SimpleCursorAdapter(getActivity(),
                android.R.layout.simple_list_item_1,
                null,
                from,
                to,
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
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
        View v = inflater.inflate(R.layout.fragment_help, container, false);
        return v;
    }




    @Override
    public void onStart(){
        super.onStart();

        //Load Preferences
        SharedPreferences sharedPref = getActivity().getSharedPreferences("pem.de.hero.userid", Context.MODE_PRIVATE);
        home = new GeoLocation(Helper.getDouble(sharedPref,"homelat",0),Helper.getDouble(sharedPref,"homelong",0));
        userid = sharedPref.getString("userid","No UserID");
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

                int position = getUserPosition(key);
                Log.d("Fragment", "onKeyExited: "+position);
                if(position!=-1){
                    list.remove(position);
                    itemToDistance.remove(key);
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
                final ListItem listItem = dataSnapshot.getValue(ListItem.class);
                if(listItem==null){
                    return;
                }

                Log.d("Fragment", "Item Title: "+listItem.getTitle());
                listItem.setid(dataSnapshot.getKey());
                listItem.setDistance(itemToDistance.get(dataSnapshot.getKey()));

                if(itemToDistance.containsKey(dataSnapshot.getKey())){
                    if(keys.contains(dataSnapshot.getKey())){
                        if(!listItem.getAgent().equals("")&&!listItem.getAgent().equals(userid)&&!listItem.getUserID().equals(userid)){
                            Log.d(TAG,"removing item");
                            list.remove(getUserPosition(dataSnapshot.getKey()));
                            adapter.notifyDataSetChanged();
                        }else{
                            itemUpdated(listItem);
                        }

                    }else{

                        if(listItem.getAgent().equals("")||listItem.getAgent().equals(userid)||listItem.getUserID().equals(userid)){
                            newItem(listItem);
                        }

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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.menu_main, menu);
        MenuItem item = menu.findItem(R.id.search);
        searchView = new SearchView(((MainActivity) getContext()).getSupportActionBar().getThemedContext());
        MenuItemCompat.setShowAsAction(item, MenuItemCompat.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW | MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
        MenuItemCompat.setActionView(item, searchView);
        searchView.setIconified(false);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                populateAdapter();
                final List<ListItem> filteredModelList = filter(list, newText);
                adapter.setFilter(filteredModelList);
                return false;
            }
        });
        searchView.setQueryHint(getResources().getString(R.string.suggestion_hint));
        searchView.setSuggestionsAdapter(suggestionadapter);
        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionClick(int position) {
                // Your code here
                searchView.setQuery(SUGGESTIONS[position],false);
                return true;
            }

            @Override
            public boolean onSuggestionSelect(int position) {
                // Your code here
                return true;
            }
        });
        MenuItemCompat.setOnActionExpandListener(item,
                new MenuItemCompat.OnActionExpandListener() {
                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem item) {
                        // Do something when collapsed
                        adapter.setFilter(list);
                        return true; // Return true to collapse action view
                    }

                    @Override
                    public boolean onMenuItemActionExpand(MenuItem item) {
                        // Do something when expanded
                        populateAdapter();
                        return true; // Return true to expand action view
                    }
                });

    }

    private void populateAdapter() {
        final MatrixCursor c = new MatrixCursor(new String[]{ BaseColumns._ID, "tags" });
        for (int i=0; i<SUGGESTIONS.length; i++) {
            c.addRow(new Object[] {i, SUGGESTIONS[i]});

        }
        suggestionadapter.changeCursor(c);
    }

    private void populateAdapter(String query) {
        final MatrixCursor c = new MatrixCursor(new String[]{ BaseColumns._ID, "tags" });
        for (int i=0; i<SUGGESTIONS.length; i++) {
            if (SUGGESTIONS[i].toLowerCase().startsWith(query.toLowerCase()))
                c.addRow(new Object[] {i, SUGGESTIONS[i]});
        }
        suggestionadapter.changeCursor(c);
    }

    private List<ListItem> filter(List<ListItem> models, String query) {

        query = query.toLowerCase();
        final List<ListItem> filteredModelList = new ArrayList<>();

        for (ListItem model : models) {
            if(!model.getTags().isEmpty()) {
                final String text = model.getTags().toLowerCase();
                String[] hint = getResources().getString(R.string.suggestion_hint).split(",");
                if(query.contains(hint[0])){
                    if(model.getUserID().equals(userid)){
                        filteredModelList.add(model);
                    }
                }
                else if(query.contains(hint[1])){
                    if(model.getAgent().equals(userid)){
                        filteredModelList.add(model);
                    }
                }
                else if (text.contains(query)) {
                    filteredModelList.add(model);
                }
            }
        }
        return filteredModelList;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle item selection
        switch (item.getItemId()) {
            case R.id.search:

                //       onCall();   //your logic

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
