package util;

import java.text.DecimalFormat;
import java.text.Normalizer;
import java.text.NumberFormat;

/**
 * Created by LieuDucManh on 4/5/2018.
 */
public class MoneyFormatter {

    public static NumberFormat format = new DecimalFormat("#,###");

    public static String formatToMoney(String input){
        int money = Integer.parseInt(input);
        return format.format(money);
    }

    public static String backToString(String input){
        String result;
        result = Normalizer.normalize(input, Normalizer.Form.NFD).replaceAll("[^0-9]", "");
        return result;
    }
}
