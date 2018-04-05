package manhquan.khoaluan_quanly;

import android.app.Activity;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import constants.QuanLyConstants;
import adapter.ListFoodOnBillAdapter;
import model.FoodOnBill;

public class OrderDetailActivity extends AppCompatActivity {

    @BindView(R.id.bill_detail_button_check_out)
    public Button buttonCheckOut;
    @BindView(R.id.bill_detail_list_food)
    public ListView listFoodOnBill;
    @BindView(R.id.bill_detail_customerName)
    public TextView txtCusName;
    @BindView(R.id.bill_detail_table)
    public TextView txtTable;
    @BindView(R.id.bill_detail_timeBook)
    public TextView txtTime;
    @BindView(R.id.bill_detail_totalCost)
    public TextView txtTotalCost;
    private ArrayList<FoodOnBill> listData;
    private ListFoodOnBillAdapter listFoodOnBillAdapter;
    private MaterialDialog dialogLoading;
    private FirebaseFirestore db;
    private String tableNumber;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill_detail);
        ButterKnife.bind(this);

        tableNumber = getIntent().getStringExtra("TableNumber");
        Toast.makeText(getApplicationContext(),"Hoa don cua ban " + tableNumber,Toast.LENGTH_SHORT).show();
        db = FirebaseFirestore.getInstance();
        showLoadingDialog();
        renderData();

        listData = GenerateRawData();
        listFoodOnBillAdapter = new ListFoodOnBillAdapter(this, listData);
        listFoodOnBill.setAdapter(listFoodOnBillAdapter);

    }

    private ArrayList<FoodOnBill> GenerateRawData() {
        ArrayList<FoodOnBill> list = new ArrayList<>();
        list.add(new FoodOnBill("F01","Gà",10000,10));
        list.add(new FoodOnBill("F02","Bò",20000,10));
        list.add(new FoodOnBill("F03","Cá",30000,10));
        list.add(new FoodOnBill("F04","Voi",40000,10));
        list.add(new FoodOnBill("F05","Hổ",50000,10));
        list.add(new FoodOnBill("F06","Chim",60000,10));
        list.add(new FoodOnBill("F07","Bó tay",70000,10));
        list.add(new FoodOnBill("F08","Gì",80000,10));
        list.add(new FoodOnBill("F09","Ờ",90000,10));
        return list;
    }

    public void showLoadingDialog(){
        dialogLoading = new MaterialDialog.Builder(this)
                .customView(R.layout.loading_dialog,true)
                .show();
    }

    public void closeLoadingDialog(){
        dialogLoading.dismiss();
    }

    public void renderData(){
        db.collection(QuanLyConstants.ORDER)
                .whereEqualTo(QuanLyConstants.RESTAURANT_ID,getRestaurantID())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for(DocumentSnapshot document : task.getResult()){
                                if(document.getId().equals("fd4uJUfnl6wxlqdIHCgM")){
                                    txtTable.setText(getResources().getString(R.string.table,
                                                tableNumber));
                                    txtTime.setText(getResources().getString(R.string.timeBook,
                                            document.get(QuanLyConstants.ORDER_TIME)));
                                    txtTotalCost.setText(getResources().getString(R.string.totalCost,
                                            document.get(QuanLyConstants.ORDER_CASH_TOTAL)));
                                    db.collection(QuanLyConstants.CUSTOMER)
                                            .document(document.getId())
                                            .get()
                                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                    if (task.isSuccessful()){
                                                        String cusName = task.getResult().get(QuanLyConstants.CUS_NAME).toString();
                                                        txtCusName.setText(getResources().getString(R.string.customerName,cusName));
                                                        closeLoadingDialog();
                                                    }
                                                }
                                            });
                                }
                            }
                        }
                    }
                });

    }

    public String getRestaurantID(){
        String langPref = QuanLyConstants.RESTAURANT_ID;
        SharedPreferences prefs = getSharedPreferences(QuanLyConstants.SHARED_PERFERENCE, Activity.MODE_PRIVATE);
        return prefs.getString(langPref,"");
    }

}
