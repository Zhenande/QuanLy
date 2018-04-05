package fragment;


import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import adapter.GridFoodAdapter;
import butterknife.BindView;
import butterknife.ButterKnife;
import constants.QuanLyConstants;
import manhquan.khoaluan_quanly.FoodDetailActivity;
import manhquan.khoaluan_quanly.R;
import model.Food;


/**
 * A simple {@link Fragment} subclass.
 */
public class FoodFragment extends Fragment {


    @BindView(R.id.list_view_food)
    public ListView listView;
    private ArrayList<Food> listData = new ArrayList<>();
    private GridFoodAdapter listFoodAdapter;
    @BindView(R.id.food_button_add)
    public FloatingActionButton buttonCreate;
    private FirebaseFirestore db;
    private static final String TAG = "FoodFragment";


    public FoodFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_food, container, false);

        ButterKnife.bind(this,view);
        db = FirebaseFirestore.getInstance();
        renderData();

        listFoodAdapter = new GridFoodAdapter(view.getContext(),3);
        listFoodAdapter.addItemsInGrid(listData);
        listView.setAdapter(listFoodAdapter);
        listFoodAdapter.notifyDataSetChanged();

        buttonCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity().getApplicationContext(), FoodDetailActivity.class);
                startActivityForResult(i, QuanLyConstants.FOOD_DETAIL);
            }
        });

        return view;
    }

    private void onChangeListener(String docID) {

        final DocumentReference docRef = db.collection("food").document(docID);
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot snapshot, FirebaseFirestoreException e) {
                if(e!= null){
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                String source = snapshot != null && snapshot.getMetadata().hasPendingWrites()
                        ? "Local" : "Server";

                if (snapshot != null && snapshot.exists()) {
                    Map<String, Object> food = snapshot.getData();
                    String foodName = food.get(QuanLyConstants.FOOD_NAME).toString();
                    for(int i = 0; i < listData.size();i++){
                        if(listData.get(i).getFoodName().equals(foodName)){
                            listData.get(i).setPrice(Integer.parseInt(food.get(QuanLyConstants.FOOD_PRICE).toString()));
                            break;
                        }
                    }
                    listFoodAdapter.notifyDataSetChanged();
                    Log.d(TAG, source + " data: " + snapshot.getData());
                } else {
                    Log.d(TAG, source + " data: null");
                }
            }
        });
    }

    /*
    * @author: ManhLD
    * @purpose: Get the collection of the food had in the restaurant.
    * */
    private void renderData() {
        listData.clear();
        db.collection("food")
                .whereEqualTo(QuanLyConstants.RESTAURANT_ID,getRestaurantID())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for(DocumentSnapshot document : task.getResult()){
                                Food food = new Food();
                                food.setDescription(document.get(QuanLyConstants.FOOD_DESCRIPTION).toString());
                                food.setFoodType(document.get(QuanLyConstants.FOOD_TYPE).toString());
                                food.setImageResource(document.get(QuanLyConstants.FOOD_IMAGE_NAME).toString());
                                food.setFoodName(document.get(QuanLyConstants.FOOD_NAME).toString());
                                food.setPrice(Integer.parseInt(document.get(QuanLyConstants.FOOD_PRICE).toString()));
                                listData.add(food);
                                onChangeListener(document.getId());
                            }
                            listFoodAdapter.addItemsInGrid(listData);
                            listView.setAdapter(listFoodAdapter);
                            listFoodAdapter.notifyDataSetChanged();
                        }
                    }
                });

    }

    /*
    * @author: ManhLD
    * @purpose: Get the restaurantID of the restaurant from SharedPreferences
    * */
    public String getRestaurantID(){
        String langPref = "restaurantID";
        SharedPreferences prefs = getActivity().getSharedPreferences("CommonPrefs", Activity.MODE_PRIVATE);
        return prefs.getString(langPref,"");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == Activity.RESULT_OK && requestCode == QuanLyConstants.FOOD_DETAIL){
            boolean flag = data.getBooleanExtra(QuanLyConstants.INTENT_FOOD_DETAIL_FLAG,false);
            if(flag){
                renderData();
            }
        }
    }
}
