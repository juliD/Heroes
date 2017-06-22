package pem.de.heroes;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;


public class ListItem implements Parcelable {
    private String description;
    private String title;
    private String address;
    private String userid;
    private String agent;
    private String date;



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

    public ListItem(Parcel parcel){
        this.title = parcel.readString();
        this.description = parcel.readString();
        this.address = parcel.readString();
        this.userid = parcel.readString();
        this.agent = parcel.readString();
        this.date = parcel.readString();
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
}