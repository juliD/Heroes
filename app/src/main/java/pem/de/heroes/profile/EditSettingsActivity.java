package pem.de.heroes.profile;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import pem.de.heroes.shared.Helper;
import pem.de.heroes.R;

public class EditSettingsActivity extends AppCompatActivity {

    private DatabaseReference ref;
    private String username;
    private String street;
    private String city;
    private int radius;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_settings);

        ref = FirebaseDatabase.getInstance().getReference();
        final SharedPreferences sharedPref = this.getSharedPreferences("pem.de.hero.userid", Context.MODE_PRIVATE);
        username = sharedPref.getString("username", "");
        street = sharedPref.getString("street", "");
        city = sharedPref.getString("city", "");
        radius = sharedPref.getInt("radius", 500);

        final EditText usernameEdit = (EditText) findViewById(R.id.username);
        final EditText streetEdit = (EditText) findViewById(R.id.street);
        final EditText cityEdit = (EditText) findViewById(R.id.city);
        final TextView radiusText = (TextView) findViewById(R.id.textView4);
        usernameEdit.setText(username);
        streetEdit.setText(street);
        cityEdit.setText(city);
        radiusText.setText(radius + " Meter");

        final SeekBar radiusBar = (SeekBar) findViewById(R.id.radius);
        radiusBar.setProgress(getProgress(radius));
        radiusBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                radiusText.setText(getRadius(progress) + " Meter");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // do nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // do nothing
            }
        });

        ImageButton cancelButton = (ImageButton) findViewById(R.id.cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ImageButton saveButton = (ImageButton) findViewById(R.id.save);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEdit.getText().toString();
                String city = cityEdit.getText().toString();
                String street = streetEdit.getText().toString();
                int radius = getRadius(radiusBar.getProgress());
                LatLng newLoc = Helper.getLocationFromAddress(street+", "+city,getApplicationContext());

                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("username", username);
                editor.putString("city", city);
                editor.putString("street", street);
                editor.putInt("radius", radius);
                Helper.putDouble(editor,"homelat",newLoc.latitude);
                Helper.putDouble(editor,"homelong",newLoc.longitude);
                editor.apply();
                finish();
            }
        });
    }

    private int getRadius(int progress) {
        return progress * 100 + 100; // minimum = 100m, step = 100m
    }

    private int getProgress(int radius) {
        return (radius - 100) / 100;
    }
}
