package fragment;



import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;


import java.util.Calendar;

import manhquan.khoaluan_quanly.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class IncomeFragment extends Fragment implements View.OnClickListener,DatePickerDialog.OnDateSetListener{

    private Button buttonStartDate;
    private Button buttonEndDate;
    private View view;
    private boolean flag = false;
    private Spinner spinnerReportTime;
    private Spinner spinnerKindOfChart;
    private BarChart chart;



    public IncomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_income, container, false);

        buttonStartDate = view.findViewById(R.id.income_button_startDate);
        buttonEndDate = view.findViewById(R.id.income_button_endDate);
        spinnerReportTime = view.findViewById(R.id.income_spinner_ReportTime);
        spinnerKindOfChart = view.findViewById(R.id.income_spinner_kindOfChart);
        chart = view.findViewById(R.id.income_chart);
        testingChart();

        ArrayAdapter<CharSequence> adapterReportTime = ArrayAdapter.createFromResource(view.getContext(), R.array.income_spinner_reportTime,
                android.R.layout.simple_spinner_item);

        ArrayAdapter<CharSequence> adapterKindOfChart = ArrayAdapter.createFromResource(view.getContext(), R.array.income_spinner_kindOfChart,
                android.R.layout.simple_spinner_item);

        adapterReportTime.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapterKindOfChart.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerReportTime.setAdapter(adapterReportTime);
        spinnerKindOfChart.setAdapter(adapterKindOfChart);

        initDateStartEnd();

        buttonStartDate.setOnClickListener(IncomeFragment.this);
        buttonEndDate.setOnClickListener(IncomeFragment.this);

        return view;
    }

    private void testingChart() {

    }

    @Override
    public void onClick(View v) {
        if(v.getId()==buttonStartDate.getId() || v.getId() == buttonEndDate.getId()){
            Calendar now = Calendar.getInstance();
            DatePickerDialog dpd = DatePickerDialog.newInstance(
                    IncomeFragment.this,
                    now.get(Calendar.YEAR),
                    now.get(Calendar.MONTH),
                    now.get(Calendar.DAY_OF_MONTH)
            );
            dpd.setVersion(DatePickerDialog.Version.VERSION_1);
            if(v.getId() == buttonStartDate.getId()){
                flag = true;
                dpd.show(getFragmentManager(),getResources().getString(R.string.income_startDate));
            }
            else{
                flag = false;
                dpd.show(getFragmentManager(),getResources().getString(R.string.income_endDate));
            }
        }
    }


    private boolean checkCorrectDate(Calendar dateChoosen){
        Calendar currentDate = Calendar.getInstance();
        if(currentDate.before(dateChoosen)){
            Toast.makeText(view.getContext(),getResources().getString(R.string.income_error_dateChoose_after_currentDate),Toast.LENGTH_LONG).show();
            return false;
        }
        String[] date;
        if(flag){
            date = buttonEndDate.getText().toString().split("/");
            Calendar dateCheck = Calendar.getInstance();
            dateCheck.set(Calendar.DAY_OF_MONTH, Integer.parseInt(date[0]));
            dateCheck.set(Calendar.MONTH, Integer.parseInt(date[1])-1);
            dateCheck.set(Calendar.YEAR, Integer.parseInt(date[2]));
            if(dateChoosen.after(dateCheck)){
                Toast.makeText(view.getContext(),getResources().getString(R.string.income_error_dateStart_after_dateEnd),Toast.LENGTH_LONG).show();
                return false;
            }
        }
        else{
            date = buttonStartDate.getText().toString().split("/");
            Calendar dateCheck = Calendar.getInstance();
            dateCheck.set(Calendar.DAY_OF_MONTH, Integer.parseInt(date[0]));
            dateCheck.set(Calendar.MONTH, Integer.parseInt(date[1])-1);
            dateCheck.set(Calendar.YEAR, Integer.parseInt(date[2]));
            if(dateChoosen.before(dateCheck)){
                Toast.makeText(view.getContext(),getResources().getString(R.string.income_error_dateStart_after_dateEnd),Toast.LENGTH_LONG).show();
                return false;
            }
        }
        return true;
    }


    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        String date = dayOfMonth+"/"+(monthOfYear+1)+"/"+year;
        Calendar dateChoose = Calendar.getInstance();
        dateChoose.set(Calendar.YEAR,year);
        dateChoose.set(Calendar.MONTH, monthOfYear);
        dateChoose.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        if(checkCorrectDate(dateChoose)) {
            if (flag) {
                buttonStartDate.setText(date);
            } else {
                buttonEndDate.setText(date);
            }
        }
    }

    private void initDateStartEnd(){
        Calendar currentDate = Calendar.getInstance();
        String dateStart = "";
        dateStart += "1/";
        dateStart += currentDate.get(Calendar.MONTH)+1 + "/";
        dateStart += currentDate.get(Calendar.YEAR);
        buttonStartDate.setText(dateStart);

        String dateEnd = "";
        dateEnd += currentDate.get(Calendar.DAY_OF_MONTH) + "/";
        dateEnd += currentDate.get(Calendar.MONTH)+1 + "/";
        dateEnd += currentDate.get(Calendar.YEAR);
        buttonEndDate.setText(dateEnd);
    }

}
