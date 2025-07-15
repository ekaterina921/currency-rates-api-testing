package Utilities;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class Utils {
    public String getYesterdaysDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd");
        return ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC).minusDays(1).format(formatter);
    }

    public String getTodaysDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd");
        return ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC).format(formatter);
    }

    public Instant reformatDateForMongoSearch(String pastDate) {
        String dayId = pastDate + "T00:00:00.000+00:00";
        return Instant.parse(dayId);
    }
}
