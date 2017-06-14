package pem.de.heroes;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

public class AddActivity extends AppCompatActivity {

    private static final String ARG_TYPE = "activity_type";
    private String type ="offer";
    DatabaseReference ref;

    String userid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);
        ref = FirebaseDatabase.getInstance().getReference();
        SharedPreferences sharedPref = this.getSharedPreferences("pem.de.hero.userid",Context.MODE_PRIVATE);
        userid = sharedPref.getString("userid","No UserID");

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras != null) {
                type= extras.getString(ARG_TYPE);
            }
        }

        Button create = (Button) findViewById(R.id.create);
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText titleView = (EditText) findViewById(R.id.add_title);
                EditText descView = (EditText) findViewById(R.id.add_description);
                TextView addrView = (TextView)findViewById(R.id.address);
                TextView cityView = (TextView) findViewById(R.id.city);
                if(titleView.getText().equals("")&&descView.getText().equals("")&&addrView.getText().equals("")){
                    return;
                }
                DatabaseReference typeref = ref.child(type);

                String child = type+System.currentTimeMillis();
                typeref.child(child).child("title").setValue(titleView.getText().toString());
                typeref.child(child).child("description").setValue(descView.getText().toString());
                typeref.child(child).child("address").setValue(addrView.getText().toString()+", "+cityView.getText().toString());
                typeref.child(child).child("userid").setValue(userid);
                finish();
            }
        });

    }

}
