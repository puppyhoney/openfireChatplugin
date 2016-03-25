package com.yaowang.openfire.robot;

import java.util.Calendar;
import java.util.List;

import org.jivesoftware.openfire.XMPPServer;

import com.yaowang.openfire.lansha.dao.RoomDao;
import com.yaowang.openfire.servlet.util.RoomUtil;
import com.yaowang.openfire.util.asynchronous.AsynchronousService;

public class ImRobotUtil implements Runnable{
	/**
	 * 机器人房间
	 */
	private static List<Object> robotRoom = null;

	public static void start(){
		reload();
		Thread thread = new Thread(new ImRobotUtil());
		thread.start();
		System.out.println("=========> IM 机器人已启动");
	}
	
	public static void reload(){
		robotRoom = RoomDao.getOnlineRooms();
	}

	@Override
	public void run() {
		while (true) {
			try {
				//避免中途修改后，错乱
				Object[] obs = robotRoom.toArray();
				for (Object room : obs) {
					String roomName = room.toString() + "@conference." + XMPPServer.getInstance().getServerInfo().getXMPPDomain();
					//在线人数
					Integer number = RoomUtil.getOnline(roomName);
					if (number == null || number <= 0) {
						continue;
					}
					Calendar calendar = Calendar.getInstance();
					int time = calendar.get(Calendar.MINUTE);
					
					if (number > 50 
							|| (number > 25 && time % 2 == 0) 
							|| (number > 10 && time % 3 == 0)
							|| time % 5 == 0) {
						// 50人以上，每分钟发言。25人以上，每两分钟发一次。10人以上，每3分钟发一次，其他，每5分钟发送一次
						RoomRobotCallable callable = new RoomRobotCallable();
						callable.setData(roomName);
						AsynchronousService.submit(callable);
					}
				}
				Thread.sleep(1 * 60 * 1000);
			} catch (Exception e) {
				//正常情况,防止机器人线程被挂掉
			}
		}
	}
	
//	/**
//	 * 获取房间是否在运行
//	 * @param room
//	 * @return
//	 */
//	public static Boolean isRun(String room){
//		return robotRoom.contains(room);
//	}
}
