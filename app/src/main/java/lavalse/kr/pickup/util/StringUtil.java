package lavalse.kr.pickup.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author LaValse
 * @date 2016-07-15
 */
public class StringUtil {
    public static String getDateString(Date date){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmm");

        return formatter.format(date);
    }

    public static String getDateString(String date){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd HH:mm");
        Date d = null;
        try {
            d = formatter.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        formatter.applyPattern("yyyyMMddHHmm");

        return formatter.format(d);
    }
}
