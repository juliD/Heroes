package pem.de.heroes;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DetailItemActivity extends AppCompatActivity implements OnMapReadyCallback{

    private static final String ARG_TYPE = "fragment_type";
    private static final String ITEM_ID = "item_id";
    String marker;
    ListItem listitem;
    LatLng home;
    DatabaseReference ref;
    private String type ="offer";
    private String itemID;
    private String preferenceUserID;
    private String listUserID;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_item);

        listitem = getIntent().getParcelableExtra("selected");
        if(listitem!=null){
            TextView title = (TextView)findViewById(R.id.detail_title);
            TextView info = (TextView)findViewById(R.id.detail_description);
            TextView address = (TextView)findViewById(R.id.show_address);
            TextView agent = (TextView)findViewById(R.id.agent);

            title.setText(listitem.getTitle());
            info.setText(listitem.getDescription());
            address.setText(listitem.getAddress());
            agent.setText(listitem.getAgent());
            Log.d("listitem", "onCreate: " + listitem.getUserID());
            listUserID = listitem.getUserID();
            marker=listitem.getTitle();


        }
        final SharedPreferences sharedPref = this.getSharedPreferences("pem.de.hero.userid", Context.MODE_PRIVATE);
        preferenceUserID = sharedPref.getString("userid","No UserID");

        home = new LatLng(Helper.getDouble(sharedPref,"homelat",0),Helper.getDouble(sharedPref,"homelong",0));
        Log.d("DetailItem",sharedPref.getString("home","Oettingenstraße 67, München"));
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.maps);
        mapFragment.getMapAsync(this);

        ref = FirebaseDatabase.getInstance().getReference();
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras != null) {
                type = extras.getString(ARG_TYPE);
                itemID = extras.getString(ITEM_ID);
            }
        }
        final DatabaseReference typeref = ref.child(type);

        Button accept = (Button) findViewById(R.id.accept);
        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(preferenceUserID.equals(listUserID)){
                   Toast.makeText(DetailItemActivity.this, "Du kannst nicht deinen eigenen Auftrag annehmen", Toast.LENGTH_SHORT).show();
                }else if(listUserID!=null){
                    Toast.makeText(DetailItemActivity.this, "Dieser Auftrag wurde schon von jemandem angenommen", Toast.LENGTH_SHORT).show();
                }else{
                    typeref.child(itemID).child("agent").setValue(preferenceUserID);
                }

            }
        });
    }

    @Override
    public void onMapReady(GoogleMap map) {
        int dimen = 50;
        BitmapDrawable bitmapdraw=(BitmapDrawable) ResourcesCompat.getDrawable(getResources(),R.drawable.ic_home_black_24dp, null);
        Bitmap b=bitmapdraw.getBitmap();
        Bitmap smallMarker = Bitmap.createScaledBitmap(b, dimen, dimen, false);
        LatLng pos = Helper.getLocationFromAddress(listitem.getAddress(),this);
        map.addMarker(new MarkerOptions().position(pos).title(marker).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        map.addMarker(new MarkerOptions().position(home).title("Home").icon(BitmapDescriptorFactory.fromBitmap(smallMarker)));
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(pos).zoom(15f).build();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }


}
