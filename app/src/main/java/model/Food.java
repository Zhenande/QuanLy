package model;

import abstractModel.FoodAbstract;

/**
 * Created by ABC on 3/23/2018.
 */

public class Food extends FoodAbstract {

    private String description;
    private int imageResource;


    public Food(String foodId, String foodName, int price, String description, int imageResource) {
        super(foodId, foodName, price);
        this.description = description;
        this.imageResource = imageResource;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getImageResource() {
        return imageResource;
    }

    public void setImageResource(int imageResource) {
        this.imageResource = imageResource;
    }
}
