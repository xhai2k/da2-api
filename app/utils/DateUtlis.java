package utils;

import java.sql.Date;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Class DateUtlis
 *
 * @author quanna
 */
public class DateUtlis {
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final String TIME_FORMAT = "HH:mm:ss";

    /**
     * Method convert date from String to DateSQL
     *
     * @param dateString
     * @return
     * @throws ParseException
     */
    public static Date convertString2DateSql(String dateString) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT);
        java.util.Date parsed;
        parsed = format.parse(dateString);
        return new java.sql.Date(parsed.getTime());
    }

    /**
     * Method convert date from String to TimeSQL
     *
     * @param timeString
     * @return
     * @throws ParseException
     */
    public static Time convertString2TimeSql(String timeString) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT);
        long ms = sdf.parse(timeString).getTime();
        return new Time(ms);
    }

    /**
     * Method set time zero
     *
     * @param date
     * @return
     */
    public static Date resetTime(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(date.getTime());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return new Date(cal.getTimeInMillis());
    }

    public static java.util.Date mergeDateTime(Date date, Time time, boolean option) {
        Calendar dateCal = Calendar.getInstance();
        if (date == null) {
            return null;
        }
        dateCal.setTime(date);
        Calendar timeCal = Calendar.getInstance();
        if (time == null && !option) {
            time = Time.valueOf("00:00:00");
        }
        if (time == null && option) {
            time = Time.valueOf("23:59:59");
        }
        timeCal.setTime(time);

        // Extract the time of the "time" object to the "date"
        dateCal.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY));
        dateCal.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE));
        dateCal.set(Calendar.SECOND, timeCal.get(Calendar.SECOND));

        // Get the time value!
        return dateCal.getTime();
    }

    public static java.util.Date addMinutes(java.util.Date date, int minutes) {
        if(date == null){
            return null;
        }else{
            long dateL = date.getTime() + minutes * 60 * 1000;
            return new java.util.Date(dateL);
        }
    }
}
