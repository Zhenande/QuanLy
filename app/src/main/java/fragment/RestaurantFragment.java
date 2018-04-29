package fragment;


import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.birin.gridlistviewadapters.dataholders.RowDataHolder;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import constants.QuanLyConstants;
import adapter.GridListViewAdapter;
import manhquan.khoaluan_quanly.MainActivity;
import manhquan.khoaluan_quanly.R;
import model.TableModel;


/**
 * A simple {@link Fragment} subclass.
 */
public class RestaurantFragment extends Fragment {


    private static final String TAG = "RestaurantFragment";
    @BindView(R.id.list_view_table)
    public ListView listView;
    private ArrayList<TableModel> listData;
    private GridListViewAdapter gridListAdapter;
    private String restaurantID;
    private View view;
    private FirebaseFirestore db;
    private String tableNumber;


    public RestaurantFragment() {
        // Required empty public constructor
        setHasOptionsMenu(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_restaurant, container, false);

        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        db = FirebaseFirestore.getInstance();
        setHasOptionsMenu(true);

        tableNumber = getTableNumber();
        restaurantID = getRestaurantID();
        ButterKnife.bind(this,view);
        listData = new ArrayList<>();
        renderData();

        gridListAdapter = new GridListViewAdapter(view.getContext(), QuanLyConstants.MAX_CARDS_LIST_TABLE);
        gridListAdapter.addItemsInGrid(listData);


        listView.setAdapter(gridListAdapter);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        menu.clear();
        inflater.inflate(R.menu.main, menu);
        menu.add(0,QuanLyConstants.CREATE_TABLE_ID,0,getResources().getString(R.string.action_create_table));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == QuanLyConstants.CREATE_TABLE_ID){
            MaterialDialog create_table_dialog = new MaterialDialog.Builder(view.getContext())
                    .positiveText(getResources().getString(R.string.main_agree))
                    .negativeText(getResources().getString(R.string.main_disagree))
                    .positiveColor(getResources().getColor(R.color.primary_dark))
                    .negativeColor(getResources().getColor(R.color.black))
                    .title(getResources().getString(R.string.action_create_table))
                    .customView(R.layout.create_table_dialog, true)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            View view = dialog.getView();
                            EditText edNumber = view.findViewById(R.id.create_table_number);
                            RadioButton rbCreate = view.findViewById(R.id.create_table_create_radio);
                            if (edNumber.getText().toString().matches("[0-9]*")) {
                                int NumberTableCurrent = Integer.parseInt(getTableNumber());
                                int NumberTableNeedChange = Integer.parseInt(edNumber.getText().toString());

                                String restaurantID = getRestaurantID();
                                if (rbCreate.isChecked()) {
                                    createTable(NumberTableCurrent, NumberTableNeedChange, restaurantID);
                                } else {
                                    if (NumberTableCurrent < NumberTableNeedChange) {
                                        Toast.makeText(view.getContext(),
                                                getResources().getString(R.string.table_error_delete_too_much,
                                                        NumberTableNeedChange, NumberTableCurrent), Toast.LENGTH_SHORT).show();
                                    } else {
                                        boolean flag_delete = true;
                                        for(int i = NumberTableCurrent-1; i >= NumberTableCurrent - NumberTableNeedChange; i--){
                                            TableModel tm = gridListAdapter.getCardData(i);
                                            if(!tm.isAvailable()){
                                                flag_delete = false;
                                                break;
                                            }
                                        }
                                        if(flag_delete) {
                                            deleteTable(NumberTableCurrent, NumberTableNeedChange, restaurantID);
                                        }
                                        else{
                                            Toast.makeText(view.getContext(), getResources().getString(R.string.delete_table_has_customer_error), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                            } else {
                                Toast.makeText(view.getContext(), getResources().getString(R.string.table_error_input_letter), Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .build();
            create_table_dialog.show();
        }

        return super.onOptionsItemSelected(item);
    }

    private void createTable(int numberTableCurrent, int numberTableNeedChange, final String restaurantID) {
        int NumberAfterChange = numberTableCurrent + numberTableNeedChange;
        for(int i = numberTableCurrent+1; i <= NumberAfterChange; i++){
            Map<String, Object> table = new HashMap<>();
            table.put(QuanLyConstants.TABLE_NUMBER,i+"");
            table.put(QuanLyConstants.TABLE_ORDER_ID,"1");
            table.put(QuanLyConstants.RESTAURANT_ID,restaurantID);
            final int num = i;
            db.collection(QuanLyConstants.TABLE)
                    .add(table)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            String tableID = documentReference.getId();
                            Map<String, Object> cook = new HashMap<>();
                            cook.put(QuanLyConstants.TABLE_NUMBER, num + "");
                            cook.put(QuanLyConstants.TABLE_EMPLOYEE_ID, "");
                            cook.put(QuanLyConstants.FOOD_NAME,"");
                            cook.put(QuanLyConstants.ORDER_TIME,"99:99");
                            db.collection(QuanLyConstants.COOK)
                                    .document(restaurantID)
                                    .collection(QuanLyConstants.TABLE)
                                    .document(tableID)
                                    .set(cook);
                        }
                    });

        }
//        recreate();
        Toast.makeText(view.getContext(),"Create Done",Toast.LENGTH_SHORT).show();
    }

    private void deleteTable(final int numberTableCurrent, int numberTableNeedChange, final String restaurantID) {
        final int NumberAfterChange = numberTableCurrent - numberTableNeedChange;
        db.collection(QuanLyConstants.TABLE)
                .whereEqualTo(QuanLyConstants.RESTAURANT_ID,restaurantID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for(DocumentSnapshot document : task.getResult()){
                                final int tableNum = Integer.parseInt(document.get(QuanLyConstants.TABLE_NUMBER).toString());
                                final String tableID = document.getId();
                                if(tableNum <= numberTableCurrent && tableNum > NumberAfterChange){
                                    db.collection(QuanLyConstants.TABLE)
                                            .document(tableID)
                                            .delete()
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    db.collection(QuanLyConstants.COOK)
                                                            .document(restaurantID)
                                                            .collection(QuanLyConstants.TABLE)
                                                            .document(tableID)
                                                            .delete();
                                                }
                                            });
                                }
                            }
//                            recreate();
                        }
                    }
                });
        Toast.makeText(view.getContext(),"Delete Done",Toast.LENGTH_SHORT).show();
    }

    private void renderData(){
        db.collection(QuanLyConstants.TABLE)
            .whereEqualTo(QuanLyConstants.RESTAURANT_ID,restaurantID)
            .get()
            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if(task.isSuccessful()){
                        for(DocumentSnapshot document : task.getResult()){
                            String tableNumber = document.get(QuanLyConstants.TABLE_NUMBER).toString();
                            // tableAvailable = true meaning that table is free
                            boolean tableAvailable = true;
                            if(!"1".equals(document.get(QuanLyConstants.TABLE_ORDER_ID).toString())){
                                tableAvailable = false;
                            }
                            TableModel table = new TableModel();
                            table.setTableNumber(tableNumber);
                            table.setAvailable(tableAvailable);
                            onChangeListener(document.getId());
                            listData.add(table);
                        }
                        gridListAdapter.clearList();
                        Collections.sort(listData, new Comparator<TableModel>() {
                            @Override
                            public int compare(TableModel o1, TableModel o2) {
                                return o1.getTableNumber().compareTo(o2.getTableNumber());
                            }
                        });
                        gridListAdapter.addItemsInGrid(listData);
                        if(!tableNumber.equals(listData.size()+"")){
                            saveTableNumber(listData.size()+"");
                            tableNumber = listData.size()+"";
                        }
                    }
                }
            });
    }

    private void onChangeListener(String docID) {

        final DocumentReference docRef = db.collection(QuanLyConstants.TABLE).document(docID);
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
                    Map<String, Object> table = snapshot.getData();
                    boolean flag = false;
                    if("1".equals(table.get(QuanLyConstants.TABLE_ORDER_ID).toString())){
                        flag = true;
                    }
                    String tableNumber = table.get(QuanLyConstants.TABLE_NUMBER).toString();
                    for(int i = 0; i < listData.size(); i++){
                        if(listData.get(i).getTableNumber().equals(tableNumber)){
                            listData.get(i).setAvailable(flag);
                            gridListAdapter.notifyDataSetChanged();
                            break;
                        }
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
        SharedPreferences prefs = view.getContext().getSharedPreferences(QuanLyConstants.SHARED_PERFERENCE, Activity.MODE_PRIVATE);
        return prefs.getString(langPref,"");
    }

    public void saveTableNumber(String tableNumber){
        String langPref = QuanLyConstants.TABLE_NUMBER;
        SharedPreferences prefs = view.getContext().getSharedPreferences(QuanLyConstants.SHARED_PERFERENCE,
                Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(langPref, tableNumber);
        editor.apply();
    }

    public String getTableNumber(){
        String langPref = QuanLyConstants.TABLE_NUMBER;
        SharedPreferences prefs = view.getContext().getSharedPreferences(QuanLyConstants.SHARED_PERFERENCE, Activity.MODE_PRIVATE);
        return prefs.getString(langPref,"0");
    }
}
