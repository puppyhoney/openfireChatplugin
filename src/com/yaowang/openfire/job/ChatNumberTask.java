package com.yaowang.openfire.job;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

import org.apache.http.NameValuePair;

import com.yaowang.openfire.plugin.YaowangChatPlugin;
import com.yaowang.openfire.resource.SystemPropertiesUtil;
import com.yaowang.openfire.util.HttpUtil;
import com.yaowang.openfire.util.JsonUtil;

public class ChatNumberTask extends TimerTask{
	@Override
	public void run() {
		//定时推送消息
		if (!YaowangChatPlugin.chatMap.isEmpty()) {
			Map<String, Integer> map = new HashMap<String, Integer>(YaowangChatPlugin.chatMap);
			YaowangChatPlugin.chatMap.clear();
			
			try {
				//转换json
				String value = JsonUtil.toJSONString(map);
				value = value.substring(1, value.length() - 1);
				value = URLEncoder.encode(value, "utf-8");
				
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				HttpUtil.addParams(params, "name", value);
				
				String res = "";
				while (true) {
					try {
						res = HttpUtil.handlePost(SystemPropertiesUtil.get("im.callback.url"), params);
						if("1".equals(res)){
							break;
						}else {
							Thread.sleep(5 * 1000);
						}
					} catch (Exception e) {
						
					}
				}
			} catch (Exception e) {
				//出错，不处理
//				e.printStackTrace();
			}
		}
	}
}
