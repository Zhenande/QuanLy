package abstractModel;

/**
 * Created by LieuDucManh on 3/17/2018.
 */

public abstract class Employee {

    private String contactnumber;
    private String iD;
    private String name;
    private String password;
    private int position;
    private String restaurantid;
    private String username;

    public Employee(String contactnumber, String iD, String name, String password, int position, String restaurantid, String username) {
        this.contactnumber = contactnumber;
        this.iD = iD;
        this.name = name;
        this.password = password;
        this.position = position;
        this.restaurantid = restaurantid;
        this.username = username;
    }

    public Employee() {
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

    public String getiD() {
        return iD;
    }

    public void setiD(String iD) {
        this.iD = iD;
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }


}
