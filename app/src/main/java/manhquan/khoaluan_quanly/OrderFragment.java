package manhquan.khoaluan_quanly;


import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
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

import static util.GlobalVariable.closeLoadingDialog;
import static util.GlobalVariable.showLoadingDialog;


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
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);
        restaurantID = getRestaurantID();
        renderData();

        if(getEmployeePosition() == 3){
            // Meaning waiter
            buttonSubmit.setVisibility(View.GONE);
        }

        buttonSubmit.setOnClickListener(this);

        return view;
    }

    private void renderData() {
        showLoadingDialog(view.getContext());
        db.collection(QuanLyConstants.COOK)
            .document(restaurantID)
            .collection(QuanLyConstants.TABLE)
            .orderBy(QuanLyConstants.ORDER_TIME, Query.Direction.ASCENDING)
            .orderBy(QuanLyConstants.TABLE_NUMBER, Query.Direction.ASCENDING)
            .get()
            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if(task.isSuccessful()){
                        for(DocumentSnapshot document : task.getResult()){
                            String[] content = document.get(QuanLyConstants.FOOD_NAME).toString().split(";");
                            List<FoodInside> listFoodName = new ArrayList<>();
                            if(!content[0].equals("")){
                                for (String aContent : content) {
                                    listFoodName.add(new FoodInside(aContent));
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
                            if(!TextUtils.isEmpty(employeeID)){
                                // meaning table are not checkout
                                if(listFoodName.size() > 0){
                                    // meaing table has food need to cook
                                    cookFood.setStatus(2);
                                }
                                else{
                                    // meaning table does not have any food need to cook
                                    cookFood.setStatus(1);
                                }
                            }
                            else{
                                cookFood.setStatus(0);
                            }
                            listData.add(cookFood);
                        }

                        LinearLayoutManager layoutManager = new LinearLayoutManager(view.getContext());
                        adapter = new CookFoodAdapter(listData);
                        recyclerView.setLayoutManager(layoutManager);
                        recyclerView.setAdapter(adapter);

//                        adapter.setChildClickListener(new OnCheckChildClickListener() {
//                            @Override
//                            public void onCheckChildCLick(View v, boolean checked, CheckedExpandableGroup group, int childIndex) {
//                                if(checked){
//                                    // Add the quantity of food to the list and prepare send to waiter
//                                    doChildSelected(group, childIndex);
//                                }
//                                else{
//                                    // Remove the quantity of food out of the list
//                                }
//                            }
//                        });

                        isFirst = false;
                        closeLoadingDialog();
                    }
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.i(TAG, e.getMessage());
                }
            });

    }

    /*
    * @author: ManhLD
    * Too much thing need to be done
    * */
//    private void doChildSelected(final CheckedExpandableGroup group, final int childIndex) {
//        FoodInside fi = (FoodInside)group.getItems().get(childIndex);
//        String[] contentFood = fi.getContent().split(" {4}SL: ");
//        final String foodName = contentFood[0];
//        final int quantityFood = Integer.parseInt(contentFood[1]);
//
//        final MaterialDialog dialog = new MaterialDialog.Builder(view.getContext())
//                .positiveText(getResources().getString(R.string.main_agree))
//                .negativeText(getResources().getString(R.string.main_disagree))
//                .positiveColor(getResources().getColor(R.color.primary_dark))
//                .negativeColor(getResources().getColor(R.color.black))
//                .customView(R.layout.dialog_remove_food_list, true)
//                .onPositive(new MaterialDialog.SingleButtonCallback() {
//                    @Override
//                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
//                        View cusView = dialog.getCustomView();
//                        EditText edNumberInput = cusView.findViewById(R.id.edQuantityFoodRemove);
//                        int quantityInput = Integer.parseInt(edNumberInput.getText().toString());
//                        if(quantityFood >= quantityInput){
//                            String tableNumber = group.getTitle();
//                            int groupIndex = getGroupIndex(tableNumber);
//                            if(listSendToWaiter.size() > 0) {
//                                for (CookFood cf : listSendToWaiter) {
//                                    if (tableNumber.equals(cf.getTitle())) {
//                                        ArrayList<FoodInside> listCurFoodInTable = (ArrayList<FoodInside>)cf.getItems();
//                                        listCurFoodInTable.add(new FoodInside(foodName + "    SL: " + quantityInput));
//                                    }
//                                }
//                            }
//                            else{
//                                ArrayList<FoodInside> listFoodDoneInTable = new ArrayList<>();
//                                listFoodDoneInTable.add(new FoodInside(foodName + "    SL: " + quantityInput));
//                                CookFood cf = new CookFood(tableNumber,listFoodDoneInTable);
//                                listSendToWaiter.add(cf);
//                            }
//                        }
//                        else{
//                            Toast.makeText(view.getContext(), view.getContext().getResources().getString(R.string.order_frag_add_too_much_food,quantityFood), Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                })
//                .onNegative(new MaterialDialog.SingleButtonCallback() {
//                    @Override
//                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
//                        group.unCheckChild(childIndex);
//                        adapter.notifyDataSetChanged();
//                    }
//                })
//                .onNeutral(new MaterialDialog.SingleButtonCallback() {
//                    @Override
//                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
//                        group.unCheckChild(childIndex);
//                        adapter.notifyDataSetChanged();
//                    }
//                })
//                .build();
//        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//            @Override
//            public void onShow(DialogInterface dialog2) {
//                View cusView = dialog.getCustomView();
//                TextView txtNameFoodRemove = cusView.findViewById(R.id.txtRemoveMultiQuantity);
//                txtNameFoodRemove.setText(getResources().getString(R.string.RemoveMultiQuantity,foodName));
//            }
//        });
//        dialog.show();
//    }

