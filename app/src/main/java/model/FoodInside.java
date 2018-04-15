package model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by LieuDucManh on 4/13/2018.
 */
public class FoodInside implements Parcelable {

    String content;

    public void setContent(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public FoodInside(String content) {
        this.content = content;
    }

    protected FoodInside(Parcel in) {
    }

    public static final Creator<FoodInside> CREATOR = new Creator<FoodInside>() {
        @Override
        public FoodInside createFromParcel(Parcel in) {
            return new FoodInside(in);
        }

        @Override
        public FoodInside[] newArray(int size) {
            return new FoodInside[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }
}
