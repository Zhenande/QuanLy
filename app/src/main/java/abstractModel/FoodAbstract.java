package abstractModel;

/**
 * Created by ABC on 3/11/2018.
 */

public abstract class FoodAbstract {

    private String foodId;
    private String foodName;
    private int price;

    public FoodAbstract(String foodId, String foodName, int price) {
        this.foodId = foodId;
        this.foodName = foodName;
        this.price = price;
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
