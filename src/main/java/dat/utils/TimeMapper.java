package dat.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;

public class TimeMapper {
    public static long unixOf(LocalDateTime localDateTime)
    {
        return localDateTime.atZone(ZoneId.systemDefault()).toEpochSecond();
    }
}
