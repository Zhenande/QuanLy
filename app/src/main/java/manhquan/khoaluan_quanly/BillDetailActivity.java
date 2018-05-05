package manhquan.khoaluan_quanly;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
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
import android.widget.EditText;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import constants.QuanLyConstants;
import adapter.ListFoodOnBillAdapter;
import model.FoodInside;
import model.FoodOnBill;
import util.MoneyFormatter;

public class BillDetailActivity extends AppCompatActivity {

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
    private String TAG = "BillDetailActivity";
    private String restaurantID;
    private String waiterID;
    private String waiterName;
    private boolean flagRemoveItem = true;
    private boolean isFirstInit = true;
    private ArrayList<Integer> listPositionFoodNotCook = new ArrayList<>();


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
            flagRemoveItem = true;
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

                    txtTotalCost.setText(getResources().getString(R.string.totalCost,document.get(QuanLyConstants.ORDER_CASH_TOTAL).toString()));
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

                                // Get the food does not cook, to prevent remove food had been serviced
                                validServiceFood();
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

                                    waiterID = document.get(QuanLyConstants.TABLE_EMPLOYEE_ID).toString();
                                    waiterName = document.get(QuanLyConstants.ORDER_WAITER_NAME).toString();
                                    txtTotalCost.setText(getResources().getString(R.string.totalCost,document.get(QuanLyConstants.ORDER_CASH_TOTAL).toString()));
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
                            for(DocumentSnapshot docFood : task.getResult()){
                                String name = docFood.get(QuanLyConstants.FOOD_NAME).toString();
                                String Str_price = docFood.get(QuanLyConstants.FOOD_PRICE).toString();
                                int price = Integer.parseInt(Str_price);
                                int quantity = Integer.parseInt(docFood.get(QuanLyConstants.FOOD_QUANTITY).toString());
                                FoodOnBill fob = new FoodOnBill();
                                fob.setFoodName(name);
                                fob.setPrice(price);
                                fob.setQuantity(quantity);
                                listData.add(fob);
                            }
                            closeLoadingDialog();

