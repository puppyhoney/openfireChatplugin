package com.yaowang.openfire.lansha;

import java.util.Map;

import com.yaowang.openfire.lansha.dao.RoomDao;
import com.yaowang.openfire.robot.ImRobotUtil;

public class UserRoomUtil {
	/**
	 * 房间对应主播
	 */
	private static Map<Object, Object> roomMap = RoomDao.getRoomUsers();
	/**
	 * 是否是主播
	 * @param userId
	 * @return
	 */
	public static Boolean isRoomUser(String roomName, String userId){
		if (roomMap.containsKey(roomName)) {
			String uid = roomMap.get((Object)roomName).toString();
			return uid != null && uid.equals(userId);
		}else {
			//不存在的主播,查询数据库
			String uid = RoomDao.getRoomUserByRoom(roomName);
			roomMap.put(roomName, uid);
			//重新加载所有房间
			ImRobotUtil.reload();
			return uid != null && uid.equals(userId);
		}
	}
}
