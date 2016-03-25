package com.yaowang.openfire.plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.jivesoftware.admin.AuthCheckFilter;
import org.jivesoftware.openfire.PresenceManager;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.openfire.interceptor.InterceptorManager;
import org.jivesoftware.openfire.interceptor.PacketInterceptor;
import org.jivesoftware.openfire.interceptor.PacketRejectedException;
import org.jivesoftware.openfire.muc.MUCRole;
import org.jivesoftware.openfire.muc.MUCRoom;
import org.jivesoftware.openfire.muc.MultiUserChatManager;
import org.jivesoftware.openfire.muc.MultiUserChatService;
import org.jivesoftware.openfire.session.Session;
import org.jivesoftware.openfire.user.User;
import org.jivesoftware.openfire.user.UserManager;
import org.jivesoftware.util.JiveGlobals;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.forms.DataForm;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;

import com.yaowang.openfire.dao.MessageDao;
import com.yaowang.openfire.job.ChatNumberTask;
import com.yaowang.openfire.lansha.OfficialUserUtil;
import com.yaowang.openfire.lansha.RoomAdminUtil;
import com.yaowang.openfire.lansha.RoomMessageUtil;
import com.yaowang.openfire.lansha.UserRoomUtil;
import com.yaowang.openfire.lansha.dao.RoomBlacklistDao;
import com.yaowang.openfire.resource.RoomUtil;
import com.yaowang.openfire.resource.SystemPropertiesUtil;
import com.yaowang.openfire.robot.ImRobotUtil;
import com.yaowang.openfire.util.ChatString;
import com.yaowang.openfire.util.HttpUtil;
import com.yaowang.openfire.util.asynchronous.AsynchronousService;
import com.yaowang.openfire.util.asynchronous.ObjectCallable;

/**
 * 群消息推送至离线用户插件
 * @author shenl
 *
 */
public class YaowangChatPlugin implements PacketInterceptor, Plugin{
	private static final Logger log = LoggerFactory.getLogger(YaowangChatPlugin.class);
	
    private static PluginManager pluginManager;
    private UserManager userManager;
    private PresenceManager presenceManager;
    private MultiUserChatManager multiUserChatManager;
    private InterceptorManager interceptorManager;
//    private ComponentManager componentManager;
//    private SessionManager sessionManager;
    //是否推送至离线用户
    private static Boolean can2offline;
    //是否启用群消息离线推送
    private static Boolean shutdown;
    //聊天组配置
    private static String chatgroup;
    //是否输出debug信息
    public static Boolean debug;
    //dnw地址
    public static String dnwurl;
    //关键词
    private static String[] shields;
    private static String[] filtrations;
    private static String[] blacklists;
    //发言次数
    public static Map<String, Integer> chatMap = null;
    //房间黑名单是否启用
    private static final Boolean blacklistIsuse = "1".equals(SystemPropertiesUtil.get("im.blacklist.isuse"));

