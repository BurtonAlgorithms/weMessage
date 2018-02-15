package scott.wemessage.commons.utils;

import org.joda.time.DateTime;
import org.joda.time.Weeks;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class DateUtils {

    public static Date getDateUsing2001(Long time) {
        if(time == null) return null;

        try {
            return new Date(get2001Time() + TimeUnit.SECONDS.toMillis(time));
        }catch(Exception ex){
            ex.printStackTrace();
            return null;
        }
    }

    public static Long convertDateTo2001Time(Date date){
        if (date == null) return null;

        try {
            return TimeUnit.MILLISECONDS.toSeconds(date.getTime()) - TimeUnit.MILLISECONDS.toSeconds(get2001Time());
        }catch(Exception ex){
            ex.printStackTrace();
            return -1L;
        }
    }

    public static String getSimpleStringFromDate(Date date){
        if (date == null){
            return null;
        }
        DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

        return df.format(date);
    }

    public static boolean isSameWeek(Date date){
        return Weeks.weeksBetween(new DateTime(date), new DateTime(Calendar.getInstance().getTime())).getWeeks() == 0;
    }

    private static long get2001Time() throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
        String dateInString = "01-01-2001 00:00:00";
        Date date = sdf.parse(dateInString);

        return date.getTime();
    }
}