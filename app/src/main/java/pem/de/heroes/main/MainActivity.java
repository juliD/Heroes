package pem.de.heroes.main;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;

import pem.de.heroes.shared.Helper;
import pem.de.heroes.R;
import pem.de.heroes.model.User;
import pem.de.heroes.profile.EditSettingsActivity;

public class MainActivity extends AppCompatActivity implements TabLayout.OnTabSelectedListener,SharedPreferences.OnSharedPreferenceChangeListener{

    private static final String TAG = "MainActivity";
    private ViewPager viewPager;
    private static final String ARG_TYPE = "fragment_type";
    FloatingActionButton fab;
    private FirebaseAuth auth;
    private String userid;
    SharedPreferences sharedPref;
    MenuItem search;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        int selectTab = 1;

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                selectTab = extras.getInt("tab");
            }
        }

        //set up shared preferences
        sharedPref=this.getSharedPreferences("pem.de.hero.userid", Context.MODE_PRIVATE);
        sharedPref.registerOnSharedPreferenceChangeListener(this);


        //Check if user is already logged in
        auth = FirebaseAuth.getInstance();

        Log.d("Main","Firebase: "+auth);
        if (auth.getCurrentUser() != null) {
            // already signed in
            userid = auth.getCurrentUser().getUid();
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("userid", userid);
            editor.apply();

            //needed for custom language for notifications when app not running
            //needs to be checked every time in case user changes language

            Locale locale;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                locale = getApplicationContext().getResources().getConfiguration().getLocales().get(0);
            } else {
                locale = getApplicationContext().getResources().getConfiguration().locale;
            }

            DatabaseReference users = FirebaseDatabase.getInstance().getReference("users").child(userid);
            users.child("locale").setValue(locale.getDisplayLanguage());
        } else {
            //sign in for the first time
            signInAnonymoulsy();
        }



        getKarma();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //set up tabs
        viewPager = (ViewPager) findViewById(R.id.pager);
        TabsPagerAdapter tabsAdapter = new TabsPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(tabsAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tablayout);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.getTabAt(0).setText(getResources().getString(R.string.action_myprofile));
        tabLayout.getTabAt(1).setText(getResources().getString(R.string.action_askforhelp));
        tabLayout.getTabAt(2).setText(getResources().getString(R.string.action_offerhelp));

        tabLayout.addOnTabSelectedListener(this);


            //add Button
            fab = (FloatingActionButton) findViewById(R.id.fab);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(MainActivity.this, AddActivity.class);
                    i.putExtra(ARG_TYPE, viewPager.getCurrentItem() == 1 ? "ask" : "offer");
                    startActivity(i);
                }
            });

        if(selectTab==0) {
            fab.setVisibility(View.GONE);
        }
        //Select the tab in the middle
        TabLayout.Tab tab = tabLayout.getTabAt(selectTab);
        tab.select();

    }

    /*
    signing in once
     */
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

                            //to refresh listeners
                            startActivity(new Intent(MainActivity.this, MainActivity.class));
                            finish();

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInAnonymously:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }

                    }
                });
    }

    /*
    add the User to the Firebase Database once
    and set shared preferences
     */
    private void addUserToDatabase(FirebaseUser user) {
        userid = user.getUid();
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("userid", userid);
        editor.putInt("radius", 500);
        LatLng home = Helper.getLocationFromAddress(sharedPref.getString("street","Marienplatz 1") +", " + sharedPref.getString("city","München"),this);

        editor.putLong("homelat",Double.doubleToRawLongBits(home.latitude));
        Helper.putDouble(editor,"homelat",home.latitude);
        Helper.putDouble(editor,"homelong",home.longitude);
        editor.apply();

        DatabaseReference users = FirebaseDatabase.getInstance().getReference("users").child(userid);
        users.child("username").setValue(sharedPref.getString("username","Anonym"));
        users.child("karma").setValue(0);
        users.child("asksCreated").setValue(0);
        users.child("asksDone").setValue(0);
        users.child("offersCreated").setValue(0);
        users.child("offersDone").setValue(0);
        users.child("city").setValue(sharedPref.getString("city","München"));
        users.child("street").setValue(sharedPref.getString("street","Marienplatz 1"));
        users.child("radius").setValue(500);


        //needed for custom language for notifications when app not running
        Locale locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = getApplicationContext().getResources().getConfiguration().getLocales().get(0);
        } else {
            locale = getApplicationContext().getResources().getConfiguration().locale;
        }

        users.child("locale").setValue(locale.getDisplayLanguage());
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        search = menu.findItem(R.id.search);
        return true;
    }

    /*
    set Karma Event Listener
     */
    public void getKarma() {
        // firebase reference on the user
        DatabaseReference userref = FirebaseDatabase.getInstance().getReference("users/"+sharedPref.getString("userid","No User ID"));

        userref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // get database value
                User me = dataSnapshot.getValue(User.class);
                if (me != null){
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putInt("karma", me.getKarma());
                    editor.commit();
                    Log.e("Main", "me exists ...");
                    TextView karma = (TextView)findViewById(R.id.karma);
                    if(karma!=null) {
                        karma.setText(me.getKarma() + " Karma");
                    }
                } else {
                    Log.e("Main", "Something went wrong with my karma");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting user failed, create log
                Log.w("on cancelled", "Database: ", databaseError.toException());
            }
        });
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        Log.d(TAG,"search is null!!");
        if(search!=null) {
            Log.d(TAG,"search not null!!");
            if (search.isActionViewExpanded()) {
                Log.d(TAG,"Expanded!!");
                MenuItemCompat.collapseActionView(search);
            }
        }
        viewPager.setCurrentItem(tab.getPosition());
        Toolbar t = (Toolbar) findViewById(R.id.toolbar);


        AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) t.getLayoutParams();
        params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS);


        if(tab.getPosition()==0){
            fab.setVisibility(View.GONE);

            TextView karma = (TextView) findViewById(R.id.karma);

            karma.setVisibility(View.GONE);

            //disable scrolling on the profile page
            params.setScrollFlags(0);

        }
        else{
            fab.setVisibility(View.VISIBLE);
            TextView karma = (TextView) findViewById(R.id.karma);
            sharedPref = this.getSharedPreferences("pem.de.hero.userid", Context.MODE_PRIVATE);
            karma.setText(sharedPref.getInt("karma",0)+" Karma");
            karma.setVisibility(View.VISIBLE);

        }


    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(viewPager.getCurrentItem()!=0){
            TextView karma = (TextView) findViewById(R.id.karma);
            karma.setText(sharedPreferences.getInt("karma",0)+" Karma");
        }
    }





}