	@Override
	public void initializePlugin(PluginManager manager, File pluginDirectory) {
		//参数配置
		initProperties();
		if (shutdown) {
			log.info("YaowangChatPlugin 未启用");
			return;
		}
		
		if (pluginManager == null) {
			interceptorManager = InterceptorManager.getInstance();
			interceptorManager.addInterceptor(this);
			userManager = UserManager.getInstance();
			XMPPServer server = XMPPServer.getInstance(); 
			presenceManager = server.getPresenceManager();
			multiUserChatManager = server.getMultiUserChatManager();
			pluginManager = manager;

//			sessionManager = server.getSessionManager();
//			componentManager = ComponentManagerFactory.getComponentManager();
			
			log.info("YaowangChatPlugin 初始化完成");
		}
		//去除验证限制
		AuthCheckFilter.addExclude("yaowangchat/service/*");
		
		//定时器(半分钟提交一次)
		String url = SystemPropertiesUtil.get("im.callback.url");
		if (StringUtils.isNotBlank(url)) {
			chatMap = new HashMap<String, Integer>();
			Timer timer = new Timer();
			timer.schedule(new ChatNumberTask(), 30 * 1000, 30 * 1000);
		}
		//IM机器人启动
		ImRobotUtil.start();
	}
	/**
	 * 初始化参数
	 */
	public static void initProperties(){
		debug = JiveGlobals.getBooleanProperty("plugin.yaowangchat.debug", false);
		shutdown = JiveGlobals.getBooleanProperty("plugin.yaowangchat.shutdown", true);
		can2offline = JiveGlobals.getBooleanProperty("plugin.yaowangchat.can2offline", false);
		chatgroup = JiveGlobals.getProperty("plugin.yaowangchat.chatgroup");
		dnwurl = JiveGlobals.getProperty("plugin.yaowangchat.dnwurl");
		//关键字
		String shield = JiveGlobals.getProperty("plugin.yaowangchat.keyword.shield");
		String filtration = JiveGlobals.getProperty("plugin.yaowangchat.keyword.filtration");
		String blacklist = JiveGlobals.getProperty("plugin.yaowangchat.blacklist");
		
		shields = shield == null ? new String[0] : shield.split(",");
		filtrations = shield == null ? new String[0] : filtration.split(",");
		blacklists = blacklist == null ? new String[0] : blacklist.split(",");
	}
	/**
	 * 添加参数
	 * @param name
	 * @param value
	 */
	public static void addProperty(String name, String value){
		String blacklist = JiveGlobals.getProperty(name);
		if (StringUtils.isNotEmpty(blacklist)) {
			blacklist += ",";
		}
		blacklist += value;
		
		JiveGlobals.setProperty(name, blacklist);
		initProperties();
	}
	/**
	 * 删除黑名单
	 * @param name
	 * @param value
	 */
	public static void removeBlacklist(String value){
		StringBuilder builder = new StringBuilder();
		for (String str : blacklists) {
			if (str.equals(value)) {
				continue;
			}
			builder.append(str).append(",");
		}
		if (builder.length() > 0) {
			builder.setLength(builder.length() - 1);
		}
		
		JiveGlobals.setProperty("plugin.yaowangchat.blacklist", builder.toString());
		initProperties();
	}
    
	@Override
	public void destroyPlugin() {
		if (!shutdown) {
			interceptorManager.removeInterceptor(this);
			log.info("YaowangChatPlugin 已关闭");
		}
	}

