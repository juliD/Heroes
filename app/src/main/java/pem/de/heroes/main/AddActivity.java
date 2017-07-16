package pem.de.heroes.main;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pem.de.heroes.shared.CounterTransactionHandler;
import pem.de.heroes.shared.Helper;
import pem.de.heroes.R;
import pem.de.heroes.model.ListItem;

public class AddActivity extends AppCompatActivity {

    private static final String TAG = "AddActivity";
    private static final String ARG_TYPE = "fragment_type";
    private String type = "offer";
    private DatabaseReference ref;
    private String street;
    private String city;
    private String token;
    private String userid;
    private String[] SUGGESTIONS;
    private String categorie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);
        SUGGESTIONS = getResources().getStringArray(R.array.suggestions);

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                type = extras.getString(ARG_TYPE);
                setTitle(type.equals("ask") ? "Anfrage erstellen" : "Angebot aufgeben");
            }
        }

        ref = FirebaseDatabase.getInstance().getReference();
        SharedPreferences sharedPref = this.getSharedPreferences("pem.de.hero.userid", Context.MODE_PRIVATE);
        userid = sharedPref.getString("userid", "No UserID");
        street = sharedPref.getString("street", "");
        city = sharedPref.getString("city", "");
        token = sharedPref.getString("pushToken", "");
        final double latitude = Helper.getDouble(sharedPref, "homelat", 0);
        final double longitude = Helper.getDouble(sharedPref, "homelong", 0);

        final TextView streetView = (TextView) findViewById(R.id.street);
        final TextView cityView = (TextView) findViewById(R.id.city);
        final EditText titleView = (EditText) findViewById(R.id.add_title);
        final EditText descView = (EditText) findViewById(R.id.add_description);
        final Spinner spinner = (Spinner) findViewById(R.id.category);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                categorie = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //When someone taps the spinner the keyboard will be closed
        spinner.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                InputMethodManager in = (InputMethodManager) v.getContext()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                in.hideSoftInputFromWindow(v.getApplicationWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
                return false;
            }
        }) ;

        List<String> categories = new ArrayList<String>(Arrays.asList(Arrays.copyOfRange(SUGGESTIONS,2,SUGGESTIONS.length)));
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,categories);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);

        streetView.setText(street);
        cityView.setText(city);

        Button create = (Button) findViewById(R.id.create);
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = titleView.getText().toString();
                String description = descView.getText().toString();
                String address = street + ", " + city;
                if(categorie==null){
                    categorie="Sonstige";
                }

                if (title.isEmpty() || description.isEmpty() || street.isEmpty() || city.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Fülle bitte alle Felder aus ...", Toast.LENGTH_SHORT).show();
                    return;
                }

                Log.d("AddActivity", "type: "+ type);
                DatabaseReference typeref = ref.child(type);

                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
                String currentDateAndTime = sdf.format(Calendar.getInstance().getTime());
                ListItem listItem = new ListItem(title, description, address, userid, "", currentDateAndTime,categorie);
                String key = typeref.push().getKey();
                Log.d("AddActivity", "added key: " + key);
                Map<String, Object> post = listItem.toMap();
                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put("/"+key,post);
                typeref.updateChildren(childUpdates);


                // Increment value for statistics
                DatabaseReference countCreated = ref.child("users").child(userid).child(type + "sCreated");
                countCreated.runTransaction(new CounterTransactionHandler(+1));

                // Add token for push notifications
                typeref.child(key).child("follower").child("owner").setValue(token);

                // Add the menssages directory
                Map<String, Object> messagesdir = new HashMap<String, Object>();
                messagesdir.put("messages", "");
                typeref.child(key).updateChildren(messagesdir);

                // Add to GeoFire
                GeoFire geoFire = new GeoFire(FirebaseDatabase.getInstance().getReference("geofire"));
                geoFire.setLocation("/"+type+"/"+key, new GeoLocation(latitude,longitude));

                finish();
            }
        });
    }
}
