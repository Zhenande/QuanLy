package fragment;



import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;


import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.tsongkha.spinnerdatepicker.DatePickerDialog;
import com.tsongkha.spinnerdatepicker.SpinnerDatePickerDialogBuilder;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import constants.QuanLyConstants;
import manhquan.khoaluan_quanly.R;
import util.DayAxisValueFormatter;
import util.DayUtil;
import util.MoneyFormatter;
import util.MoneyAxisValueFormatter;
import util.MonthAxisValueFormatter;
import util.OrderAxisValueFormatter;
import util.WeekAxisValueFormatter;

import static util.GlobalVariable.closeLoadingDialog;
import static util.GlobalVariable.showLoadingDialog;


/**
 * A simple {@link Fragment} subclass.
 */
public class IncomeFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemSelectedListener, DatePickerDialog.OnDateSetListener {

    @BindView(R.id.income_button_startDate)
    public Button buttonStartDate;
    @BindView(R.id.income_button_endDate)
    public Button buttonEndDate;
    private View view;
    private boolean flag = false;
    @BindView(R.id.income_spinner_ReportTime)
    public Spinner spinnerReportTime;
    @BindView(R.id.income_spinner_kindOfChart)
    public Spinner spinnerKindOfChart;
    @BindView(R.id.income_chart)
    public CombinedChart chart_combined;
    @BindView(R.id.linear_report_date)
    LinearLayout dateLayout;
    @BindView(R.id.linear_report_month)
    LinearLayout monthLayout;
    @BindView(R.id.linear_report_quarter)
    LinearLayout quarterLayout;
    @BindView(R.id.linear_report_year)
    LinearLayout yearLayout;
    @BindView(R.id.linear_report_quarterYear)
    LinearLayout quarterYearLayout;
    @BindView(R.id.income_button_Month)
    Button buttonMonth;
    @BindView(R.id.income_button_Year)
    Button buttonYear;
    @BindView(R.id.income_button_Action)
    Button buttonAction;
    @BindView(R.id.income_spinner_quarter)
    Spinner spinnerQuarter;
    private SpinnerDatePickerDialogBuilder dateSpinner;
    private FirebaseFirestore db;
    @SuppressLint("SimpleDateFormat")
    private SimpleDateFormat sdf_full_date = new SimpleDateFormat("yyyy/MM/dd");
    @SuppressLint("SimpleDateFormat")
    private SimpleDateFormat sdf_date_display = new SimpleDateFormat("dd/MM/yyyy");
    private int dayOfYear = 1;
    private String restaurantID;
    private final String TAG = "IncomeFragment";


