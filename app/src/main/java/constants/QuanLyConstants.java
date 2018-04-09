package constants;

/**
 * Created by LieuDucManh on 3/11/2018.
 */

public class QuanLyConstants {
    public static final int IMAGE_VIEW_CLICK_ID = 0;
    public static final int CREATE_EMPLOYEE = 2;
    public static final int MAX_CARDS_LIST_TABLE = 3;
    public static final int DETAIL_EMPLOYEE = 4;
    public static final int FOOD_DETAIL = 5;
    public static final int CREATE_TABLE_ID = 6;
    public static final String INTENT_FOOD_DETAIL_NAME = "FoodName";
    public static final String INTENT_FOOD_DETAIL_FLAG = "FoodDetailFlag";
    public static final String RESTAURANT_ID = "RestaurantID";
    public static final String ID = "ID";
    public static final String INTENT_DOCUMENT_ID = "documentID";
    public static final String FLAG = "flag";
    public static final String SHARED_PERFERENCE = "CommonPrefs";
    public static final String SHARED_LANGUAGE = "Language";
    public static final String SHARED_POSITION = "Position";


    // Field of employee
    public static final String EMPLOYEE = "employee";
    public static final String EMPLOYEE_NAME = "Name";
    public static final String EMPLOYEE_USERNAME = "Username";
    public static final String EMPLOYEE_PASSWORD = "Password";
    public static final String EMPLOYEE_CONTACT = "ContactNumber";
    public static final String EMPLOYEE_POSITION = "Position";

    // Field of food
    public static final String FOOD = "food";
    public static final String FOOD_NAME = "Name";
    public static final String FOOD_IMAGE_NAME = "ImageName";
    public static final String FOOD_PATH_IMAGE = "images/";
    public static final String FOOD_PRICE = "Price";
    public static final String FOOD_DESCRIPTION = "Description";
    public static final String FOOD_TYPE = "Type";
    public static final String FOOD_QUANTITY = "Quantity";

    // Field of Order
    public static final String ORDER = "order";
    public static final String ORDER_TABLE = "Table";
    public static final String ORDER_CASH_TOTAL = "CashTotal";
    public static final String ORDER_TIME = "Time";
    public static final String ORDER_DATE = "Date";
    public static final String ORDER_CheckOut = "CheckOut";
    public static final String CUSTOMER_ID = "CustomerID";
    public static final String FOOD_ON_ORDER = "foodOnOrder";
    public static final String TABLE = "table";
    public static final String TABLE_NUMBER = "Number";
    public static final String TABLE_ORDER_ID = "OrderID";


    // Field of Customer
    public static final String CUSTOMER = "customer";
    public static final String CUS_NAME = "Name";

    // Field of Restaurant
    public static final String RESTAURANT = "restaurant";
    public static final String RESTAURANT_NAME = "Name";
    public static final String RESTAURANT_FOOD_TYPE = "FoodType";
    public static final String FOOD_TYPE_NAME = "Name";
}
