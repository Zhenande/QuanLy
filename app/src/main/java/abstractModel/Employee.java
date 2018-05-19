package abstractModel;

/**
 * Created by LieuDucManh on 3/17/2018.
 */

public abstract class Employee {

    private String contactnumber;
    private String name;
    private String password;
    private int position;
    private String emID;
    private String dayWork;
    private String restaurantid;
    private boolean onWork;

    public Employee(String contactnumber, String name, String password, int position, String restaurantid) {
        this.contactnumber = contactnumber;
        this.name = name;
        this.password = password;
        this.position = position;
        this.restaurantid = restaurantid;
    }

    public Employee() {
    }

    public boolean isOnWork() {
        return onWork;
    }

    public void setOnWork(boolean onWork) {
        this.onWork = onWork;
    }

    public String getEmID() {
        return emID;
    }

    public void setEmID(String emID) {
        this.emID = emID;
    }

    public String getDayWork() {
        return dayWork;
    }

    public void setDayWork(String dayWork) {
        this.dayWork = dayWork;
    }

    public String getRestaurantid() {
        return restaurantid;
    }

    public void setRestaurantid(String restaurantid) {
        this.restaurantid = restaurantid;
    }

    public String getContactnumber() {
        return contactnumber;
    }

    public void setContactnumber(String contactnumber) {
        this.contactnumber = contactnumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

}
