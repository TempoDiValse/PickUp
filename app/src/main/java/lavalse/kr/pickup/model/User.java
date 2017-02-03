package lavalse.kr.pickup.model;

import android.os.Parcel;
import android.os.Parcelable;

import lavalse.kr.pickup.util.StringUtil;

/**
 * @author LaValse
 * @date 2016-07-15
 */
public class User implements Parcelable{
    private String id;
    private String ip;
    private String date;
    private String comment;

    public User(String id, String comment){
        this.id = id;
        this.comment = comment;
    }

    private User(Parcel dest){
        id = dest.readString();
        ip = dest.readString();
        date = dest.readString();
        comment = dest.readString();
    }

    public String getIP() {
        return ip;
    }

    public void setIP(String ip) {
        this.ip = ip;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = StringUtil.getDateString(date);
    }

    public String getID() {

        return id;
    }

    public void setID(String id) {
        this.id = id;
    }

    public String getComment(){
        return comment;
    }

    public void setComment(String comment){
        this.comment = comment;
    }

    @Override
    public String toString() {
        return "User: "+id+" IP: "+ip+" Date: "+date;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(ip);
        parcel.writeString(date);
        parcel.writeString(comment);
    }

    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>(){

        @Override
        public User createFromParcel(Parcel parcel) {
            return new User(parcel);
        }

        @Override
        public User[] newArray(int i) {
            return new User[i];
        }
    };
}
