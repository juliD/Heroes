package pem.de.heroes;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;
import java.util.Locale;

/**
 * Created by Julia on 14.06.2017.
 */

public class Helper {

    public static LatLng getLocationFromAddress(String strAddress, Context context){
        Geocoder coder = new Geocoder(context, Locale.getDefault());

        LatLng p1 = null;

        try {
            List<Address> address = coder.getFromLocationName(strAddress, 5);
            Log.d("Helper",strAddress);
            while (address.size()==0) {
                address = coder.getFromLocationName(strAddress, 1);
                Log.d("Helper","in loop address:" +strAddress);
            }

            if (address == null) {
                return null;
            }
            Address location = address.get(0);
            p1 = new LatLng(location.getLatitude(), location.getLongitude());

            return p1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return p1;
    }

    /**
     *
     * @param home homeaddress
     * @param point address of offer
     * @return distance in meter
     */
    public static float calculateDistance(LatLng home, LatLng point){

        Location loc1 = new Location("");
        loc1.setLatitude(home.latitude);
        loc1.setLongitude(home.longitude);

        Location loc2 = new Location("");
        loc2.setLatitude(point.latitude);
        loc2.setLongitude(point.longitude);

        return loc1.distanceTo(loc2);
    }

    public static String distanceToString (float distance){
        String s = "";
        if(distance<100){
            s = "hier";
        }
        else if(distance<200 ){
            s = "sehr nah";
        }
        else if(distance<500){
            s="nah";
        }
        else{
            s="Umgebung";
        }
        return s;
    }
    static SharedPreferences.Editor putDouble(final SharedPreferences.Editor edit, final String key, final double value) {
        return edit.putLong(key, Double.doubleToRawLongBits(value));
    }

    static double getDouble(final SharedPreferences prefs, final String key, final double defaultValue) {
        return Double.longBitsToDouble(prefs.getLong(key, Double.doubleToLongBits(defaultValue)));
    }

}
