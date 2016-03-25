package com.yaowang.openfire.servlet.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.tree.DefaultElement;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.util.WebManager;
import org.xmpp.packet.Message;
import org.xmpp.packet.PacketExtension;
import org.xmpp.packet.Presence;


public class MessageUtil {
	/**
	 * 消息类型
	 */
	private static final String MSG_TYPE = "msg_type";
	private static final String MSG_DATA = "msg_data";
	private static final String MSG_FORMNAME = "msg_formname";

	/**
	 * 房间发送消息
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	public static String groupchatMessage(HttpServletRequest request, WebManager webManager) throws Exception{
		String from = request.getParameter("from");
		String to = request.getParameter("to");
		if (StringUtils.isEmpty(from)) {
			return "from is null";
		}
		if (StringUtils.isEmpty(to)) {
			return "to is null";
		}
		//获取用户
		List<String> memberJIDs = UserUtil.getUserJID(new String[]{from });
		String body = request.getParameter("body");
		Message message = message(body, request, webManager, memberJIDs.get(0)+"/xiff2", to, Message.Type.groupchat);
		
		try {
			//上线
			Presence presence = new Presence();
			presence.setFrom(message.getFrom());
			presence.setTo(to + "/" + from);
			presence.setStatus("available");
			presence.addChildElement("x", "http://jabber.org/protocol/muc");
			XMPPServer.getInstance().getRoutingTable().routePacket(presence.getTo(), presence, false);
			
			//发送消息
			XMPPServer.getInstance().getRoutingTable().routePacket(message.getTo(), message, true);
			
			//下线
//			presence = new Presence();
//			presence.setFrom(message.getFrom());
//			presence.setTo(to);
//			presence.addChildElement("x", "http://jabber.org/protocol/muc");
			presence.setType(Presence.Type.unavailable);
			XMPPServer.getInstance().getRoutingTable().routePacket(presence.getTo(), presence, false);
		} catch (Exception e) {
			return e.getMessage();
		}
		return null;
	}
	/**
	 * 发送私聊
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	public static String chatMessage(HttpServletRequest request, WebManager webManager) throws Exception{
		String from = request.getParameter("from");
		String to = request.getParameter("to");
		if (StringUtils.isEmpty(from)) {
			return "from is null";
		}
		if (StringUtils.isEmpty(to)) {
			return "to is null";
		}
		//获取用户
		List<String> memberJIDs = UserUtil.getUserJID(new String[]{from, to });
		String body = request.getParameter("body");
		Message message = message(body, request, webManager, memberJIDs.get(0), memberJIDs.get(1), Message.Type.chat);

		try {
			XMPPServer.getInstance().getRoutingTable().routePacket(message.getTo(), message, true);
		} catch (Exception e) {
			return e.getMessage();
		}
		return null;
	}
	/**
	 * 发送消息
	 */
	public static Message message(String body, HttpServletRequest request, WebManager webManager, String from, String to, org.xmpp.packet.Message.Type msgType) throws Exception{
		//获取用户
		Message message = new Message();
		message.setType(msgType);
		message.setBody(body);
		message.setFrom(from);
		message.setTo(to);
		
		// 封装property数据
		if (request != null) {
			String type = request.getParameter("type");
			String data = request.getParameter("data");
			String fromname = request.getParameter("fromname");
			if (StringUtils.isNotBlank(type)) {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put(MSG_TYPE, type);
				map.put(MSG_DATA, data);
				map.put(MSG_FORMNAME, fromname);
				// 构建消息
				message.addExtension(getPacketExtension(map));
			}
		}
		
		Element fragment = message.addChildElement("x", "jabber:x:event");
		fragment.addElement("offline");
		fragment.addElement("delivered");
		fragment.addElement("displayed");
		fragment.addElement("composing");
		
		return message;
	}
	
	/**
	 * @Description: 构建XML自定义消息
	 * @param map参数集
	 * @return
	 */
	private static PacketExtension getPacketExtension(final Map<String, Object> map) {
		Element element = new DefaultElement("properties", new Namespace("", "http://www.jivesoftware.com/xmlns/xmpp/properties"));
		for (String key : map.keySet()) {
			Object data = map.get(key);
			
			Element elementProperty = new DefaultElement("property");
			element.add(elementProperty);
			
			Element elementName = new DefaultElement("name");
			elementName.setText(key);
			elementProperty.add(elementName);
			
			Element elementValue = new DefaultElement("value");
			if (data instanceof Integer) {
				elementValue.addAttribute("type", "integer");
			}else {
				elementValue.addAttribute("type", "string");
			}
			elementValue.setText(data.toString());
			elementProperty.add(elementValue);
		}
		PacketExtension packetExtension = new PacketExtension(element);
		return packetExtension;
	}
	
	public static void main(String[] args) {
		Element element = new DefaultElement("properties", new Namespace("", "http://www.jivesoftware.com/xmlns/xmpp/properties"));
		Element elementProperty = new DefaultElement("property");
		element.add(elementProperty);
		
		Element elementName = new DefaultElement("name");
		elementName.setText("name_value");
		elementProperty.add(elementName);
		
		Element elementValue = new DefaultElement("value");
		elementValue.addAttribute("type", "integer");
		elementValue.setText("value_value");
		elementProperty.add(elementValue);
		
		System.out.println(element.asXML());
	}
}
