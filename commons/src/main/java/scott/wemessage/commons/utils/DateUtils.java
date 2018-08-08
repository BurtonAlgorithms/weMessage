/*
 *  weMessage - iMessage for Android
 *  Copyright (C) 2018 Roman Scott
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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