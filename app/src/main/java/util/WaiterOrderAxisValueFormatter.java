package util;

import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.util.ArrayList;

import model.WaiterOrder;

/**
 * Created by LieuDucManh on 4/17/2018.
 */
public class WaiterOrderAxisValueFormatter implements IAxisValueFormatter {



    private ArrayList<WaiterOrder> listData;

    public WaiterOrderAxisValueFormatter(ArrayList<WaiterOrder> listData) {
        this.listData = listData;
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        int index = (int)value;
        return listData.get(index).getWaiterName();
    }


}
