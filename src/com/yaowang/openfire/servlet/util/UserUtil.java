package com.yaowang.openfire.servlet.util;

import java.util.ArrayList;
import java.util.List;

import org.jivesoftware.openfire.XMPPServer;
import org.xmpp.packet.JID;

public class UserUtil {
	/**
	 * 获取用户
	 * @param users
	 * @return
	 */
	public static List<String> getUserJID(String[] users){
		ArrayList<String> memberJIDs = new ArrayList<String>();
		for (String userJID : users) {
			if (userJID.indexOf('@') == -1) {
				String username = JID.escapeNode(userJID);
				String domain = XMPPServer.getInstance().getServerInfo().getXMPPDomain();
				userJID = username + '@' + domain;
			} else {
				String username = JID.escapeNode(userJID.substring(0, userJID.indexOf('@')));
				String rest = userJID.substring(userJID.indexOf('@'), userJID.length());
				userJID = username + rest.trim();
			}
			
			memberJIDs.add(userJID);
		}
		return memberJIDs;
	}
}
