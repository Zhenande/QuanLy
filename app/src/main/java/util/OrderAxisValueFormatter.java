package util;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.text.DecimalFormat;

/**
 * Created by vpmn-os-quocnb on 4/18/2018.
 */

public class OrderAxisValueFormatter implements IAxisValueFormatter {

    private DecimalFormat mFormat;

    public OrderAxisValueFormatter() {
        mFormat = new DecimalFormat("###,###,###,###");
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        return mFormat.format(value);
    }

}
