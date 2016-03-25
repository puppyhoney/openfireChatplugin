package com.yaowang.openfire.robot;

import java.util.List;

import org.jivesoftware.openfire.XMPPServer;
import org.xmpp.packet.Message;
import org.xmpp.packet.Presence;

import com.yaowang.openfire.lansha.dao.RobotGroupDao;
import com.yaowang.openfire.lansha.dao.RobotMessageDao;
import com.yaowang.openfire.lansha.dao.RobotUserDao;
import com.yaowang.openfire.servlet.util.MessageUtil;
import com.yaowang.openfire.servlet.util.UserUtil;
import com.yaowang.openfire.util.asynchronous.ObjectCallable;

/**
 * 房间
 * @author shenl
 *
 */
public class RoomRobotCallable extends ObjectCallable{
	@Override
	public Object run() throws Exception {
		String roomName = getData().toString();
		//获取一个组
		Object[] groups = RobotGroupDao.getGroup();
		if (groups != null) {
			// 获取用户
			List<Object[]> users = RobotUserDao.getUsers((Integer)groups[1]);
			if (!users.isEmpty()) {
				String groupId = groups[0].toString();
				List<Object[]> msg = RobotMessageDao.getMessage(groupId);
				if (msg == null) {
					return null;
				}
				for (int i = 0; i < msg.size(); i++) {
					Object[] m = msg.get(i);
					String body = m[0].toString();
					
					Object[] user = users.get(Integer.valueOf(m[1].toString()) - 1);
					String from = user[1].toString() + "|" + user[0].toString() + "|" + Math.random();
					//获取用户
					List<String> memberJIDs = UserUtil.getUserJID(new String[]{from });
					Message message = MessageUtil.message(body, null, null, memberJIDs.get(0)+"/xiff2", roomName, Message.Type.groupchat);

					try {
						//上线
						Presence presence = new Presence();
						presence.setFrom(message.getFrom());
						presence.setTo(roomName + "/" + from);
						presence.setStatus("available");
						presence.addChildElement("x", "http://jabber.org/protocol/muc");
						XMPPServer.getInstance().getRoutingTable().routePacket(presence.getTo(), presence, false);
						
						//发送消息
						XMPPServer.getInstance().getRoutingTable().routePacket(message.getTo(), message, true);
						
						//下线
//						presence = new Presence();
//						presence.setFrom(message.getFrom());
//						presence.setTo(to);
//						presence.addChildElement("x", "http://jabber.org/protocol/muc");
						presence.setType(Presence.Type.unavailable);
						XMPPServer.getInstance().getRoutingTable().routePacket(presence.getTo(), presence, false);
						
						// 等待
						Integer time = (Integer)m[2];
						Thread.sleep(time * 1000);
					} catch (Exception e) {
						return e.getMessage();
					}
				}
			}
		}
		
		return null;
	}
}
