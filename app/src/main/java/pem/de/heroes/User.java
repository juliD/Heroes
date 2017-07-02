package pem.de.heroes;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User {

    private String userid;
    private String username;
    private int karma;
    private int asksCreated;
    private int asksFullfilled;
    private int offersCreated;
    private int offersUsed;

    public User(){
        //default constructor required for datasnapshot
    }

    public User(String userid, String username, int karma){
        this.userid = userid;
        this.username = username;
        this.karma = karma;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getKarma() {
        return karma;
    }

    public void setKarma(int karma) {
        this.karma = karma;
    }

    public int getAsksCreated() {
        return asksCreated;
    }

    public void setAsksCreated(int asksCreated) {
        this.asksCreated = asksCreated;
    }

    public int getAsksFullfilled() {
        return asksFullfilled;
    }

    public void setAsksFullfilled(int asksFullfilled) {
        this.asksFullfilled = asksFullfilled;
    }

    public int getOffersCreated() {
        return offersCreated;
    }

    public void setOffersCreated(int offersCreated) {
        this.offersCreated = offersCreated;
    }

    public int getOffersUsed() {
        return offersUsed;
    }

    public void setOffersUsed(int offersUsed) {
        this.offersUsed = offersUsed;
    }
}
