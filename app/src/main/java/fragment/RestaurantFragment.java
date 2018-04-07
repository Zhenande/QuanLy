package fragment;


import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import butterknife.BindView;
import butterknife.ButterKnife;
import constants.QuanLyConstants;
import adapter.GridListViewAdapter;
import manhquan.khoaluan_quanly.R;
import model.TableModel;


/**
 * A simple {@link Fragment} subclass.
 */
public class RestaurantFragment extends Fragment {


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
                        if(!tableNumber.equals(listData.size())){
                            saveTableNumber(listData.size()+"");
                            tableNumber = listData.size()+"";
                        }
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
