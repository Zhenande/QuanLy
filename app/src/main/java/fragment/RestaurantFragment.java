package fragment;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;

import constants.QuanLyConstants;
import adapter.GridListViewAdapter;
import manhquan.khoaluan_quanly.R;
import model.TableModel;


/**
 * A simple {@link Fragment} subclass.
 */
public class RestaurantFragment extends Fragment {


    private ListView listView;
    private ArrayList<TableModel> listData;
    private GridListViewAdapter gridListAdapter;



    public RestaurantFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_restaurant, container, false);

        listView = view.findViewById(R.id.list_view_table);
        listData = generateRawData();
        gridListAdapter = new GridListViewAdapter(view.getContext(), QuanLyConstants.MAX_CARDS_LIST_TABLE);
        gridListAdapter.addItemsInGrid(listData);

        listView.setAdapter(gridListAdapter);

        return view;
    }

    private ArrayList<TableModel> generateRawData(){
        ArrayList<TableModel> list = new ArrayList<>();
        for(int i = 0; i < 10; i++){
            TableModel abc;
            if(i%2 == 0) {
                abc = new TableModel(i + 1 + "", true);
            }
            else {
                abc = new TableModel(i + 1 + "", false);
            }
            list.add(abc);
        }
        return list;
    }

}
