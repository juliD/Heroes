package pem.de.heroes;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.gms.maps.model.LatLng;

public class PrefManager {
    static SharedPreferences pref;
    private SharedPreferences.Editor editor;
    Context context;

    // shared pref mode
    int PRIVATE_MODE = 0;

    // Shared preferences file name
    private static final String PREF_NAME = "heroes";

    private static final String IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch";

    public PrefManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void setFirstTimeLaunch(boolean isFirstTime) {
        editor.putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime);
        editor.commit();
    }

    public boolean isFirstTimeLaunch() {
        return pref.getBoolean(IS_FIRST_TIME_LAUNCH, true);
    }

    public void setUserid(String userid){
        editor.putString("userid", userid);
        editor.commit();
    }
    public void setUsername(String username){
        editor.putString("username", username);
    }

    public static String getUsername(){
        return pref.getString("username","Anonym");
    }

    public void setHome(){
        LatLng home = Helper.getLocationFromAddress("Marienplatz 5, München",context);
        editor.putString("address","Marienplatz 5");
        editor.putString("city","München");

        editor.putLong("homelat",Double.doubleToRawLongBits(home.latitude));
        Helper.putDouble(editor,"homelat",home.latitude);
        Helper.putDouble(editor,"homelong",home.longitude);
        editor.commit();
    }



}
