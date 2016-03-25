package com.yaowang.openfire.lansha;

import java.util.List;
import java.util.Map;

import com.yaowang.openfire.lansha.dao.RoomAdminDao;

/**
 * 房管
 * @author shenl
 *
 */
public class RoomAdminUtil {
	private static Map<String, List<Object>> userMap = RoomAdminDao.getAdminMap();
	/**
	 * 是否是房管
	 * @param userId
	 * @return
	 */
	public static Boolean isRoomAdmin(String roomName, String userId){
		if (userMap.containsKey(roomName)) {
			List<Object> uids = userMap.get(roomName);
			return uids != null && uids.contains(userId);
		}else {
			//不存在的主播,查询数据库
			List<Object> uids = RoomAdminDao.getAdminList(roomName);
			userMap.put(roomName, uids);
			return uids != null && uids.contains(userId);
		}
	}
	/**
	 * 删除房间管理员
	 * @param roomName
	 */
	public static void remove(String roomName){
		userMap.remove(roomName);
	}
}
