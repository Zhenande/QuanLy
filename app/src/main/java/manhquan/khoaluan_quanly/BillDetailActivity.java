package manhquan.khoaluan_quanly;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import constants.QuanLyConstants;
import adapter.ListFoodOnBillAdapter;
import model.FoodOnBill;

public class BillDetailActivity extends AppCompatActivity {

    @BindView(R.id.bill_detail_button_check_out)
    public Button buttonCheckOut;
    @BindView(R.id.bill_detail_list_food)
    public ListView listFoodOnBill;
    private ArrayList<FoodOnBill> listData;
    private ListFoodOnBillAdapter listFoodOnBillAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill_detail);
        ButterKnife.bind(this);

        String tableNumber = getIntent().getStringExtra("TableNumber");
        Toast.makeText(getApplicationContext(),"Hoa don cua ban " + tableNumber,Toast.LENGTH_SHORT).show();


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


}
