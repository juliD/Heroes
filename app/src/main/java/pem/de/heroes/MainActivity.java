package pem.de.heroes;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity implements TabLayout.OnTabSelectedListener {

    private static final String TAG = "MainActivity";
    private ViewPager viewPager;
    private static final String ARG_TYPE = "activity_type";
    FloatingActionButton fab;
    private FirebaseAuth auth;
    private String userid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        Log.d("Main","onCreate");
        //Check if user is already logged in
        auth = FirebaseAuth.getInstance();
        Log.d("Main","Firebase: "+auth);
        if (auth.getCurrentUser() != null) {
            // already signed in
            update(auth.getCurrentUser());
        } else {
            Log.d("Main","Firebase: signing in now");
            signInAnonymoulsy();
        }

        viewPager = (ViewPager) findViewById(R.id.pager);
        TabsPagerAdapter tabsAdapter = new TabsPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(tabsAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tablayout);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.getTabAt(0).setText(getResources().getString(R.string.action_myprofile));
        tabLayout.getTabAt(1).setText(getResources().getString(R.string.action_askforhelp));
        tabLayout.getTabAt(2).setText(getResources().getString(R.string.action_offerhelp));


        tabLayout.addOnTabSelectedListener(this);



        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, AddActivity.class);

                if(viewPager.getCurrentItem()==1){
                    i.putExtra(ARG_TYPE,"ask");
                }

                startActivity(i);
            }
        });

        ImageButton edit =(ImageButton)findViewById(R.id.edit);
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, EditSettingsActivity.class);
                startActivity(i);
            }
        });


        //Select the tab in the middle
        TabLayout.Tab tab = tabLayout.getTabAt(1);
        tab.select();

    }
    public void signInAnonymoulsy(){
        auth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInAnonymously:success");
                            FirebaseUser user = auth.getCurrentUser();
                            addUserToDatabase(user);
                            update(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInAnonymously:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            update(null);
                        }

                    }
                });
    }

    private void addUserToDatabase(FirebaseUser user) {

        SharedPreferences sharedPref = this.getSharedPreferences("pem.de.hero.userid",Context.MODE_PRIVATE);
        DatabaseReference users = FirebaseDatabase.getInstance().getReference("users");
        users.child(user.getUid()).child("username").setValue(sharedPref.getString("username","Anonym"));
        users.child(user.getUid()).child("karma").setValue(0);


    }

    public void update(FirebaseUser currentUser){

        userid = currentUser.getUid();
        SharedPreferences sharedPref = this.getSharedPreferences("pem.de.hero.userid",Context.MODE_PRIVATE);

        Log.d("MainActivity","authorization: "+ userid);


        if(!sharedPref.contains("userid")){
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("userid", userid);
            LatLng home = Helper.getLocationFromAddress(sharedPref.getString("street","Marienplatz 1") +", " + sharedPref.getString("city","München"),this);

            editor.putLong("homelat",Double.doubleToRawLongBits(home.latitude));
            Helper.putDouble(editor,"homelat",home.latitude);
            Helper.putDouble(editor,"homelong",home.longitude);
            editor.apply();


        }







    }



    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        viewPager.setCurrentItem(tab.getPosition());
        if(tab.getPosition()==0){
            fab.setVisibility(View.GONE);

            TextView karma = (TextView) findViewById(R.id.karma);

            karma.setVisibility(View.GONE);

            ImageButton edit =(ImageButton)findViewById(R.id.edit);
            edit.setVisibility(View.VISIBLE);
        }
        else{
            fab.setVisibility(View.VISIBLE);
            TextView karma = (TextView) findViewById(R.id.karma);
            SharedPreferences sharedPref = this.getSharedPreferences("pem.de.hero.userid", Context.MODE_PRIVATE);
            karma.setText(sharedPref.getInt("karma",0)+" Karma");
            karma.setVisibility(View.VISIBLE);

            ImageButton edit =(ImageButton)findViewById(R.id.edit);
            edit.setVisibility(View.GONE);
        }


    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }
}
