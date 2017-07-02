package pem.de.heroes.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by Julia on 25.06.2017.
 */

public class InstanceIdService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d("InstanceIdService", "Refreshed token: " + refreshedToken);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(refreshedToken);
    }
    private void sendRegistrationToServer(String token) {
        Log.d("InstanceIdService","this is the Refreshed token: "+token);
        SharedPreferences sharedPref=this.getSharedPreferences("pem.de.hero.userid", Context.MODE_PRIVATE);
        String userid = sharedPref.getString("userid","No User ID");
        DatabaseReference users = FirebaseDatabase.getInstance().getReference("users/"+userid);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("pushToken",token);
        editor.commit();

        users.child("pushToken").setValue(token);
    }
}