                            if(flagRemoveItem){
                                listViewFoodOnBill.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> parent,final View view, final int position, long id) {
                                        final FoodOnBill fob = listData.get(position-1);
                                        if(fob.getQuantity() == 1){
                                            new MaterialDialog.Builder(BillDetailActivity.this)
                                                    .positiveText(getResources().getString(R.string.main_agree))
                                                    .negativeText(getResources().getString(R.string.main_disagree))
                                                    .positiveColor(getResources().getColor(R.color.primary_dark))
                                                    .negativeColor(getResources().getColor(R.color.black))
                                                    .content(getResources().getString(R.string.dialogRemoveFood, fob.getFoodName()))
                                                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                                                        @Override
                                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                            if(listPositionFoodNotCook.contains(position)) {
                                                                //meaning food want to remove doesn't cooked
                                                                removeFoodSingleQuantity(fob, view);
                                                            }
                                                            else{
                                                                //meaing food have been cooked. If the food was cooked, you can't remove it from bill
                                                                Toast.makeText(BillDetailActivity.this, getResources().getString(R.string.error_food_cooked), Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    })
                                                    .build()
                                                    .show();
                                        }
                                        else{
                                            final MaterialDialog dialog = new MaterialDialog.Builder(BillDetailActivity.this)
                                                    .positiveText(getResources().getString(R.string.main_agree))
                                                    .negativeText(getResources().getString(R.string.main_disagree))
                                                    .positiveColor(getResources().getColor(R.color.primary_dark))
                                                    .negativeColor(getResources().getColor(R.color.black))
                                                    .customView(R.layout.dialog_remove_food_list, true)
                                                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                                                        @Override
                                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                            View cusView = dialog.getCustomView();
                                                            EditText edNumberInput = cusView.findViewById(R.id.edQuantityFoodRemove);
                                                            int numberInput = Integer.parseInt(edNumberInput.getText().toString());
                                                            if(listPositionFoodNotCook.contains(position)){
                                                                Toast.makeText(BillDetailActivity.this, getResources().getString(R.string.error_food_cooked), Toast.LENGTH_SHORT).show();
                                                            }
                                                            else {
                                                                if(numberInput <= fob.getQuantity()){
                                                                    if(numberInput == fob.getQuantity()){
                                                                        listData.remove(fob);
                                                                    }
                                                                    else{
                                                                        // fob is the final variable, so i can not change the value in fob
                                                                        listData.get(position-1).setQuantity(fob.getQuantity()-numberInput);
                                                                        View viewTemp = listFoodOnBillAdapter.getView(position-1,null,listViewFoodOnBill);
                                                                        TextView txtTotal = viewTemp.findViewById(R.id.food_on_bill_list_total_price);
                                                                        txtTotal.setText(MoneyFormatter.formatToMoney((fob.getQuantity()-numberInput)*fob.getPrice()+""));
                                                                    }
                                                                    listFoodOnBillAdapter.notifyDataSetChanged();
                                                                    removeFoodMultiQuantity(fob, numberInput);
                                                                }else{
                                                                    Toast.makeText(BillDetailActivity.this, getResources().getString(R.string.remove_too_much_food), Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        }
                                                    })
                                                    .build();
                                            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                                                @Override
                                                public void onShow(DialogInterface dialog2) {
                                                    View cusView = dialog.getCustomView();
                                                    TextView txtNameFoodRemove = cusView.findViewById(R.id.txtRemoveMultiQuantity);
                                                    txtNameFoodRemove.setText(getResources().getString(R.string.RemoveMultiQuantity,fob.getFoodName()));
                                                }
                                            });
                                            dialog.show();
                                        }
                                    }
                                });
                            }
                            listFoodOnBillAdapter.notifyDataSetChanged();
                        }
                    }
                });
    }

    private void removeFoodMultiQuantity(final FoodOnBill fob, final int numberFoodRemove) {
        final String foodNameRemove = fob.getFoodName();
        db.collection(QuanLyConstants.ORDER)
            .document(saveOrderID)
            .get()
            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){
                        final DocumentSnapshot documentOrder = task.getResult();
                        documentOrder.getReference()
                                .collection(QuanLyConstants.FOOD_ON_ORDER)
                                .whereEqualTo(QuanLyConstants.FOOD_NAME, foodNameRemove)
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if(task.isSuccessful()){
                                            for(DocumentSnapshot document : task.getResult()){
                                                // Update the quantity of the food
                                                int curQuantity = Integer.parseInt(document.get(QuanLyConstants.FOOD_QUANTITY).toString());
                                                int nowQuantity = curQuantity - numberFoodRemove;
                                                if(nowQuantity == 0){
                                                    document.getReference().delete();
                                                }
                                                else{
                                                    document.getReference()
                                                            .update(QuanLyConstants.FOOD_QUANTITY,nowQuantity+"");
                                                }
                                                // End

                                                // Update the Total Cost of Order
                                                int curTotalCost = MoneyFormatter.backToNumber(documentOrder.get(QuanLyConstants.ORDER_CASH_TOTAL).toString());
                                                int nowTotalCost = curTotalCost - fob.getPrice() * numberFoodRemove;
                                                String displayMoney = MoneyFormatter.formatToMoney(nowTotalCost+"") + " VNĐ";
                                                documentOrder.getReference()
                                                        .update(QuanLyConstants.ORDER_CASH_TOTAL, displayMoney);
                                                // End

                                                // Remove food haven't cook
                                                removeFoodOfCook(fob.getFoodName(),numberFoodRemove);

                                                txtTotalCost.setText(getResources().getString(R.string.totalCost,displayMoney));
                                                Toast.makeText(BillDetailActivity.this, getResources().getString(R.string.string_done), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e(TAG, e.getMessage());
                                    }
                                });
                    }
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG,e.getMessage());
                }
            });
    }

    /*
    * @author: ManhLD
    * Remove food in list have 1 quantity
    * */
    private void removeFoodSingleQuantity(FoodOnBill fob, View view) {
        int amountFood = fob.getQuantity() * fob.getPrice();
        int cashTotal = MoneyFormatter.backToNumber(txtTotalCost.getText().toString());
        txtTotalCost.setText(getResources().getString(R.string.totalCost,
                MoneyFormatter.formatToMoney(cashTotal - amountFood + "") + " VNĐ"));
        listData.remove(fob);
        listFoodOnBillAdapter.notifyDataSetChanged();
        view.setBackgroundColor(getResources().getColor(R.color.white));
        removeFoodOfCook(fob.getFoodName(), 1);

    }

    private void removeFoodOfCook(final String foodName,final int quantity){
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
                            String[] content = document.get(QuanLyConstants.FOOD_NAME).toString().split(";");
                            ArrayList<String> listFoodOfCook = new ArrayList<>();
                            listFoodOfCook.addAll(Arrays.asList(content));
                            StringBuilder newFoodQuantity = new StringBuilder();
                            for (int i = 0; i < listFoodOfCook.size(); i++){
                                String[] data = listFoodOfCook.get(i).split(" {4}SL: ");
                                String name = data[0];
                                int foodQuantity = Integer.parseInt(data[1]);
                                if(foodName.equals(name)){
                                    if(foodQuantity == quantity){
                                        listFoodOfCook.remove(i);
                                    }
                                    else{
                                        //meaning does not delete all of quantity of that food
                                        int newQuantity = foodQuantity - quantity;
                                        String newFood = data[0] + "    SL: " + newQuantity;
                                        //remove the old food in the arraylist
                                        listFoodOfCook.remove(i);
                                        //add new food quantity in the arraylist with index of the old food
                                        listFoodOfCook.add(i,newFood);
                                        newFoodQuantity.append(newFood);
                                        newFoodQuantity.append(";");
                                    }
                                }
                                else{
                                    newFoodQuantity.append(listFoodOfCook.get(i));
                                    newFoodQuantity.append(";");
                                }
                            }
                            document.getReference().update(QuanLyConstants.FOOD_NAME, newFoodQuantity.toString());
                            Toast.makeText(BillDetailActivity.this, getResources().getString(R.string.string_done), Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, e.getMessage());
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
                            // meaning still have food does not service
                            highlightFoodNotCook(foodRemain.split(";"));
                            if(!isFirstInit) {
                                Toast.makeText(BillDetailActivity.this, getResources().getString(R.string.error_still_food_need_service), Toast.LENGTH_SHORT).show();
                            }
                            else{
                                isFirstInit = false;
                            }
                        }
                        else{
                            // Everything good
                            if(!isFirstInit) {
                                doCheckOutDone();
                            }
                            else{
                                isFirstInit = false;
                            }
                        }
                    }
                }
            });
    }

    private void highlightFoodNotCook(String[] foodName) {
        for (String s : foodName) {
            String compare = s.split(" {4}")[0];
            for (int j = 0; j < listData.size(); j++) {
                if (compare.equals(listData.get(j).getFoodName())) {
                    if(isFirstInit){
                        listPositionFoodNotCook.add(j);
                    }
                    else{
                        // fill row with primary color
                        View view = getViewByPosition(j, listViewFoodOnBill);
                        view.setBackgroundColor(getResources().getColor(R.color.blueLight));
                    }
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
        // Release table by set table order id = 1
        db.collection(QuanLyConstants.TABLE)
                .document(tableID)
                .set(table, SetOptions.merge());
        // End

        // Set checkout of order is true
        Map<String, Object> order = new HashMap<>();
        order.put(QuanLyConstants.ORDER_CheckOut,true);
        // Check again the result update
        order.put(QuanLyConstants.ORDER_CASH_TOTAL, txtTotalCost.getText().toString().split("Total: ")[0]);
        DocumentReference docRef = db.collection(QuanLyConstants.ORDER).document(saveOrderID);
        docRef.set(order, SetOptions.merge());
        // End

        // Remove the table of the waiter
        db.collection(QuanLyConstants.NOTIFICATION)
            .document(waiterID)
            .collection(QuanLyConstants.TABLE)
            .document(tableID)
            .delete();
        // End

        // Change the status in the list of cook
        db.collection(QuanLyConstants.COOK)
            .document(restaurantID)
            .collection(QuanLyConstants.TABLE)
            .document(tableID)
            .get()
            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){
                        Map<String, Object> update = new HashMap<>();
                        update.put(QuanLyConstants.TABLE_EMPLOYEE_ID,"");
                        update.put(QuanLyConstants.ORDER_TIME,"99:99");
                        task.getResult().getReference().set(update, SetOptions.merge());
                    }
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, e.getMessage());
                }
            });
        // End


        Toast.makeText(getApplicationContext(),getResources().getString(R.string.checkOutDone),Toast.LENGTH_SHORT).show();
        onBackPressed();
    }

    public String getRestaurantID(){
        String langPref = QuanLyConstants.RESTAURANT_ID;
        SharedPreferences prefs = getSharedPreferences(QuanLyConstants.SHARED_PERFERENCE, Activity.MODE_PRIVATE);
        return prefs.getString(langPref,"");
    }

}
