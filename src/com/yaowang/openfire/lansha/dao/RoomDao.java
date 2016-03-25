package com.yaowang.openfire.lansha.dao;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.yaowang.openfire.resource.SystemPropertiesUtil;
import com.yaowang.openfire.util.dao.DatabaseUtil;

public class RoomDao extends DatabaseUtil{
	protected static String dataname = SystemPropertiesUtil.get("im.blacklist.datasource");

	/**
	 * 获取房间对应主播id
	 * @return
	 */
	public static Map<Object, Object> getRoomUsers(){
		String sql = "select openfire_room, uid from " + dataname + ".yw_user_room";
		try {
			return queryForMap(sql, null);
		} catch (SQLException e) {
			return null;
		}
	}
	/**
	 * 查询房间主播id
	 * @param roomName
	 * @return
	 */
	public static String getRoomUserByRoom(String roomName){
		String sql = "select uid from " + dataname + ".yw_user_room where openfire_room = ?";
		try {
			return queryForString(sql, Arrays.asList((Object)roomName));
		} catch (SQLException e) {
			return null;
		}
	}
	
	/**
	 * 获取所有在线房间
	 * @return
	 */
	public static List<Object> getOnlineRooms(){
		String sql = "select openfire_room from " + dataname + ".yw_user_room where online = '1'";
		try {
			return queryForList(sql, null);
		} catch (SQLException e) {
			return null;
		}
	}
}
