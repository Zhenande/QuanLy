package abstractModel;

import java.io.Serializable;

/**
 * Created by LieuDucManh on 3/11/2018.
 */

public abstract class FoodAbstract implements Serializable {

    private String foodId;
    private String foodName;
    private int price;

    public FoodAbstract(String foodId, String foodName, int price) {
        this.foodId = foodId;
        this.foodName = foodName;
        this.price = price;
    }

    public FoodAbstract() {
    }

    public String getFoodId() {
        return foodId;
    }

    public void setFoodId(String foodId) {
        this.foodId = foodId;
    }

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }
}
