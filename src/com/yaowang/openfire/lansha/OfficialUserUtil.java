package com.yaowang.openfire.lansha;

import java.util.Map;

import com.yaowang.openfire.lansha.dao.OfficialUserDao;

public class OfficialUserUtil {
	/**
	 * 官方用户
	 */
	private static Map<Object, Object> roomMap = OfficialUserDao.getOfficialUser();
	/**
	 * 是否是主播
	 * @param userId
	 * @return
	 */
	public static Object isOfficialUser(String userId){
		return roomMap.get(userId);
	}
	/**
	 * 刷新官方用户
	 */
	public static void clear(){
		roomMap = OfficialUserDao.getOfficialUser();
	}
}
