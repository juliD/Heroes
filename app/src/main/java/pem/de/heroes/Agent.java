package pem.de.heroes;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by fealt on 22.06.2017.
 */
@IgnoreExtraProperties
public class Agent {

    private String userid;
    private String username;
    private int karma;

    public Agent(){
        //default constructor required for datasnapshot
    }

    public Agent(String userid, String username, int karma){
        this.userid = userid;
        this.username = username;
        this.karma = karma;
    }


    public String getUserid(){
        return userid;
    }
    public String getUsername(){
        return username;
    }

    public int getKarma(){
        return karma;
    }

    public void setUserid(String userid){
        this.userid = userid;
    }

    public void setUsername(String username){
        this.username = username;
    }
    public void setKarma(int karma){
        this.karma = karma;
    }



}
