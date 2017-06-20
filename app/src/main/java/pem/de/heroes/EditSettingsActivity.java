package pem.de.heroes;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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

        final TextView usernameView = (TextView) findViewById(R.id.username);
        final TextView streetView = (TextView) findViewById(R.id.street);
        final TextView cityView = (TextView) findViewById(R.id.city);
        usernameView.setText(username);
        streetView.setText(street);
        cityView.setText(city);

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
                String username = usernameView.getText().toString();
                String city = cityView.getText().toString();
                String street = streetView.getText().toString();

                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("username", username);
                editor.putString("city", city);
                editor.putString("street", street);
                editor.apply();
                finish();
            }
        });
    }
}
