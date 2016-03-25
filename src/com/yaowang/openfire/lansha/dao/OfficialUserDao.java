package com.yaowang.openfire.lansha.dao;

import java.sql.SQLException;
import java.util.Map;

import com.yaowang.openfire.resource.SystemPropertiesUtil;
import com.yaowang.openfire.util.dao.DatabaseUtil;

/**
 * 官方账户
 * @author shenl
 *
 */
public class OfficialUserDao extends DatabaseUtil{
	protected static String dataname = SystemPropertiesUtil.get("im.blacklist.datasource");
	
	/**
	 * 获取官方账户
	 * @return
	 */
	public static Map<Object, Object> getOfficialUser(){
		String sql = "select id, official_type from " + dataname + ".yw_user where official_type is not null or official_type <> ''";
		try {
			return queryForMap(sql, null);
		} catch (SQLException e) {
			return null;
		}
	}
}
