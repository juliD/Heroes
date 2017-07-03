package pem.de.heroes.model;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User {

    private String userid;
    private String username;
    private int karma;

    private String city;
    private String street;
    private int radius;
    private double homelat;
    private double homelong;

    private int asksCreated;
    private int asksDone;
    private int offersCreated;
    private int offersDone;

    public User(String userid, String username, int karma){
        this.userid = userid;
        this.username = username;
        this.karma = karma;
    }

    public User() {
        // default constructor required for DataSnapshot
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

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public double getHomelat() {
        return homelat;
    }

    public void setHomelat(double homelat) {
        this.homelat = homelat;
    }

    public double getHomelong() {
        return homelong;
    }

    public void setHomelong(double homelong) {
        this.homelong = homelong;
    }

    public int getAsksCreated() {
        return asksCreated;
    }

    public void setAsksCreated(int asksCreated) {
        this.asksCreated = asksCreated;
    }

    public int getAsksDone() {
        return asksDone;
    }

    public void setAsksDone(int asksDone) {
        this.asksDone = asksDone;
    }

    public int getOffersCreated() {
        return offersCreated;
    }

    public void setOffersCreated(int offersCreated) {
        this.offersCreated = offersCreated;
    }

    public int getOffersDone() {
        return offersDone;
    }

    public void setOffersDone(int offersDone) {
        this.offersDone = offersDone;
    }
}
