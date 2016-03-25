package com.yaowang.openfire.lansha.dao;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import com.yaowang.openfire.resource.SystemPropertiesUtil;
import com.yaowang.openfire.util.dao.DatabaseUtil;

/**
 * 聊天组用户
 * @author shenl
 *
 */
public class RobotUserDao extends DatabaseUtil{
	protected static String dataname = SystemPropertiesUtil.get("im.blacklist.datasource");

	/**
	 * 随机获取用户
	 * @param length
	 * @return
	 */
	public static List<Object[]> getUsers(Integer length){
		String sql = "select id, username from " + dataname + ".yw_im_robot_user where status = '1' and weight >= ? order by rand() limit ?";
		try {
			return queryForLists(sql, Arrays.asList(getWeight(), length), 2);
		} catch (SQLException e) {
			return null;
		}
	}
}
