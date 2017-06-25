package pem.de.heroes;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;


public class DetailItemActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String ARG_TYPE = "fragment_type";
    private static final String ITEM_ID = "item_id";
    private DatabaseReference agentref;
    private DatabaseReference typeref;
    private DatabaseReference ownerref;
    private SharedPreferences sharedPref;
    String marker;
    ListItem listitem;
    LatLng home;
    private DatabaseReference ref;
    private String type = "offer";
    private String itemID;
    private String preferenceUserID;
    private String listUserID;
    TextView agent_textview;
    TextView owner_username_textview;
    private Button accept;

    User agent;
    User owner;

    private CustomViewPager viewPager;
    private DetailViewPagerAdapter myViewPagerAdapter;
    private LinearLayout dotsLayout;
    private TextView[] dots;
    private int[] layouts;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.newactivity_detail_item);

        ref = FirebaseDatabase.getInstance().getReference();
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                type = extras.getString(ARG_TYPE);
                itemID = extras.getString(ITEM_ID);
            }
        }
        typeref = ref.child(type);

        sharedPref = this.getSharedPreferences("pem.de.hero.userid", Context.MODE_PRIVATE);
        preferenceUserID = sharedPref.getString("userid", "No UserID");

        viewPager = (CustomViewPager) findViewById(R.id.view_pager);
        dotsLayout = (LinearLayout) findViewById(R.id.layoutDots);


        // layouts of all detail sliders
        layouts = new int[]{
                R.layout.detail_item_request,
                R.layout.detail_item_chat
        };

        // adding bottom dots
        addBottomDots(0);


        myViewPagerAdapter = new DetailViewPagerAdapter();
        viewPager.setAdapter(myViewPagerAdapter);
        viewPager.addOnPageChangeListener(viewPagerPageChangeListener);
        viewPager.setPagingEnabled(false);

        listitem = getIntent().getParcelableExtra("selected");


    }

    @Override
    public void onMapReady(GoogleMap map) {
        int dimen = 50;
        BitmapDrawable bitmapdraw = (BitmapDrawable) ResourcesCompat.getDrawable(getResources(), R.drawable.ic_home_black_24dp, null);
        Bitmap b = bitmapdraw.getBitmap();
        Bitmap smallMarker = Bitmap.createScaledBitmap(b, dimen, dimen, false);
        LatLng pos = Helper.getLocationFromAddress(listitem.getAddress(), this);
        map.addMarker(new MarkerOptions().position(pos).title(listitem.getTitle()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        map.addMarker(new MarkerOptions().position(home).title("Dein Zuhause").icon(BitmapDescriptorFactory.fromBitmap(smallMarker)));
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(pos).zoom(15f).build();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }





    private void buildRequestPage(View view) {
        accept = (Button) view.findViewById(R.id.accept);
        if (listitem != null) {
            TextView title = (TextView) view.findViewById(R.id.detail_title);
            TextView info = (TextView) view.findViewById(R.id.detail_description);
            agent_textview = (TextView) view.findViewById(R.id.agent);

            listUserID = listitem.getUserID();

            title.setText(listitem.getTitle());
            info.setText(listitem.getDescription());


            //enable swiping
            if(listUserID.equals(preferenceUserID) || listitem.getAgent().equals(preferenceUserID)){
                viewPager.setPagingEnabled(true);
            }

            if (listUserID.equals(preferenceUserID)) {
                if (listitem.getAgent().equals("")) {
                    agent_textview.setText("Leider hat noch niemand deine Anfrage angenommen.");
                } else {
                    agentref = ref.child("users").child(listitem.getAgent());
                    setAgent();
                }
            } else {
                if (listitem.getAgent().equals("")) {
                    agent_textview.setText("Noch niemand hat den Vorgang angenommen. Schnapp' ihn dir!");
                } else if (listitem.getAgent().equals(preferenceUserID)) {
                    agent_textview.setText("Du hast diese Anfrage angenommen!");
                } else {
                    agent_textview.setText("Jemand anderes hat die Anfrage leider vor dir angenommen.");
                }
            }


            //color button green
            if (preferenceUserID.equals(listUserID)) {
                if(!(listitem.getAgent().equals(""))){
                    accept.setText("Karma überweisen");
                    accept.setBackgroundColor(ContextCompat.getColor(this, R.color.complete));
                }else{
                    accept.setText("Anfrage löschen");
                    accept.setBackgroundColor(ContextCompat.getColor(this,R.color.delete));
                }

            }

            accept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (preferenceUserID.equals(listUserID)) {
                        if(!(listitem.getAgent().equals(""))){
                            finish();
                            //code after this should be run anyways even though we call finish
                            Toast.makeText(DetailItemActivity.this, "100 Karma wurde vergeben!", Toast.LENGTH_SHORT).show();
                            giveKarma(agentref.child("karma"));
                            ref.child("geofire").child(type).child(itemID).removeValue();
                            typeref.child(itemID).removeValue();




                        }else{
                            finish();
                            Toast.makeText(DetailItemActivity.this, "Anfrage wurde gelöscht", Toast.LENGTH_SHORT).show();
                            ref.child("geofire").child(type).child(itemID).removeValue();
                            typeref.child(itemID).removeValue();



                        }

                    }  else {
                        typeref.child(itemID).child("agent").setValue(preferenceUserID);
                        agent_textview.setText("Du hast diese Anfrage angenommen!");
                        viewPager.setPagingEnabled(true);
                    }

                }
            });
        }
    }

    private void buildChatPage(View view) {
        if(listitem!=null){
            owner_username_textview = (TextView) view.findViewById(R.id.username);
            TextView address = (TextView) view.findViewById(R.id.show_address);

            ownerref = ref.child("users").child(listitem.getUserID());

            setUsername();
            address.setText(listitem.getAddress());

            home = new LatLng(Helper.getDouble(sharedPref, "homelat", 0), Helper.getDouble(sharedPref, "homelong", 0));
            SupportMapFragment mapFragment =
                    (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.maps);
            mapFragment.getMapAsync(this);
        }
    }

    public void setAgent() {
        if (!listitem.getAgent().equals("")) {
            ValueEventListener agentListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    //get database value

                    agent = dataSnapshot.getValue(User.class);
                    if(agent!=null){
                        agent_textview.setText(agent.getUsername());
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Getting agent failed, create log
                    Log.w("on cancelled", "loadPost:onCancelled", databaseError.toException());
                    Toast.makeText(DetailItemActivity.this, "Failed to load post.", Toast.LENGTH_SHORT).show();
                }
            };
            //firebase referenz auf den User, der im listitem steht.
            agentref.addListenerForSingleValueEvent(agentListener);
        }
    }


    private void giveKarma(DatabaseReference reference){
        reference.runTransaction(new Transaction.Handler(){
            @Override
                public Transaction.Result doTransaction(final MutableData currentData) {
                    if (currentData.getValue() == null) {
                        currentData.setValue(100);
                    } else {
                        int karma = currentData.getValue(Integer.class);
                        currentData.setValue(karma + 100);
                    }
                    return Transaction.success(currentData);
                }

            public void onComplete(DatabaseError firebaseError, boolean committed, DataSnapshot currentData) {
                if (firebaseError != null) {
                    System.out.println("Firebase counter increment failed.");
                } else {
                    System.out.println("Firebase counter increment succeeded.");
                }
            }
        });
    }

    public void setUsername() {
            ValueEventListener ownerListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    //get database value
                    owner = dataSnapshot.getValue(User.class);
                    if(owner!=null){
                        owner_username_textview.setText(owner.getUsername());
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Getting agent failed, create log
                    Log.w("on cancelled", "loadPost:onCancelled", databaseError.toException());
                    Toast.makeText(DetailItemActivity.this, "Failed to load post.", Toast.LENGTH_SHORT).show();
                }
            };
            //firebase referenz auf den User, der im listitem steht.
            ownerref.addListenerForSingleValueEvent(ownerListener);

    }





    private void addBottomDots(int currentPage) {
        dots = new TextView[layouts.length];

        int[] colorsActive = getResources().getIntArray(R.array.array_detail_dot_active);
        int[] colorsInactive = getResources().getIntArray(R.array.array_detail_dot_inactive);

        dotsLayout.removeAllViews();
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("&#8226;"));
            dots[i].setTextSize(35);
            dots[i].setTextColor(colorsInactive[currentPage]);
            dotsLayout.addView(dots[i]);
        }

        if (dots.length > 0)
            dots[currentPage].setTextColor(colorsActive[currentPage]);
    }

    private int getItem(int i) {
        return viewPager.getCurrentItem() + i;
    }

    ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {
            addBottomDots(position);
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }

    };

    /**
     * View pager adapter
     */
    public class DetailViewPagerAdapter extends PagerAdapter {
        private LayoutInflater layoutInflater;

        public DetailViewPagerAdapter() {
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View view = layoutInflater.inflate(layouts[position], container, false);
            //Elemente müssen mit zugehöriger View angesprochen werden.
            if (position == 0) {
                buildRequestPage(view);
            } else if (position == 1) {
                buildChatPage(view);
            }
            container.addView(view);

            return view;
        }

        @Override
        public int getCount() {
            return layouts.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view == obj;
        }


        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View view = (View) object;
            container.removeView(view);
        }
    }

}
