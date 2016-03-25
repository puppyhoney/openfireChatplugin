package com.yaowang.openfire.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
/**
 * 自动加载配置文件类.能够分开发环境和部署环境两种
 * 开发环境能够做到定时刷新里面的值
 * @author shenl
 *
 */
public class ResourcesLoad {
	private final Map<String, String> map = new ConcurrentHashMap<String, String>();//存放对应属性值
	private String url;
	private ResourcesLoad(){}
	
	/**
	 * 初始化
	 * @param url
	 * @return
	 */
	public static Map<String, String> load(String url) {
		ResourcesLoad load = new ResourcesLoad();
		load.url = url;
		try {
			load.init();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return load.map;
	}
	/**
	 * 初始化
	 * @param url
	 * @return
	 */
	public static Properties loadProperties(String url){
		Map<String, String> map = load(url);
		Properties properties = new Properties();
		properties.putAll(map);
		return properties;
	}
	
	/**
	 * 初始化语法
	 * @throws IOException
	 */
	private void init() throws IOException {
		map.clear();
		InputStream is = this.getClass().getClassLoader().getResourceAsStream(url); 
		readProperties(map, is);
	}
	
	/**
	 * 读取配置文件
	 * @param in
	 * @return
	 * @throws IOException 
	 * @throws  
	 */
	public static Map<String, String> readProperties(Map<String, String> map, InputStream in) throws IOException {
		Properties properties = new Properties();
		try {
			properties.load(new InputStreamReader(in, "utf-8"));
		}finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		Enumeration<Object> it = (Enumeration<Object>) properties.keys();
		while (it.hasMoreElements()) {
			String key = (String)it.nextElement();
			String t = (String) properties.get(key);
			if (map.containsKey(key)) {
				//重复
			}else {
				map.put(key.trim(), t);
			}
		}
		
		return map;
	}
}