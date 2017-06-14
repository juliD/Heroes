package pem.de.heroes;

import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

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

public class DetailItemActivity extends AppCompatActivity implements OnMapReadyCallback{

    String marker;
    ListItem listitem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_item);

        listitem = getIntent().getParcelableExtra("selected");
        if(listitem!=null){
            TextView title = (TextView)findViewById(R.id.detail_title);
            TextView info = (TextView)findViewById(R.id.detail_description);

            title.setText(listitem.getTitle());
            info.setText(listitem.getDescription());
            marker=listitem.getTitle();
        }

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.maps);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        LatLng pos = Helper.getLocationFromAddress(listitem.getAddress(),this);
        map.addMarker(new MarkerOptions().position(pos).title(marker).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(pos).zoom(15f).build();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }
}
