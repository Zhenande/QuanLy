package manhquan.khoaluan_quanly;

import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;

import adapter.FoodChooseListAdapter;
import butterknife.BindView;
import butterknife.ButterKnife;
import constants.QuanLyConstants;
import model.FoodOnBill;

public class CartActivity extends AppCompatActivity {

    private static final String TAG = "CartActivity";
    public static ArrayList<FoodOnBill> listFoodChoose;
    @BindView(R.id.food_choose_list_view)
    public ListView listViewFood;
    public static FoodChooseListAdapter listViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        ButterKnife.bind(this);

        Bundle bundle = getIntent().getBundleExtra(QuanLyConstants.INTENT_FOOD_CHOOSE_CART);

        listFoodChoose = (ArrayList<FoodOnBill>)bundle.getSerializable(QuanLyConstants.INTENT_FOOD_CHOOSE_CART);
        Log.i(TAG,listFoodChoose.size() + "");

        listViewAdapter = new FoodChooseListAdapter(this,listFoodChoose);
        ViewGroup header = (ViewGroup)getLayoutInflater().inflate(R.layout.food_choose_list_header,listViewFood,false);
        listViewFood.addHeaderView(header);

        listViewFood.setAdapter(listViewAdapter);

    }

    public static void removeFood(int position){
        listFoodChoose.remove(position);
        listViewAdapter.notifyDataSetChanged();
    }

}
