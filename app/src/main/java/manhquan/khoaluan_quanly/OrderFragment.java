package manhquan.khoaluan_quanly;


import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import adapter.CookFoodAdapter;
import butterknife.BindView;
import butterknife.ButterKnife;
import constants.QuanLyConstants;
import model.CookFood;
import model.FoodInside;
import util.GlobalVariable;


/**
 * A simple {@link Fragment} subclass.
 */
public class OrderFragment extends Fragment implements View.OnClickListener {


    private View view;
    @BindView(R.id.order_fragment_recyclerView)
    public RecyclerView recyclerView;
    @BindView(R.id.order_fragment_submit)
    public FloatingActionButton buttonSubmit;
    private List<CookFood> listData = new ArrayList<>();
    private List<CookFood> listSendToWaiter = new ArrayList<>();
    private CookFoodAdapter adapter;
    private FirebaseFirestore db;
    private String restaurantID;
    private MaterialDialog dialogLoading;
    private static final String TAG = "OrderFragment";
    private boolean isFirst = true;

    public OrderFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_order, container, false);

        ButterKnife.bind(this,view);
        db = FirebaseFirestore.getInstance();
        restaurantID = getRestaurantID();
        renderData();

        buttonSubmit.setOnClickListener(this);

        return view;
    }

    private void renderData() {
        showLoadingDialog();
        db.collection(QuanLyConstants.COOK)
            .document(restaurantID)
            .collection(QuanLyConstants.TABLE)
            .orderBy(QuanLyConstants.ORDER_TIME, Query.Direction.ASCENDING)
            .get()
            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if(task.isSuccessful()){
                        for(DocumentSnapshot document : task.getResult()){
                            String[] content = document.get(QuanLyConstants.FOOD_NAME).toString().split(";");
                            List<FoodInside> listFoodName = new ArrayList<>();
                            if(!content[0].equals("")){
                                for (int i = 0; i < content.length; i++) {
                                    listFoodName.add(new FoodInside(content[i]));
                                }
                            }
                            String time = document.get(QuanLyConstants.ORDER_TIME).toString();
                            String employeeID = document.get(QuanLyConstants.TABLE_EMPLOYEE_ID).toString();
                            onChangeListener(document.getId());
                            if(TextUtils.isEmpty(time)){
                                time = "99:99";
                            }
                            String title = getResources().getString(R.string.table, document.get(QuanLyConstants.TABLE_NUMBER));
                            CookFood cookFood = new CookFood(title,listFoodName);
                            cookFood.setTime(time);
                            cookFood.setEmployeeID(employeeID);
                            listData.add(cookFood);
                        }

                        LinearLayoutManager layoutManager = new LinearLayoutManager(view.getContext());
                        adapter = new CookFoodAdapter(listData);
                        recyclerView.setLayoutManager(layoutManager);
                        recyclerView.setAdapter(adapter);

                        isFirst = false;
                        closeLoadingDialog();

                    }
                }
            });

    }

    private void onChangeListener(String docID) {

        final DocumentReference docRef = db.collection(QuanLyConstants.COOK)
                                            .document(restaurantID)
                                            .collection(QuanLyConstants.TABLE)
                                            .document(docID);
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot snapshot, FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                String source = snapshot != null && snapshot.getMetadata().hasPendingWrites()
                        ? "Local" : "Server";

                if (snapshot != null && snapshot.exists()) {
                    Map<String, Object> order = snapshot.getData();
                    if(!TextUtils.isEmpty(order.get(QuanLyConstants.FOOD_NAME).toString())){
                        String[] content = snapshot.get(QuanLyConstants.FOOD_NAME).toString().split(";");
                        List<FoodInside> listFoodName = new ArrayList<>();
                        if(!content[0].equals("")){
                            for (int i = 0; i < content.length; i++) {
                                listFoodName.add(new FoodInside(content[i]));
                            }
                        }
                        String time = order.get(QuanLyConstants.ORDER_TIME).toString();
                        String employeeID = order.get(QuanLyConstants.TABLE_EMPLOYEE_ID).toString();
                        String title = view.getContext().getResources().getString(R.string.table, order.get(QuanLyConstants.TABLE_NUMBER));
                        for(CookFood cf : listData){
                            if(cf.getTitle().equals(title)){
                                listData.remove(cf);
                                break;
                            }
                        }

                        CookFood cookFood = new CookFood(title,listFoodName);
                        cookFood.setTime(time);
                        cookFood.setEmployeeID(employeeID);
                        listData.add(cookFood);

                        Collections.sort(listData, new Comparator<CookFood>() {
                            @Override
                            public int compare(CookFood o1, CookFood o2) {
                                if(o1.getTime().compareTo(o2.getTime())==0){
                                    return 1;
                                }
                                return o1.getTime().compareTo(o2.getTime());
                            }
                        });

                        recyclerView.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                        adapter.onGroupCollapsed(0, 1000);

                        // Play ringtone when new food arrive -- Start

                        if(!isFirst){
                            try {
                                Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                                Ringtone r = RingtoneManager.getRingtone(view.getContext(), notification);
                                r.play();
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                        // ------------------------------------- End
                    }
                } else {
                    Log.d(TAG, source + " data: null");
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

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(id == R.id.order_fragment_submit){
            doActionSubmit();
        }
    }

    private void doActionSubmit() {
        listSendToWaiter.clear();
        for(int i = 0; i < adapter.getGroups().size(); i++){
            CookFood cf = (CookFood)adapter.getGroups().get(i);
            if(cf.getItemCount()>0){
                List<FoodInside> listTemp = new ArrayList<>();
                int kill = 0;
                int numItem = cf.getItemCount();
                // cf.getItemCount() will reduce after remove item
                for(int j = 0; j < numItem; j++){
                    if(cf.isChildChecked(j)){
                        FoodInside fi = (FoodInside)listData.get(i).getItems().get(kill);
                        listTemp.add(fi);
                        adapter.getGroups().get(i).getItems().remove(kill);
                    }else{
                        kill++;
                    }
                }
                if(listTemp.size()>0) {
                    CookFood cf2 = new CookFood(cf.getTitle(), listTemp);
                    cf2.setTime(cf.getTime());
                    cf2.setEmployeeID(cf.getEmployeeID());
                    listSendToWaiter.add(cf2);
                }
            }
        }

        updateDataToServer();
        Collections.sort(listData, new Comparator<CookFood>() {
            @Override
            public int compare(CookFood o1, CookFood o2) {
                if(o1.getTime().compareTo(o2.getTime())==0){
                    return 1;
                }
                return o1.getTime().compareTo(o2.getTime());
            }
        });
        adapter.clearChoices();
        adapter.notifyDataSetChanged();
    }

    private void updateDataToServer() {
        // Remove the food have been cooked and prepare bring to the customer -- Start
        db.collection(QuanLyConstants.COOK)
            .document(restaurantID)
            .collection(QuanLyConstants.TABLE)
            .orderBy(QuanLyConstants.ORDER_TIME, Query.Direction.ASCENDING)
            .get()
            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if(task.isSuccessful()){
                        int i = 0;
                        for(DocumentSnapshot document : task.getResult()){
                            Map<String, Object> cook = new HashMap<>();
                            cook.put(QuanLyConstants.FOOD_NAME, getFoodName(i));
                            if(adapter.getGroups().get(i).getItems().size()==0){
                                cook.put(QuanLyConstants.ORDER_TIME, "99:99");
                            }
                            DocumentReference docRef = document.getReference();
                            docRef.set(cook, SetOptions.merge());
                            i++;
                        }
                    }
                }
            });
        // ---------------------------------------------------------- END

        // Send the notification to the Waiter -- Start

        for(CookFood cf : listSendToWaiter){
            Map<String, Object> notification = new HashMap<>();
            notification.put(QuanLyConstants.ORDER_TIME, cf.getTime());
            notification.put(QuanLyConstants.TABLE_NUMBER, cf.getTitle());
            notification.put(QuanLyConstants.FOOD_NAME, getFoodNameSend(cf));

            final String employeeID = cf.getEmployeeID();
            final Map<String, Object> finalNoti = notification;
            db.collection(QuanLyConstants.NOTIFICATION)
                    .document(cf.getEmployeeID())
                    .collection(QuanLyConstants.TABLE)
                    .whereEqualTo(QuanLyConstants.TABLE_NUMBER,cf.getTitle())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if(task.isSuccessful()){
                                for(DocumentSnapshot document : task.getResult()){
                                    if(document.exists()){
                                        String[] curentFoodName = document.get(QuanLyConstants.FOOD_NAME).toString().split(";");
                                        String[] updFoodName = finalNoti.get(QuanLyConstants.FOOD_NAME).toString().split(";");
                                        ArrayList<String> listFoodUpdate = new ArrayList<>();
                                        listFoodUpdate.addAll(Arrays.asList(updFoodName));
                                        for(int i = 0; i < curentFoodName.length; i++){
                                            String[] curFoodName = curentFoodName[i].split(": ");
                                            for(int j = 0; j < listFoodUpdate.size() ; j++){
                                                String[] upFoodName = listFoodUpdate.get(j).split(": ");
                                                if(curFoodName[0].equals(upFoodName[0])){
                                                    int curQuantity = Integer.parseInt(curFoodName[1]);
                                                    int updQuantity = Integer.parseInt(upFoodName[1]);
                                                    curentFoodName[i] = curFoodName[0] + ": " + (curQuantity + updQuantity);
                                                    listFoodUpdate.remove(j);
                                                }
                                            }
                                        }
                                        StringBuilder foodSend = new StringBuilder();
                                        for(String s : curentFoodName){
                                            foodSend.append(s);
                                            foodSend.append(";");
                                        }
                                        for(String s : listFoodUpdate){
                                            foodSend.append(s);
                                            foodSend.append(";");
                                        }
                                        document.getReference()
                                                .update(QuanLyConstants.FOOD_NAME,foodSend.toString());
                                    }
                                }
                                //Use when the collection Notification of employee does not have table
                                if(task.getResult().isEmpty()){
                                    db.collection(QuanLyConstants.NOTIFICATION)
                                            .document(employeeID)
                                            .collection(QuanLyConstants.TABLE)
                                            .add(finalNoti);
                                }
                            }
                        }
                    });

        }

        // -------------------------------------- END
    }

    private String getFoodNameSend(CookFood cf) {
        StringBuilder builder = new StringBuilder();
        for(FoodInside fi : (List<FoodInside>)cf.getItems()){
            builder.append(fi.getContent());
            builder.append(";");
        }
        return builder.toString();
    }

    private String getFoodName(int i) {
        StringBuilder builder = new StringBuilder();
        for(int j = 0; j < adapter.getGroups().get(i).getItems().size(); j++){
            FoodInside fi = (FoodInside)adapter.getGroups().get(i).getItems().get(j);
            builder.append(fi.getContent());
            builder.append(";");
        }
        return builder.toString();
    }

    public void showLoadingDialog(){
        dialogLoading = new MaterialDialog.Builder(view.getContext())
                .backgroundColor(getResources().getColor(R.color.primary_dark))
                .customView(R.layout.loading_dialog,true)
                .show();
    }

    public void closeLoadingDialog(){
        dialogLoading.dismiss();
    }
}
