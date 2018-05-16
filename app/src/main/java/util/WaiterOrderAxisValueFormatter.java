package util;

import android.util.Log;

import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

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
        try{
            return listData.get(index-1).getWaiterName();
        }
        catch (Exception e){
            Log.e("WaiterOrderAxis",e.getMessage());
        }
        return "";
    }


}
