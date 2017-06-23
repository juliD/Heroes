package pem.de.heroes;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import com.google.firebase.database.ValueEventListener;

public class DetailItemActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String ARG_TYPE = "fragment_type";
    private static final String ITEM_ID = "item_id";
    private DatabaseReference userref;
    String marker;
    ListItem listitem;
    LatLng home;
    private DatabaseReference ref;
    private String type = "offer";
    private String itemID;
    private String preferenceUserID;
    private String listUserID;
    TextView agent_textview;
    private ValueEventListener agentListener;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_item);

        ref = FirebaseDatabase.getInstance().getReference();
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                type = extras.getString(ARG_TYPE);
                itemID = extras.getString(ITEM_ID);
            }
        }
        final DatabaseReference typeref = ref.child(type);

        final SharedPreferences sharedPref = this.getSharedPreferences("pem.de.hero.userid", Context.MODE_PRIVATE);
        preferenceUserID = sharedPref.getString("userid", "No UserID");


        listitem = getIntent().getParcelableExtra("selected");
        if (listitem != null) {
            TextView title = (TextView) findViewById(R.id.detail_title);
            TextView info = (TextView) findViewById(R.id.detail_description);
            TextView address = (TextView) findViewById(R.id.show_address);
            agent_textview = (TextView) findViewById(R.id.agent);
            listUserID = listitem.getUserID();

            title.setText(listitem.getTitle());
            info.setText(listitem.getDescription());
            address.setText(listitem.getAddress());

            if(listUserID.equals(preferenceUserID)){
                if (listitem.getAgent().equals("")){
                    agent_textview.setText("Leider hat noch niemand deine Anfrage angenommen.");
                }else{
                    userref = ref.child("users").child(listitem.getAgent());
                    setAgent();
                }
            }else{
                if(listitem.getAgent().equals("")){
                    agent_textview.setText("Noch niemand hat den Vorgang angenommen. Schanpp' ihn dir!");
                }
                else if(listitem.getAgent().equals(preferenceUserID)){
                    agent_textview.setText("Du hast diese Anfrage angenommen!");
                }else{
                    agent_textview.setText("Jemand anderes hat die Anfrage leider vor dir angenommen.");
                }
            }
            marker = listitem.getTitle();
        }


        home = new LatLng(Helper.getDouble(sharedPref, "homelat", 0), Helper.getDouble(sharedPref, "homelong", 0));
        Log.d("DetailItem", sharedPref.getString("home", "Oettingenstraße 67, München"));
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.maps);
        mapFragment.getMapAsync(this);


        Button accept = (Button) findViewById(R.id.accept);
        if (preferenceUserID.equals(listUserID) && listUserID.equals("")) {
            accept.setBackgroundColor(ContextCompat.getColor(this, R.color.gray));
        }

        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (preferenceUserID.equals(listUserID)) {
                    Toast.makeText(DetailItemActivity.this, "Du kannst nicht deinen eigene Anfrage annehmen", Toast.LENGTH_SHORT).show();
                } else if (listUserID.equals("")) {
                    Toast.makeText(DetailItemActivity.this, "Diese Anfrage wurde schon von jemandem angenommen", Toast.LENGTH_SHORT).show();
                } else {
                    typeref.child(itemID).child("agent").setValue(preferenceUserID);
                }

            }
        });
    }

    public void setAgent() {
        if (!listitem.getAgent().equals("")) {
            agentListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    //get database value
                    Agent agent = dataSnapshot.getValue(Agent.class);
                    agent_textview.setText(agent.getUsername() + " hat deine Anfrage angenommen.");
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Getting agent failed, create log
                    Log.w("on cancelled", "loadPost:onCancelled", databaseError.toException());
                    Toast.makeText(DetailItemActivity.this, "Failed to load post.", Toast.LENGTH_SHORT).show();
                }
            };
            userref.addListenerForSingleValueEvent(agentListener);
        }
    }


    @Override
    public void onMapReady(GoogleMap map) {
        int dimen = 50;
        BitmapDrawable bitmapdraw = (BitmapDrawable) ResourcesCompat.getDrawable(getResources(), R.drawable.ic_home_black_24dp, null);
        Bitmap b = bitmapdraw.getBitmap();
        Bitmap smallMarker = Bitmap.createScaledBitmap(b, dimen, dimen, false);
        LatLng pos = Helper.getLocationFromAddress(listitem.getAddress(), this);
        map.addMarker(new MarkerOptions().position(pos).title(marker).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        map.addMarker(new MarkerOptions().position(home).title("Home").icon(BitmapDescriptorFactory.fromBitmap(smallMarker)));
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(pos).zoom(15f).build();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }


}
