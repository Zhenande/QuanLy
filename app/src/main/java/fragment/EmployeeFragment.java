package fragment;


import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import abstractModel.Employee;
import butterknife.BindView;
import butterknife.ButterKnife;
import constants.QuanLyConstants;
import adapter.EmployeeListViewAdapter;
import manhquan.khoaluan_quanly.EmployeeDetailActivity;
import manhquan.khoaluan_quanly.R;
import model.Cook;


/**
 * A simple {@link Fragment} subclass.
 */
public class EmployeeFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    private static final String TAG = "EmployeeFragment";
    public List<Employee> listData;

    @BindView(R.id.employee_listview)
    public ListView employeeListView;
    @BindView(R.id.employee_fragment_spinner_date)
    public Spinner dateSpinner;
    private EmployeeListViewAdapter employeeAdapter;

    @BindView(R.id.employee_button_addAccount)
    public FloatingActionButton buttonAdd;
//    private ArrayList<String> listEmployeeID = new ArrayList<>();
    private View view;
    private MaterialDialog dialogLoading;
    private FirebaseFirestore db;


    public EmployeeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_employee, container, false);
        view.setBackgroundColor(getResources().getColor(R.color.table_color));

        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        ButterKnife.bind(this,view);

        listData = new ArrayList<>();
        db = FirebaseFirestore.getInstance();

        showLoadingDialog();

        ViewGroup myHeader = (ViewGroup)inflater.inflate(R.layout.employee_list_item, employeeListView,false);
        employeeListView.addHeaderView(myHeader,null,false);

        ArrayAdapter<CharSequence> adapterWeekDate = ArrayAdapter.createFromResource(view.getContext(), R.array.week_date,
                    android.R.layout.simple_spinner_item);

        adapterWeekDate.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dateSpinner.setAdapter(adapterWeekDate);

        employeeAdapter = new EmployeeListViewAdapter(view.getContext(),listData);
        employeeListView.setAdapter(employeeAdapter);

        dateSpinner.setOnItemSelectedListener(this);
        Calendar cal = Calendar.getInstance();
        int date = cal.get(Calendar.DAY_OF_WEEK);
        dateSpinner.setSelection(date-1);

        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity().getApplicationContext(), EmployeeDetailActivity.class);
                i.putExtra(QuanLyConstants.INTENT_DOCUMENT_ID,"");
                startActivityForResult(i, QuanLyConstants.DETAIL_EMPLOYEE);
            }
        });

        employeeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(getActivity().getApplicationContext(), EmployeeDetailActivity.class);
                i.putExtra(QuanLyConstants.INTENT_DOCUMENT_ID,listData.get(position-1).getEmID());
                startActivityForResult(i, QuanLyConstants.DETAIL_EMPLOYEE);
            }
        });

        return view;
    }


    /*
    * @author: ManhLD
    * @purpose: Get the collection of the employee work in the restaurant.
    * */
    private void renderData(final int posDate) {
        db = FirebaseFirestore.getInstance();
        db.collection(QuanLyConstants.EMPLOYEE)
                .whereEqualTo(QuanLyConstants.RESTAURANT_ID, getRestaurantID())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            listData.clear();
                            for(DocumentSnapshot document : task.getResult()){
                                int position = Integer.parseInt(document.get(QuanLyConstants.EMPLOYEE_POSITION).toString());
                                if(position>1){
                                    Employee em = new Cook();
                                    String[] fullDayWork = document.get(QuanLyConstants.EMPLOYEE_WORKDAY).toString().split(";");
                                    String[] dateDisplay = fullDayWork[posDate].split(" ");
                                    if(!TextUtils.isEmpty(dateDisplay[1])){
                                        // meaning date like monday 6:00-12:00;
                                        // so the pos[1] will not null and we will display it.
                                        em.setDayWork(dateDisplay[1]);
                                        em.setEmID(document.getId());
                                        em.setName(document.get(QuanLyConstants.EMPLOYEE_NAME).toString());
                                        em.setContactnumber(document.get(QuanLyConstants.EMPLOYEE_CONTACT).toString());
                                        em.setPosition(position);
                                        listData.add(em);
                                    }
                                }
                            }
                            closeLoadingDialog();
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
                boolean flag = data.getBooleanExtra(QuanLyConstants.FLAG, false);
                if (flag) {
                    // Get to know life circle
                    renderData(0);
                }
            }
        }
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

    public void showLoadingDialog(){
        dialogLoading = new MaterialDialog.Builder(view.getContext())
                .customView(R.layout.loading_dialog,true)
                .show();
    }

    public void closeLoadingDialog(){
        dialogLoading.dismiss();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        renderData(position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
