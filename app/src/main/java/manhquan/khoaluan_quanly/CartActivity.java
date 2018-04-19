package manhquan.khoaluan_quanly;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
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
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.ServerTimestamp;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import adapter.FoodChooseListAdapter;
import butterknife.BindView;
import butterknife.ButterKnife;
import constants.QuanLyConstants;
import model.FoodOnBill;
import util.GlobalVariable;
import util.MoneyFormatter;

public class CartActivity extends AppCompatActivity implements  AdapterView.OnItemSelectedListener, View.OnClickListener, FoodChooseListAdapter.CallBackFood {

    private static final String TAG = "CartActivity";
    public ArrayList<FoodOnBill> listFoodChoose;
    @BindView(R.id.food_choose_list_view)
    public ListView listViewFood;
    @BindView(R.id.cart_spinner_tableNumber)
    public Spinner spinnerTable;
    @BindView(R.id.cart_cus_id)
    public EditText edCusID;
    @BindView(R.id.food_choose_button_order)
    public Button buttonOrder;
    @BindView(R.id.cart_button_checkID)
    public Button buttonCheckID;
    public  FoodChooseListAdapter listViewAdapter;
    private ArrayList<String> listTable = new ArrayList<>();
    private FirebaseFirestore db;
    private int posTable;
    private boolean flag = false;
    private final long DELAY = 1500;
    @SuppressLint("SimpleDateFormat")
    private SimpleDateFormat sdf_Date = new SimpleDateFormat("dd/MM/yyyy");
    @SuppressLint("SimpleDateFormat")
    private SimpleDateFormat sdf_Time = new SimpleDateFormat("HH:mm");
    private String restaurantID;
    private MaterialDialog dialogLoading;
    private StringBuilder nameFoodSendToCook = new StringBuilder();
    private String time;
    private List<String> listFullTable = new ArrayList<>();
    @ServerTimestamp
    private Date serverTime;


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

        getTableAvailable();

        listFoodChoose = (ArrayList<FoodOnBill>)getIntent().getSerializableExtra(QuanLyConstants.INTENT_FOOD_CHOOSE_CART);

        listViewAdapter = new FoodChooseListAdapter(this,listFoodChoose,this);
        ViewGroup header = (ViewGroup)getLayoutInflater().inflate(R.layout.food_choose_list_header,listViewFood,false);
        listViewFood.addHeaderView(header);

        listViewFood.setAdapter(listViewAdapter);
        buttonOrder.setOnClickListener(this);
        buttonCheckID.setOnClickListener(this);

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

