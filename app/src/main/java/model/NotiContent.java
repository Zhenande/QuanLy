package model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by LieuDucManh on 4/13/2018.
 */

public class NotiContent implements Parcelable {

    public String content;

    public String getContent() {
        return content;
    }

    public NotiContent(String content) {
        this.content = content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    protected NotiContent(Parcel in) {
        content = in.readString();
    }

    public static final Creator<NotiContent> CREATOR = new Creator<NotiContent>() {
        @Override
        public NotiContent createFromParcel(Parcel in) {
            return new NotiContent(in);
        }

        @Override
        public NotiContent[] newArray(int size) {
            return new NotiContent[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(content);
    }
}
