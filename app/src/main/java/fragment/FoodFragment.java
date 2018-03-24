package fragment;


import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;

import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;

import adapter.GridFoodAdapter;
import constants.QuanLyConstants;
import manhquan.khoaluan_quanly.FoodDetailActivity;
import manhquan.khoaluan_quanly.R;
import model.Food;


/**
 * A simple {@link Fragment} subclass.
 */
public class FoodFragment extends Fragment {


    private ListView listView;
    private ArrayList<Food> listData = new ArrayList<>();
    private GridFoodAdapter listFoodAdapter;
    private FloatingActionButton buttonCreate;

    public FoodFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_food, container, false);

        listView = view.findViewById(R.id.list_view_food);
        buttonCreate = view.findViewById(R.id.food_button_add);
        generateRawData();

        listFoodAdapter = new GridFoodAdapter(view.getContext(),3);
        listFoodAdapter.addItemsInGrid(listData);

        listView.setAdapter(listFoodAdapter);
        buttonCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity().getApplicationContext(), FoodDetailActivity.class);
                startActivityForResult(i, QuanLyConstants.CREATE_EMPLOYEE);
            }
        });

        return view;
    }

    private void generateRawData() {
        Food food1 = new Food("1","Bo",50000,"abc",R.drawable.beefsteak,"MonChinh");
        Food food2 = new Food("2","Ga",60000,"abcd",R.drawable.beefsteak, "MonPhu");
        Food food3 = new Food("3","Voi",70000,"abcde",R.drawable.beefsteak,"MonPhu");
        Food food4 = new Food("4","Kien",80000,"abcf",R.drawable.beefsteak,"MonPhu");
        Food food5 = new Food("5","Ngua",90000,"abcg",R.drawable.beefsteak,"MonPhu");
        Food food6 = new Food("6","Ca",100000,"abch",R.drawable.beefsteak,"MonPhu");
        Food food7 = new Food("7","Khung Long",110000,"abc",R.drawable.beefsteak,"MonPhu");
        listData.add(food1);
        listData.add(food2);
        listData.add(food3);
        listData.add(food4);
        listData.add(food5);
        listData.add(food6);
        listData.add(food7);
    }

}
