package org.example.camunda.process.solution.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {
  private static SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:SS");

  public static String now() {
    return sdf.format(new Date());
  }
}