    public IncomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_income, container, false);
        view.setBackgroundColor(getResources().getColor(R.color.table_color));

        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        dateSpinner = new SpinnerDatePickerDialogBuilder();
        ButterKnife.bind(this,view);

        db = FirebaseFirestore.getInstance();

        ArrayAdapter<CharSequence> adapterReportTime = ArrayAdapter.createFromResource(view.getContext(), R.array.income_spinner_reportTime,
                android.R.layout.simple_spinner_item);

        ArrayAdapter<CharSequence> adapterKindOfChart = ArrayAdapter.createFromResource(view.getContext(), R.array.income_spinner_kindOfChart,
                android.R.layout.simple_spinner_item);

        ArrayAdapter<CharSequence> adapterQuarter = ArrayAdapter.createFromResource(view.getContext(), R.array.income_spinner_quarter,
                android.R.layout.simple_spinner_item);

        adapterReportTime.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapterKindOfChart.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapterQuarter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerReportTime.setAdapter(adapterReportTime);
        spinnerKindOfChart.setAdapter(adapterKindOfChart);
        spinnerQuarter.setAdapter(adapterQuarter);

        restaurantID = getRestaurantID();


        initDateStartEnd();

        spinnerReportTime.setOnItemSelectedListener(IncomeFragment.this);
        buttonStartDate.setOnClickListener(IncomeFragment.this);
        buttonEndDate.setOnClickListener(IncomeFragment.this);
        buttonMonth.setOnClickListener(IncomeFragment.this);
        buttonYear.setOnClickListener(IncomeFragment.this);
        buttonAction.setOnClickListener(IncomeFragment.this);

        return view;
    }


    @Override
    public void onClick(View v) {
        dateSpinner.context(view.getContext())
                .callback(this);
        int id = v.getId();
        if(id == buttonStartDate.getId()){
            flag = true;
            String[] date = buttonStartDate.getText().toString().split("/");
            dateSpinner.defaultDate(Integer.parseInt(date[2]),Integer.parseInt(date[1])-1,Integer.parseInt(date[0]));
            dateSpinner.showTitle(true);
            dateSpinner.build().show();
        }
        else if(id == buttonEndDate.getId()){
            flag = false;
            String[] date = buttonEndDate.getText().toString().split("/");
            dateSpinner.defaultDate(Integer.parseInt(date[2]),Integer.parseInt(date[1])-1,Integer.parseInt(date[0]));
            dateSpinner.showTitle(true);
            dateSpinner.build().show();
        }
        else if(id == buttonMonth.getId()){
            String[] date = buttonMonth.getText().toString().split("/");
            dateSpinner.defaultDate(Integer.parseInt(date[1]),Integer.parseInt(date[0])-1,1);
            dateSpinner.showTitle(true);
            dateSpinner.build().show();
        }
        else if(id == buttonYear.getId()){
            String[] date = buttonYear.getText().toString().split("/");
            dateSpinner.defaultDate(Integer.parseInt(date[0]),0,1);
            dateSpinner.showTitle(true);
            dateSpinner.build().show();
        }
        else if(id == buttonAction.getId()){
            if(checkDateChart()){
                if(chart_combined.isShown()){
                    chart_combined.clear();
                }
                createChart();
            }
        }
    }

    /*
    * @author: ManhLD
    * Can not create chart have day more than 31
    * */
    private boolean checkDateChart() {
        try {
            Date startDate = sdf_date_display.parse(buttonStartDate.getText().toString());
            Date endDate = sdf_date_display.parse(buttonEndDate.getText().toString());
            Calendar calStart = Calendar.getInstance();
            calStart.setTime(startDate);
            int dayAdd = getDaysForMonth(calStart.get(Calendar.MONTH),calStart.get(Calendar.YEAR));
            calStart.add(Calendar.DAY_OF_MONTH, dayAdd);
            Calendar calEnd = Calendar.getInstance();
            calEnd.setTime(endDate);
            if(calStart.before(calEnd)){
                Toast.makeText(view.getContext(), view.getContext().getResources().getString(R.string.income_time_date_error), Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return true;
    }

    /*
    * @author: ManhLD
    * Use to get the days of the month
    * */
    private int getDaysForMonth(int month, int year) {

        // month is 0-based

        if (month == 1) {
            boolean is29Feb = false;

            if (year < 1582)
                is29Feb = (year < 1 ? year + 1 : year) % 4 == 0;
            else if (year > 1582)
                is29Feb = year % 4 == 0 && (year % 100 != 0 || year % 400 == 0);

            return is29Feb ? 29 : 28;
        }

        if (month == 3 || month == 5 || month == 8 || month == 10)
            return 30;
        else
            return 31;
    }

    private void createChart() {
        switch (spinnerKindOfChart.getSelectedItemPosition()){
            case 0: drawChartRevenue();
                    break;
            case 1: drawChartOrder();
                    break;
            case 2: drawChartBoth();
                    break;
        }
    }

    private void drawChartRevenue() {
        switch (spinnerReportTime.getSelectedItemPosition()){
            case 0: drawChart0_0();
                    break;
            case 1: drawChart0_1();
                    break;
            case 2: drawChart0_2();
                    break;
            case 3: drawChart0_3();
                    break;
        }
    }

    private void drawChartOrder() {
        switch (spinnerReportTime.getSelectedItemPosition()){
            case 0: drawChart1_0();
                break;
            case 1: drawChart1_1();
                break;
            case 2: drawChart1_2();
                break;
            case 3: drawChart1_3();
                break;
        }
    }

    private void drawChartBoth() {
        switch (spinnerReportTime.getSelectedItemPosition()){
            case 0: drawChart2_0();
                break;
            case 1: drawChart2_1();
                break;
            case 2: drawChart2_2();
                break;
            case 3: drawChart2_3();
                break;
        }
    }

    /*
    * @authot: ManhLD
    * Draw chart Time: Date and Type: Income
    * */
    private void drawChart0_0() {
        showLoadingDialog(view.getContext());
        db.collection(QuanLyConstants.ORDER)
            .whereEqualTo(QuanLyConstants.RESTAURANT_ID,restaurantID)
            .whereEqualTo(QuanLyConstants.ORDER_CheckOut,true)
            .whereGreaterThanOrEqualTo(QuanLyConstants.ORDER_DATE, DayUtil.changeDayDisplayToDaySort(buttonStartDate.getText().toString()))
            .whereLessThanOrEqualTo(QuanLyConstants.ORDER_DATE, DayUtil.changeDayDisplayToDaySort(buttonEndDate.getText().toString()))
            .orderBy(QuanLyConstants.ORDER_DATE)
            .get()
            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if(task.isSuccessful()){
                        float counting = dayOfYear+1;
                        String compareDate = DayUtil.changeDayDisplayToDaySort(buttonStartDate.getText().toString());
                        List<BarEntry> listBar = new ArrayList<>();
                        double income = 0;
                        for(DocumentSnapshot document : task.getResult()){
                            boolean flagBreak = true;
                            do{
                                if(document.get(QuanLyConstants.ORDER_DATE).toString().equals(compareDate)){
                                    income += MoneyFormatter.backToNumber(document.get(QuanLyConstants.ORDER_CASH_TOTAL).toString());
                                    flagBreak = false;
                                }
                                else{
                                    if(Double.compare(income,0)>0){
                                        listBar.add(new BarEntry(counting, (float)income));
                                        income = 0;
                                    }
                                    else{
                                        // if the compareday is the day restaurant does not open
                                        listBar.add(new BarEntry(counting, 0f));
                                    }
                                    compareDate = increaseDate(compareDate);
                                    counting++;
                                }
                            }while (flagBreak);
                        }

                        if(Double.compare(income,0) > 0){
                            listBar.add(new BarEntry(counting, (float)income));
                        }

                        if(listBar.size()==0){
                            Toast.makeText(view.getContext(), "The time you choose does not have any order. Please check again!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        IAxisValueFormatter xAxisFormatter = new DayAxisValueFormatter(chart_combined);

                        XAxis xAxis = chart_combined.getXAxis();
                        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                        xAxis.setDrawGridLines(false);
                        xAxis.setGranularity(1f);
                        xAxis.setLabelCount(listBar.size()+1);
                        xAxis.setValueFormatter(xAxisFormatter);

                        IAxisValueFormatter yAxisFormatter = new MoneyAxisValueFormatter();

                        YAxis leftAxis = chart_combined.getAxisLeft();
                        leftAxis.setLabelCount(8, false);
                        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
                        leftAxis.setValueFormatter(yAxisFormatter);
                        leftAxis.setSpaceTop(15f);
                        leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

                        BarDataSet barDataSet = new BarDataSet(listBar, "Income");
                        BarData barData = new BarData(barDataSet);
                        CombinedData combinedData = new CombinedData();
                        combinedData.setData(barData);
                        chart_combined.setData(combinedData);
                        chart_combined.getXAxis().setAxisMinimum(counting-listBar.size()+0.5f);
                        chart_combined.getXAxis().setAxisMaximum(counting+0.5f);
                        chart_combined.animateY(2000);
                        chart_combined.invalidate();
                        closeLoadingDialog();
                    }
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG,e.getMessage());
                }
            });
    }

    /*
    * @authot: ManhLD
    * Draw chart Time: Month and Type: Income
    * */
    private void drawChart0_1() {
        showLoadingDialog(view.getContext());
        Calendar cal = Calendar.getInstance();
        String[] monthC = buttonMonth.getText().toString().split("/");
        cal.set(Calendar.MONTH, Integer.parseInt(monthC[0])-1);
        cal.set(Calendar.YEAR, Integer.parseInt(monthC[1]));
        cal.set(Calendar.DAY_OF_MONTH, 1);
        final String startDate = sdf_full_date.format(cal.getTime());
        final int dayOfMonth = getLastDateOfMonth(Integer.parseInt(monthC[0])-1,Integer.parseInt(monthC[1]));
        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        String endDate = sdf_full_date.format(cal.getTime());
        db.collection(QuanLyConstants.ORDER)
                .whereEqualTo(QuanLyConstants.RESTAURANT_ID,restaurantID)
                .whereEqualTo(QuanLyConstants.ORDER_CheckOut,true)
                .whereGreaterThanOrEqualTo(QuanLyConstants.ORDER_DATE, startDate)
                .whereLessThanOrEqualTo(QuanLyConstants.ORDER_DATE, endDate)
                .orderBy(QuanLyConstants.ORDER_DATE)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            float counting = dayOfYear+1;
                            String compareDate = startDate;
                            List<BarEntry> listBar = new ArrayList<>();
                            double income = 0;
                            for(DocumentSnapshot document : task.getResult()){
                                boolean flagBreak = true;
                                do{
                                    if(document.get(QuanLyConstants.ORDER_DATE).toString().equals(compareDate)){
                                        income += MoneyFormatter.backToNumber(document.get(QuanLyConstants.ORDER_CASH_TOTAL).toString());
                                        flagBreak = false;
                                    }
                                    else{
                                        if(Double.compare(income,0)>0){
                                            listBar.add(new BarEntry(counting, (float)income));
                                            income = 0;
                                        }
                                        else{
                                            // if the compareday is the day restaurant does not open
                                            listBar.add(new BarEntry(counting, 0f));
                                        }
                                        compareDate = increaseDate(compareDate);
                                        counting++;
                                    }
                                }while (flagBreak);
                            }

                            if(Double.compare(income,0) > 0){
                                listBar.add(new BarEntry(counting, (float)income));
                            }

                            // When the user choose the current
                            // Ex: currentDate is 19/4/2018 but the user choose to create chart 4/2018
                            // So i need to add the remaining date of the month.
                            while (listBar.size() < dayOfMonth){
                                listBar.add(new BarEntry(counting, 0f));
                                counting++;
                            }

                            IAxisValueFormatter xAxisFormatter = new DayAxisValueFormatter(chart_combined);

                            XAxis xAxis = chart_combined.getXAxis();
                            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                            xAxis.setDrawGridLines(false);
                            xAxis.setGranularity(1f);
                            xAxis.setLabelCount(listBar.size()+1);
                            xAxis.setValueFormatter(xAxisFormatter);

                            IAxisValueFormatter yAxisFormatter = new MoneyAxisValueFormatter();

                            YAxis leftAxis = chart_combined.getAxisLeft();
                            leftAxis.setLabelCount(8, false);
                            leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
                            leftAxis.setValueFormatter(yAxisFormatter);
                            leftAxis.setSpaceTop(15f);
                            leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

                            BarDataSet barDataSet = new BarDataSet(listBar, "Income");
                            BarData barData = new BarData(barDataSet);
                            CombinedData combinedData = new CombinedData();
                            combinedData.setData(barData);
                            chart_combined.setData(combinedData);
                            chart_combined.animateY(2000);
                            chart_combined.getXAxis().setAxisMinimum(counting-listBar.size()+0.5f);
                            chart_combined.getXAxis().setAxisMaximum(counting+0.5f);
                            chart_combined.setDragEnabled(true);
                            chart_combined.invalidate();
                            closeLoadingDialog();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG,e.getMessage());
                    }
                });
    }

    /*
    * @authot: ManhLD
    * Draw chart Time: Quarter and Type: Income
    * */
    private void drawChart0_2() {
        showLoadingDialog(view.getContext());
        Calendar cal = Calendar.getInstance();
        int startMonth = (spinnerQuarter.getSelectedItemPosition()*3);
        cal.set(Calendar.MONTH, startMonth);
        cal.set(Calendar.YEAR, Integer.parseInt(buttonYear.getText().toString()));
        cal.set(Calendar.DAY_OF_MONTH, 1);
        final String startDate = sdf_full_date.format(cal.getTime());
        final int dayOfMonth = getLastDateOfMonth(startMonth+2,Integer.parseInt(buttonYear.getText().toString()));
        cal.set(Calendar.MONTH, startMonth+2);
        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        String endDate = sdf_full_date.format(cal.getTime());
        final int maxDayQuarter = 13;
        db.collection(QuanLyConstants.ORDER)
                .whereEqualTo(QuanLyConstants.RESTAURANT_ID,restaurantID)
                .whereEqualTo(QuanLyConstants.ORDER_CheckOut,true)
                .whereGreaterThanOrEqualTo(QuanLyConstants.ORDER_DATE, startDate)
                .whereLessThanOrEqualTo(QuanLyConstants.ORDER_DATE, endDate)
                .orderBy(QuanLyConstants.ORDER_DATE)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            float counting = 1;
                            String[] getStartDate = startDate.split("/");
                            Calendar compareDate = Calendar.getInstance();
                            compareDate.set(Calendar.DAY_OF_MONTH, Integer.parseInt(getStartDate[2]));
                            compareDate.set(Calendar.MONTH, Integer.parseInt(getStartDate[1])-1);
                            compareDate.set(Calendar.YEAR, Integer.parseInt(getStartDate[0]));
                            compareDate.add(Calendar.DAY_OF_MONTH,7);
                            List<BarEntry> listBar = new ArrayList<>();
                            double income = 0;
                            Log.i("INCOME", task.getResult().size() + "");
                            for(DocumentSnapshot document : task.getResult()){
                                boolean flagBreak = true;
                                String[] orderDate = document.get(QuanLyConstants.ORDER_DATE).toString().split("/");
                                Calendar cal = Calendar.getInstance();
                                cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(orderDate[2]));
                                cal.set(Calendar.MONTH, Integer.parseInt(orderDate[1])-1);
                                cal.set(Calendar.YEAR, Integer.parseInt(orderDate[0]));
                                do{
                                    if(cal.before(compareDate)){
                                        income += MoneyFormatter.backToNumber(document.get(QuanLyConstants.ORDER_CASH_TOTAL).toString());
                                        flagBreak = false;
                                    }
                                    else{
                                        if(Double.compare(income,0)>0){
                                            listBar.add(new BarEntry(counting, (float)income));
                                            income = 0;
                                        }
                                        else{
                                            // if the compareday is the day restaurant does not open
                                            listBar.add(new BarEntry(counting, 0f));
                                        }
                                        compareDate.add(Calendar.DAY_OF_MONTH, 7);
                                        counting++;
                                    }
                                }while (flagBreak);
                            }

                            if(Double.compare(income,0) > 0){
                                listBar.add(new BarEntry(counting, (float)income));
                                counting++;
                            }

                            // When the user choose the current quarter
                            // Ex: currentDate is 19/4/2018 but the user choose to create chart Quarter 2 / 2018
                            // So i need to add the remaining date of the month.
                            while (listBar.size() <= maxDayQuarter){
                                listBar.add(new BarEntry(counting, 0f));
                                counting++;
                            }

                            IAxisValueFormatter xAxisFormatter = new WeekAxisValueFormatter();

                            XAxis xAxis = chart_combined.getXAxis();
                            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                            xAxis.setDrawGridLines(false);
                            xAxis.setGranularity(1f);
                            xAxis.setLabelCount(listBar.size()+1);
                            xAxis.setValueFormatter(xAxisFormatter);

                            IAxisValueFormatter yAxisFormatter = new MoneyAxisValueFormatter();

                            YAxis leftAxis = chart_combined.getAxisLeft();
                            leftAxis.setLabelCount(8, false);
                            leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
                            leftAxis.setValueFormatter(yAxisFormatter);
                            leftAxis.setSpaceTop(15f);
                            leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

                            BarDataSet barDataSet = new BarDataSet(listBar, "Income");
                            BarData barData = new BarData(barDataSet);
                            CombinedData combinedData = new CombinedData();
                            combinedData.setData(barData);
                            chart_combined.setData(combinedData);
                            chart_combined.animateY(2000);
                            chart_combined.getXAxis().setAxisMinimum(counting-listBar.size()-0.5f);
                            chart_combined.getXAxis().setAxisMaximum(counting-1.5f);
                            chart_combined.setDragEnabled(true);
                            chart_combined.invalidate();
                            closeLoadingDialog();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG,e.getMessage());
                    }
                });
    }

    /*
    * @authot: ManhLD
    * Draw chart Time: Year and Type: Income
    * */
    private void drawChart0_3() {
        showLoadingDialog(view.getContext());
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, 0);
        cal.set(Calendar.YEAR, Integer.parseInt(buttonYear.getText().toString()));
        cal.set(Calendar.DAY_OF_MONTH, 1);
        final String startDate = sdf_full_date.format(cal.getTime());
        cal.set(Calendar.MONTH, 11);
        cal.set(Calendar.DAY_OF_MONTH, 31);
        String endDate = sdf_full_date.format(cal.getTime());
        final int maxMonth = 12;
        db.collection(QuanLyConstants.ORDER)
                .whereEqualTo(QuanLyConstants.RESTAURANT_ID,restaurantID)
                .whereEqualTo(QuanLyConstants.ORDER_CheckOut,true)
                .whereGreaterThanOrEqualTo(QuanLyConstants.ORDER_DATE, startDate)
                .whereLessThanOrEqualTo(QuanLyConstants.ORDER_DATE, endDate)
                .orderBy(QuanLyConstants.ORDER_DATE)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            float counting = 1;
                            String[] getStartDate = startDate.split("/");
                            Calendar compareDate = Calendar.getInstance();
                            compareDate.set(Calendar.DAY_OF_MONTH, Integer.parseInt(getStartDate[2]));
                            compareDate.set(Calendar.MONTH, Integer.parseInt(getStartDate[1])-1);
                            compareDate.set(Calendar.YEAR, Integer.parseInt(getStartDate[0]));
                            compareDate.add(Calendar.MONTH,1);
                            List<BarEntry> listBar = new ArrayList<>();
                            double income = 0;
                            for(DocumentSnapshot document : task.getResult()){
                                boolean flagBreak = true;
                                do{
                                    String[] orderDate = document.get(QuanLyConstants.ORDER_DATE).toString().split("/");
                                    Calendar cal = Calendar.getInstance();
                                    cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(orderDate[2]));
                                    cal.set(Calendar.MONTH, Integer.parseInt(orderDate[1])-1);
                                    cal.set(Calendar.YEAR, Integer.parseInt(orderDate[0]));
                                    if(cal.before(compareDate)){
                                        income += MoneyFormatter.backToNumber(document.get(QuanLyConstants.ORDER_CASH_TOTAL).toString());
                                        flagBreak = false;
                                    }
                                    else{
                                        if(Double.compare(income,0)>0){
                                            listBar.add(new BarEntry(counting, (float)income));
                                            income = 0;
                                        }
                                        else{
                                            // if the compareday is the day restaurant does not open
                                            listBar.add(new BarEntry(counting, 0f));
                                        }
                                        compareDate.add(Calendar.MONTH, 1);
                                        counting++;
                                    }
                                }while (flagBreak);
                            }

                            if(Double.compare(income,0) > 0){
                                listBar.add(new BarEntry(counting, (float)income));
                                counting++;
                            }

                            // When the user choose the current quarter
                            // Ex: currentDate is 19/4/2018 but the user choose to create chart Quarter 2 / 2018
                            // So i need to add the remaining date of the month.
                            while (listBar.size() < maxMonth){
                                listBar.add(new BarEntry(counting, 0f));
                                counting++;
                            }

                            IAxisValueFormatter xAxisFormatter = new MonthAxisValueFormatter();

                            XAxis xAxis = chart_combined.getXAxis();
                            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                            xAxis.setDrawGridLines(false);
                            xAxis.setGranularity(1f);
                            xAxis.setLabelCount(listBar.size());
                            xAxis.setValueFormatter(xAxisFormatter);

                            IAxisValueFormatter yAxisFormatter = new MoneyAxisValueFormatter();

                            YAxis leftAxis = chart_combined.getAxisLeft();
                            leftAxis.setLabelCount(8, false);
                            leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
                            leftAxis.setValueFormatter(yAxisFormatter);
                            leftAxis.setSpaceTop(15f);
                            leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

                            BarDataSet barDataSet = new BarDataSet(listBar, "Income");
                            BarData barData = new BarData(barDataSet);
                            CombinedData combinedData = new CombinedData();
                            combinedData.setData(barData);
                            chart_combined.setData(combinedData);
                            chart_combined.animateY(2000);
                            chart_combined.getXAxis().setAxisMinimum(counting-listBar.size()-0.5f);
                            chart_combined.getXAxis().setAxisMaximum(counting-0.5f);
                            chart_combined.setDragEnabled(true);
                            chart_combined.invalidate();
                            closeLoadingDialog();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG,e.getMessage());
                    }
                });
    }

    /*
    * @authot: ManhLD
    * Draw chart Time: Date and Type: Order
    * */
    private void drawChart1_0() {
        showLoadingDialog(view.getContext());
        db.collection(QuanLyConstants.ORDER)
                .whereEqualTo(QuanLyConstants.RESTAURANT_ID,restaurantID)
                .whereEqualTo(QuanLyConstants.ORDER_CheckOut,true)
                .whereGreaterThanOrEqualTo(QuanLyConstants.ORDER_DATE, DayUtil.changeDayDisplayToDaySort(buttonStartDate.getText().toString()))
                .whereLessThanOrEqualTo(QuanLyConstants.ORDER_DATE, DayUtil.changeDayDisplayToDaySort(buttonEndDate.getText().toString()))
                .orderBy(QuanLyConstants.ORDER_DATE)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            float counting = dayOfYear+1;
                            String compareDate = DayUtil.changeDayDisplayToDaySort(buttonStartDate.getText().toString());
                            List<BarEntry> listBar = new ArrayList<>();
                            int numberOrder = 0;
                            for(DocumentSnapshot document : task.getResult()){
                                boolean flagBreak = true;
                                do{
                                    if(document.get(QuanLyConstants.ORDER_DATE).toString().equals(compareDate)){
                                        numberOrder++;
                                        flagBreak = false;
                                    }
                                    else{
                                        if(numberOrder > 0){
                                            listBar.add(new BarEntry(counting, (float)numberOrder));
                                            numberOrder = 0;
                                        }
                                        else{
                                            // if the compareday is the day restaurant does not open
                                            listBar.add(new BarEntry(counting, 0f));
                                        }
                                        compareDate = increaseDate(compareDate);
                                        counting++;
                                    }
                                }while (flagBreak);
                            }

                            if(numberOrder > 0){
                                listBar.add(new BarEntry(counting, (float)numberOrder));
                            }

                            IAxisValueFormatter xAxisFormatter = new DayAxisValueFormatter(chart_combined);

                            XAxis xAxis = chart_combined.getXAxis();
                            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                            xAxis.setDrawGridLines(false);
                            xAxis.setGranularity(1f);
                            xAxis.setLabelCount(listBar.size()+1);
                            xAxis.setValueFormatter(xAxisFormatter);

                            IAxisValueFormatter yAxisFormatter = new OrderAxisValueFormatter();

                            YAxis leftAxis = chart_combined.getAxisLeft();
                            leftAxis.setLabelCount(8, false);
                            leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
                            leftAxis.setValueFormatter(yAxisFormatter);
                            leftAxis.setSpaceTop(15f);
                            leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

                            BarDataSet barDataSet = new BarDataSet(listBar, "Order Number");
                            BarData barData = new BarData(barDataSet);
                            CombinedData combinedData = new CombinedData();
                            combinedData.setData(barData);
                            chart_combined.setData(combinedData);
                            chart_combined.getXAxis().setAxisMinimum(counting-listBar.size()+0.5f);
                            chart_combined.getXAxis().setAxisMaximum(counting+0.5f);
                            chart_combined.animateY(2000);
                            chart_combined.invalidate();
                            closeLoadingDialog();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, e.getMessage());
                    }
                });
    }

    /*
    * @authot: ManhLD
    * Draw chart Time: Month and Type: Order
    * */
    private void drawChart1_1() {
        showLoadingDialog(view.getContext());
        Calendar cal = Calendar.getInstance();
        String[] monthC = buttonMonth.getText().toString().split("/");
        cal.set(Calendar.MONTH, Integer.parseInt(monthC[0])-1);
        cal.set(Calendar.YEAR, Integer.parseInt(monthC[1]));
        cal.set(Calendar.DAY_OF_MONTH, 1);
        final String startDate = sdf_full_date.format(cal.getTime());
        final int dayOfMonth = getLastDateOfMonth(Integer.parseInt(monthC[0])-1,Integer.parseInt(monthC[1]));
        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        String endDate = sdf_full_date.format(cal.getTime());
        db.collection(QuanLyConstants.ORDER)
                .whereEqualTo(QuanLyConstants.RESTAURANT_ID,restaurantID)
                .whereEqualTo(QuanLyConstants.ORDER_CheckOut,true)
                .whereGreaterThanOrEqualTo(QuanLyConstants.ORDER_DATE, startDate)
                .whereLessThanOrEqualTo(QuanLyConstants.ORDER_DATE, endDate)
                .orderBy(QuanLyConstants.ORDER_DATE)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            float counting = dayOfYear+1;
                            String compareDate = startDate;
                            List<BarEntry> listBar = new ArrayList<>();
                            int orderNumber = 0;
                            for(DocumentSnapshot document : task.getResult()){
                                boolean flagBreak = true;
                                do{
                                    if(document.get(QuanLyConstants.ORDER_DATE).toString().equals(compareDate)){
                                        orderNumber ++;
                                        flagBreak = false;
                                    }
                                    else{
                                        if(orderNumber > 0){
                                            listBar.add(new BarEntry(counting, (float)orderNumber));
                                            orderNumber = 0;
                                        }
                                        else{
                                            // if the compareday is the day restaurant does not open
                                            listBar.add(new BarEntry(counting, 0f));
                                        }
                                        compareDate = increaseDate(compareDate);
                                        counting++;
                                    }
                                }while (flagBreak);
                            }

                            if(orderNumber > 0){
                                listBar.add(new BarEntry(counting, (float)orderNumber));
                            }

                            // When the user choose the current
                            // Ex: currentDate is 19/4/2018 but the user choose to create chart 4/2018
                            // So i need to add the remaining date of the month.
                            while (listBar.size() < dayOfMonth){
                                listBar.add(new BarEntry(counting, 0f));
                                counting++;
                            }

                            IAxisValueFormatter xAxisFormatter = new DayAxisValueFormatter(chart_combined);

                            XAxis xAxis = chart_combined.getXAxis();
                            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                            xAxis.setDrawGridLines(false);
                            xAxis.setGranularity(1f);
                            xAxis.setLabelCount(listBar.size()+1);
                            xAxis.setValueFormatter(xAxisFormatter);

                            IAxisValueFormatter yAxisFormatter = new MoneyAxisValueFormatter();

                            YAxis leftAxis = chart_combined.getAxisLeft();
                            leftAxis.setLabelCount(8, false);
                            leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
                            leftAxis.setValueFormatter(yAxisFormatter);
                            leftAxis.setSpaceTop(15f);
                            leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

                            BarDataSet barDataSet = new BarDataSet(listBar, "Income");
                            BarData barData = new BarData(barDataSet);
                            CombinedData combinedData = new CombinedData();
                            combinedData.setData(barData);
                            chart_combined.setData(combinedData);
                            chart_combined.animateY(2000);
                            chart_combined.getXAxis().setAxisMinimum(counting-listBar.size()+0.5f);
                            chart_combined.getXAxis().setAxisMaximum(counting+0.5f);
                            chart_combined.setDragEnabled(true);
                            chart_combined.invalidate();
                            closeLoadingDialog();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG,e.getMessage());
                    }
                });
    }

    /*
    * @authot: ManhLD
    * Draw chart Time: Quarter and Type: Number Order
    * */
    private void drawChart1_2() {
        showLoadingDialog(view.getContext());
        Calendar cal = Calendar.getInstance();
        int startMonth = (spinnerQuarter.getSelectedItemPosition()*3);
        cal.set(Calendar.MONTH, startMonth);
        cal.set(Calendar.YEAR, Integer.parseInt(buttonYear.getText().toString()));
        cal.set(Calendar.DAY_OF_MONTH, 1);
        final String startDate = sdf_full_date.format(cal.getTime());
        final int dayOfMonth = getLastDateOfMonth(startMonth+2,Integer.parseInt(buttonYear.getText().toString()));
        cal.set(Calendar.MONTH, startMonth+2);
        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        String endDate = sdf_full_date.format(cal.getTime());
        final int maxDayQuarter = 13;
        db.collection(QuanLyConstants.ORDER)
                .whereEqualTo(QuanLyConstants.RESTAURANT_ID,restaurantID)
                .whereEqualTo(QuanLyConstants.ORDER_CheckOut,true)
                .whereGreaterThanOrEqualTo(QuanLyConstants.ORDER_DATE, startDate)
                .whereLessThanOrEqualTo(QuanLyConstants.ORDER_DATE, endDate)
                .orderBy(QuanLyConstants.ORDER_DATE)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            float counting = 1;
                            String[] getStartDate = startDate.split("/");
                            Calendar compareDate = Calendar.getInstance();
                            compareDate.set(Calendar.DAY_OF_MONTH, Integer.parseInt(getStartDate[2]));
                            compareDate.set(Calendar.MONTH, Integer.parseInt(getStartDate[1])-1);
                            compareDate.set(Calendar.YEAR, Integer.parseInt(getStartDate[0]));
                            compareDate.add(Calendar.DAY_OF_MONTH,7);
                            List<BarEntry> listBar = new ArrayList<>();
                            int numberOrder = 0;
                            for(DocumentSnapshot document : task.getResult()){
                                boolean flagBreak = true;
                                do{
                                    String[] orderDate = document.get(QuanLyConstants.ORDER_DATE).toString().split("/");
                                    Calendar cal = Calendar.getInstance();
                                    cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(orderDate[2]));
                                    cal.set(Calendar.MONTH, Integer.parseInt(orderDate[1])-1);
                                    cal.set(Calendar.YEAR, Integer.parseInt(orderDate[0]));
                                    if(cal.before(compareDate)){
                                        numberOrder++;
                                        flagBreak = false;
                                    }
                                    else{
                                        if(numberOrder>0){
                                            listBar.add(new BarEntry(counting, (float)numberOrder));
                                            numberOrder = 0;
                                        }
                                        else{
                                            // if the compareday is the day restaurant does not open
                                            listBar.add(new BarEntry(counting, 0f));
                                        }
                                        compareDate.add(Calendar.DAY_OF_MONTH, 7);
                                        counting++;
                                    }
                                }while (flagBreak);
                            }

                            if(numberOrder > 0){
                                listBar.add(new BarEntry(counting, (float)numberOrder));
                                counting++;
                            }

                            // When the user choose the current quarter
                            // Ex: currentDate is 19/4/2018 but the user choose to create chart Quarter 2 / 2018
                            // So i need to add the remaining date of the month.
                            while (listBar.size() < maxDayQuarter){
                                listBar.add(new BarEntry(counting, 0f));
                                counting++;
                            }

                            IAxisValueFormatter xAxisFormatter = new WeekAxisValueFormatter();

                            XAxis xAxis = chart_combined.getXAxis();
                            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                            xAxis.setDrawGridLines(false);
                            xAxis.setGranularity(1f);
                            xAxis.setLabelCount(listBar.size()+1);
                            xAxis.setValueFormatter(xAxisFormatter);

                            IAxisValueFormatter yAxisFormatter = new MoneyAxisValueFormatter();

                            YAxis leftAxis = chart_combined.getAxisLeft();
                            leftAxis.setLabelCount(8, false);
                            leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
                            leftAxis.setValueFormatter(yAxisFormatter);
                            leftAxis.setSpaceTop(15f);
                            leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

                            BarDataSet barDataSet = new BarDataSet(listBar, "Number Order");
                            BarData barData = new BarData(barDataSet);
                            CombinedData combinedData = new CombinedData();
                            combinedData.setData(barData);
                            chart_combined.setData(combinedData);
                            chart_combined.animateY(2000);
                            chart_combined.getXAxis().setAxisMinimum(counting-listBar.size()-0.5f);
                            chart_combined.getXAxis().setAxisMaximum(counting-0.5f);
                            chart_combined.setDragEnabled(true);
                            chart_combined.invalidate();
                            closeLoadingDialog();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG,e.getMessage());
                    }
                });
    }

    /*
    * @authot: ManhLD
    * Draw chart Time: Year and Type: Number Order
    * */
    private void drawChart1_3() {
        showLoadingDialog(view.getContext());
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, 0);
        cal.set(Calendar.YEAR, Integer.parseInt(buttonYear.getText().toString()));
        cal.set(Calendar.DAY_OF_MONTH, 1);
        final String startDate = sdf_full_date.format(cal.getTime());
        cal.set(Calendar.MONTH, 11);
        cal.set(Calendar.DAY_OF_MONTH, 31);
        String endDate = sdf_full_date.format(cal.getTime());
        final int maxDayYear = 12;
        db.collection(QuanLyConstants.ORDER)
                .whereEqualTo(QuanLyConstants.RESTAURANT_ID,restaurantID)
                .whereEqualTo(QuanLyConstants.ORDER_CheckOut,true)
                .whereGreaterThanOrEqualTo(QuanLyConstants.ORDER_DATE, startDate)
                .whereLessThanOrEqualTo(QuanLyConstants.ORDER_DATE, endDate)
                .orderBy(QuanLyConstants.ORDER_DATE)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            float counting = 1;
                            String[] getStartDate = startDate.split("/");
                            Calendar compareDate = Calendar.getInstance();
                            compareDate.set(Calendar.DAY_OF_MONTH, Integer.parseInt(getStartDate[2]));
                            compareDate.set(Calendar.MONTH, Integer.parseInt(getStartDate[1])-1);
                            compareDate.set(Calendar.YEAR, Integer.parseInt(getStartDate[0]));
                            compareDate.add(Calendar.MONTH,1);
                            List<BarEntry> listBar = new ArrayList<>();
                            int numberOrder = 0;
                            for(DocumentSnapshot document : task.getResult()){
                                boolean flagBreak = true;
                                do{
                                    String[] orderDate = document.get(QuanLyConstants.ORDER_DATE).toString().split("/");
                                    Calendar cal = Calendar.getInstance();
                                    cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(orderDate[2]));
                                    cal.set(Calendar.MONTH, Integer.parseInt(orderDate[1])-1);
                                    cal.set(Calendar.YEAR, Integer.parseInt(orderDate[0]));
                                    if(cal.before(compareDate)){
                                        numberOrder ++;
                                        flagBreak = false;
                                    }
                                    else{
                                        if(numberOrder>0){
                                            listBar.add(new BarEntry(counting, (float)numberOrder));
                                            numberOrder = 0;
                                        }
                                        else{
                                            // if the compareday is the day restaurant does not open
                                            listBar.add(new BarEntry(counting, 0f));
                                        }
                                        compareDate.add(Calendar.MONTH, 1);
                                        counting++;
                                    }
                                }while (flagBreak);
                            }

                            if(numberOrder > 0){
                                listBar.add(new BarEntry(counting, (float)numberOrder));
                                counting++;
                            }

                            // When the user choose the current quarter
                            // Ex: currentDate is 19/4/2018 but the user choose to create chart Quarter 2 / 2018
                            // So i need to add the remaining date of the month.
                            while (listBar.size() < maxDayYear){
                                listBar.add(new BarEntry(counting, 0f));
                                counting++;
                            }

                            IAxisValueFormatter xAxisFormatter = new MonthAxisValueFormatter();

                            XAxis xAxis = chart_combined.getXAxis();
                            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                            xAxis.setDrawGridLines(false);
                            xAxis.setGranularity(1f);
                            xAxis.setLabelCount(listBar.size()+1);
                            xAxis.setValueFormatter(xAxisFormatter);

                            IAxisValueFormatter yAxisFormatter = new MoneyAxisValueFormatter();

                            YAxis leftAxis = chart_combined.getAxisLeft();
                            leftAxis.setLabelCount(8, false);
                            leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
                            leftAxis.setValueFormatter(yAxisFormatter);
                            leftAxis.setSpaceTop(15f);
                            leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

                            BarDataSet barDataSet = new BarDataSet(listBar, "Income");
                            BarData barData = new BarData(barDataSet);
                            CombinedData combinedData = new CombinedData();
                            combinedData.setData(barData);
                            chart_combined.setData(combinedData);
                            chart_combined.animateY(2000);
                            chart_combined.getXAxis().setAxisMinimum(counting-listBar.size()-0.5f);
                            chart_combined.getXAxis().setAxisMaximum(counting-0.5f);
                            chart_combined.setDragEnabled(true);
                            chart_combined.invalidate();
                            closeLoadingDialog();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG,e.getMessage());
                    }
                });
    }

    /*
    * @authot: ManhLD
    * Draw chart Time: Date and Type: Both
    * */
    private void drawChart2_0() {
        showLoadingDialog(view.getContext());
        db.collection(QuanLyConstants.ORDER)
                .whereEqualTo(QuanLyConstants.RESTAURANT_ID,restaurantID)
                .whereEqualTo(QuanLyConstants.ORDER_CheckOut,true)
                .whereGreaterThanOrEqualTo(QuanLyConstants.ORDER_DATE, DayUtil.changeDayDisplayToDaySort(buttonStartDate.getText().toString()))
                .whereLessThanOrEqualTo(QuanLyConstants.ORDER_DATE, DayUtil.changeDayDisplayToDaySort(buttonEndDate.getText().toString()))
                .orderBy(QuanLyConstants.ORDER_DATE)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            float counting = dayOfYear+1;
                            String compareDate = DayUtil.changeDayDisplayToDaySort(buttonStartDate.getText().toString());

                            int numberOrder = 0;
                            double income = 0;
                            List<BarEntry> listBar = new ArrayList<>();
                            List<Entry> listLineEntry = new ArrayList<>();

                            chart_combined.setDrawOrder(new CombinedChart.DrawOrder[]{
                                    CombinedChart.DrawOrder.BAR, CombinedChart.DrawOrder.LINE
                            });

                            for(DocumentSnapshot document : task.getResult()){
                                boolean flagBreak = true;
                                do{
                                    /*
                                    * With the Order just got, i compare Order_Date with the StartDate that the user have choosen
                                    * Just in case, the restaurant have day off, i must check that the day off have or not.
                                    * */
                                    if(document.get(QuanLyConstants.ORDER_DATE).toString().equals(compareDate)){
                                        numberOrder++;
                                        income += MoneyFormatter.backToNumber(document.get(QuanLyConstants.ORDER_CASH_TOTAL).toString());
                                        flagBreak = false;
                                    }
                                    else{
                                        if(numberOrder > 0){
                                            listBar.add(new BarEntry(counting, (float)income));
                                            listLineEntry.add(new Entry(counting, (float)numberOrder));
                                            numberOrder = 0;
                                            income = 0;
                                        }
                                        else{
                                            // if the compareday is the day restaurant does not open
                                            listBar.add(new BarEntry(counting, 0f));
//                                            listLineEntry.add(new Entry(counting, 0f));
                                        }
                                        compareDate = increaseDate(compareDate);
                                        counting++;
                                    }
                                }while (flagBreak);
                            }

                            if(numberOrder > 0){
                                listBar.add(new BarEntry(counting, (float)income));
                                listLineEntry.add(new Entry(counting, (float) numberOrder));
                            }

                            if(listLineEntry.size()==0){
                                listLineEntry.add(new Entry(0,0f));
                            }
                            if(listBar.size()==0){
                                listBar.add(new BarEntry(0,0f));
                            }

                            // Set value formatter for the x Axis
                            IAxisValueFormatter xAxisFormatter = new DayAxisValueFormatter(chart_combined);

                            XAxis xAxis = chart_combined.getXAxis();
                            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                            xAxis.setDrawGridLines(false);
                            xAxis.setGranularity(1f);
                            xAxis.setLabelCount(listBar.size()+1);
                            xAxis.setValueFormatter(xAxisFormatter);

                            // Set value formatter for the Left Axis
                            IAxisValueFormatter yLeftAxisFormatter = new MoneyAxisValueFormatter();

                            YAxis leftAxis = chart_combined.getAxisLeft();
                            leftAxis.setLabelCount(8, false);
                            leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
                            leftAxis.setValueFormatter(yLeftAxisFormatter);
                            leftAxis.setSpaceTop(15f);
                            leftAxis.setAxisMinimum(0f);

                            // Set value formatter for the Right Axis
                            IAxisValueFormatter yRightAxisFormatter = new OrderAxisValueFormatter();

                            YAxis rightAxis = chart_combined.getAxisRight();
                            rightAxis.setLabelCount(8, false);
                            rightAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
                            rightAxis.setValueFormatter(yRightAxisFormatter);
                            rightAxis.setSpaceTop(15f);
                            rightAxis.setAxisMinimum(0f);

                            BarDataSet barDataSet = new BarDataSet(listBar, "Income");
                            barDataSet.setAxisDependency(YAxis.AxisDependency.LEFT); // set the listBar data to the Left Axis
                            BarData barData = new BarData(barDataSet);

                            LineDataSet lineDataSet = new LineDataSet(listLineEntry, "Number Order");
                            lineDataSet.setAxisDependency(YAxis.AxisDependency.RIGHT); // set the listLineEntry data to the Right Axis
                            lineDataSet.setColor(view.getContext().getResources().getColor(R.color.yellow));
                            LineData lineData = new LineData(lineDataSet);

                            CombinedData combinedData = new CombinedData();
                            combinedData.setData(barData);
                            combinedData.setData(lineData);

                            chart_combined.setData(combinedData);
                            chart_combined.animateY(2000);
                            chart_combined.getXAxis().setAxisMinimum(counting-listBar.size()+0.5f);
                            chart_combined.getXAxis().setAxisMaximum(counting+0.5f);
                            chart_combined.setDragEnabled(true);
                            chart_combined.invalidate();
                            closeLoadingDialog();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG,e.getMessage());
                    }
                });
    }

    /*
    * @authot: ManhLD
    * Draw chart Time: Month and Type: Both
    * */
    private void drawChart2_1() {
        showLoadingDialog(view.getContext());
        Calendar cal = Calendar.getInstance();
        String[] monthC = buttonMonth.getText().toString().split("/");
        cal.set(Calendar.MONTH, Integer.parseInt(monthC[0])-1);
        cal.set(Calendar.YEAR, Integer.parseInt(monthC[1]));
        cal.set(Calendar.DAY_OF_MONTH, 1);
        final String startDate = sdf_full_date.format(cal.getTime());
        final int dayOfMonth = getLastDateOfMonth(Integer.parseInt(monthC[0])-1,Integer.parseInt(monthC[1]));
        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        String endDate = sdf_full_date.format(cal.getTime());
        db.collection(QuanLyConstants.ORDER)
                .whereEqualTo(QuanLyConstants.RESTAURANT_ID,restaurantID)
                .whereEqualTo(QuanLyConstants.ORDER_CheckOut,true)
                .whereGreaterThanOrEqualTo(QuanLyConstants.ORDER_DATE, startDate)
                .whereLessThanOrEqualTo(QuanLyConstants.ORDER_DATE, endDate)
                .orderBy(QuanLyConstants.ORDER_DATE)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            float counting = dayOfYear+1;
                            String compareDate = startDate;
                            List<BarEntry> listBar = new ArrayList<>();
                            List<Entry> listLineEntry = new ArrayList<>();
                            int numberOrder = 0;
                            double income = 0;
                            for(DocumentSnapshot document : task.getResult()){
                                boolean flagBreak = true;
                                do{
                                    if(document.get(QuanLyConstants.ORDER_DATE).toString().equals(compareDate)){
                                        income += MoneyFormatter.backToNumber(document.get(QuanLyConstants.ORDER_CASH_TOTAL).toString());
                                        numberOrder++;
                                        flagBreak = false;
                                    }
                                    else{
                                        if(Double.compare(income,0)>0){
                                            listBar.add(new BarEntry(counting, (float)income));
                                            listLineEntry.add(new Entry(counting, (float)numberOrder));
                                            income = 0;
                                            numberOrder = 0;
                                        }
                                        else{
                                            // if the compareday is the day restaurant does not open
                                            listBar.add(new BarEntry(counting, 0f));
//                                            listLineEntry.add(new Entry(counting, 0f));
                                        }
                                        compareDate = increaseDate(compareDate);
                                        counting++;
                                    }
                                }while (flagBreak);
                            }

                            if(Double.compare(income,0) > 0){
                                listBar.add(new BarEntry(counting, (float)income));
                                listLineEntry.add(new Entry(counting, (float)numberOrder));
                                counting++;
                            }

                            // When the user choose the current
                            // Ex: currentDate is 19/4/2018 but the user choose to create chart 4/2018
                            // So i need to add the remaining date of the month.
                            while (listBar.size() < dayOfMonth){
                                listBar.add(new BarEntry(counting, 0f));
//                                listLineEntry.add(new Entry(counting, 0f));
                                counting++;
                            }

                            if(listLineEntry.size()==0){
                                listLineEntry.add(new Entry(0,0f));
                            }

                            IAxisValueFormatter xAxisFormatter = new DayAxisValueFormatter(chart_combined);

                            XAxis xAxis = chart_combined.getXAxis();
                            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                            xAxis.setDrawGridLines(false);
                            xAxis.setGranularity(1f);
                            xAxis.setLabelCount(listBar.size()+1);
                            xAxis.setValueFormatter(xAxisFormatter);

                            IAxisValueFormatter yAxisFormatter = new MoneyAxisValueFormatter();

                            YAxis leftAxis = chart_combined.getAxisLeft();
                            leftAxis.setLabelCount(8, false);
                            leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
                            leftAxis.setValueFormatter(yAxisFormatter);
                            leftAxis.setSpaceTop(15f);
                            leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

                            BarDataSet barDataSet = new BarDataSet(listBar, "Income");
                            barDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
                            BarData barData = new BarData(barDataSet);

                            LineDataSet lineDataSet = new LineDataSet(listLineEntry, "Order Number");
                            lineDataSet.setAxisDependency(YAxis.AxisDependency.RIGHT);
                            lineDataSet.setColor(view.getContext().getResources().getColor(R.color.yellow));
                            LineData lineData = new LineData(lineDataSet);

                            CombinedData combinedData = new CombinedData();
                            combinedData.setData(barData);
                            combinedData.setData(lineData);
                            chart_combined.setData(combinedData);
                            chart_combined.animateY(2000);
                            chart_combined.getXAxis().setAxisMinimum(counting-listBar.size()+0.5f);
                            chart_combined.getXAxis().setAxisMaximum(counting+0.5f);
                            chart_combined.setDragEnabled(true);
                            chart_combined.invalidate();
                            closeLoadingDialog();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG,e.getMessage());
                    }
                });
    }

    /*
    * @authot: ManhLD
    * Draw chart Time: Quarter and Type: Income
    * */
    private void drawChart2_2() {
        showLoadingDialog(view.getContext());
        Calendar cal = Calendar.getInstance();
        int startMonth = (spinnerQuarter.getSelectedItemPosition()*3);
        cal.set(Calendar.MONTH, startMonth);
        cal.set(Calendar.YEAR, Integer.parseInt(buttonYear.getText().toString()));
        cal.set(Calendar.DAY_OF_MONTH, 1);
        final String startDate = sdf_full_date.format(cal.getTime());
        final int dayOfMonth = getLastDateOfMonth(startMonth+2,Integer.parseInt(buttonYear.getText().toString()));
        cal.set(Calendar.MONTH, startMonth+2);
        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        String endDate = sdf_full_date.format(cal.getTime());
        final int maxDayQuarter = 13;
        db.collection(QuanLyConstants.ORDER)
                .whereEqualTo(QuanLyConstants.RESTAURANT_ID,restaurantID)
                .whereEqualTo(QuanLyConstants.ORDER_CheckOut,true)
                .whereGreaterThanOrEqualTo(QuanLyConstants.ORDER_DATE, startDate)
                .whereLessThanOrEqualTo(QuanLyConstants.ORDER_DATE, endDate)
                .orderBy(QuanLyConstants.ORDER_DATE)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            float counting = 1;
                            String[] getStartDate = startDate.split("/");
                            Calendar compareDate = Calendar.getInstance();
                            compareDate.set(Calendar.DAY_OF_MONTH, Integer.parseInt(getStartDate[2]));
                            compareDate.set(Calendar.MONTH, Integer.parseInt(getStartDate[1])-1);
                            compareDate.set(Calendar.YEAR, Integer.parseInt(getStartDate[0]));
                            compareDate.add(Calendar.DAY_OF_MONTH,7);
                            List<BarEntry> listBar = new ArrayList<>();
                            List<Entry> listLineEntry = new ArrayList<>();

                            double income = 0;
                            int numberOrder = 0;
                            for(DocumentSnapshot document : task.getResult()){
                                boolean flagBreak = true;
                                do{
                                    String[] orderDate = document.get(QuanLyConstants.ORDER_DATE).toString().split("/");
                                    Calendar cal = Calendar.getInstance();
                                    cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(orderDate[2]));
                                    cal.set(Calendar.MONTH, Integer.parseInt(orderDate[1])-1);
                                    cal.set(Calendar.YEAR, Integer.parseInt(orderDate[0]));
                                    if(cal.before(compareDate)){
                                        income += MoneyFormatter.backToNumber(document.get(QuanLyConstants.ORDER_CASH_TOTAL).toString());
                                        numberOrder++;
                                        flagBreak = false;
                                    }
                                    else{
                                        if(Double.compare(income,0)>0){
                                            listBar.add(new BarEntry(counting, (float)income));
                                            listLineEntry.add(new Entry(counting, (float)numberOrder));
                                            income = 0;
                                            numberOrder = 0;
                                        }
                                        else{
                                            // if the compareday is the day restaurant does not open
                                            listBar.add(new BarEntry(counting, 0f));
//                                            listLineEntry.add(new Entry(counting, 0f));
                                        }
                                        compareDate.add(Calendar.DAY_OF_MONTH, 7);
                                        counting++;
                                    }
                                }while (flagBreak);
                            }

                            if(Double.compare(income,0) > 0){
                                listBar.add(new BarEntry(counting, (float)income));
                                listLineEntry.add(new Entry(counting, (float)numberOrder));
                                counting++;
                            }

                            // When the user choose the current quarter
                            // Ex: currentDate is 19/4/2018 but the user choose to create chart Quarter 2 / 2018
                            // So i need to add the remaining date of the month.
                            while (listBar.size() < maxDayQuarter){
                                listBar.add(new BarEntry(counting, 0f));
//                                listLineEntry.add(new Entry(counting, 0f));
                                counting++;
                            }

                            if(listLineEntry.size()==0){
                                listLineEntry.add(new Entry(0,0f));
                            }

                            IAxisValueFormatter xAxisFormatter = new WeekAxisValueFormatter();

                            XAxis xAxis = chart_combined.getXAxis();
                            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                            xAxis.setDrawGridLines(false);
                            xAxis.setGranularity(1f);
                            xAxis.setLabelCount(listBar.size()+1);
                            xAxis.setValueFormatter(xAxisFormatter);

                            IAxisValueFormatter yAxisFormatter = new MoneyAxisValueFormatter();

                            YAxis leftAxis = chart_combined.getAxisLeft();
                            leftAxis.setLabelCount(8, false);
                            leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
                            leftAxis.setValueFormatter(yAxisFormatter);
                            leftAxis.setSpaceTop(15f);
                            leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

                            // Set value formatter for the Right Axis
                            IAxisValueFormatter yRightAxisFormatter = new OrderAxisValueFormatter();

                            YAxis rightAxis = chart_combined.getAxisRight();
                            rightAxis.setLabelCount(8, false);
                            rightAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
                            rightAxis.setValueFormatter(yRightAxisFormatter);
                            rightAxis.setSpaceTop(15f);
                            rightAxis.setAxisMinimum(0f);

                            BarDataSet barDataSet = new BarDataSet(listBar, "Income");
                            barDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
                            BarData barData = new BarData(barDataSet);

                            LineDataSet lineDataSet = new LineDataSet(listLineEntry, "Number Order");
                            lineDataSet.setAxisDependency(YAxis.AxisDependency.RIGHT);
                            lineDataSet.setColor(view.getContext().getResources().getColor(R.color.yellow));
                            LineData lineData = new LineData(lineDataSet);

                            CombinedData combinedData = new CombinedData();
                            combinedData.setData(barData);
                            combinedData.setData(lineData);

                            chart_combined.setData(combinedData);
                            chart_combined.animateY(2000);
                            chart_combined.getXAxis().setAxisMinimum(counting-listBar.size()-0.5f);
                            chart_combined.getXAxis().setAxisMaximum(counting-0.5f);
                            chart_combined.setDragEnabled(true);
                            chart_combined.invalidate();
                            closeLoadingDialog();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG,e.getMessage());
                    }
                });
    }

    /*
    * @authot: ManhLD
    * Draw chart Time: Year and Type: Both
    * */
    private void drawChart2_3() {
        showLoadingDialog(view.getContext());
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, 0);
        cal.set(Calendar.YEAR, Integer.parseInt(buttonYear.getText().toString()));
        cal.set(Calendar.DAY_OF_MONTH, 1);
        final String startDate = sdf_full_date.format(cal.getTime());
        cal.set(Calendar.MONTH, 11);
        cal.set(Calendar.DAY_OF_MONTH, 31);
        String endDate = sdf_full_date.format(cal.getTime());
        final int maxDayYear = 12;
        db.collection(QuanLyConstants.ORDER)
                .whereEqualTo(QuanLyConstants.RESTAURANT_ID,restaurantID)
                .whereEqualTo(QuanLyConstants.ORDER_CheckOut,true)
                .whereGreaterThanOrEqualTo(QuanLyConstants.ORDER_DATE, startDate)
                .whereLessThanOrEqualTo(QuanLyConstants.ORDER_DATE, endDate)
                .orderBy(QuanLyConstants.ORDER_DATE)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            float counting = 1;
                            String[] getStartDate = startDate.split("/");
                            Calendar compareDate = Calendar.getInstance();
                            compareDate.set(Calendar.DAY_OF_MONTH, Integer.parseInt(getStartDate[2]));
                            compareDate.set(Calendar.MONTH, Integer.parseInt(getStartDate[1])-1);
                            compareDate.set(Calendar.YEAR, Integer.parseInt(getStartDate[0]));
                            compareDate.add(Calendar.MONTH,1);
                            List<BarEntry> listBar = new ArrayList<>();
                            List<Entry> listLineEntry = new ArrayList<>();

                            double income = 0;
                            int numberOrder = 0;
                            for(DocumentSnapshot document : task.getResult()){
                                boolean flagBreak = true;
                                do{
                                    String[] orderDate = document.get(QuanLyConstants.ORDER_DATE).toString().split("/");
                                    Calendar cal = Calendar.getInstance();
                                    cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(orderDate[2]));
                                    cal.set(Calendar.MONTH, Integer.parseInt(orderDate[1])-1);
                                    cal.set(Calendar.YEAR, Integer.parseInt(orderDate[0]));
                                    if(cal.before(compareDate)){
                                        income += MoneyFormatter.backToNumber(document.get(QuanLyConstants.ORDER_CASH_TOTAL).toString());
                                        numberOrder++;
                                        flagBreak = false;
                                    }
                                    else{
                                        if(Double.compare(income,0)>0){
                                            listBar.add(new BarEntry(counting, (float)income));
                                            listLineEntry.add(new Entry(counting, (float)numberOrder));
                                            income = 0;
                                            numberOrder = 0;
                                        }
                                        else{
                                            // if the compareday is the day restaurant does not open
                                            listBar.add(new BarEntry(counting, 0f));
//                                            listLineEntry.add(new Entry(counting, 0f));
                                        }
                                        compareDate.add(Calendar.MONTH, 1);
                                        counting++;
                                    }
                                }while (flagBreak);
                            }


                            if(Double.compare(income,0) > 0){
                                listBar.add(new BarEntry(counting, (float)income));
                                listLineEntry.add(new Entry(counting, (float)numberOrder));
                                counting++;
                            }

                            // When the user choose the current quarter
                            // Ex: currentDate is 19/4/2018 but the user choose to create chart Quarter 2 / 2018
                            // So i need to add the remaining date of the month.
                            while (listBar.size() < maxDayYear){
                                listBar.add(new BarEntry(counting, 0f));
//                                listLineEntry.add(new Entry(counting, 0f));
                                counting++;
                            }

                            if(listLineEntry.size()==0){
                                listLineEntry.add(new Entry(0,0f));
                            }

                            IAxisValueFormatter xAxisFormatter = new MonthAxisValueFormatter();

                            XAxis xAxis = chart_combined.getXAxis();
                            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                            xAxis.setDrawGridLines(false);
                            xAxis.setGranularity(1f);
                            xAxis.setLabelCount(listBar.size()+1);
                            xAxis.setValueFormatter(xAxisFormatter);

                            IAxisValueFormatter yAxisFormatter = new MoneyAxisValueFormatter();

                            YAxis leftAxis = chart_combined.getAxisLeft();
                            leftAxis.setLabelCount(8, false);
                            leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
                            leftAxis.setValueFormatter(yAxisFormatter);
                            leftAxis.setSpaceTop(15f);
                            leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

                            BarDataSet barDataSet = new BarDataSet(listBar, "Income");
                            barDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
                            BarData barData = new BarData(barDataSet);

                            LineDataSet lineDataSet = new LineDataSet(listLineEntry, "Number Order");
                            lineDataSet.setAxisDependency(YAxis.AxisDependency.RIGHT);
                            lineDataSet.setColor(view.getContext().getResources().getColor(R.color.yellow));
                            LineData lineData = new LineData(lineDataSet);

                            CombinedData combinedData = new CombinedData();
                            combinedData.setData(barData);
                            combinedData.setData(lineData);
                            chart_combined.setData(combinedData);
                            chart_combined.animateY(2000);
                            chart_combined.getXAxis().setAxisMinimum(counting-listBar.size()-0.5f);
                            chart_combined.getXAxis().setAxisMaximum(counting-0.5f);
                            chart_combined.setDragEnabled(true);
                            chart_combined.invalidate();
                            closeLoadingDialog();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG,e.getMessage());
                    }
                });
    }

    /*
    * @author: ManhLD
    * Increase date by string 1 date
    * */
    private String increaseDate(String compareDate) {
        char[] date = compareDate.toCharArray();
        if(date[9] == '9'){
            date[8]++;
            date[9] = '0';
        }
        else{
            date[9]++;
        }
        StringBuilder builder = new StringBuilder();
        for (char aDate : date) {
            builder.append(aDate);
        }
        return builder.toString();
    }



    /*
    * @author: ManhLD
    * @purpose: Check the date selected is correct or not.
    * Both date can not be after the current day
    * The day start must after the date end.
    * */
    private boolean checkCorrectDate(Calendar dateChoosen){
        Calendar currentDate = Calendar.getInstance();
        if(currentDate.before(dateChoosen)){
            Toast.makeText(view.getContext(),getResources().getString(R.string.income_error_dateChoose_after_currentDate),Toast.LENGTH_LONG).show();
            return false;
        }
        String[] date;
        if(flag){
            // Date start have just selected
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


    /*
    * @author: ManhLD
    * @purpose: Check the Year selected is correct or not. The month selected can not after the current Year.
    * */
    private boolean checkCorrectYear(int year) {
        Calendar cal = Calendar.getInstance();
        if(cal.get(Calendar.YEAR) <= year){
            Toast.makeText(getActivity().getApplicationContext(),view.getResources().getString(R.string.year_input_after_current_year),Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    /*
    * @author: ManhLD
    * @purpose: Check the Month selected is correct or not. The month selected can not after the current Month.
    * */
    private boolean checkCorrectMonth(Calendar dateChoose) {
        Calendar dateCheck = Calendar.getInstance();
        dateCheck.get(Calendar.MONTH);
        dateCheck.get(Calendar.YEAR);
        if(dateChoose.get(Calendar.YEAR) > dateCheck.get(Calendar.YEAR)){
            Toast.makeText(getActivity().getApplicationContext(),"Year of month",Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(dateChoose.get(Calendar.YEAR) == dateCheck.get(Calendar.YEAR)){
            if(dateChoose.get(Calendar.MONTH) > dateCheck.get(Calendar.MONTH)){
                Toast.makeText(getActivity().getApplicationContext(),"Year of month",Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }

    /*
    * @author: ManhLD
    * @purpose: Render the Date Start = The first day of the month (1/1/2018)
    *           Render the Date End = The current day
    * */
    private void initDateStartEnd(){
        Calendar currentDate = Calendar.getInstance();

        buttonEndDate.setText(view.getContext().getResources().getString(R.string.year_date,sdf_date_display.format(currentDate.getTime())));

        currentDate.set(Calendar.DAY_OF_MONTH,1);
        dayOfYear = currentDate.get(Calendar.DAY_OF_YEAR);
        buttonStartDate.setText(view.getContext().getResources().getString(R.string.year_date,sdf_date_display.format(currentDate.getTime())));
    }

    /*
    * @author: ManhLD
    * @purpose: Render the current Month and Year to the Button
    * */
    private void initMonth() {
        Calendar currentMonth = Calendar.getInstance();
        String curMonth = currentMonth.get(Calendar.MONTH)+1 + "/";
        curMonth += currentMonth.get(Calendar.YEAR);
        currentMonth.set(Calendar.DAY_OF_MONTH,1);
        dayOfYear = currentMonth.get(Calendar.DAY_OF_YEAR);
        buttonMonth.setText(curMonth);
    }

    private void initQuarter(){
        Calendar currentQuarter = Calendar.getInstance();
        String curYear = currentQuarter.get(Calendar.YEAR) + "";
        switch (spinnerQuarter.getSelectedItemPosition()){
            case 0: dayOfYear = 1;
                    break;
            case 1: currentQuarter.set(Calendar.MONTH, 3);
                    currentQuarter.set(Calendar.DAY_OF_MONTH,1);
                    dayOfYear = currentQuarter.get(Calendar.DAY_OF_YEAR);
                    break;
            case 2: currentQuarter.set(Calendar.MONTH, 6);
                    currentQuarter.set(Calendar.DAY_OF_MONTH,1);
                    dayOfYear = currentQuarter.get(Calendar.DAY_OF_YEAR);
                    break;
            case 3: currentQuarter.set(Calendar.MONTH, 9);
                    currentQuarter.set(Calendar.DAY_OF_MONTH,1);
                    dayOfYear = currentQuarter.get(Calendar.DAY_OF_YEAR);
                    break;
        }
        buttonYear.setText(curYear);
    }

    /*
    * @author: ManhLD
    * @purpose: Render the current Year to the Button
    * */
    private void initYear() {
        Calendar currentYear = Calendar.getInstance();
        String curYear = currentYear.get(Calendar.YEAR) + "";
        dayOfYear = 1;
        buttonYear.setText(curYear);
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (position){
            case 0: dateLayout.setVisibility(View.VISIBLE);
                monthLayout.setVisibility(View.GONE);
                quarterYearLayout.setVisibility(View.GONE);
                yearLayout.setVisibility(View.GONE);
                initDateStartEnd();
                break;
            case 1: dateLayout.setVisibility(View.GONE);
                monthLayout.setVisibility(View.VISIBLE);
                quarterYearLayout.setVisibility(View.GONE);
                yearLayout.setVisibility(View.GONE);
                initMonth();
                break;
            case 2: dateLayout.setVisibility(View.GONE);
                monthLayout.setVisibility(View.GONE);
                quarterYearLayout.setVisibility(View.VISIBLE);
                yearLayout.setVisibility(View.VISIBLE);
                quarterLayout.setVisibility(View.VISIBLE);
                initQuarter();
                break;
            case 3: dateLayout.setVisibility(View.GONE);
                monthLayout.setVisibility(View.GONE);
                quarterLayout.setVisibility(View.GONE);
                quarterYearLayout.setVisibility(View.VISIBLE);
                yearLayout.setVisibility(View.VISIBLE);
                initYear();
                break;
        }
    }


    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    /*
    * @author: ManhLD
    * Use to get day of the month
    * P/s: Month input 0-11 not 1-12. remember -1 when set the value month.
    * */
    private int getLastDateOfMonth(int month, int year) {
        if (month == 1) {
            boolean is29Feb = false;

            if (year < 1582)
                is29Feb = (year < 1 ? year + 1 : year) % 4 == 0;
            else if (year > 1582)
                is29Feb = year % 4 == 0 && (year % 100 != 0 || year % 400 == 0);

            return is29Feb ? 29 : 28;
        }

        if (month == 3 || month == 5 || month == 8 || month == 10)
            return 30;
        else
            return 31;
    }


    /*
    * @author: ManhLD
    * @param: view
    * @param: year
    * @param: monthOfYear
    * @param: dayOfMonth
    * @purpose: Render date selected to the button
    * */
    @Override
    public void onDateSet(com.tsongkha.spinnerdatepicker.DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        Calendar dateChoose = Calendar.getInstance();
        dateChoose.set(Calendar.YEAR,year);
        dateChoose.set(Calendar.MONTH, monthOfYear);
        dateChoose.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        switch (spinnerReportTime.getSelectedItemPosition()){
            case 0: if(checkCorrectDate(dateChoose)) {
                if (flag) {
                    buttonStartDate.setText(view.getContext().getResources().getString(R.string.year_date,sdf_date_display.format(dateChoose.getTime())));
                } else {
                    buttonEndDate.setText(view.getContext().getResources().getString(R.string.year_date,sdf_date_display.format(dateChoose.getTime())));
                }
            }
                break;
            case 1: if(checkCorrectMonth(dateChoose)){
                buttonMonth.setText(view.getContext().getResources().getString(R.string.month_year_date,monthOfYear+1,year));
                }
                break;
            case 2:
            case 3: if(checkCorrectYear(year)){
                buttonYear.setText(view.getContext().getResources().getString(R.string.year_date,year+""));
                }
                break;
        }
    }

    public String getRestaurantID(){
        String langPref = QuanLyConstants.RESTAURANT_ID;
        SharedPreferences prefs = view.getContext().getSharedPreferences(QuanLyConstants.SHARED_PERFERENCE, Activity.MODE_PRIVATE);
        return prefs.getString(langPref,"");
    }
}
