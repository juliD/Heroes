package pem.de.heroes;

import android.os.Parcel;
import android.os.Parcelable;


public class ListItem implements Parcelable {
    private String description;
    private String title;
    private String address;
    private String userID;
    private String agent;



    public ListItem(String title, String description, String address, String userID, String agent) {
        this.description = description;
        this.title = title;
        this.address = address;
        this.userID = userID;
        this.agent = agent;
    }

    public ListItem() {
    }
    public String getUserID() { return userID;}
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

    public ListItem(Parcel parcel){
        this.title = parcel.readString();
        this.description = parcel.readString();
        this.address = parcel.readString();
        this.userID = parcel.readString();
        this.agent = parcel.readString();
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
        parcel.writeString(userID);
        parcel.writeString(agent);

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