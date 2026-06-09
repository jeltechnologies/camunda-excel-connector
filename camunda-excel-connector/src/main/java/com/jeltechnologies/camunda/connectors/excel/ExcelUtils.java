package com.jeltechnologies.camunda.connectors.excel;

import java.math.BigDecimal;

public class ExcelUtils {

	private static final String NUMBERS = "0123456789,.";

	private ExcelUtils() {
	}

	public static String incrementColumn(String columnName) {
		int columnNumber = getExcelColumnNumber(columnName);
		return getExcelColumnName(columnNumber + 1);
	}

	public static String getExcelColumnName(int number) {
		final StringBuilder sb = new StringBuilder();
		int num = number - 1;
		while (num >= 0) {
			int numChar = (num % 26) + 65;
			sb.append((char) numChar);
			num = (num / 26) - 1;
		}
		return sb.reverse().toString();
	}

	public static int getExcelColumnNumber(String column) {
		int result = 0;
		for (int i = 0; i < column.length(); i++) {
			result *= 26;
			result += column.charAt(i) - 'A' + 1;
		}
		return result;
	}

	public static String stripToDoubleString(String in) {
		if (in == null) {
			return "";
		} else {
			StringBuilder builder = new StringBuilder();
			boolean fractionFound = false;
			for (int pos = 0; !fractionFound && pos < in.length(); pos++) {
				char c = in.charAt(pos);
				if (c == ',') {
					c = '.';
				}
				if (NUMBERS.indexOf(c) > -1) {
					builder.append(c);
				}
			}
			return builder.toString();
		}
	}

	public static BigDecimal getBigDecimal(String in) {
		String doubleString = stripToDoubleString(in);
		if (doubleString.contentEquals("")) {
			return new BigDecimal(0);
		} else {
			return new BigDecimal(doubleString);
		}
	}

}
