package model;

import com.thoughtbot.expandablecheckrecyclerview.models.MultiCheckExpandableGroup;

import java.util.List;

/**
 * Created by LieuDucManh on 4/13/2018.
 */
public class CookFood extends MultiCheckExpandableGroup {

    private String time;
    private String employeeID;
    private int status;

    public CookFood(String title ,List<FoodInside> items) {
        super(title ,items);
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getEmployeeID() {
        return employeeID;
    }

    public void setEmployeeID(String employeeID) {
        this.employeeID = employeeID;
    }
}
