package com.yaowang.openfire.servlet.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.forms.DataForm;
import org.jivesoftware.openfire.forms.FormField;
import org.jivesoftware.openfire.forms.spi.XDataFormImpl;
import org.jivesoftware.openfire.forms.spi.XFormFieldImpl;
import org.jivesoftware.openfire.muc.MUCRoom;
import org.jivesoftware.openfire.muc.NotAllowedException;
import org.jivesoftware.stringprep.Stringprep;
import org.jivesoftware.stringprep.StringprepException;
import org.jivesoftware.util.WebManager;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;

@SuppressWarnings("deprecation")
public class RoomUtil {
	/**
	 * 添加用户
	 * @param request
	 * @param response
	 * @throws IOException 
	 */
	public static String addUser(HttpServletRequest request, WebManager webManager) throws Exception{
		String id = request.getParameter("id");
		String[] users = request.getParameterValues("users");
		if (StringUtils.isEmpty(id)) {
			return "room id is null";
		}
		if (ArrayUtils.isEmpty(users)) {
			return "users not find";
		}
		
		JID roomJID = new JID(id);
		String roomName = roomJID.getNode();
		MUCRoom room = webManager.getMultiUserChatManager().getMultiUserChatService(roomJID).getChatRoom(roomName);
		if (room == null) {
			return "room not find";
	    }
		
		List<String> memberJIDs = UserUtil.getUserJID(users);
		
     	IQ iq = new IQ(IQ.Type.set);
        Element frag = iq.setChildElement("query", "http://jabber.org/protocol/muc#admin");
    	for (String memberJID : memberJIDs){
            Element item = frag.addElement("item");
            item.addAttribute("affiliation", "member");
            item.addAttribute("jid", memberJID);
    	}
    	room.getIQAdminHandler().handleIQ(iq, room.getRole());
    	return null;
	}
	/**
	 * 删除用户
	 * @param request
	 * @param response
	 * @throws IOException 
	 */
	public static String removeUser(HttpServletRequest request, WebManager webManager) throws IOException{
		String id = request.getParameter("id");
		String[] users = request.getParameterValues("users");
		
		if (StringUtils.isEmpty(id)) {
			return "room id is null";
		}
		if (ArrayUtils.isEmpty(users)) {
			return "users not find";
		}
		
		JID roomJID = new JID(id);
		String roomName = roomJID.getNode();
		MUCRoom room = webManager.getMultiUserChatManager().getMultiUserChatService(roomJID).getChatRoom(roomName);
		if (room == null) {
			return "room not find";
	    }
		
		List<String> memberJIDs = UserUtil.getUserJID(users);
		for (String userJID : memberJIDs) {
			IQ iq = new IQ(IQ.Type.set);
			Element frag = iq.setChildElement("query", "http://jabber.org/protocol/muc#admin");
			Element item = frag.addElement("item");
			item.addAttribute("affiliation", "none");
			item.addAttribute("jid", userJID);
			try {
				room.getIQAdminHandler().handleIQ(iq, room.getRole());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	/**
	 * 创建房间
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	public static String createRoom(HttpServletRequest request, WebManager webManager) throws IOException{
		String roomName = request.getParameter("roomName");
		String mucName = request.getParameter("mucName");
		if (StringUtils.isEmpty(roomName)) {
			return "roomName id is null";
		}
		if (StringUtils.isEmpty(mucName)) {
			return "mucName id is null";
		}
		//
		String naturalName = roomName;
		String description = roomName;
		//使用者修改昵称
		Boolean changeSubject = true;
		String maxUsers = "0";
		//权限
		String broadcastModerator = "true";
	    String broadcastParticipant = "true";
	    String broadcastVisitor = "true";
	    String publicRoom = "true";
	    String persistentRoom = "true";
	    String moderatedRoom = null;
	    String membersOnly = null;
	    String allowInvites = null;
	    String password = "";
	    String whois = "moderator";
	    String enableLog = null;
	    String reservedNick = null;
	    String canChangeNick = "true";
	    String registrationEnabled = "true";
		
		try {
			roomName = Stringprep.nodeprep(roomName);
		} catch (StringprepException e) {
			return e.getMessage();
		}
		JID roomJID = new JID(roomName, mucName, null);
		MUCRoom room = webManager.getMultiUserChatManager().getMultiUserChatService(roomJID).getChatRoom(roomName);
		JID address = new JID("admin", webManager.getServerInfo().getXMPPDomain(), null);
        try {
            room = webManager.getMultiUserChatManager().getMultiUserChatService(roomJID).getChatRoom(roomName, address);
            if (!room.getOwners().contains(address.asBareJID())) {
                return "room_already_exists";
            }
        } catch (NotAllowedException e) {
            return "not_enough_permissions";
        }
        FormField field;
        XDataFormImpl dataForm = new XDataFormImpl(DataForm.TYPE_SUBMIT);

        field = new XFormFieldImpl("FORM_TYPE");
        field.setType(FormField.TYPE_HIDDEN);
        field.addValue("http://jabber.org/protocol/muc#roomconfig");
        dataForm.addField(field);

        field = new XFormFieldImpl("muc#roomconfig_roomname");
        field.addValue(naturalName);
        dataForm.addField(field);

        field = new XFormFieldImpl("muc#roomconfig_roomdesc");
        field.addValue(description);
        dataForm.addField(field);

        field = new XFormFieldImpl("muc#roomconfig_changesubject");
        field.addValue((changeSubject == null) ? "0": "1");
        dataForm.addField(field);

        field = new XFormFieldImpl("muc#roomconfig_maxusers");
        field.addValue(maxUsers);
        dataForm.addField(field);

        field = new XFormFieldImpl("muc#roomconfig_presencebroadcast");
        if (broadcastModerator != null) {
            field.addValue("moderator");
        }
        if (broadcastParticipant != null) {
            field.addValue("participant");
        }
        if (broadcastVisitor != null) {
            field.addValue("visitor");
        }
        dataForm.addField(field);

        field = new XFormFieldImpl("muc#roomconfig_publicroom");
        field.addValue((publicRoom == null) ? "0": "1");
        dataForm.addField(field);

        field = new XFormFieldImpl("muc#roomconfig_persistentroom");
        field.addValue((persistentRoom == null) ? "0": "1");
        dataForm.addField(field);

        field = new XFormFieldImpl("muc#roomconfig_moderatedroom");
        field.addValue((moderatedRoom == null) ? "0": "1");
        dataForm.addField(field);

        field = new XFormFieldImpl("muc#roomconfig_membersonly");
        field.addValue((membersOnly == null) ? "0": "1");
        dataForm.addField(field);

        field = new XFormFieldImpl("muc#roomconfig_allowinvites");
        field.addValue((allowInvites == null) ? "0": "1");
        dataForm.addField(field);

        field = new XFormFieldImpl("muc#roomconfig_passwordprotectedroom");
        field.addValue((password == null) ? "0": "1");
        dataForm.addField(field);

        field = new XFormFieldImpl("muc#roomconfig_roomsecret");
        field.addValue(password);
        dataForm.addField(field);

        field = new XFormFieldImpl("muc#roomconfig_whois");
        field.addValue(whois);
        dataForm.addField(field);

        field = new XFormFieldImpl("muc#roomconfig_enablelogging");
        field.addValue((enableLog == null) ? "0": "1");
        dataForm.addField(field);

        field = new XFormFieldImpl("x-muc#roomconfig_reservednick");
        field.addValue((reservedNick == null) ? "0": "1");
        dataForm.addField(field);

        field = new XFormFieldImpl("x-muc#roomconfig_canchangenick");
        field.addValue((canChangeNick == null) ? "0": "1");
        dataForm.addField(field);

        field = new XFormFieldImpl("x-muc#roomconfig_registration");
        field.addValue((registrationEnabled == null) ? "0": "1");
        dataForm.addField(field);

        // Keep the existing list of admins
        field = new XFormFieldImpl("muc#roomconfig_roomadmins");
        for (JID jid : room.getAdmins()) {
            field.addValue(jid.toString());
        }
        dataForm.addField(field);

        // Keep the existing list of owners
        field = new XFormFieldImpl("muc#roomconfig_roomowners");
        for (JID jid : room.getOwners()) {
            field.addValue(jid.toString());
        }
        dataForm.addField(field);

        // Create an IQ packet and set the dataform as the main fragment
        IQ iq = new IQ(IQ.Type.set);
        Element element = iq.setChildElement("query", "http://jabber.org/protocol/muc#owner");
        element.add(dataForm.asXMLElement());
        // Send the IQ packet that will modify the room's configuration
        try {
			room.getIQOwnerHandler().handleIQ(iq, room.getRole());
		} catch (Exception e) {
			return e.getMessage();
		}
		
		return null;
	}
	
	/**
	 * 销毁房间
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	public static String destroyRoom(HttpServletRequest request, WebManager webManager) throws IOException{
		String id = request.getParameter("id");
		
		if (StringUtils.isEmpty(id)) {
			return "room id is null";
		}
		
		JID roomJID = new JID(id);
		String roomName = roomJID.getNode();
		MUCRoom room = webManager.getMultiUserChatManager().getMultiUserChatService(roomJID).getChatRoom(roomName);
		if (room == null) {
			return "room not find";
	    }
		
		room.destroyRoom(null, "");
		
		return null;
	}
	
	/**
	 * 获取在线用户
	 * @param request
	 * @param response
	 * @throws IOException 
	 */
	public static Object online(HttpServletRequest request, WebManager webManager) throws IOException{
		String id = request.getParameter("id");
		if (StringUtils.isEmpty(id)) {
			return "room id is null";
		}
		
		Integer number = getOnline(id);
		if (number == null) {
			return "room not find";
		}
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("number", number+"");
		
		return map;
	}
	/**
	 * 获取房间在线人数
	 * @param name
	 * @return
	 */
	public static Integer getOnline(String name){
		JID roomJID = new JID(name);
		String roomName = roomJID.getNode();
		MUCRoom room = XMPPServer.getInstance().getMultiUserChatManager().getMultiUserChatService(roomJID).getChatRoom(roomName);
		if (room == null) {
			return null;
	    }
		return room.getOccupantsCount();
	}
}
