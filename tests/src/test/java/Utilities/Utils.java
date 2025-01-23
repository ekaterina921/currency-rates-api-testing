package Utilities;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class Utils {
    public String getYesterdaysDate(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern( "uuuu-MM-dd" );
        return ZonedDateTime.now().minusDays(1).format(formatter);
    }
}
