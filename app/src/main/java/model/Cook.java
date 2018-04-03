package model;

import abstractModel.Employee;

/**
 * Created by LieuDucManh on 3/17/2018.
 */

public class Cook extends Employee {

    public Cook(String contactnumber, String iD, String name, String password, int position, String restaurantid, String username) {
        super(contactnumber, iD, name, password, position, restaurantid, username);
    }

    public Cook() {
    }
}
