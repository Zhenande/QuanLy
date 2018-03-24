package model;

import abstractModel.FoodAbstract;

/**
 * Created by ABC on 3/23/2018.
 */

public class Food extends FoodAbstract {

    private String description;
    private String foodType;
    private int imageResource;


    public Food(String foodId, String foodName, int price, String description, int imageResource, String foodType) {
        super(foodId, foodName, price);
        this.description = description;
        this.foodType = foodType;
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

    public String getFoodType() {
        return foodType;
    }

    public void setFoodType(String foodType) {
        this.foodType = foodType;
    }
}