	@Override
	public void interceptPacket(Packet packet, Session session, boolean incoming, boolean processed) throws PacketRejectedException {
        JID recipient = packet.getTo();
        if (recipient == null) {
        	return;
        }
        String username = recipient.getNode();
        // 广播消息或是不存在/没注册的用户.
        if (username == null) {
        	return;
        } else if (!recipient.getDomain().matches(".*"+XMPPServer.getInstance().getServerInfo().getXMPPDomain())) {
        	// 非当前openfire服务器信息
        	return;
        } else {
        	if (packet instanceof Message) {
        		Message message = (Message)packet;
        		//关键字过滤
        		messageFilter(message, incoming, processed);
        		
        		if (!"1".equals(SystemPropertiesUtil.get("im.unlinemessage.isuse"))) {
					return;
				}
	        	if (!isRegisteredUser(recipient)) {
	        		return;
		        } else if ("".equals(recipient.getResource())) {
		        	
		        }
	        	this.doAction(message, incoming, processed, session);
        	}
        }
	}
	/**
	 * 是否注册用户
	 * @param jid
	 * @return
	 */
	private Boolean isRegisteredUser(JID jid){
//		log.info("jid=" + jid);
//		return true;
		if (jid == null) {
			return false;
		}
		try {
			return userManager.isRegisteredUser(jid);
		} catch (Exception e) {
			return false;
		}
	}
	/**
	 * 关键字过滤
	 * @param message
	 */
	private void messageFilter(Message message, boolean incoming, boolean processed){
		//消息过滤
		String msg = message.getBody();
		
		if (StringUtils.isNotBlank(msg) && message.getType() == Message.Type.groupchat) {
			//发言人
			if (message.getFrom() != null && message.getFrom().getResource() != null && message.getFrom().getResource().equals(message.getFrom().getNode())) {
				//关键字屏蔽
				for (String keyword : shields) {
					Pattern pat = Pattern.compile(keyword);
					Matcher mat = pat.matcher(msg);
					boolean b = mat.find();
					if (b) {
						message.setBody("");
						return;
					}
				}
				
				//关键字过滤
				for (String keyword : filtrations) {
					Pattern pat = Pattern.compile(keyword);
					Matcher mat = pat.matcher(msg);
					//循环匹配字符串
					while (mat.find()) {
						String str = mat.group();
						msg = msg.replaceFirst(str, ChatString.getFiltration(str));
					}
				}
				
				MultiUserChatService service  = multiUserChatManager.getMultiUserChatService(message.getTo());
				Collection<MUCRole> roles = service.getMUCRoles(message.getFrom());
				if (!roles.isEmpty()) {
					//获取昵称
					MUCRole role = roles.toArray(new MUCRole[]{})[0];
					String from = role.getNickname();
					String roomName = message.getTo().getNode();
					String userId = RoomUtil.getUserId(from);
					
					if (StringUtils.isNotBlank(userId)) {
						//黑名单
						for (String keyword : blacklists) {
							if (StringUtils.isNotBlank(keyword)) {
								Pattern pat = Pattern.compile(keyword);
								Matcher mat = pat.matcher(userId);
								boolean b = mat.find();
								if (b) {
									message.setBody("");
									return;
								}
							}
						}
						
						//房间黑名单
						if (blacklistIsuse) {
							int count = RoomBlacklistDao.getRoomBlacklist(roomName, userId, new Date());
							if (count > 0) {
								message.setBody("");
								return;
							}
						}
						
						//统计发言次数
						if (chatMap != null) {
							//自己给自己统计
							String key = from + "|" + roomName;
							if (chatMap.containsKey(key)) {
								int count = chatMap.get(key);
								chatMap.put(key, count + 1);
							}else {
								chatMap.put(key, 1);
							}
						}
						
						//获取用户角色(0:普通用户 1:主播 2:房管  3:超管 4:官方)
						String userType = "0";
						if (UserRoomUtil.isRoomUser(roomName, userId)) {
							//主播
							userType = "1";
						}else if (RoomAdminUtil.isRoomAdmin(roomName, userId)) {
							//房管
							userType = "2";
						}else {
							Object type = OfficialUserUtil.isOfficialUser(userId);
							if (type != null) {
								if ("2".equals(type)) {
									//超管
									userType = "3";
								}else {
									//官方
									userType = "4";
								}
							}
						}
						//存放历史聊天记录
						RoomMessageUtil.put(roomName, msg, from, userType);
						
						//新增用户类型
//						Element user = new DOMElement("userType");
//						user.setText(userType);
//						DataForm userForm = new DataForm(user);
//						message.addExtension(userForm);
						
						Element properties = new DOMElement("properties");
//						properties.addAttribute("xmlns", "http://www.jivesoftware.com/xmlns/xmpp/properties");
						
						Element property = new DOMElement("property");
						properties.add(property);
						
						Element name = new DOMElement("name");
						name.addText("user_type");
						property.add(name);
						
						Element value = new DOMElement("value");
						value.addAttribute("type", "string");
						value.addText(userType);
						property.add(value);
						
						DataForm dataForm = new DataForm(properties);
						message.addExtension(dataForm);
					}
				}
			}
			
			message.setBody(msg);
		}
	}
	/**
	 * 
	 * @param packet
	 * @param incoming
	 * @param processed
	 * @param session
	 */
	private void doAction(Message message, boolean incoming, boolean processed, Session session) {
//		log.info("群聊天信息：" + message.getBody());
//		log.info("群聊天信息：" + message.getType());
//		log.info("群聊天信息：" + message.getTo());
			
		//
		if (message.getType() == Message.Type.chat) {
			//单聊
			if (can2offline) {
				//记录聊天消息
				saveMessage(message, message.getBody(), Message.Type.chat.toString(), message.getFrom(), Arrays.asList(message.getTo()));
			}
		}else if (message.getType() == Message.Type.groupchat) {
			//群聊
			if (processed || incoming) {  
                return;  
            }

//			List<?> els = message.getElement().elements("x");
//			// 非系统消息
//			if (els != null && !els.isEmpty()) {
			try {
				JID grouopJid = message.getFrom();
				if (StringUtils.isNotBlank(chatgroup) && !grouopJid.getDomain().matches(chatgroup + ".+")) {
					//非用户聊天组
					return;
				}
				
				JID roomJID = new JID(grouopJid.getNode() + "@" + grouopJid.getDomain());
				MUCRoom room = multiUserChatManager.getMultiUserChatService(roomJID).getChatRoom(grouopJid.getNode());

				if (room == null) {
					return;
				}

				String body = message.getBody();
				if (body == null) {
					return;
				}
				
				//接受者
				JID toJid = message.getTo();
				if (!(grouopJid.getResource().equals(toJid.getNode()) || grouopJid.getResource().matches(".+\\|"+toJid.getNode()+"\\|.*"))) {
					//如果发送者和接受者是同一个人,才处理发送离线消息处理
					return;
				}
//				if (debug) {
//					log.info(grouopJid.getNode() + "=" + grouopJid.getResource() + "=" + toJid.getNode() + "=" + toJid.getResource());
//				}
//				log.info("群聊天信息：" + body);
					
				//在群聊中用户
				Collection<MUCRole> roles = room.getOccupants();
				
				//重置消息类型
				message.setType(Message.Type.chat);
				//获取群内用户
				for (JID userJID : room.getMembers()) {
//					log.info("群用户：" + userJID);
					//如果打开着群，则不推送
					if (!isOnline(userJID, roles)) {
						//是否推送至离线用户
						if (!can2offline) {
							User user = userManager.getUser(userJID.getNode());
							if (!presenceManager.isAvailable(user)) {
//								log.info("离线用户不推送:" + userJID);
								//离线用户
								continue;
							}
						}
						
//						if (debug) {
//							log.info("推送群离线消息：" + userJID + "&body=" + message.getBody());
//						}
						//离线消息
						message.setTo(userJID);
						XMPPServer.getInstance().getRoutingTable().routePacket(userJID, message, true);
						
						//系统推送
//						sessionManager.sendServerMessage(userJID, room.getJID().toBareJID(), message.getBody());
					}
				}
				
				//记录聊天消息
				saveMessage(message, message.getBody(), Message.Type.groupchat.toString(), roomJID, room.getMembers());
				//重置消息类型
				message.setType(Message.Type.groupchat);
			} catch (Exception e) {
				log.info("exception=" + e.getMessage());
			}
		}
	}
	
