package model;

import abstractModel.Employee;

/**
 * Created by LieuDucManh on 3/17/2018.
 */

public class Manager extends Employee {
    public Manager(String contactnumber, String name, String password, int position, String restaurantid) {
        super(contactnumber, name, password, position, restaurantid);
    }

    public Manager() {
    }
}
