package ru.ruilko.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

public class Utils {
	public static String getDescription(Exception e) {
	    final Writer result = new StringWriter();
	    final PrintWriter printWriter = new PrintWriter(result);
	    e.printStackTrace(printWriter);
	    String stacktrace = e.getMessage() + " - " + result.toString();
	    printWriter.close();
	    return stacktrace;
	}
}
