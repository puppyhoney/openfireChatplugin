package com.yaowang.openfire.lansha;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 存放历史聊天记录(用户类型，昵称，消息，时间)
 * @author shenl
 *
 */
public class RoomMessageUtil {
	private static Map<String, LinkedList<Object[]>> messageMap = new HashMap<String, LinkedList<Object[]>>();
	private static Integer maxlength = 100;

	/**
	 * 放入房间消息
	 * @param roomName
	 * @param message
	 */
	public static void put(String roomName, String message, String nickName, String userType){
		if (messageMap.containsKey(roomName)) {
			LinkedList<Object[]> list = messageMap.get(roomName);
			if (list.size() >= maxlength) {
				//超过最大值
				list.removeFirst();
			}
			list.add(new Object[]{userType, nickName, message, new Date()});
		}else {
			LinkedList<Object[]> list = new LinkedList<Object[]>();
			list.add(new Object[]{userType, nickName, message, new Date()});
			messageMap.put(roomName, list);
		}
	}
	/**
	 * 获取房间消息
	 * @param roomName
	 * @return
	 */
	public static List<Object[]> get(String roomName, Integer length){
		LinkedList<Object[]> list = messageMap.get(roomName);
		if (length == null) {
			return list;
		}
		if (list == null) {
			return null;
		}
		if (list.size() <= length) {
			return list;
		}else {
			return list.subList(list.size() - length, list.size());
		}
	}
}
