package pem.de.heroes;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import static android.support.v4.view.ViewPager.SCROLL_STATE_DRAGGING;

public class WelcomeActivity extends AppCompatActivity {

    private CustomViewPager viewPager;
    private MyViewPagerAdapter myViewPagerAdapter;
    private LinearLayout dotsLayout;
    private TextView[] dots;
    private int[] layouts;
    private Button btnNext;
    private SharedPreferences sharedPref;
    private String username;
    private boolean statusaddress = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPref = this.getSharedPreferences("pem.de.hero.userid", Context.MODE_PRIVATE);

        //Um das Tutorial anzuzeigen Kommentierung entfernen
        //sharedPref.edit().clear().commit();

        if (sharedPref.contains("username")) {
            //username has already been written into shared preferences => not the first time using the app
            launchHomeScreen();
            finish();
        }


        // Making notification bar transparent
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        setContentView(R.layout.activity_welcome);

        viewPager = (CustomViewPager) findViewById(R.id.view_pager);
        dotsLayout = (LinearLayout) findViewById(R.id.layoutDots);
        btnNext = (Button) findViewById(R.id.btn_next);


        // layouts of all welcome sliders
        layouts = new int[]{
                R.layout.welcome_slide1,
                R.layout.welcome_slide2,
                R.layout.welcome_slide3,
                R.layout.welcome_slide4};

        // adding bottom dots
        addBottomDots(0);

        // making notification bar transparent
        changeStatusBarColor();

        myViewPagerAdapter = new MyViewPagerAdapter();
        viewPager.setAdapter(myViewPagerAdapter);
        viewPager.addOnPageChangeListener(viewPagerPageChangeListener);


        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // checking for last page
                // if last page home screen will be launched
                int current = getItem(+1);
                // move to next screen
                switch (current) {
                    case 1:
                    case 2:
                        viewPager.setCurrentItem(current);
                        break;
                    case 3:
                        if (writeAddress()) {
                            viewPager.setCurrentItem(current);
                            statusaddress = true;
                        }
                        break;
                    case 4:
                        if (writeUsername() && statusaddress) {
                            launchHomeScreen();
                        }
                        break;
                }
            }
        });
    }

    private void addBottomDots(int currentPage) {
        dots = new TextView[layouts.length];

        int[] colorsActive = getResources().getIntArray(R.array.array_dot_active);
        int[] colorsInactive = getResources().getIntArray(R.array.array_dot_inactive);

        dotsLayout.removeAllViews();
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("&#8226;"));
            dots[i].setTextSize(35);
            dots[i].setTextColor(colorsInactive[currentPage]);
            dotsLayout.addView(dots[i]);
        }

        if (dots.length > 0)
            dots[currentPage].setTextColor(colorsActive[currentPage]);
    }

    private int getItem(int i) {
        return viewPager.getCurrentItem() + i;
    }

    private void launchHomeScreen() {
        startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
        finish();
    }

    private boolean writeAddress() {
        EditText stadt_edit = (EditText) findViewById(R.id.city);
        String city = stadt_edit.getText().toString();
        EditText street_edit = (EditText) findViewById(R.id.street);
        String street = street_edit.getText().toString();

        if (city.equals("") || street.equals("")) {
            Toast.makeText(this, "Bitte gib deine Stadt und Straße ein", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("city", city);
            editor.putString("street", street);
            editor.apply();
            viewPager.setPagingEnabled(true);
            return true;
        }
    }

    private boolean writeUsername() {
        EditText editText = (EditText) findViewById(R.id.username);
        String username = editText.getText().toString();
        if (username.equals("")) {
            Toast.makeText(this, "Bitte gib deinen Namen ein", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("username", username);
            editor.putInt("karma", 0);
            editor.apply();
            return true;
        }
    }

    //  viewpager change listener
    ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {
            addBottomDots(position);

            // changing the next button text "weiter" or "Start"
            if (position == layouts.length - 1) {
                // last page. make button text to Start
                btnNext.setText("Start");
            }
            if (position == 2 && !statusaddress) {
                viewPager.setPagingEnabled(false);
            } else {
                // Pages are still left to be seen
                btnNext.setText("Weiter");
            }

        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }

    };

    /**
     * Making notification bar transparent
     */
    private void changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }

    /**
     * View pager adapter
     */
    public class MyViewPagerAdapter extends PagerAdapter {
        private LayoutInflater layoutInflater;

        public MyViewPagerAdapter() {
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View view = layoutInflater.inflate(layouts[position], container, false);
            container.addView(view);

            return view;
        }

        @Override
        public int getCount() {
            return layouts.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view == obj;
        }


        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View view = (View) object;
            container.removeView(view);
        }
    }


}