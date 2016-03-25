package com.yaowang.openfire.lansha.dao;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import com.yaowang.openfire.resource.SystemPropertiesUtil;
import com.yaowang.openfire.util.dao.DatabaseUtil;

/**
 * 聊天组消息
 * @author shenl
 *
 */
public class RobotMessageDao extends DatabaseUtil{
	protected static String dataname = SystemPropertiesUtil.get("im.blacklist.datasource");

	/**
	 * 获取消息
	 * @param groupId
	 * @return
	 */
	public static List<Object[]> getMessage(String groupId){
		String sql = "select note, username, time from " + dataname + ".yw_im_robot_message where status = '1' and group_id = ? order by order_no";
		
		try {
			return queryForLists(sql, Arrays.asList((Object)groupId), 3);
		} catch (SQLException e) {
			return null;
		}
	}
}
