package model;

import abstractModel.Employee;

/**
 * Created by LieuDucManh on 3/17/2018.
 */

public class Waiter extends Employee {
    public Waiter(String contactnumber, String name, String password, int position, String restaurantid) {
        super(contactnumber, name, password, position, restaurantid);
    }

    public Waiter() {
    }
}
