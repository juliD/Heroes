package pem.de.heroes.detail;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import pem.de.heroes.shared.CounterTransactionHandler;
import pem.de.heroes.shared.Helper;
import pem.de.heroes.R;
import pem.de.heroes.shared.CustomViewPager;
import pem.de.heroes.model.ListItem;
import pem.de.heroes.model.User;


public class DetailItemActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String ARG_TYPE = "fragment_type";
    private static final String ITEM_ID = "item_id";

    private String type = "offer";
    private ListItem listitem;
    private String itemID;

    private DatabaseReference ref;
    private DatabaseReference typeref;
    private DatabaseReference userref;
    private DatabaseReference ownerref;
    private DatabaseReference agentref;
    private DatabaseReference messagesref;
    private SharedPreferences sharedPref;

    private String preferenceUserID;
    private boolean mine;
    private boolean accepted;
    private boolean acceptedByMe;
    private String token;

    private LatLng home;
    private LatLng othersLocation;
    private String othersAddress;

    private TextView title;
    private TextView description;
    private TextView date;
    private TextView category;
    private TextView agent;
    private TextView user;
    private TextView showDetails;
    private GridLayout details;
    private Button accept;
    private Button reset;

    private CustomViewPager viewPager;
    private LinearLayout dotsLayout;
    private int[] layouts;
    private LinearLayout linear;
    private ScrollView scrollview;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_item);
        setTitle(""); // title is shown in extra header

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                type = extras.getString(ARG_TYPE);
                itemID = extras.getString(ITEM_ID);
            }
        }

        listitem = getIntent().getParcelableExtra("selected");
        String listUserID = listitem.getUserID();
        String listAgentID = listitem.getAgent();

        ref = FirebaseDatabase.getInstance().getReference();
        typeref = ref.child(type);
        userref = ref.child("users");
        ownerref = ref.child("users").child(listUserID);
        agentref = ref.child("users").child(listAgentID);
        messagesref = typeref.child(itemID).child("messages");

        sharedPref = this.getSharedPreferences("pem.de.hero.userid", Context.MODE_PRIVATE);
        preferenceUserID = sharedPref.getString("userid", "No UserID");
        token = sharedPref.getString("pushToken", "No token");
        home = new LatLng(Helper.getDouble(sharedPref, "homelat", 0), Helper.getDouble(sharedPref, "homelong", 0));

        mine = listUserID.equals(preferenceUserID);
        accepted = !listAgentID.equals("");
        acceptedByMe = listAgentID.equals(preferenceUserID);


        // layout
        viewPager = (CustomViewPager) findViewById(R.id.view_pager);
        dotsLayout = (LinearLayout) findViewById(R.id.layoutDots);

        // layouts of all detail sliders
        layouts = new int[]{
                R.layout.fragment_detail_item_request,
                R.layout.fragment_detail_item_chat
        };

        viewPager.setAdapter(new DetailViewPagerAdapter());

        // adding bottom dots
        addBottomDots(0);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                addBottomDots(position);
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {

            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });

        // enable swiping
        showChat();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_detail_item, menu);

        MenuItem delete = menu.findItem(R.id.delete);
        if (mine) {
            delete.setVisible(true);
        } else {
            delete.setVisible(false);
        }
        delete.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // owner gets -1 for his 'created' medal
                ownerref.child(type + "sCreated").runTransaction(new CounterTransactionHandler(-1));

                // item is deleted in geofire and in the list
                ref.child("geofire").child(type).child(itemID).removeValue();
                typeref.child(itemID).removeValue();

                // finish activity and show toast
                finish();
                Toast.makeText(DetailItemActivity.this, type.equals("ask") ? R.string.ask_was_deleted : R.string.offer_was_deleted, Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        return true;
    }

    @Override
    public void onMapReady(GoogleMap map) {
        int dimen = 50;
        BitmapDrawable bitmapdraw = (BitmapDrawable) ResourcesCompat.getDrawable(getResources(), R.drawable.ic_home_black_24dp, null);
        Bitmap b = bitmapdraw.getBitmap();
        Bitmap smallMarker = Bitmap.createScaledBitmap(b, dimen, dimen, false);
        map.addMarker(new MarkerOptions().position(othersLocation).title(othersAddress).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        map.addMarker(new MarkerOptions().position(home).title("Dein Zuhause").icon(BitmapDescriptorFactory.fromBitmap(smallMarker)));
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(othersLocation).zoom(15f).build();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    private void showChat() {
        if (mine && accepted || acceptedByMe) {
            viewPager.setPagingEnabled(true);
            dotsLayout.setVisibility(View.VISIBLE);
        } else {
            viewPager.setPagingEnabled(false);
            dotsLayout.setVisibility(View.INVISIBLE);
        }
    }

    private void loadUser(String userID, final UserLoadedEventListener listener) {
        userref.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    listener.onUserLoaded(user);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void loadUsername(final TextView view, String userID, int defaultTextRes) {
        if (userID.equals(preferenceUserID)) {
            view.setText(R.string.you);
        } else if (userID.equals("")) {
            view.setText(defaultTextRes);
        } else {
            loadUser(userID, new UserLoadedEventListener() {
                @Override
                public void onUserLoaded(User user) {
                    view.setText(user.getUsername());
                }
            });
        }
    }

    private void buildRequestPage(View view) {
        if (listitem != null) {
            title = (TextView) view.findViewById(R.id.title);
            description = (TextView) view.findViewById(R.id.description);
            date = (TextView) view.findViewById(R.id.date);
            category = (TextView) view.findViewById(R.id.category);
            agent = (TextView) view.findViewById(R.id.agent);
            user = (TextView) view.findViewById(R.id.user);
            showDetails = (TextView) view.findViewById(R.id.show_details);
            details = (GridLayout) view.findViewById(R.id.details);
            accept = (Button) view.findViewById(R.id.accept);
            reset = (Button) view.findViewById(R.id.reset);

            // static texts
            title.setText(listitem.getTitle());
            description.setText(listitem.getDescription());
            category.setText(listitem.getCategory());

            SimpleDateFormat from = new SimpleDateFormat("yyyyMMddHHmm");
            SimpleDateFormat to = new SimpleDateFormat("dd.MM.yyyy HH:mm");
            try {
                Date value = from.parse(listitem.getDate());
                date.setText(to.format(value));
            } catch (ParseException e) {
                date.setText(R.string.not_available);
            }

            // accept button for transfering karma and accepting
            accept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (type.equals("ask") && mine && accepted || type.equals("offer") && acceptedByMe) {
                        // agent/owner gets karma and +1 for his 'done' medal
                        DatabaseReference userref = type.equals("ask") ? agentref : ownerref;
                        userref.child("karma").runTransaction(new CounterTransactionHandler(+100));
                        userref.child("asksDone").runTransaction(new CounterTransactionHandler(+1));

                        // item is removed from geofire and the list
                        ref.child("geofire").child(type).child(itemID).removeValue();
                        typeref.child(itemID).removeValue();

                        // finish and show toast
                        finish();
                        Toast.makeText(DetailItemActivity.this, R.string.karma_was_credited, Toast.LENGTH_SHORT).show();
                    } else if (!mine && !accepted) {
                        // refresh booleans
                        accepted = true;
                        acceptedByMe = true;

                        // set agent and register user for push notifications
                        typeref.child(itemID).child("agent").setValue(preferenceUserID);
                        typeref.child(itemID).child("follower").child("agent").setValue(token);
                        listitem.setAgent(preferenceUserID);

                        // refresh UI
                        showChat();
                        refreshRequestPage();
                    }
                }
            });

            // reset button
            reset.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // refresh booleans
                    accepted = false;
                    acceptedByMe = false;

                    // agent is removed and push notifications for this item are disabled
                    typeref.child(itemID).child("agent").setValue("");
                    typeref.child(itemID).child("follower").child("agent").setValue("");
                    listitem.setAgent("");

                    // refresh UI
                    showChat();
                    refreshRequestPage();
                }
            });

            // show and hide details
            details.setVisibility(View.GONE);
            showDetails.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (details.getVisibility() == View.VISIBLE) {
                        showDetails.setText(R.string.show_details);
                        details.setVisibility(View.GONE);
                    } else {
                        showDetails.setText(R.string.hide_details);
                        details.setVisibility(View.VISIBLE);
                    }
                }
            });

            // everything else
            refreshRequestPage();
        }
    }

    private void refreshRequestPage() {
        // user and agent
        loadUsername(user, listitem.getUserID(), R.string.not_available);
        loadUsername(agent, listitem.getAgent(), R.string.no_agent_yet_mine);

        // address
        if (mine && accepted || acceptedByMe) {
            String userID = mine ? listitem.getAgent() : listitem.getUserID();
            final DetailItemActivity activity = this;
            loadUser(userID, new UserLoadedEventListener() {
                @Override
                public void onUserLoaded(User user) {
                    othersAddress = user.getStreet() + ", " + user.getCity();
                    othersLocation = Helper.getLocationFromAddress(othersAddress, activity);
                    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.maps);
                    mapFragment.getMapAsync(activity);
                }
            });
        }

        // accept button
        accept.setVisibility(View.GONE);
        if (type.equals("ask") && mine && accepted || type.equals("offer") && acceptedByMe) {
            accept.setVisibility(View.VISIBLE);
            accept.setText(R.string.give_karma);
        } else if (!mine && !accepted) {
            accept.setVisibility(View.VISIBLE);
            accept.setText(R.string.accept);
        }else if(mine && !accepted){
            accept.setVisibility(View.VISIBLE);
            accept.setText(R.string.give_karma);
            accept.setBackgroundColor(ContextCompat.getColor(this, R.color.gray));
            accept.setEnabled(false);
        }

        // reset button
        if (accepted) {
            reset.setVisibility(View.VISIBLE);
            reset.setText(acceptedByMe ? R.string.reset_accept : R.string.reset_agent);
        } else if(!accepted && mine){
            reset.setVisibility(View.VISIBLE);
            reset.setText(R.string.reset_agent);
            reset.setBackgroundColor(ContextCompat.getColor(this, R.color.gray));
            reset.setEnabled(false);
        }else{
            reset.setVisibility(View.GONE);
        }
    }

    private void buildChatPage(View view) {
        if (listitem != null) {
            final TextView chat_partner_textview = (TextView) view.findViewById(R.id.username);
            linear = (LinearLayout) view.findViewById(R.id.linearlayout);

            if(mine){
                loadUsername(chat_partner_textview, listitem.getAgent(), R.string.not_available);
            }else{
                loadUsername(chat_partner_textview, listitem.getUserID(), R.string.not_available);
            }

            messagesEventListener();

            final ImageButton btn_send = (ImageButton) view.findViewById(R.id.send);
            final EditText messagefield = (EditText) view.findViewById(R.id.messagefield);
            scrollview = ((ScrollView) view.findViewById(R.id.scrollview));

            btn_send.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // creates a node in messages that will later contain the message and userid
                    if (messagefield.getText().length() > 0) {
                        Map<String, Object> messagemap = new HashMap<String, Object>();
                        String messagekey = messagesref.push().getKey();
                        messagesref.updateChildren(messagemap);

                        //adds the message and the userid to the node
                        Map<String, Object> messagemapInner = new HashMap<String, Object>();
                        messagemapInner.put("userid", preferenceUserID);
                        messagemapInner.put("message", messagefield.getText().toString());
                        messagesref.child(messagekey).updateChildren(messagemapInner);

                        messagefield.setText("");
                    } else {
                        Toast.makeText(DetailItemActivity.this, R.string.message_is_too_short, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void seeChatMessages(DataSnapshot dataSnapshot) {
        LayoutInflater inflator = (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // iterates through all the children of messages. Takes the values and creates a new TextView with them.
        Iterator i = dataSnapshot.getChildren().iterator();
        while (i.hasNext()) {
            String chat_message = (String) ((DataSnapshot) i.next()).getValue();
            String chat_userid = (String) ((DataSnapshot) i.next()).getValue();

            View item = inflator.inflate(R.layout.fragment_message_layout, null);

            TextView chatmessage = (TextView) item.findViewById(R.id.chatmessage);
            TextView usernamechat = (TextView) item.findViewById(R.id.usernamechat);
            LinearLayout linearlayout2 = (LinearLayout) item.findViewById(R.id.linearlayout2);
            LinearLayout containermessage = (LinearLayout) item.findViewById(R.id.containermessage);


            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(0, 12, 0, 12);
            item.setLayoutParams(layoutParams);

            chatmessage.setText(chat_message);

            if (chat_userid.equals(preferenceUserID)) {
                usernamechat.setText(R.string.you);
                linearlayout2.setGravity(Gravity.END);
                containermessage.setBackgroundResource(R.drawable.roundedrectangle);
                GradientDrawable gd = (GradientDrawable) containermessage.getBackground().getCurrent();
                gd.setColor(ContextCompat.getColor(this, R.color.colorPrimary));
                gd.setStroke(4, (ContextCompat.getColor(this, R.color.colorPrimary)));
                containermessage.setGravity(Gravity.END);

                usernamechat.setGravity(Gravity.END);
                chatmessage.setGravity(Gravity.END);
            } else {
                loadUsername(usernamechat, chat_userid, R.string.not_available);
                linearlayout2.setGravity(Gravity.START);
                containermessage.setBackgroundResource(R.drawable.roundedrectangle);
                GradientDrawable gd = (GradientDrawable) containermessage.getBackground().getCurrent();
                gd.setColor(ContextCompat.getColor(this, R.color.colorAccent));
                gd.setStroke(4, (ContextCompat.getColor(this, R.color.colorAccent)));
                containermessage.setGravity(Gravity.START);
                usernamechat.setGravity(Gravity.START);
                chatmessage.setGravity(Gravity.START);
            }

            linear.addView(item);

        }

        // makes sure that chat is scrolled to the bottom and the latest message is displayed.
        scrollview.post(new Runnable() {
            @Override
            public void run() {
                scrollview.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    private void messagesEventListener() {
        messagesref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                seeChatMessages(dataSnapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                seeChatMessages(dataSnapshot);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void addBottomDots(int currentPage) {
        TextView[] dots = new TextView[layouts.length];

        int[] colorsActive = getResources().getIntArray(R.array.array_detail_dot_active);
        int[] colorsInactive = getResources().getIntArray(R.array.array_detail_dot_inactive);

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

    /**
     * View pager adapter
     */
    public class DetailViewPagerAdapter extends PagerAdapter {
        private LayoutInflater layoutInflater;

        public DetailViewPagerAdapter() {
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View view = layoutInflater.inflate(layouts[position], container, false);

            // Elemente müssen mit zugehöriger View angesprochen werden.
            if (position == 0) {
                buildRequestPage(view);
            } else if (position == 1) {
                buildChatPage(view);
            }

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
