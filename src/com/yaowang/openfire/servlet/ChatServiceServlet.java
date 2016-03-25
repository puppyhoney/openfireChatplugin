package com.yaowang.openfire.servlet;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.jasper.runtime.JspSourceDependent;
import org.jivesoftware.util.JiveGlobals;
import org.jivesoftware.util.WebManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.yaowang.openfire.lansha.OfficialUserUtil;
import com.yaowang.openfire.lansha.RoomAdminUtil;
import com.yaowang.openfire.lansha.RoomMessageUtil;
import com.yaowang.openfire.plugin.YaowangChatPlugin;
import com.yaowang.openfire.servlet.util.MessageUtil;
import com.yaowang.openfire.servlet.util.RoomUtil;
/**
 * 聊天服务
 * @author shenl
 *
 */
@SuppressWarnings("unchecked")
public class ChatServiceServlet extends BaseServlet implements JspSourceDependent{
	private static final long serialVersionUID = 1L;
	private static final Logger log = LoggerFactory.getLogger(ChatServiceServlet.class);
	//token
	private static final String TOKEN = JiveGlobals.getProperty("plugin.yaowangchat.token");
	
	@Override
	public void init() throws ServletException {
		log.info("init");
		super.init();
	}
	
	@Override
	public Map<String, Long> getDependants() {
		log.info("getDependants");
		return null;
	}
	
	@Override
	public void _jspService(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String token = request.getParameter("token");
		if (!TOKEN.equals(token)) {
			write(request, response, getFailed("token error"));
			return;
		}
		
		String method = request.getParameter("method");
		if (StringUtils.isEmpty(method)) {
			write(request, response, getFailed("call error,method not find:method=" + method));
			return;
		}
		
		WebManager webManager = new WebManager();
		webManager.init(request, response, request.getSession(), request.getServletContext());
		Class<?> cls = this.getClass();
		try {
			Method method1 = cls.getMethod(method, HttpServletRequest.class, HttpServletResponse.class, WebManager.class);
			Object o = method1.invoke(this, request, response, webManager);
			if (o == null) {
				//成功
				write(request, response, EMPTY_ENTITY);
			}else if (o instanceof String) {
				write(request, response, getFailed(o.toString()));
			}else if (o instanceof HashMap) {
				writerEntity(request, response, (HashMap<String, Object>)o);
			}
		} catch (NoSuchMethodException e) {
			write(request, response, getFailed("interface not find:" + e.getMessage()));
			return;
		} catch (Exception e) {
			write(request, response, getFailed("call error:" + e.getMessage()));
			e.printStackTrace();
			return;
		}
	}
	/**
	 * 添加用户
	 * @param request
	 * @param response
	 * @throws IOException 
	 */
	public Object save(HttpServletRequest request, HttpServletResponse response, WebManager webManager) throws Exception{
		return RoomUtil.addUser(request, webManager);
	}

	/**
	 * 删除用户
	 * @param request
	 * @param response
	 * @throws IOException 
	 */
	public Object remove(HttpServletRequest request, HttpServletResponse response, WebManager webManager) throws IOException{
		return RoomUtil.removeUser(request, webManager);
	}
	/**
	 * 创建房间
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	public Object createRoom(HttpServletRequest request, HttpServletResponse response, WebManager webManager) throws IOException{
		return RoomUtil.createRoom(request, webManager);
	}
	/**
	 * 销毁房间
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	public Object destroyRoom(HttpServletRequest request, HttpServletResponse response, WebManager webManager) throws IOException{
		return RoomUtil.destroyRoom(request, webManager);
	}
	/**
	 * 获取在线用户
	 * @param request
	 * @param response
	 * @throws IOException 
	 */
	public Object online(HttpServletRequest request, HttpServletResponse response, WebManager webManager) throws IOException{
		return RoomUtil.online(request, webManager);
	}
	/**
	 * 房间发送消息
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	public Object groupchatMessage(HttpServletRequest request, HttpServletResponse response, WebManager webManager) throws Exception{
		return MessageUtil.groupchatMessage(request, webManager);
	}
	/**
	 * 发送私聊
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	public Object chatMessage(HttpServletRequest request, HttpServletResponse response, WebManager webManager) throws Exception{
		return MessageUtil.chatMessage(request, webManager);
	}
	/**
	 * 插件参数刷新
	 * @param request
	 * @param response
	 * @param webManager
	 * @return
	 * @throws Exception
	 */
	public Object plguinRefresh(HttpServletRequest request, HttpServletResponse response, WebManager webManager) throws Exception{
		YaowangChatPlugin.initProperties();
		return null;
	}
	/**
	 * 添加黑名单
	 * @param request
	 * @param response
	 * @param webManager
	 * @return
	 * @throws Exception
	 */
	public Object addBlacklist(HttpServletRequest request, HttpServletResponse response, WebManager webManager) throws Exception{
		String name = request.getParameter("name");
		if (StringUtils.isNotBlank(name)) {
			YaowangChatPlugin.addProperty("plugin.yaowangchat.blacklist", ".+\\|" + name + "\\|.+");
		}
		return null;
	}
	/**
	 * 删除黑名单
	 * @param request
	 * @param response
	 * @param webManager
	 * @return
	 * @throws Exception
	 */
	public Object removeBlacklist(HttpServletRequest request, HttpServletResponse response, WebManager webManager) throws Exception{
		String name = request.getParameter("name");
		if (StringUtils.isNotBlank(name)) {
			YaowangChatPlugin.removeBlacklist(".+\\|" + name + "\\|.+");
		}
		
		return null;
	}
	/**
	 * 刷新房管
	 * @param request
	 * @param response
	 * @param webManager
	 * @return
	 */
	public Object clearRoomAdmin(HttpServletRequest request, HttpServletResponse response, WebManager webManager){
		//刷新房管
		String name = request.getParameter("name");
		RoomAdminUtil.remove(name);
		return null;
	}
	/**
	 * 刷新官方账户
	 * @param request
	 * @param response
	 * @param webManager
	 * @return
	 */
	public Object clearOfficialUser(HttpServletRequest request, HttpServletResponse response, WebManager webManager){
		//刷新官方用户
		OfficialUserUtil.clear();
		return null;
	}
	/**
	 * 获取消息
	 * @param request
	 * @param response
	 * @param webManager
	 * @return
	 */
	public Object getMessage(HttpServletRequest request, HttpServletResponse response, WebManager webManager){
		String name = request.getParameter("name");
		String length = request.getParameter("length");
		List<Object[]> list = null;
		if (StringUtils.isEmpty(length)) {
			list = RoomMessageUtil.get(name, null);
		}else {
			list = RoomMessageUtil.get(name, Integer.valueOf(length));
		}
		if (list == null) {
			return null;
		}
		
		return JSON.toJSONString(list, SerializerFeature.WriteDateUseDateFormat);
	}
}
