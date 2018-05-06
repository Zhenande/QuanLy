package util;

import java.text.DecimalFormat;
import java.text.Normalizer;
import java.text.NumberFormat;

/**
 * Created by LieuDucManh on 4/5/2018.
 */
public class MoneyFormatter {

    public static NumberFormat format = new DecimalFormat("#,###");

    public static String formatToMoney(int input){
        return format.format(input);
    }

    public static int backToNumber(String input){
        String result;
        result = Normalizer.normalize(input, Normalizer.Form.NFD).replaceAll("[^0-9]", "");
        return Integer.parseInt(result);
    }
}
