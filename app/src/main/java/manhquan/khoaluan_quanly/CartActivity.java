package manhquan.khoaluan_quanly;

import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;

import constants.QuanLyConstants;
import model.FoodOnBill;

public class CartActivity extends AppCompatActivity {

    private static final String TAG = "CartActivity";
    private ArrayList<FoodOnBill> listFoodChoose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        Bundle bundle = getIntent().getBundleExtra(QuanLyConstants.INTENT_FOOD_CHOOSE_CART);

        listFoodChoose = (ArrayList<FoodOnBill>)bundle.getSerializable(QuanLyConstants.INTENT_FOOD_CHOOSE_CART);
        Log.i(TAG,listFoodChoose.size() + "");

    }
}
