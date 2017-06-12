package pem.de.heroes;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity implements TabLayout.OnTabSelectedListener {

    private static final String TAG = "MainActivity";
    private ViewPager viewPager;
    private static final String ARG_TYPE = "activity_type";
    FloatingActionButton fab;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //Check if user is already logged in
        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            // already signed in

        } else {
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

                if(viewPager.getCurrentItem()==0){
                    i.putExtra(ARG_TYPE,"ask");
                }

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

    public void update(FirebaseUser currentUser){


    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = auth.getCurrentUser();
        update(currentUser);
    }


    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        viewPager.setCurrentItem(tab.getPosition());
        if(tab.getPosition()==0){
            fab.setVisibility(View.GONE);

        }
        else{
            fab.setVisibility(View.VISIBLE);
        }


    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }
}
