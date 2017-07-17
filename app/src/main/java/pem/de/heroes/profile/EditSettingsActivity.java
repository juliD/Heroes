package pem.de.heroes.profile;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import pem.de.heroes.model.User;
import pem.de.heroes.shared.Helper;
import pem.de.heroes.R;

public class EditSettingsActivity extends AppCompatActivity {

    private DatabaseReference userRef;
    private SharedPreferences sharedPref;

    private EditText usernameEdit;
    private EditText streetEdit;
    private EditText cityEdit;
    private TextView radiusText;
    private SeekBar radiusBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_settings);
        setTitle(getString(R.string.edit_profile));

        usernameEdit = (EditText) findViewById(R.id.username);
        streetEdit = (EditText) findViewById(R.id.street);
        cityEdit = (EditText) findViewById(R.id.city);
        radiusText = (TextView) findViewById(R.id.radiusText);
        radiusBar = (SeekBar) findViewById(R.id.radius);

        sharedPref = this.getSharedPreferences("pem.de.hero.userid", Context.MODE_PRIVATE);

        String userId = sharedPref.getString("userid", "");
        userRef = FirebaseDatabase.getInstance().getReference().child("users").child(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User me = dataSnapshot.getValue(User.class);

                String username = me.getUsername();
                String street = me.getStreet();
                String city = me.getCity();
                int radius = me.getRadius();

                usernameEdit.setText(username);
                streetEdit.setText(street);
                cityEdit.setText(city);
                radiusText.setText(radius + " Meter");
                radiusBar.setProgress(getProgress(radius));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_edit_settings, menu);

        MenuItem save = menu.findItem(R.id.save);
        save.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                final String username = usernameEdit.getText().toString();
                final String city = cityEdit.getText().toString();
                final String street = streetEdit.getText().toString();
                final int radius = getRadius(radiusBar.getProgress());
                    if (username.isEmpty() || city.isEmpty() || street.isEmpty()) {
                    Toast.makeText(getApplicationContext(), R.string.fill_in, Toast.LENGTH_SHORT).show();
                    return true;
                }

                LatLng newLoc = Helper.getLocationFromAddress(street + ", " + city, getApplicationContext());
                    if (newLoc == null) {
                    Toast.makeText(getApplicationContext(), R.string.address_error, Toast.LENGTH_SHORT).show();
                    return true;
                }
                final double homelat = newLoc.latitude;
                final double homelong = newLoc.longitude;

                userRef.runTransaction(new Transaction.Handler() {
                    @Override
                    public Transaction.Result doTransaction(MutableData mutableData) {
                        User me = mutableData.getValue(User.class);
                        me.setUsername(username);
                        me.setCity(city);
                        me.setStreet(street);
                        me.setRadius(radius);
                        mutableData.setValue(me);
                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putInt("radius", radius);
                        Helper.putDouble(editor, "homelat", homelat);
                        Helper.putDouble(editor, "homelong", homelong);
                        editor.apply();
                    }
                });

                finish();
                return true;
            }
        });

        return true;
    }

    private int getRadius(int progress) {
        return progress * 100 + 100; // minimum = 100m, step = 100m
    }

    private int getProgress(int radius) {
        return (radius - 100) / 100;
    }
}
