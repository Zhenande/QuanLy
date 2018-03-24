package manhquan.khoaluan_quanly;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;

import constants.QuanLyConstants;

public class FoodDetailActivity extends AppCompatActivity {

    private String foodName;
    private boolean flag = false; // meaning on create
    public EditText txtName;
    public EditText txtPrice;
    public EditText txtDescription;
    public EditText txtType;
    public Button buttonCreate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_detail);

        txtName = findViewById(R.id.food_detail_name);
        txtPrice = findViewById(R.id.food_detail_price);
        txtDescription = findViewById(R.id.food_detail_description);
        txtType = findViewById(R.id.food_detail_food_type);
        buttonCreate = findViewById(R.id.food_detail_button_add);

        foodName = getIntent().getStringExtra(QuanLyConstants.INTENT_FOOD_DETAIL_NAME);
        if(!TextUtils.isEmpty(foodName)){
            flag = true; // meaning we are on updated
        }
    }
}
