package util;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.text.DecimalFormat;

/**
 * Created by LieuDucManh on 4/18/2018.
 */

public class MoneyAxisValueFormatter implements IAxisValueFormatter {

    private DecimalFormat mFormat;

    public MoneyAxisValueFormatter() {
        mFormat = new DecimalFormat("###,###,###,###");
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        return mFormat.format(value) + " VNĐ";
    }

}
