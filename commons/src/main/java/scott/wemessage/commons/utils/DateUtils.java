package scott.wemessage.commons.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class DateUtils {

    public static Date getDateUsing2001(int time) {
        try {
            return new Date(get2001Time() + TimeUnit.SECONDS.toMillis(time));
        }catch(Exception ex){
            ex.printStackTrace();
            return null;
        }
    }

    public static int convertDateTo2001Time(Date date){
        try {
            return (int) (TimeUnit.MILLISECONDS.toSeconds(date.getTime()) - TimeUnit.MILLISECONDS.toSeconds(get2001Time()));
        }catch(Exception ex){
            ex.printStackTrace();
            return -1;
        }
    }

    public static String getSimpleStringFromDate(Date date){
        if (date == null){
            return null;
        }
        DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

        return df.format(date);
    }

    private static long get2001Time() throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
        String dateInString = "01-01-2001 00:00:00";
        Date date = sdf.parse(dateInString);

        return date.getTime();
    }

    public static boolean isSameDay(Date dateOne, Date dateTwo) {
        if (dateOne == null || dateTwo == null) {
            throw new IllegalArgumentException("The dates must not be null");
        }
        Calendar calOne = Calendar.getInstance();
        calOne.setTime(dateOne);
        Calendar calTwo = Calendar.getInstance();
        calTwo.setTime(dateTwo);

        return (calOne.get(Calendar.ERA) == calTwo.get(Calendar.ERA) &&
                calOne.get(Calendar.YEAR) == calTwo.get(Calendar.YEAR) &&
                calOne.get(Calendar.DAY_OF_YEAR) == calTwo.get(Calendar.DAY_OF_YEAR));
    }

    public static boolean wasDateYesterday(Date yesterday, Date today){
        Calendar calendarOne = Calendar.getInstance();
        calendarOne.setTime(today);
        calendarOne.add(Calendar.DAY_OF_YEAR, -1);

        Calendar calendarTwo = Calendar.getInstance();
        calendarTwo.setTime(yesterday);

        return (calendarOne.get(Calendar.YEAR) == calendarTwo.get(Calendar.YEAR)
                && calendarOne.get(Calendar.DAY_OF_YEAR) == calendarTwo.get(Calendar.DAY_OF_YEAR));
    }
}