    @SuppressLint("SimpleDateFormat")
    private String getBillNumberOrder() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf_short_date = new SimpleDateFormat("ddMMkkmmss");
        return sdf_short_date.format(cal.getTime());
    }

    /*
    * @author: ManhLD
    * It won't get table available, because customer can order several times. Not only one times.
    * */
    private void getTableAvailable(){
        restaurantID = getRestaurantID();
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
                            listFullTable.add(document.get(QuanLyConstants.TABLE_NUMBER).toString());
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
                                R.layout.spinner_item_text,listFullTable);

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
        GlobalVariable.tableChoose = listFullTable.get(position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(id == R.id.food_choose_button_order){
            if(checkTableAvailable()) {
                // meaning table is free
                clickButtonOrder();
            }else{
                // meaning table is having customer
                callExtraFood();
            }
        }
        else if(id == R.id.cart_button_checkID){
            checkID();
        }
    }

    private void callExtraFood() {
        // Reset data of 2 global variable
        GlobalVariable.tableCusID = "";
        GlobalVariable.tableChoose = "";

        showLoadingDialog();

        db.collection(QuanLyConstants.TABLE)
            .whereEqualTo(QuanLyConstants.TABLE_NUMBER, spinnerTable.getSelectedItem().toString())
            .get()
            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if(task.isSuccessful()){
                        for(DocumentSnapshot document : task.getResult()){
                            String orderID = document.get(QuanLyConstants.TABLE_ORDER_ID).toString();
                            updateOrderInfor(orderID);
                        }
                    }
                }
            });
    }

    private void updateOrderInfor(String orderID) {
        db.collection(QuanLyConstants.ORDER)
            .document(orderID)
            .get()
            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){
                        final DocumentReference document = task.getResult().getReference();
                        document.collection(QuanLyConstants.FOOD_ON_ORDER)
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if(task.isSuccessful()){
                                            for(DocumentSnapshot document : task.getResult()){
                                                FoodOnBill fob = new FoodOnBill();
                                                fob.setFoodId(document.getId());
                                                fob.setFoodName(document.get(QuanLyConstants.FOOD_NAME).toString());
                                                fob.setQuantity(Integer.parseInt(document.get(QuanLyConstants.FOOD_QUANTITY).toString()));

                                                for(FoodOnBill fob2 : listFoodChoose){
                                                    if(fob2.getFoodName().equals(fob.getFoodName())){
                                                        fob.setQuantity(fob.getQuantity() + fob2.getQuantity());
                                                        DocumentReference docRef = document.getReference();
                                                        docRef.update(QuanLyConstants.FOOD_QUANTITY, fob.getQuantity());
                                                        nameFoodSendToCook.append(fob.getFoodName());
                                                        nameFoodSendToCook.append(";");
                                                        listFoodChoose.remove(fob2);
                                                        break;
                                                    }
                                                }
                                                if(listFoodChoose.size()==0){
                                                    break;
                                                }
                                            }
                                            if(listFoodChoose.size()>0){
                                                for(FoodOnBill fob : listFoodChoose){
                                                    Map<String, Object> food = new HashMap<>();
                                                    food.put(QuanLyConstants.FOOD_NAME, fob.getFoodName());
                                                    food.put(QuanLyConstants.FOOD_PRICE, fob.getPrice()+"");
                                                    food.put(QuanLyConstants.FOOD_QUANTITY, fob.getQuantity()+"");
                                                    nameFoodSendToCook.append(fob.getFoodName());
                                                    nameFoodSendToCook.append(";");
                                                    document.collection(QuanLyConstants.FOOD_ON_ORDER)
                                                            .document(fob.getFoodId())
                                                            .set(food);
                                                }
                                            }

                                            closeLoadingDialog();
                                            Toast.makeText(CartActivity.this, "Order Success", Toast.LENGTH_SHORT).show();
                                            onBackPressed();
                                        }
                                    }
                                });
                    }
                }
            });
    }

    private boolean checkTableAvailable() {
        boolean returnD = false;
        for(int i = 0; i < listTable.size(); i++){
            if(listTable.contains(spinnerTable.getSelectedItem().toString())){
                returnD = true;
            }
        }
        return returnD;
    }

    private void checkID() {
        if(TextUtils.isEmpty(edCusID.getText().toString())){
            edCusID.setError(getResources().getString(R.string.required));
            edCusID.requestFocus();
        }
        else{
            db.collection(QuanLyConstants.CUSTOMER)
                    .whereEqualTo(QuanLyConstants.CUS_CONTACT,edCusID.getText().toString())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if(task.isSuccessful()){
                                for(DocumentSnapshot document : task.getResult()){
                                    if(document.exists()){
                                        edCusID.setEnabled(false);
                                        buttonCheckID.setEnabled(false);
                                        Toast.makeText(CartActivity.this, "Done", Toast.LENGTH_SHORT).show();
                                    }else{
                                        Toast.makeText(CartActivity.this, "Can not find the Customer have phone number: "
                                                   + edCusID.getText().toString(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        }
                    });
        }
    }

    /*
    * @authot: ManhLD
    * Process order food
    * */
    public void clickButtonOrder(){
        // Reset data of 2 global variable
        GlobalVariable.tableCusID = "";
        GlobalVariable.tableChoose = "";

        showLoadingDialog();
        // First, Get the CustomerID through the PhoneNumber
        // Because the PhoneNumber is Unique

        if(TextUtils.isEmpty(edCusID.getText().toString())){
            new MaterialDialog.Builder(this)
                    .positiveText(getResources().getString(R.string.main_agree))
                    .negativeText(getResources().getString(R.string.main_disagree))
                    .positiveColor(getResources().getColor(R.color.primary_dark))
                    .negativeColor(getResources().getColor(R.color.black))
                    .content(getResources().getString(R.string.cart_dialog_warning_no_cusID))
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            Calendar cal = Calendar.getInstance();
                            Map<String, Object> order = new HashMap<>();
                            order.put(QuanLyConstants.ORDER_CASH_TOTAL, getCashTotal());
                            order.put(QuanLyConstants.ORDER_CheckOut,false);
                            order.put(QuanLyConstants.CUSTOMER_ID, "1");
                            order.put(QuanLyConstants.ORDER_DATE, sdf_Date.format(cal.getTime()));
                            order.put(QuanLyConstants.ORDER_TIME, sdf_Time.format(cal.getTime()));
                            order.put(QuanLyConstants.RESTAURANT_ID, restaurantID);
                            order.put(QuanLyConstants.BILL_NUMBER, getBillNumberOrder());
                            order.put(QuanLyConstants.TABLE_EMPLOYEE_ID, GlobalVariable.employeeID);
                            createOrderDetail(order);
                            time = sdf_Time.format(cal.getTime());
                        }
                    })
                    .build()
                    .show();
        }
        else{
            db.collection(QuanLyConstants.CUSTOMER)
                    .whereEqualTo(QuanLyConstants.CUS_CONTACT, edCusID.getText().toString())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if(task.isSuccessful()){
                                for(DocumentSnapshot document : task.getResult()){
                                    Calendar cal = Calendar.getInstance();
                                    Map<String, Object> order = new HashMap<>();
                                    order.put(QuanLyConstants.ORDER_CASH_TOTAL, getCashTotal());
                                    order.put(QuanLyConstants.ORDER_CheckOut,false);
                                    order.put(QuanLyConstants.CUSTOMER_ID, document.getId());
                                    order.put(QuanLyConstants.ORDER_DATE, sdf_Date.format(cal.getTime()));
                                    order.put(QuanLyConstants.ORDER_TIME, sdf_Time.format(cal.getTime()));
                                    order.put(QuanLyConstants.RESTAURANT_ID, restaurantID);
                                    order.put(QuanLyConstants.BILL_NUMBER, getBillNumberOrder());
                                    order.put(QuanLyConstants.TABLE_EMPLOYEE_ID, GlobalVariable.employeeID);
                                    createOrderDetail(order);
                                    time = sdf_Time.format(cal.getTime());
                                }
                            }
                        }
                    });
        }
    }


    /*
    * @author: ManhLD
    * Add the food choose into subcollection FoodOnOrder in collection Order
    * */
    private void createOrderDetail(Map<String, Object> order) {
        db.collection(QuanLyConstants.ORDER)
            .add(order)
            .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                @Override
                public void onComplete(@NonNull Task<DocumentReference> task) {
                    DocumentReference document = task.getResult();
                    for(FoodOnBill fob : listFoodChoose){
                        Map<String, Object> food = new HashMap<>();
                        food.put(QuanLyConstants.FOOD_NAME, fob.getFoodName());
                        food.put(QuanLyConstants.FOOD_PRICE, fob.getPrice()+"");
                        food.put(QuanLyConstants.FOOD_QUANTITY, fob.getQuantity()+"");
                        nameFoodSendToCook.append(fob.getFoodName());
                        nameFoodSendToCook.append(";");
                        document.collection(QuanLyConstants.FOOD_ON_ORDER)
                                .document(fob.getFoodId())
                                .set(food);
                    }
                    // document.getID = OrderID need to be set on table
                    takingTable(document.getId());
                    Toast.makeText(CartActivity.this, "Order Success", Toast.LENGTH_SHORT).show();
                }
            });
    }

    /*
    * @author: ManhLD
    * Set the value OrderID of table. So this table will not be available
    * And the icon in restaurant
    * */
    private void takingTable(String orderID) {
        final Map<String, Object> table = new HashMap<>();
        table.put(QuanLyConstants.TABLE_ORDER_ID, orderID);
        db.collection(QuanLyConstants.TABLE)
            .whereEqualTo(QuanLyConstants.RESTAURANT_ID,restaurantID)
            .get()
            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if(task.isSuccessful()){
                        for(DocumentSnapshot document : task.getResult()){
                            String tableNumber = document.get(QuanLyConstants.TABLE_NUMBER).toString();
                            if(tableNumber.equals(spinnerTable.getSelectedItem().toString())) {
                                DocumentReference docRef = db.collection(QuanLyConstants.TABLE)
                                        .document(document.getId());
                                docRef.set(table, SetOptions.merge());
                                sendFoodToCook(document.getId());
                                break;
                            }
                        }
                    }
                }
            });

    }

    private void sendFoodToCook(String tableID) {
        Map<String, Object> cook = new HashMap<>();
        cook.put(QuanLyConstants.FOOD_NAME, nameFoodSendToCook.toString());
        cook.put(QuanLyConstants.ORDER_TIME, time);
        cook.put(QuanLyConstants.TABLE_EMPLOYEE_ID, GlobalVariable.employeeID);
        db.collection(QuanLyConstants.COOK)
            .document(restaurantID)
            .collection(QuanLyConstants.TABLE)
            .document(tableID)
            .update(cook)
            .addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    closeLoadingDialog();
                    onBackPressed();
                }
            });
        listFoodChoose.clear();
    }


    private String getCashTotal() {
        int cashTotal = 0;
        for(FoodOnBill fob : listFoodChoose){
            cashTotal += (fob.getPrice() * fob.getQuantity());
        }
        return MoneyFormatter.formatToMoney(cashTotal+"") + " VNĐ";
    }

    public void showLoadingDialog(){
        dialogLoading = new MaterialDialog.Builder(this)
                .backgroundColor(getResources().getColor(R.color.primary_dark))
                .customView(R.layout.loading_dialog,true)
                .show();
    }

    public void closeLoadingDialog(){
        dialogLoading.dismiss();
    }

    @Override
    public void onFoodRemove(int position) {
        listFoodChoose.remove(position);
        listViewAdapter.notifyDataSetChanged();
    }

    @Override
    public void finish() {
        Intent dataBack = new Intent();
        dataBack.putExtra(QuanLyConstants.INTENT_FOOD_CHOOSE_CART,listFoodChoose);
        this.setResult(Activity.RESULT_OK,dataBack);
        super.finish();
    }
}
