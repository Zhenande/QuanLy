package fragment;


import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import abstractModel.Employee;
import butterknife.BindView;
import butterknife.ButterKnife;
import constants.QuanLyConstants;
import adapter.EmployeeListViewAdapter;
import manhquan.khoaluan_quanly.AddAccountActivity;
import manhquan.khoaluan_quanly.EmployeeDetailActivity;
import manhquan.khoaluan_quanly.R;
import model.Cook;


/**
 * A simple {@link Fragment} subclass.
 */
public class EmployeeFragment extends Fragment {

    private static final String TAG = "EmployeeFragment";
    public List<Employee> listData;
    @BindView(R.id.employee_listview)
    public ListView employeeListView;
    private EmployeeListViewAdapter employeeAdapter;
    @BindView(R.id.employee_button_addAccount)
    public FloatingActionButton buttonAdd;
    private ArrayList<String> listEmployeeID = new ArrayList<>();


    public EmployeeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_employee, container, false);
        view.setBackgroundColor(getResources().getColor(R.color.table_color));

        ButterKnife.bind(view);

        listData = new ArrayList<>();
        GetDataFromServer();
        employeeAdapter = new EmployeeListViewAdapter(view.getContext(),listData);
        employeeListView.setAdapter(employeeAdapter);

        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(view.getContext(), AddAccountActivity.class);
                startActivityForResult(i, QuanLyConstants.CREATE_EMPLOYEE);
            }
        });

        employeeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(getActivity().getApplicationContext(), EmployeeDetailActivity.class);
                i.putExtra("documentID",listEmployeeID.get(position));
                startActivityForResult(i, QuanLyConstants.DETAIL_EMPLOYEE);
            }
        });

        return view;
    }

    private void GetDataFromServer() {
        listData.clear();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("employee")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for(DocumentSnapshot document : task.getResult()){
                                int position = Integer.parseInt(document.get("Position").toString());
                                if(position>1){
                                    Employee em = new Cook();
                                    em.setName(document.get("Name").toString());
                                    em.setPosition(position);
                                    listData.add(em);
                                    listEmployeeID.add(document.getId());
                                    Log.i("ListNumber", listData.size()+"");
                                }
                            }
                            employeeAdapter.notifyDataSetChanged();
                        }
                        else{
                            Log.e(TAG, "Error getting document");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity().getApplicationContext(),"I'm inside OnFailure",Toast.LENGTH_SHORT).show();
                        Log.i("GetListEmployee",e.getMessage());
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK ) {
            if (requestCode == QuanLyConstants.CREATE_EMPLOYEE || requestCode == QuanLyConstants.DETAIL_EMPLOYEE) {
                boolean flag = data.getBooleanExtra("flag", false);
                if (flag) {
                    GetDataFromServer();
                }
            }
        }
    }
}
