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
import android.view.MenuItem;
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
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import adapter.FoodChooseListAdapter;
import butterknife.BindView;
import butterknife.ButterKnife;
import constants.QuanLyConstants;
import fragment.FoodFragment;
import model.FoodOnBill;
import util.GlobalVariable;
import util.MoneyFormatter;

import static util.GlobalVariable.closeLoadingDialog;
import static util.GlobalVariable.showLoadingDialog;

public class CartActivity extends AppCompatActivity implements  AdapterView.OnItemSelectedListener, View.OnClickListener, FoodChooseListAdapter.CallBackFood {

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
    private SimpleDateFormat sdf_Date = new SimpleDateFormat("yyyy/MM/dd");
    @SuppressLint("SimpleDateFormat")
    private SimpleDateFormat sdf_Time = new SimpleDateFormat("HH:mm");
    private String restaurantID;
    private StringBuilder nameFoodSendToCook = new StringBuilder();
    private String time;
    private List<String> listFullTable = new ArrayList<>();
    private boolean isCallExtraFood = false;
    private int cashTotal;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        ButterKnife.bind(this);
        db = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);

        if(!TextUtils.isEmpty(GlobalVariable.tableCusID)){
            edCusID.setText(GlobalVariable.tableCusID);
        }

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home: this.onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("SimpleDateFormat")
    private String getBillNumberOrder() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf_short_date = new SimpleDateFormat("yyMMddkkmmss");
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
                        Collections.sort(listFullTable, new Comparator<String>() {
                            @Override
                            public int compare(String o1, String o2) {
                                int table1 = Integer.parseInt(o1);
                                int table2 = Integer.parseInt(o2);
                                return Integer.compare(table1,table2);
                            }
                        });

                        // Set table have choose before
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
                            if(!listTable.contains(listFullTable.get(posTable))){
                                edCusID.setEnabled(false);
                            }
                        }
                        else{
                            if(!listTable.contains(listFullTable.get(0))){
                                edCusID.setEnabled(false);
                            }
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
        if(!listTable.contains(listFullTable.get(position))){
            edCusID.setEnabled(false);
        }else{
            edCusID.setEnabled(true);
        }
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
                isCallExtraFood = true;
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

        showLoadingDialog(this);

        db.collection(QuanLyConstants.TABLE)
            .whereEqualTo(QuanLyConstants.RESTAURANT_ID, restaurantID)
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

    private void updateOrderInfor(final String orderID) {
        db.collection(QuanLyConstants.ORDER)
            .document(orderID)
            .get()
            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){
//                        Calendar cal = Calendar.getInstance();
//                        time = sdf_Time.format(cal.getTime());
                        final DocumentReference document = task.getResult().getReference();
                        cashTotal = MoneyFormatter.backToNumber(task.getResult().get(QuanLyConstants.ORDER_CASH_TOTAL).toString());
                        document.collection(QuanLyConstants.FOOD_ON_ORDER)
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if(task.isSuccessful()){
                                            // Check food have in order, if the food exist in order
                                            // i will update the quantity of food
                                            for(DocumentSnapshot document : task.getResult()){
                                                FoodOnBill fob = new FoodOnBill();
                                                fob.setFoodId(document.getId());
                                                fob.setFoodName(document.get(QuanLyConstants.FOOD_NAME).toString());
                                                fob.setQuantity(Integer.parseInt(document.get(QuanLyConstants.FOOD_QUANTITY).toString()));
                                                fob.setPrice(Integer.parseInt(document.get(QuanLyConstants.FOOD_PRICE).toString()));

                                                // fob2 meaning new food need send to cook
                                                // Checking the food have on the list or not
                                                for(FoodOnBill fob2 : listFoodChoose){
                                                    if(fob2.getFoodName().equals(fob.getFoodName())){
                                                        fob.setQuantity(fob.getQuantity() + fob2.getQuantity());
                                                        DocumentReference docRef = document.getReference();
                                                        docRef.update(QuanLyConstants.FOOD_QUANTITY, fob.getQuantity());

                                                        nameFoodSendToCook.append(fob.getFoodName());
                                                        nameFoodSendToCook.append("    SL: ");
                                                        nameFoodSendToCook.append(fob2.getQuantity());
                                                        nameFoodSendToCook.append(";");

                                                        cashTotal += (fob.getPrice()*fob2.getQuantity());
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
                                                    nameFoodSendToCook.append("    SL: ");
                                                    nameFoodSendToCook.append(fob.getQuantity());
                                                    nameFoodSendToCook.append(";");

                                                    cashTotal += (fob.getPrice()*fob.getQuantity());
                                                    document.collection(QuanLyConstants.FOOD_ON_ORDER)
                                                            .document(fob.getFoodId())
                                                            .set(food);
                                                }
                                            }
                                            document.update(QuanLyConstants.ORDER_CASH_TOTAL, MoneyFormatter.formatToMoney(cashTotal) + " VNĐ");
                                            UpdateFoodSendToCook(orderID);
                                        }
                                    }
                                });
                    }
                }
            });
    }

    private void UpdateFoodSendToCook(String orderID) {
        db.collection(QuanLyConstants.TABLE)
            .whereEqualTo(QuanLyConstants.RESTAURANT_ID,restaurantID)
            .whereEqualTo(QuanLyConstants.TABLE_ORDER_ID, orderID)
            .get()
            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if(task.isSuccessful()){
                        for(DocumentSnapshot document : task.getResult()){
                            sendFoodToCook(document.getId());
                        }
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

        showLoadingDialog(this);
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
                            order.put(QuanLyConstants.ORDER_WAITER_NAME, GlobalVariable.employeeName);
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
                        nameFoodSendToCook.append("    SL: ");
                        nameFoodSendToCook.append(fob.getQuantity());
                        nameFoodSendToCook.append(";");
                        document.collection(QuanLyConstants.FOOD_ON_ORDER)
                                .document(fob.getFoodId())
                                .set(food);
                    }
                    // document.getID = OrderID need to be set on table
                    takingTable(document.getId());
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

    /*
    * @author: ManhLD
    * Send food need to prepare to the cook
    * */
    private void sendFoodToCook(String tableID) {
        final Map<String, Object> cook = new HashMap<>();
        if(!isCallExtraFood) {
            // If the customer call extra, we would not change the time of that table
            cook.put(QuanLyConstants.ORDER_TIME, time);
        }
        cook.put(QuanLyConstants.TABLE_EMPLOYEE_ID, GlobalVariable.employeeID);
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
                        DocumentReference docRef = document.getReference();
                        if(!TextUtils.isEmpty(document.get(QuanLyConstants.FOOD_NAME).toString())){
                            /// Get the currrent food and quantity of them that not done cooking
                            /// Check the customer call for another food that have pending on cooking
                            /// If have, we just increase quantity of them
                            String[] currentFood = document.get(QuanLyConstants.FOOD_NAME).toString().split(";");
                            String[] updateFood = nameFoodSendToCook.toString().split(";");
                            ArrayList<String> listUpdateFood = new ArrayList<>();
                            listUpdateFood.addAll(Arrays.asList(updateFood));
                            for(int i = 0; i < currentFood.length ; i++){
                                String[] curFoodName = currentFood[i].split(": ");
                                for(int j = 0 ; j < listUpdateFood.size() ; j++){
                                    String[] updFoodName = listUpdateFood.get(j).split(": ");
                                    if(curFoodName[0].equals(updFoodName[0])){
                                        int newQuantity = Integer.parseInt(curFoodName[1]) + Integer.parseInt(updFoodName[1]);
                                        currentFood[i] = updFoodName[0] + ": " + newQuantity;
                                        listUpdateFood.remove(j);
                                        break;
                                    }
                                }
                            }

                            StringBuilder foodSend = new StringBuilder();
                            for(String s : currentFood){
                                foodSend.append(s);
                                foodSend.append(";");
                            }
                            for(String s : listUpdateFood){
                                foodSend.append(s);
                                foodSend.append(";");
                            }

                            cook.put(QuanLyConstants.FOOD_NAME, foodSend.toString());
                            docRef.set(cook, SetOptions.merge());
                        }
                        else{
                            cook.put(QuanLyConstants.FOOD_NAME, nameFoodSendToCook.toString());
                            docRef.set(cook, SetOptions.merge());
                        }
                        FoodFragment fragment = (FoodFragment) getFragmentManager().findFragmentById(R.id.main_app_framelayout);
                        if(fragment != null){
                            fragment.refreshMenu();
                        }
                        closeLoadingDialog();
                        Toast.makeText(CartActivity.this, "Order Success", Toast.LENGTH_SHORT).show();
                        onBackPressed();
                    }
                }
            });
        listFoodChoose.clear();
    }


    private String getCashTotal() {
        int cashTotal = 0;
        for(FoodOnBill fob : listFoodChoose){
            cashTotal += (fob.getPrice() * fob.getQuantity());
        }
        return MoneyFormatter.formatToMoney(cashTotal) + " VNĐ";
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
