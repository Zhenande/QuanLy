package manhquan.khoaluan_quanly;

import android.app.Activity;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import constants.QuanLyConstants;
import adapter.ListFoodOnBillAdapter;
import model.FoodOnBill;
import util.MoneyFormatter;

public class OrderDetailActivity extends AppCompatActivity {

    @BindView(R.id.order_detail_button_check_out)
    public Button buttonCheckOut;
    @BindView(R.id.order_detail_list_food)
    public ListView listViewFoodOnBill;
    @BindView(R.id.order_detail_customerName)
    public TextView txtCusName;
    @BindView(R.id.order_detail_table)
    public TextView txtTable;
    @BindView(R.id.order_detail_timeBook)
    public TextView txtTime;
    @BindView(R.id.order_detail_totalCost)
    public TextView txtTotalCost;
    private ArrayList<FoodOnBill> listData;
    private ListFoodOnBillAdapter listFoodOnBillAdapter;
    private MaterialDialog dialogLoading;
    private FirebaseFirestore db;
    private String tableNumber;
    private String tableID;
    private String saveOrderID;
    private String TAG = "OrderDetailActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);
        ButterKnife.bind(this);

        tableNumber = getIntent().getStringExtra(QuanLyConstants.TABLE_NUMBER);

        db = FirebaseFirestore.getInstance();
        listData = new ArrayList<>();
        showLoadingDialog();
        renderData();

        LayoutInflater layoutInflater = getLayoutInflater();
        ViewGroup myHeader = (ViewGroup)layoutInflater.inflate(R.layout.food_on_bill_header,listViewFoodOnBill,false);
        listViewFoodOnBill.addHeaderView(myHeader,null,false);

        listFoodOnBillAdapter = new ListFoodOnBillAdapter(this, listData);
        listViewFoodOnBill.setAdapter(listFoodOnBillAdapter);

    }

    public void showLoadingDialog(){
        dialogLoading = new MaterialDialog.Builder(this)
                .customView(R.layout.loading_dialog,true)
                .show();
    }

    public void closeLoadingDialog(){
        dialogLoading.dismiss();
    }

    /*
     * @author: ManhLD
     * @param: tableID
     * First, the app will get the table id of the table was selected
     * Second, it will put that into this method to display the needed information
     * */
    public void renderData(){
        db.collection(QuanLyConstants.TABLE)
            .whereEqualTo(QuanLyConstants.RESTAURANT_ID,getRestaurantID())
            .get()
            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if(task.isSuccessful()){
                        for(DocumentSnapshot document : task.getResult()){
                            if(document.get(QuanLyConstants.TABLE_NUMBER).equals(tableNumber)){
                                tableID = document.getId();
                                renderCusInfo(document.get(QuanLyConstants.TABLE_ORDER_ID).toString());
                            }
                        }
                    }
                }
            });
    }

    /*
     * @author: ManhLD
     * @param: orderID
     * First, the app will get the table id of the table was selected
     * Second, it will put Order into this method to display the needed information
     * */
    private void renderCusInfo(final String orderID){
        db.collection(QuanLyConstants.ORDER)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for(DocumentSnapshot document : task.getResult()){
                                if(document.getId().equals(orderID)){
                                    txtTable.setText(getResources().getString(R.string.table,
                                            tableNumber));
                                    txtTime.setText(getResources().getString(R.string.timeBook,
                                            document.get(QuanLyConstants.ORDER_TIME)));
                                    String customerID = document.get(QuanLyConstants.CUSTOMER_ID).toString();
                                    Log.i(TAG,document.getId());
                                    saveOrderID = orderID;
                                    renderCusName(customerID);
                                    renderListFood(orderID);
                                }
                            }
                        }
                    }});
    }

    /*
    * @author: ManhLD
    * @param: customerID
    * Get Customer Name with CustomerID and display it on the UI
    * */
    private void renderCusName(String customerID) {
        db.collection(QuanLyConstants.CUSTOMER)
                .document(customerID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        String cusName = task.getResult().get(QuanLyConstants.CUS_NAME).toString();
                        txtCusName.setText(getResources().getString(R.string.customerName,cusName));
                    }});
    }


    /*
     * @author: ManhLD
     * @param: orderID
     * Get List Food with orderID and display it on the UI
     * This method call inside
     * */
    private void renderListFood(String orderID) {
        db.collection(QuanLyConstants.ORDER).document(orderID)
                .collection(QuanLyConstants.FOOD_ON_ORDER)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            int totalCost = 0;
                            for(DocumentSnapshot docFood : task.getResult()){
                                String name = docFood.get(QuanLyConstants.FOOD_NAME).toString();
                                String Str_price = docFood.get(QuanLyConstants.FOOD_PRICE).toString();
                                int price = Integer.parseInt(Str_price);
                                int quantity = Integer.parseInt(docFood.get(QuanLyConstants.FOOD_QUANTITY).toString());
                                FoodOnBill fob = new FoodOnBill();
                                fob.setFoodName(name);
                                fob.setPrice(price);
                                fob.setQuantity(quantity);
                                totalCost += (price * quantity);
                                listData.add(fob);
                            }
                            closeLoadingDialog();
                            txtTotalCost.setText(getResources().getString(R.string.totalCost,
                                    MoneyFormatter.formatToMoney(totalCost+"") + " VNĐ"));
                            listFoodOnBillAdapter.notifyDataSetChanged();
                        }
                    }
                });
    }

    @OnClick(R.id.order_detail_button_check_out)
    public void buttonCheckOutClick(){
        new MaterialDialog.Builder(this)
            .content(getResources().getString(R.string.dialogCheckOut))
            .positiveText(getResources().getString(R.string.main_agree))
            .negativeText(getResources().getString(R.string.main_disagree))
            .positiveColor(getResources().getColor(R.color.primary_dark))
            .negativeColor(getResources().getColor(R.color.black))
            .onPositive(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    Map<String, Object> table = new HashMap<>();
                    table.put(QuanLyConstants.TABLE_ORDER_ID,"1");
                    db.collection(QuanLyConstants.TABLE)
                            .document(tableID)
                            .set(table, SetOptions.merge());
                    Map<String, Object> order = new HashMap<>();
                    order.put(QuanLyConstants.ORDER_CheckOut,true);
                    db.collection(QuanLyConstants.ORDER)
                            .document(saveOrderID)
                            .set(order, SetOptions.merge());
                    Toast.makeText(getApplicationContext(),getResources().getString(R.string.checkOutDone),Toast.LENGTH_SHORT).show();
                    onBackPressed();
                }
            })
            .build()
            .show();
    }

    public String getRestaurantID(){
        String langPref = QuanLyConstants.RESTAURANT_ID;
        SharedPreferences prefs = getSharedPreferences(QuanLyConstants.SHARED_PERFERENCE, Activity.MODE_PRIVATE);
        return prefs.getString(langPref,"");
    }

}