//    private int getGroupIndex(String tableNumber) {
//
//    }

    private void onChangeListener(String tableID) {

        final DocumentReference docRef = db.collection(QuanLyConstants.COOK)
                                            .document(restaurantID)
                                            .collection(QuanLyConstants.TABLE)
                                            .document(tableID);
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot snapshot, FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

//                String source = snapshot != null && snapshot.getMetadata().hasPendingWrites()
//                        ? "Local" : "Server";

                if (snapshot != null && snapshot.exists()) {
                    Map<String, Object> order = snapshot.getData();
//                    if(!TextUtils.isEmpty(order.get(QuanLyConstants.FOOD_NAME).toString())){
                        // Get the new change of food name
                        String[] content = snapshot.get(QuanLyConstants.FOOD_NAME).toString().split(";");
                        List<FoodInside> listFoodName = new ArrayList<>();
                        if(!content[0].equals("")){
                            for (String aContent : content) {
                                listFoodName.add(new FoodInside(aContent));
                            }
                        }
                        // Get the new change of time, employeeID, title
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
                        if(!TextUtils.isEmpty(employeeID)){
                            // meaning table are not checkout
                            if(listFoodName.size() > 0){
                                // meaing table has food need to cook
                                cookFood.setStatus(2);
                            }
                            else{
                                // meaning table does not have any food need to cook
                                cookFood.setStatus(1);
                            }
                        }
                        else{
                            cookFood.setStatus(0);
                        }
                        listData.add(cookFood);

                        Collections.sort(listData, new Comparator<CookFood>() {
                            @Override
                            public int compare(CookFood o1, CookFood o2) {
                                if(o1.getTime().compareTo(o2.getTime())==0){
                                    // if time equal, we will sort follow table number
                                    return o1.getTitle().compareTo(o2.getTitle());
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
//                } else {
////                    Log.d(TAG, source + " data: null");
////                }
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
            if(isHaveChecked()){
                doActionSubmit();
            }
            else{
                Toast.makeText(view.getContext(), view.getContext().getResources().getString(R.string.order_frag_no_checked), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean isHaveChecked() {
        for(int i = 0; i < adapter.getGroups().size(); i++){
            CookFood cf = (CookFood)adapter.getGroups().get(i);
            for(int j = 0; j < cf.getItems().size(); j++){
                if(cf.isChildChecked(j)){
                    return true;
                }
            }
        }
        return false;
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
                if(kill == 0){
                    // meaning checked all the food in table
                    // so i will change the icon in the right
                    cf.setStatus(1);
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
//        Collections.sort(listData, new Comparator<CookFood>() {
//            @Override
//            public int compare(CookFood o1, CookFood o2) {
//                if(o1.getTime().compareTo(o2.getTime())==0){
//                    return 1;
//                }
//                return o1.getTime().compareTo(o2.getTime());
//            }
//        });
        adapter.clearChoices();
        adapter.notifyDataSetChanged();
    }

    private void updateDataToServer() {
        showLoadingDialog(view.getContext());
        // Remove the food have been cooked and prepare bring to the customer -- Start
        db.collection(QuanLyConstants.COOK)
            .document(restaurantID)
            .collection(QuanLyConstants.TABLE)
            .orderBy(QuanLyConstants.ORDER_TIME, Query.Direction.ASCENDING)
            .orderBy(QuanLyConstants.TABLE_NUMBER, Query.Direction.ASCENDING)
            .get()
            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if(task.isSuccessful()){
                        int i = 0;
                        for(DocumentSnapshot document : task.getResult()){
//                            String tableNumber = document.get(QuanLyConstants.TABLE_NUMBER).toString();
                            Map<String, Object> cook = new HashMap<>();
                            cook.put(QuanLyConstants.FOOD_NAME, getFoodName(i));
                            DocumentReference docRef = document.getReference();
                            docRef.set(cook, SetOptions.merge());
                            i++;
                        }
                        closeLoadingDialog();
                        Toast.makeText(view.getContext(), view.getContext().getResources().getString(R.string.string_done), Toast.LENGTH_SHORT).show();
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

    public int getEmployeePosition(){
        String langPref = QuanLyConstants.SHARED_POSITION;
        SharedPreferences prefs = view.getContext().getSharedPreferences(QuanLyConstants.SHARED_PERFERENCE, Activity.MODE_PRIVATE);
        return prefs.getInt(langPref,0);
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

}
