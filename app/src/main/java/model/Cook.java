package model;

import abstractModel.Employee;

/**
 * Created by LieuDucManh on 3/17/2018.
 */

public class Cook extends Employee {

    public Cook(String contactnumber, String name, String password, int position, String restaurantid) {
        super(contactnumber, name, password, position, restaurantid);
    }

    public Cook() {
    }
}
