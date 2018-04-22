package util;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

/**
 * Created by LieuDucManh on 4/19/2018.
 */

public class MonthAxisValueFormatter implements IAxisValueFormatter {

    public MonthAxisValueFormatter() {
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {

        int weekNumber = (int)value;

        if(weekNumber == 0 || weekNumber == 13){
            return "";
        }

        return "Month " + weekNumber;

    }
}
