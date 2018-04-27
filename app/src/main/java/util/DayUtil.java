package util;

/**
 * Created by vpmn-os-quocnb on 4/20/2018.
 */

public class DayUtil {



    public static String changeDayDisplayToDaySort(String daydisplay){
        String[] day = daydisplay.split("/");
        StringBuilder builder = new StringBuilder();
        builder.append(day[2]);
        builder.append("/");
        builder.append(day[1]);
        builder.append("/");
        builder.append(day[0]);

        return builder.toString();
    }

}
