package fragment;



import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import android.app.Fragment;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Map;

import adapter.MenuFoodListAdapter;
import butterknife.BindView;
import butterknife.ButterKnife;
import constants.QuanLyConstants;
import manhquan.khoaluan_quanly.FoodDetailActivity;
import manhquan.khoaluan_quanly.R;
import model.Food;
import model.FoodOnBill;
import util.GlideApp;
import util.MoneyFormatter;


/**
 * A simple {@link Fragment} subclass.
 */
public class MenuFragment extends Fragment implements AdapterView.OnItemClickListener {


    private static final String TAG = "MenuFragment";
    private View view;
    @BindView(R.id.list_view_food)
    public ListView listView;
    private ArrayList<Food> listData = new ArrayList<>();
    private MenuFoodListAdapter listFoodAdapter;
    private MaterialDialog dialogLoading;
    private String restaurantID;
    private FirebaseFirestore db;
    private String foodType;
    private MaterialDialog dialogChoose;
    private CallbackTmp call;

    public interface CallbackTmp {
        void onReturnData(FoodOnBill foodOnBill);
    }

    @SuppressLint("ValidFragment")
    public MenuFragment(CallbackTmp call) {
        this.call = call;
        // Required empty public constructor
    }

    public MenuFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_menu, container, false);

        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        ButterKnife.bind(this,view);
        db = FirebaseFirestore.getInstance();
        restaurantID = getRestaurantID();
        foodType = getArguments().getCharSequence(QuanLyConstants.FOOD_TYPE).toString();

        listFoodAdapter = new MenuFoodListAdapter(view.getContext(),listData);
        listView.setAdapter(listFoodAdapter);
        listView.setOnItemClickListener(this);

        renderData();

        return view;
    }

    private void onChangeListener(String docID) {

        final DocumentReference docRef = db.collection(QuanLyConstants.FOOD).document(docID);
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
    public void renderData() {
        listData.clear();
        showLoadingDialog();
        db.collection(QuanLyConstants.FOOD)
                .whereEqualTo(QuanLyConstants.RESTAURANT_ID,restaurantID)
                .whereEqualTo(QuanLyConstants.FOOD_TYPE, foodType)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for(DocumentSnapshot document : task.getResult()){
                                Food food = new Food();
                                food.setFoodId(document.getId());
                                food.setDescription(document.get(QuanLyConstants.FOOD_DESCRIPTION).toString());
                                food.setFoodType(document.get(QuanLyConstants.FOOD_TYPE).toString());
                                food.setImageResource(document.get(QuanLyConstants.FOOD_IMAGE_NAME).toString());
                                food.setFoodName(document.get(QuanLyConstants.FOOD_NAME).toString());
                                food.setPrice(Integer.parseInt(document.get(QuanLyConstants.FOOD_PRICE).toString()));
                                listData.add(food);
                                onChangeListener(document.getId());
                            }
                            listFoodAdapter.notifyDataSetChanged();
                            closeLoadingDialog();
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

    public int getPosition(){
        String langPref = QuanLyConstants.SHARED_POSITION;
        SharedPreferences prefs = view.getContext().getSharedPreferences(QuanLyConstants.SHARED_PERFERENCE, Activity.MODE_PRIVATE);
        return prefs.getInt(langPref,0);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final Food food = listData.get(position);
        int pos_res = getPosition();
        if(pos_res == 1 || pos_res == 2){
            Intent i = new Intent(this.view.getContext(), FoodDetailActivity.class);
            i.putExtra(QuanLyConstants.INTENT_FOOD_DETAIL_NAME,food.getFoodName());
            this.view.getContext().startActivity(i);
        }
        else{
            dialogChoose = new MaterialDialog.Builder(view.getContext())
                    .positiveText(getResources().getString(R.string.main_agree))
                    .negativeText(getResources().getString(R.string.main_disagree))
                    .positiveColor(getResources().getColor(R.color.primary_dark))
                    .negativeColor(getResources().getColor(R.color.black))
                    .customView(R.layout.choose_food_dialog,true)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            View view = dialog.getView();
                            EditText edQuantity = view.findViewById(R.id.choose_food_dialog_quantity);
                            FoodOnBill foodChoose = new FoodOnBill();
                            foodChoose.setFoodName(food.getFoodName());
                            foodChoose.setPrice(food.getPrice());
                            foodChoose.setFoodId(food.getFoodId());
                            foodChoose.setQuantity(Integer.parseInt(edQuantity.getText().toString()));

                            call.onReturnData(foodChoose);
                        }
                    })
                    .build();
            dialogChoose.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    clickOnItemListView(dialogChoose,food);
                }
            });
            dialogChoose.show();
        }
    }

    @SuppressLint("SetTextI18n")
    private void clickOnItemListView(MaterialDialog dialog, Food food) {
        View view = dialog.getView();
        ImageView imgFood = view.findViewById(R.id.choose_food_dialog_image);
        TextView txtFoodName = view.findViewById(R.id.choose_food_dialog_name);
        TextView txtFoodPrice = view.findViewById(R.id.choose_food_dialog_price);
        TextView txtFoodDescription = view.findViewById(R.id.choose_food_dialog_description);
        ImageButton buttonSub = view.findViewById(R.id.choose_food_dialog_sub);
        ImageButton buttonSum = view.findViewById(R.id.choose_food_dialog_sum);
        final EditText editText = view.findViewById(R.id.choose_food_dialog_quantity);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        StorageReference imageRef = storageReference.child(QuanLyConstants.FOOD_PATH_IMAGE + food.getImageResource());
        GlideApp.with(view.getContext())
                .load(imageRef)
                .into(imgFood);
        txtFoodPrice.setText(MoneyFormatter.formatToMoney(food.getPrice()+"") + " VNĐ");
        txtFoodName.setText(food.getFoodName());
        txtFoodDescription.setText(food.getDescription());
        editText.setText("1");
        buttonSub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int quantity = Integer.parseInt(editText.getText().toString());
                if(quantity > 1){
                    quantity--;
                    editText.setText(quantity+"");
                }
            }
        });
        buttonSum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int quantity = Integer.parseInt(editText.getText().toString())+1;
                editText.setText(quantity+"");
            }
        });
    }

}
