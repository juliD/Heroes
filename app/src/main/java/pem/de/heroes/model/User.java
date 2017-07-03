package pem.de.heroes.model;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User {

    private String userid;
    private String username;
    private int karma;
    private int asksCreated;
    private int asksDone;
    private int offersCreated;
    private int offersDone;

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
