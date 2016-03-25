package com.yaowang.openfire.util;

import java.util.Date;

import net.sf.json.JSONArray;
import net.sf.json.JsonConfig;

public class JsonUtil {
	private final static JsonConfig config = new JsonConfig();
	static{
		config.registerJsonValueProcessor(Date.class, new DateJsonBeanProcessor()); 
	}
    /**
     * 转换成json格式
     * @param entity
     * @return
     */
    public static String toJSONString(Object...entity){
    	String json = JSONArray.fromObject(entity, config).toString();
        if (json.startsWith("[[")){
            json = json.substring(1);
        }
        
        if (json.endsWith("]]")){
            json = json.substring(0, json.length()-1);
        }
        
        return json;
    }
}
