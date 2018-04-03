package model;

import abstractModel.FoodAbstract;

/**
 * Created by LieuDucManh on 3/23/2018.
 */

public class Food extends FoodAbstract {

    private String description;
    private String foodType;
    private String imageResource;


    public Food(String foodId, String foodName, int price, String description, String imageResource, String foodType) {
        super(foodId, foodName, price);
        this.description = description;
        this.foodType = foodType;
        this.imageResource = imageResource;
    }

    public Food() {
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageResource() {
        return imageResource;
    }

    public void setImageResource(String imageResource) {
        this.imageResource = imageResource;
    }

    public String getFoodType() {
        return foodType;
    }

    public void setFoodType(String foodType) {
        this.foodType = foodType;
    }
}
