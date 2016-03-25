package com.yaowang.openfire.resource;

public class RoomUtil {
	/**
	 * 解析用户id
	 * @param nickName
	 * @return
	 */
	public static String getUserId(String nickName){
		String[] strs = nickName.split("\\|");
		if (strs.length == 3 && strs[1].length() == 32) {
			return strs[1];
		}
		return "";
	}
}
