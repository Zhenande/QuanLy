package util;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;


/**
 * Created by LieuDucManh on 4/19/2018.
 */

public class WeekAxisValueFormatter implements IAxisValueFormatter {



    public WeekAxisValueFormatter() {

    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {

        int weekNumber = (int)value;

        if(weekNumber == 0 || weekNumber == 14){
            return "";
        }

        return "Week " + weekNumber;

    }
}
