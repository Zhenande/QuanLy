package model;

import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import java.util.List;

/**
 * Created by LieuDucManh on 4/13/2018.
 */

public class Notification extends ExpandableGroup<NotiContent>{

    private String time;

    public Notification(String title, List<NotiContent> items) {
        super(title, items);
    }


    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
