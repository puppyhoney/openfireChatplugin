package com.yaowang.openfire.util;

import java.text.SimpleDateFormat;

import net.sf.json.JsonConfig;
import net.sf.json.processors.JsonValueProcessor;

public class DateJsonBeanProcessor implements JsonValueProcessor {
	private static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	@Override
	public Object processArrayValue(Object arg0, JsonConfig arg1) {
		return null;
	}

	@Override
	public Object processObjectValue(String arg0, Object arg1, JsonConfig arg2) {
		if (arg1 != null) {
			return FORMAT.format(arg1);
		}else {
			return null;
		}
	}
}
