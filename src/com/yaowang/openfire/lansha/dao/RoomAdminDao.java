package com.yaowang.openfire.lansha.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.yaowang.openfire.resource.SystemPropertiesUtil;
import com.yaowang.openfire.util.dao.DatabaseUtil;

public class RoomAdminDao extends DatabaseUtil{
	protected static String dataname = SystemPropertiesUtil.get("im.blacklist.datasource");
	
	/**
	 * 获取房间管理员
	 * @return
	 */
	public static Map<String, List<Object>> getAdminMap(){
		String sql = "select (select openfire_room from " + dataname + ".yw_user_room r where r.id = a.room_id), user_id from " + dataname + ".yw_user_room_admin a";
		
		Map<String, List<Object>> map = new HashMap<String, List<Object>>();
		try {
			Connection conn = dataSource.getConnection();
			try {
				conn.setAutoCommit(false); 
				PreparedStatement pstmt = conn.prepareStatement(sql);
				ResultSet rs= pstmt.executeQuery();
				while (rs.next()) {
					String name = rs.getString(1);
					String userid = rs.getString(2);
					
					if (StringUtils.isEmpty(name)) {
						continue;
					}
					if (map.containsKey(name)) {
						List<Object> list = map.get(name);
						list.add(userid);
					}else {
						List<Object> list = new ArrayList<Object>();
						list.add(userid);
						map.put(name, list);
					}
				}
				return map;
			} finally{
				conn.close();
			}
		} catch (Exception e) {
			return map;
		}
	}
	
	public static List<Object> getAdminList(String roomName){
		String sql = "select user_id from " + dataname + ".yw_user_room_admin where room_id in (select id from yw_user_room where openfire_room = ?)";
		try {
			return queryForList(sql, Arrays.asList((Object)roomName));
		} catch (SQLException e) {
			return null;
		}
	}
}
