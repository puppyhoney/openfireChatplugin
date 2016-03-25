package com.yaowang.openfire.lansha.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import com.yaowang.openfire.resource.SystemPropertiesUtil;
import com.yaowang.openfire.util.dao.DatabaseUtil;

public class RoomBlacklistDao extends DatabaseUtil{
	protected static String dataname = SystemPropertiesUtil.get("im.blacklist.datasource");
	/**
	 * 是否禁止发言
	 * @param roomName
	 * @param userId
	 * @param validTime
	 * @return
	 */
	public static Integer getRoomBlacklist(String roomName, String userId, Date validTime){
		try {
			return DatabaseUtil.queryForInt("select count(1) from " + dataname + ".lansha_room_blacklist where im_room = ? and user_id = ? and valid_time >= ?", new ArrayList<Object>(Arrays.asList(roomName, userId, validTime)));
		} catch (SQLException e) {
			return 0;
		}
	}
}
