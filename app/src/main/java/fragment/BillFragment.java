package fragment;


import android.app.Fragment;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.tsongkha.spinnerdatepicker.DatePicker;
import com.tsongkha.spinnerdatepicker.DatePickerDialog;
import com.tsongkha.spinnerdatepicker.SpinnerDatePickerDialogBuilder;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import manhquan.khoaluan_quanly.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class BillFragment extends Fragment implements View.OnClickListener, DatePickerDialog.OnDateSetListener {


    @BindView(R.id.bill_fragment_list_bill)
    ListView listBill;
    @BindView(R.id.bill_fragment_button_date)
    Button buttonDate;
    @BindView(R.id.bill_fragment_id)
    EditText edBillNumber;
    @BindView(R.id.bill_fragment_search)
    Button buttonSearch;

    private SpinnerDatePickerDialogBuilder dateSpinner;
    private View view;

    public BillFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_bill, container, false);
        ButterKnife.bind(this,view);

        dateSpinner = new SpinnerDatePickerDialogBuilder();

        buttonDate.setOnClickListener(BillFragment.this);
        buttonSearch.setOnClickListener(BillFragment.this);

        Calendar cal = Calendar.getInstance();
        buttonDate.setText(view.getResources().getString(R.string.full_date,new Object[]{cal.get(Calendar.DAY_OF_MONTH),
                                                            cal.get(Calendar.MONTH)+1,
                                                            cal.get(Calendar.YEAR)}));

        return view;
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(id == R.id.bill_fragment_button_date){
            Calendar cal = Calendar.getInstance();
            dateSpinner.context(view.getContext())
                    .callback(this)
                    .showTitle(true)
                    .defaultDate(cal.get(Calendar.YEAR),cal.get(Calendar.MONTH)+1,cal.get(Calendar.DAY_OF_MONTH))
                    .build().show();

        }
        else if(id == R.id.bill_fragment_search){

        }
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        Calendar dateChoose = Calendar.getInstance();
        dateChoose.set(Calendar.YEAR,year);
        dateChoose.set(Calendar.MONTH, monthOfYear);
        dateChoose.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        if(checkCorrectDate(dateChoose)){
            buttonDate.setText(dayOfMonth+"/"+(monthOfYear+1)+"/"+year);
        }
    }

    /*
    * @author: ManhLD
    * @param: dateChoosen
    * @purpose: Check the selected day by the user is correct.
    * The day can be called correct is the day can not after today.
    * */
    private boolean checkCorrectDate(Calendar dateChoosen){
        Calendar currentDate = Calendar.getInstance();
        if(currentDate.before(dateChoosen)){
            Toast.makeText(view.getContext(),getResources().getString(R.string.income_error_dateChoose_after_currentDate),Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }
}
