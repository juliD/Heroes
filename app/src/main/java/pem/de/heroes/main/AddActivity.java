package pem.de.heroes.main;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import pem.de.heroes.shared.Helper;
import pem.de.heroes.R;
import pem.de.heroes.model.ListItem;

public class AddActivity extends AppCompatActivity {

    private static final String ARG_TYPE = "fragment_type";
    private String type ="offer";
    DatabaseReference ref;
    private String street;
    private String city;
    private String agent;
    private String token;

    String userid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);
        setTitle("Anfrage auf");
        ref = FirebaseDatabase.getInstance().getReference();
        SharedPreferences sharedPref = this.getSharedPreferences("pem.de.hero.userid",Context.MODE_PRIVATE);
        userid = sharedPref.getString("userid","No UserID");
        street = sharedPref.getString("street","");
        city = sharedPref.getString("city","");
        token = sharedPref.getString("pushToken","");
        final double latitude = Helper.getDouble(sharedPref,"homelat",0);
        final double longitude = Helper.getDouble(sharedPref,"homelong",0);

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras != null) {
                type= extras.getString(ARG_TYPE);
                if (type.equals("ask")) {
                    setTitle("Anfrage erstellen");
                }else{
                    setTitle("Angebot aufgeben");
                }

            }
        }
        TextView addrView = (TextView)findViewById(R.id.address);
        TextView cityView = (TextView) findViewById(R.id.city);
        addrView.setText(street);
        cityView.setText(city);

        Button create = (Button) findViewById(R.id.create);
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText titleView = (EditText) findViewById(R.id.add_title);
                EditText descView = (EditText) findViewById(R.id.add_description);
                TextView addrView = (TextView)findViewById(R.id.address);

                if(titleView.getText().equals("")&&descView.getText().equals("")&&addrView.getText().equals("")){
                    return;
                }
                Log.d("AddActivity","type: "+ type);
                DatabaseReference typeref = ref.child(type);

                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
                String currentDateandTime = sdf.format(Calendar.getInstance().getTime());
                ListItem listItem = new ListItem(titleView.getText().toString(), descView.getText().toString(),street+", "+city,userid,"",currentDateandTime);
                String key = typeref.push().getKey();
                Log.d("AddActivity","added key: "+ key);
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


                GeoFire geoFire = new GeoFire(FirebaseDatabase.getInstance().getReference("geofire"));
                geoFire.setLocation("/"+type+"/"+key, new GeoLocation(latitude,longitude));


                finish();
            }
        });

    }

}