package com.yaowang.openfire.util;

import org.apache.commons.lang.StringUtils;

public class ChatString {
	/**
	 * 获取*
	 * @param msg
	 * @return
	 */
	public static String getFiltration(String msg){
		if (StringUtils.isEmpty(msg)) {
			return "";
		}
		
		int index = msg.length();
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < index; i++) {
			builder.append("*");
		}
		return builder.toString();
	}
	/**
	 * 关键字搜索
	 * @param str
	 * @param keyword
	 * @return
	 */
	public static boolean indexOf(String str, String keyword){
		if (StringUtils.isBlank(keyword)) {
			return false;
		}
		return str.matches(keyword);
	}
}
