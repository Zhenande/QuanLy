package fragment;


import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;


import java.util.ArrayList;

import adapter.FoodPagerAdapter;
import butterknife.BindView;
import butterknife.ButterKnife;
import constants.QuanLyConstants;
import manhquan.khoaluan_quanly.CartActivity;
import manhquan.khoaluan_quanly.FoodDetailActivity;
import manhquan.khoaluan_quanly.R;
import model.Food;
import model.FoodOnBill;


/**
 * A simple {@link Fragment} subclass.
 */
public class FoodFragment extends Fragment implements TabLayout.OnTabSelectedListener {


    @BindView(R.id.food_button_add)
    public FloatingActionButton buttonCreate;
    @BindView(R.id.food_fragment_viewPager)
    public ViewPager viewPager;
    @BindView(R.id.food_fragment_tablayout)
    public TabLayout tabLayout;
    private FirebaseFirestore db;
    private static final String TAG = "FoodFragment";
    private String restaurantID;
    private ArrayList<String> listFoodType = new ArrayList<>();
    private MaterialDialog dialogLoading;
    private View view;
    private FoodPagerAdapter adapter;
    public volatile static ArrayList<FoodOnBill> listFoodChoose = new ArrayList<>();
    private int positionEm;
    private boolean flag = false;


    public FoodFragment() {
        // Required empty public constructor
    }

    public synchronized static void addFoodChoose(FoodOnBill food){
        boolean flagAdd = true;
        for(FoodOnBill fob : listFoodChoose){
            if(fob.getFoodName().equals(food.getFoodName())){
                fob.setQuantity(fob.getQuantity()+food.getQuantity());
                flagAdd = false;
                break;
            }
        }
        if(flagAdd){
            listFoodChoose.add(food);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_food, container, false);

        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        ButterKnife.bind(this,view);
        db = FirebaseFirestore.getInstance();
        restaurantID = getRestaurantID();
        showLoadingDialog();
        GetListFoodType();

        positionEm = getPosition();
        if(positionEm!=1){
            buttonCreate.setImageResource(R.drawable.icon_cart);
            flag = true;
        }

        buttonCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!flag){
                    Intent i = new Intent(getActivity().getApplicationContext(), FoodDetailActivity.class);
                    startActivityForResult(i, QuanLyConstants.FOOD_DETAIL);
                }
                else{
                    if(listFoodChoose.size()>0){
                        Intent i = new Intent(getActivity().getApplicationContext(), CartActivity.class);
                        i.putExtra(QuanLyConstants.INTENT_FOOD_CHOOSE_CART, listFoodChoose);
                        startActivityForResult(i, QuanLyConstants.INTENT_CART_ACTIVITY);
                    }
                    else{
                        Toast.makeText(v.getContext(),getResources().getString(R.string.food_frag_error_no_food),Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });


        return view;
    }

    /*
    * @author: ManhLD
    * Defines the number of tabs by setting appropriate fragment and tab name.
    * */
    private void setupViewPager(ViewPager viewPager) {
        adapter = new FoodPagerAdapter(getChildFragmentManager(),view.getContext());
        for(String name : listFoodType){
            adapter.addFragment(new MenuFragment(),name);
        }
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager,true);
        tabLayout.addOnTabSelectedListener(FoodFragment.this);
    }

    private void GetListFoodType() {
        db.collection(QuanLyConstants.RESTAURANT)
                .document(restaurantID)
                .collection(QuanLyConstants.RESTAURANT_FOOD_TYPE)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for(DocumentSnapshot document : task.getResult()){
                                listFoodType.add(document.get(QuanLyConstants.FOOD_TYPE_NAME).toString());
                            }
                            setupViewPager(viewPager);
                            closeLoadingDialog();
                            Log.i(TAG, listFoodType.size()+"");
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
        SharedPreferences prefs = getActivity().getSharedPreferences(QuanLyConstants.SHARED_PERFERENCE, Activity.MODE_PRIVATE);
        return prefs.getString(langPref,"");
    }

    public void showLoadingDialog(){
        dialogLoading = new MaterialDialog.Builder(view.getContext())
                .customView(R.layout.loading_dialog,true)
                .show();
    }

    public void closeLoadingDialog(){
        dialogLoading.dismiss();
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK){
            if(requestCode == QuanLyConstants.FOOD_DETAIL){
                adapter.notifyDataSetChanged();
            }
        }
    }

    public int getPosition(){
        String langPref = QuanLyConstants.SHARED_POSITION;
        SharedPreferences prefs = view.getContext().getSharedPreferences(QuanLyConstants.SHARED_PERFERENCE, Activity.MODE_PRIVATE);
        return prefs.getInt(langPref,0);
    }


}
