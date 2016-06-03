package com.gov.culturems.utils;

public class ParseUtil {

	public static double parseDouble(String str) {
		return parseDouble(str, 0);
	}

	public static double parseDouble(String str, double defaultValue) {
		double result = defaultValue;
		try {
			result = Double.parseDouble(str);
		} catch (Exception e) {
			e.printStackTrace();
			result = defaultValue;
		}
		return result;
	}

	public static long parseLong(String str) {
		return parseLong(str, 0);
	}

	public static long parseLong(String str, long defaultValue) {
		long result = defaultValue;
		try {
			result = Long.parseLong(str);
		} catch (Exception e) {
			e.printStackTrace();
			result = defaultValue;
		}
		return result;
	}

	public static int parseInt(String str) {
		return parseInt(str, 0);
	}

	public static int parseInt(String str, int defaultValue) {
		int result = defaultValue;
		try {
			result = Integer.parseInt(str);
		} catch (Exception e) {
			e.printStackTrace();
			result = defaultValue;
		}
		return result;
	}

}
