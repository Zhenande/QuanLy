package fragment;


import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

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
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("food")
                .whereEqualTo("RestaurantID",getRestaurantID())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for(DocumentSnapshot document : task.getResult()){
                                Food food = new Food();
                                food.setDescription(document.get("Description").toString());
                                food.setFoodType(document.get("Type").toString());
                                food.setImageResource(document.get("ImageName").toString());
                                food.setFoodName(document.get("Name").toString());
                                food.setPrice(Integer.parseInt(document.get("Price").toString()));
                                listData.add(food);
                            }
                            listFoodAdapter.addItemsInGrid(listData);
                            listView.setAdapter(listFoodAdapter);
                            listFoodAdapter.notifyDataSetChanged();
                        }
                    }
                });

    }

    public String getRestaurantID(){
        String langPref = "restaurantID";
        SharedPreferences prefs = getActivity().getSharedPreferences("CommonPrefs", Activity.MODE_PRIVATE);
        return prefs.getString(langPref,"");
    }

}
