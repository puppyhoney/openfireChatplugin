package com.yaowang.openfire.lansha.dao;

import java.sql.SQLException;
import java.util.Arrays;

import com.yaowang.openfire.resource.SystemPropertiesUtil;
import com.yaowang.openfire.util.dao.DatabaseUtil;

/**
 * 消息组
 * @author shenl
 *
 */
public class RobotGroupDao extends DatabaseUtil{
	protected static String dataname = SystemPropertiesUtil.get("im.blacklist.datasource");

	/**
	 * 随机获取一条聊天组
	 * @return
	 */
	public static Object[] getGroup(){
		String sql = "select id, users from " + dataname + ".yw_im_robot_group where status = '1' and weight >= ? order by rand() limit 1";
		try {
			return queryForObjs(sql, Arrays.asList(getWeight()), 2);
		} catch (SQLException e) {
			return null;
		}
	}
}
