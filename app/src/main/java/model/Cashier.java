package model;

import abstractModel.Employee;

/**
 * Created by ABC on 3/17/2018.
 */

public class Cashier extends Employee {
    public Cashier(String contactnumber, String iD, String name, String password, int position, String restaurantid, String username) {
        super(contactnumber, iD, name, password, position, restaurantid, username);
    }

    public Cashier() {
    }
}
