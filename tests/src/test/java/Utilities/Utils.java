package Utilities;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class Utils {
    public String getYesterdaysDate(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern( "uuuu-MM-dd" );
        return ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC).minusDays(1).format(formatter);
    }

    public String getTodaysDate(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern( "uuuuMMdd" );
        return ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC).format(formatter);
    }
}
