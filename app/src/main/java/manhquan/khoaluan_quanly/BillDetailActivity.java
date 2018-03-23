package manhquan.khoaluan_quanly;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import constants.QuanLyConstants;
import adapter.GridFoodOnBillAdapter;
import model.FoodOnBill;

public class BillDetailActivity extends AppCompatActivity {

    private TextView txtCustomerName;
    private TextView txtTimeBook;
    private TextView txtTotalCost;
    private Button buttonCheckOut;
    private ListView listFoodOnBill;
    private ArrayList<FoodOnBill> listData;
    private GridFoodOnBillAdapter gridFoodOnBillAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill_detail);

        String tableNumber = getIntent().getStringExtra("TableNumber");
        Toast.makeText(getApplicationContext(),"Hoa don cua ban " + tableNumber,Toast.LENGTH_SHORT).show();

        txtCustomerName = findViewById(R.id.bill_detail_customerName);
        txtTimeBook = findViewById(R.id.bill_detail_timeBook);
        txtTotalCost = findViewById(R.id.bill_detail_totalCost);
        buttonCheckOut = findViewById(R.id.bill_detail_button_check_out);
        listFoodOnBill = findViewById(R.id.bill_detail_list_food);

        listData = GenerateRawData();
        gridFoodOnBillAdapter = new GridFoodOnBillAdapter(this, QuanLyConstants.MAX_CARDS_LIST_FOOD_ON_BILL);
        gridFoodOnBillAdapter.addItemsInGrid(listData);

        listFoodOnBill.setAdapter(gridFoodOnBillAdapter);
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


}
