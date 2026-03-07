package com.huynh.personal_expense_be.shared.utility;

import java.time.Instant;
import java.time.ZoneId;

public class Utility {

    public  static int getMonthFromInstant(Instant instant) {
        return instant.atZone(ZoneId.systemDefault()).toLocalDate().getMonthValue();
    }

    public static int getYearFromInstant(Instant instant) {
        return instant.atZone(ZoneId.systemDefault()).toLocalDate().getYear();
    }
}
