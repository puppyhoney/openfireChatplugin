package com.yaowang.openfire.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.xmpp.packet.JID;

import com.yaowang.openfire.plugin.YaowangChatPlugin;
import com.yaowang.openfire.resource.SystemPropertiesUtil;
import com.yaowang.openfire.util.dao.DatabaseUtil;

public class MessageDao extends DatabaseUtil{
	protected static String dataname = SystemPropertiesUtil.get("im.blacklist.datasource");

	/**
	 * 保存离线消息
	 * @param message
	 * @return
	 * @throws SQLException 
	 */
	public static final Integer saveMessage(String message, String type, JID fromJid, List<JID> toJids, String msg_type, String formname) throws SQLException{
		if (message.length() >= 100) {
			message = message.subSequence(0, 99) + "...";
		}
		Date now = new Date();
		String from = YaowangChatPlugin.getUsername(fromJid.toString());
		//保存数据库
		String sql = "insert into " + dataname + ".chatplugin_message(form_user, to_user, message, type, create_time, msg_type, formname) values(?,?,?,?,?,?,?)";
		List<Object[]> args = new ArrayList<Object[]>();
		for (JID toJid : toJids) {
			Object[] objects = new Object[7];
			args.add(objects);
			
			//参数
			objects[0] = from;
			objects[1] = YaowangChatPlugin.getUsername(toJid.toString());
			objects[2] = message;
			objects[3] = type;
			objects[4] = now;
			objects[5] = msg_type;
			objects[6] = formname;
		}
		
//		log.info("DatabaseUtil.getMaxActive=" + DatabaseUtil.getDataSource().getMaxActive());
//		log.info("DatabaseUtil.getNumActive=" + DatabaseUtil.getDataSource().getNumActive());
		
		return update(sql, args);
	}
}
