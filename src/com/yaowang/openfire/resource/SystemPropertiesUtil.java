package com.yaowang.openfire.resource;

import java.util.Map;

import com.yaowang.openfire.util.ResourcesLoad;

public class SystemPropertiesUtil {
	private static Map<String, String> resource = ResourcesLoad.load("/conf/system.properties");
	
	public static String get(String key){
		return resource.get(key);
	}
}