	private Boolean isOnline(JID userJID, Collection<MUCRole> roles){
		for (MUCRole role : roles) {
			if (role.getUserAddress().toString().matches(userJID.toString() + ".*")) {
				//用户在线
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 截取用户名
	 * @param username
	 * @return
	 */
	public static final String getUsername(String username){
		if (username == null || username.indexOf("@") < 0) {
			return username;
		}
		return username.substring(0, username.indexOf("@"));
	}
	
	/**
	 * 保存离线消息
	 * @param body
	 * @return
	 */
	public void saveMessage(final Packet packet, final String body, final String type, final JID fromJid, final Collection<JID> toJids){		
		ObjectCallable callable = new ObjectCallable() {
			@Override
			public Object run() throws Exception {
				Element els = ((Message)packet).getElement().element("properties");
				List<Element> els1 = els.elements("property");
				String msg_type = "";
				String msg_formname = "";
				for (Element element : els1) {
					if ("msg_type".equals(element.element("name").getText())) {
						msg_type = element.element("value").getText();
					}else if ("msg_formname".equals(element.element("name").getText())) {
						msg_formname = element.element("value").getText();
					}
				}
				
				//接受者
				List<JID> getJid = new ArrayList<JID>();
				for (JID toJid : toJids) {
					User user = userManager.getUser(toJid.getNode());
					if (!presenceManager.isAvailable(user)) {
//						log.info("saveMessage");
						//离线用户推送
						getJid.add(toJid);
					}
				}
				if (!getJid.isEmpty()) {
					MessageDao.saveMessage(body, type, fromJid, getJid, msg_type, msg_formname);
					//通知推送
					HttpUtil.handleGet(dnwurl + "/mobile/chat/push.html");
				}
				return null;
			}
		};
		
		AsynchronousService.submit(callable);
	}
}
