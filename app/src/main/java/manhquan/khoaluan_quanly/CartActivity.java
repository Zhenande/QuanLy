package manhquan.khoaluan_quanly;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Timer;
import java.util.TimerTask;

import adapter.FoodChooseListAdapter;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import constants.QuanLyConstants;
import fragment.FoodFragment;
import model.FoodOnBill;
import util.GlobalVariable;

public class CartActivity extends AppCompatActivity implements  AdapterView.OnItemSelectedListener, View.OnClickListener {

    private static final String TAG = "CartActivity";
    public static ArrayList<FoodOnBill> listFoodChoose;
    @BindView(R.id.food_choose_list_view)
    public ListView listViewFood;
    @BindView(R.id.cart_spinner_tableNumber)
    public Spinner spinnerTable;
    @BindView(R.id.cart_cus_id)
    public EditText edCusID;
    @BindView(R.id.food_choose_button_order)
    public Button buttonOrder;
    public static FoodChooseListAdapter listViewAdapter;
    private ArrayList<String> listTable = new ArrayList<>();
    private FirebaseFirestore db;
    private int posTable;
    private boolean flag = false;
    private final long DELAY = 1500;
    private int lengthOfCus = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        ButterKnife.bind(this);
        db = FirebaseFirestore.getInstance();

        if(!TextUtils.isEmpty(GlobalVariable.tableCusID)){
            edCusID.setText(GlobalVariable.tableCusID);
        }

        listFoodChoose = FoodFragment.listFoodChoose;

        getTableAvailable();

        listViewAdapter = new FoodChooseListAdapter(this,listFoodChoose);
        ViewGroup header = (ViewGroup)getLayoutInflater().inflate(R.layout.food_choose_list_header,listViewFood,false);
        listViewFood.addHeaderView(header);

        listViewFood.setAdapter(listViewAdapter);
        buttonOrder.setOnClickListener(this);

        edCusID.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.i(TAG,"AfterTextChanged");
                final String finalS = s.toString();
                if(s.toString().length() > 9){
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            GlobalVariable.tableCusID = finalS;
                        }
                    },DELAY);
                }
            }
        });
    }

    public static void removeFood(int position){
        listFoodChoose.remove(position);
        listViewAdapter.notifyDataSetChanged();
    }

    private void getTableAvailable(){
        String restaurantID = getRestaurantID();
        if(!TextUtils.isEmpty(GlobalVariable.tableChoose)){
            flag = true;
        }
        db.collection(QuanLyConstants.TABLE)
            .whereEqualTo(QuanLyConstants.RESTAURANT_ID, restaurantID)
            .get()
            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if(task.isSuccessful()){
                        for(DocumentSnapshot document : task.getResult()){
                            if("1".equals(document.get(QuanLyConstants.TABLE_ORDER_ID).toString())){
                                String tableNum = document.get(QuanLyConstants.TABLE_NUMBER).toString();
                                listTable.add(tableNum);
                            }
                        }
                        Collections.sort(listTable, new Comparator<String>() {
                            @Override
                            public int compare(String o1, String o2) {
                                return o1.compareTo(o2);
                            }
                        });

                        if(flag){
                            for(int i = 0; i < listTable.size();i++){
                                if(GlobalVariable.tableChoose.equals(listTable.get(i))){
                                    posTable = i;
                                }
                            }
                        }

                        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(CartActivity.this,
                                R.layout.spinner_item_text,listTable);

                        spinnerAdapter.setDropDownViewResource(R.layout.spinner_item_text);
                        spinnerTable.setAdapter(spinnerAdapter);

                        if(flag){
                            spinnerTable.setSelection(posTable);
                        }

                        spinnerTable.setOnItemSelectedListener(CartActivity.this);

                    }
                }
            });
    }

    /*
     * @author: ManhLD
     * @purpose: Get the restaurantID of the restaurant from SharedPreferences
     * */
    public String getRestaurantID(){
        String langPref = QuanLyConstants.RESTAURANT_ID;
        SharedPreferences prefs = getSharedPreferences(QuanLyConstants.SHARED_PERFERENCE, Activity.MODE_PRIVATE);
        return prefs.getString(langPref,"");
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        GlobalVariable.tableChoose = listTable.get(position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onClick(View v) {

    }

    @OnClick(R.id.food_choose_button_order)
    public void clickButtonOrder(){
        GlobalVariable.tableCusID = "";
        GlobalVariable.tableChoose = "";

    }
}
