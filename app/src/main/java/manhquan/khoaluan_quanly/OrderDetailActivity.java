package manhquan.khoaluan_quanly;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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
    @BindView(R.id.order_detail_dateBook)
    public TextView txtDateBook;
    @BindView(R.id.order_detail_number)
    public TextView txtBillNumber;
    private ArrayList<FoodOnBill> listData;
    private ListFoodOnBillAdapter listFoodOnBillAdapter;
    private MaterialDialog dialogLoading;
    private FirebaseFirestore db;
    private String tableNumber;
    private String tableID;
    private String saveOrderID;
    private String TAG = "OrderDetailActivity";
    private String restaurantID;
    private String employeeID;
    private int flag_cook = -1;
    private int flag_waiter = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        ButterKnife.bind(this);
        db = FirebaseFirestore.getInstance();

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        restaurantID = getRestaurantID();
        tableNumber = getIntent().getStringExtra(QuanLyConstants.TABLE_NUMBER);
        if(TextUtils.isEmpty(tableNumber)) {
            // Open from BillFragment
            saveOrderID = getIntent().getStringExtra(QuanLyConstants.TABLE_ORDER_ID);
            processCheckOrder();
            displayOrderDetail();
        }
        else{
            // Open from RestaurantFragment
            renderData();
        }

        listData = new ArrayList<>();



        LayoutInflater layoutInflater = getLayoutInflater();
        ViewGroup myHeader = (ViewGroup)layoutInflater.inflate(R.layout.food_on_bill_header,listViewFoodOnBill,false);
        listViewFoodOnBill.addHeaderView(myHeader,null,false);

        listFoodOnBillAdapter = new ListFoodOnBillAdapter(this, listData);
        listViewFoodOnBill.setAdapter(listFoodOnBillAdapter);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home: this.onBackPressed();
                                    break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void processCheckOrder() {
        buttonCheckOut.setVisibility(View.GONE);
        txtTable.setVisibility(View.GONE);
    }

    private void displayOrderDetail() {
        showLoadingDialog();
        db.collection(QuanLyConstants.ORDER)
            .document(saveOrderID)
            .get()
            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot document) {
                    txtTable.setText(getResources().getString(R.string.table,
                            tableNumber));
                    txtTime.setText(getResources().getString(R.string.timeBook,
                            document.get(QuanLyConstants.ORDER_TIME)));

                    // Cast from yyyy/MM/dd to dd/MM/yyyy -- Start
                    String[] temp = document.get(QuanLyConstants.ORDER_DATE).toString().split("/");
                    StringBuilder builder = new StringBuilder();
                    builder.append(temp[2]);
                    builder.append("/");
                    builder.append(temp[1]);
                    builder.append("/");
                    builder.append(temp[0]);
                    // ------------------------------------- End

                    txtDateBook.setText(getResources().getString(R.string.dateBook, builder.toString()));
                    txtBillNumber.setText(getResources().getString(R.string.billNumber,document.get(QuanLyConstants.BILL_NUMBER).toString()));
                    String customerID = document.get(QuanLyConstants.CUSTOMER_ID).toString();
                    Log.i(TAG,document.getId());
                    if(!"1".equals(customerID)){
                        renderCusName(customerID);
                    }else{
                        txtCusName.setText(getResources().getString(R.string.customerName,
                                getResources().getString(R.string.customerNoName)));
                    }
                    renderListFood(saveOrderID);
                }
            });
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
        showLoadingDialog();
        db.collection(QuanLyConstants.TABLE)
            .whereEqualTo(QuanLyConstants.RESTAURANT_ID,restaurantID)
            .get()
            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if(task.isSuccessful()){
                        for(DocumentSnapshot document : task.getResult()){
                            if(document.get(QuanLyConstants.TABLE_NUMBER).toString().equals(tableNumber)){
                                tableID = document.getId();
                                renderCusInfo(document.get(QuanLyConstants.TABLE_ORDER_ID).toString());
                            }
                        }
                    }
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.i(TAG,e.getMessage());
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

                                    // Cast from yyyy/MM/dd to dd/MM/yyyy -- Start
                                    String[] temp = document.get(QuanLyConstants.ORDER_DATE).toString().split("/");
                                    StringBuilder builder = new StringBuilder();
                                    builder.append(temp[2]);
                                    builder.append("/");
                                    builder.append(temp[1]);
                                    builder.append("/");
                                    builder.append(temp[0]);
                                    // ------------------------------------- End

                                    employeeID = document.get(QuanLyConstants.TABLE_EMPLOYEE_ID).toString();
                                    txtDateBook.setText(getResources().getString(R.string.dateBook, builder.toString()));
                                    String customerID = document.get(QuanLyConstants.CUSTOMER_ID).toString();
                                    txtBillNumber.setText(getResources().getString(R.string.billNumber,document.get(QuanLyConstants.BILL_NUMBER).toString()));
                                    saveOrderID = orderID;
                                    if(!"1".equals(customerID)) {
                                        renderCusName(customerID);
                                    }else{
                                        txtCusName.setText(getResources().getString(R.string.customerName,
                                                getResources().getString(R.string.customerNoName)));
                                    }
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
                            listViewFoodOnBill.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                                    final FoodOnBill fob = listData.get(position-1);
                                    new MaterialDialog.Builder(OrderDetailActivity.this)
                                            .positiveText(getResources().getString(R.string.main_agree))
                                            .negativeText(getResources().getString(R.string.main_disagree))
                                            .positiveColor(getResources().getColor(R.color.primary_dark))
                                            .negativeColor(getResources().getColor(R.color.black))
                                            .content(getResources().getString(R.string.dialogRemoveFood, fob.getFoodName()))
                                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                                @Override
                                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                    int amountFood = fob.getQuantity() * fob.getPrice();
                                                    int cashTotal = Integer.parseInt(MoneyFormatter.backToString(txtTotalCost.getText().toString()));
                                                    txtTotalCost.setText(getResources().getString(R.string.totalCost,
                                                            MoneyFormatter.formatToMoney(cashTotal-amountFood+"") + " VNĐ"));
                                                    listData.remove(position-1);
                                                    listFoodOnBillAdapter.notifyDataSetChanged();
                                                    View view = getViewByPosition(position-1,listViewFoodOnBill);
                                                    view.setBackgroundColor(getResources().getColor(R.color.white));
                                                }
                                            })
                                            .build()
                                            .show();
                                }
                            });
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
                    validServiceFood();
                }
            })
            .build()
            .show();
    }

    private void validServiceFood() {
        db.collection(QuanLyConstants.COOK)
            .document(restaurantID)
            .collection(QuanLyConstants.TABLE)
            .document(tableID)
            .get()
            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){
                        DocumentSnapshot document = task.getResult();
                        String foodRemain = document.get(QuanLyConstants.FOOD_NAME).toString();
                        if(!TextUtils.isEmpty(foodRemain)){
                            flag_cook = 0; // meaning still have food does not service
                            highlightFoodNotCook(foodRemain.toString().split(";"));
                            Toast.makeText(OrderDetailActivity.this, getResources().getString(R.string.error_still_food_need_service), Toast.LENGTH_SHORT).show();
                        }
                        else{
                            flag_cook = 1; // Everything good
                            doCheckOutDone();
                        }
                    }
                }
            });


    }

    private void highlightFoodNotCook(String[] foodName) {
        for (String s : foodName) {
            String compare = s.split("    ")[0];
            for (int j = 0; j < listData.size(); j++) {
                if (compare.equals(listData.get(j).getFoodName())) {
                    View view = getViewByPosition(j, listViewFoodOnBill);
                    view.setBackgroundColor(getResources().getColor(R.color.primary));
                }
            }
        }
    }


    private View getViewByPosition(int pos, ListView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;
        pos++; // because listview have header
        if (pos < firstListItemPosition || pos > lastListItemPosition ) {
            return listView.getAdapter().getView(pos, null, listView);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }


    private void doCheckOutDone() {
        Map<String, Object> table = new HashMap<>();
        table.put(QuanLyConstants.TABLE_ORDER_ID,"1");
        db.collection(QuanLyConstants.TABLE)
                .document(tableID)
                .set(table, SetOptions.merge());
        Map<String, Object> order = new HashMap<>();
        order.put(QuanLyConstants.ORDER_CheckOut,true);
        order.put(QuanLyConstants.ORDER_CASH_TOTAL, MoneyFormatter.backToString(txtTotalCost.getText().toString()));
        DocumentReference docRef = db.collection(QuanLyConstants.ORDER).document(saveOrderID);
        docRef.set(order, SetOptions.merge());
        docRef.collection(QuanLyConstants.FOOD_ON_ORDER)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for(DocumentSnapshot document : task.getResult()){
                                String foodName = document.get(QuanLyConstants.FOOD_NAME).toString();
                                boolean flag = false;
                                for(FoodOnBill fob : listData){
                                    if(fob.getFoodName().equals(foodName)){
                                        flag = true;
                                        listData.remove(fob);
                                        break;
                                    }
                                }
                                if(!flag){
                                    document.getReference().delete();
                                }
                            }
                        }
                    }
                });
        Toast.makeText(getApplicationContext(),getResources().getString(R.string.checkOutDone),Toast.LENGTH_SHORT).show();
        onBackPressed();
    }

    public String getRestaurantID(){
        String langPref = QuanLyConstants.RESTAURANT_ID;
        SharedPreferences prefs = getSharedPreferences(QuanLyConstants.SHARED_PERFERENCE, Activity.MODE_PRIVATE);
        return prefs.getString(langPref,"");
    }

}
