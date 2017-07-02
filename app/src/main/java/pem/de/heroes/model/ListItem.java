package pem.de.heroes.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.firebase.database.Exclude;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class ListItem implements Parcelable,Comparable<ListItem> {
    private String description;
    private String title;
    private String address;
    private String userid;
    private String agent;
    private String date;
    private String id;
    private int distance;


    public ListItem(String title, String description, String address, String userid, String agent, String date) {
        this.description = description;
        this.title = title;
        this.address = address;
        this.userid = userid;
        this.agent = agent;
        this.date = date;
    }

    public ListItem() {
    }

    public String getid() {
        return id;
    }

    public void setid(String id) {
        this.id = id;
    }
    public String getUserID() { return userid;}
    public String getTitle() {
        return title;
    }
    public String getDescription() {
        return description;
    }
    public String getAddress() {
        return address;
    }
    public String getAgent() {return agent;}
    public String getDate(){return date;}
    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public ListItem(Parcel parcel){
        this.title = parcel.readString();
        this.description = parcel.readString();
        this.address = parcel.readString();
        this.userid = parcel.readString();
        this.agent = parcel.readString();
        this.date = parcel.readString();
        this.id = parcel.readString();
        this.distance = parcel.readInt();
    }

    @Override
    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags){
        parcel.writeString(title);
        parcel.writeString(description);
        parcel.writeString(address);
        parcel.writeString(userid);
        parcel.writeString(agent);
        parcel.writeString(date);
        parcel.writeString(id);
        parcel.writeInt(distance);
    }

    @Exclude
    public Map<String, Object> toMap(){
        HashMap<String, Object> result = new HashMap<>();
        result.put("title",title);
        result.put("description",description);
        result.put("address",address);
        result.put("agent",agent);
        result.put("date",date);
        result.put("userid",userid);


        return result;
    }

    public static final Parcelable.Creator<ListItem> CREATOR = new Parcelable.Creator<ListItem>(){
        @Override
        public ListItem createFromParcel(Parcel src){
            return new ListItem(src);
        }

        @Override
        public ListItem[] newArray (int size){
            return new ListItem[size];
        }
    };

    @Override
    public int compareTo(@NonNull ListItem o) {
        DateFormat format = new SimpleDateFormat("yyyyMMddHHmm", Locale.ENGLISH);
        Date first=new Date();
        Date second=new Date();
        try {
            first = format.parse(this.getDate());
            second = format.parse(o.getDate());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if(first.before(second)){
            return 1;
        }
        else if(second.before(first)){
            return -1;
        }else{
            return 0;
        }
    }
}