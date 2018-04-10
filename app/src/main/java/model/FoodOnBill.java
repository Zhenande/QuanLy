package model;

import java.io.Serializable;

import abstractModel.FoodAbstract;

/**
 * Created by LieuDucManh on 3/11/2018.
 */

public class FoodOnBill extends FoodAbstract implements Serializable{

    private int quantity;

    public FoodOnBill(String foodId, String foodName, int price, int quantity) {
        super(foodId, foodName, price);
        this.quantity = quantity;
    }

    public FoodOnBill() {
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }


}
