package com.yaowang.openfire.servlet;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspFactory;
import javax.servlet.jsp.JspWriter;

import org.apache.jasper.runtime.HttpJspBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.yaowang.openfire.plugin.YaowangChatPlugin;
import com.yaowang.openfire.util.JsonUtil;

public abstract class BaseServlet extends HttpJspBase{
	private static final long serialVersionUID = 1L;
	private static final Logger log = LoggerFactory.getLogger(BaseServlet.class);
	
	/**
	 * 空数据
	 */
	protected static final String EMPTY_ARRAY = "{\"status\": 1, \"data\": []}";
	protected static final String EMPTY_ENTITY = "{\"status\": 1, \"data\": {}}";
	protected static final String EMPTY_STRING = "{\"status\": 1, \"data\": \"\"}";
	
	/**
	 * 错误消息
	 * @param failed
	 * @return
	 */
	protected String getFailed(String...failed){
		StringBuilder builder = new StringBuilder("{\"status\": 0, \"failed\": \"");
		for (String string : failed) {
			builder.append(string);
		}
		builder.append("\"}");
		return builder.toString();
	}
    /**
     * 往客户端写数据
     */
    protected void write(HttpServletRequest request, HttpServletResponse response, String...strs) throws IOException{
    	StringBuilder builder = new StringBuilder();
    	for (String str : strs) {
    		builder.append(str);
		}
    	write(request, response, builder.toString());
    }
	/**
	 * 往客户端写数据
	 * @param request
	 * @param response
	 * @param str
	 * @throws IOException
	 */
	protected void write(HttpServletRequest request, HttpServletResponse response, String str) throws IOException{
		JspWriter out = JspFactory.getDefaultFactory().getPageContext(this, request, response, null, true, 8192, true).getOut();
//    	String encoding = request.getCharacterEncoding();
//        response.setContentType("text/html;charset=" + encoding);
//        Writer writer = response.getWriter();
        try {
//        	//unicode转码
//            writer.write(StringEscapeUtils.escapeJava(str));
//        	out.write(URLEncoder.encode(str, "utf-8"));
				out.write(str);
        } finally{
        	out.flush();
        	out.close();
        }
        
        if (YaowangChatPlugin.debug) {
        	log.info(str);
		}
    }
    /**
     * 按照json格式输出返回
     * @param entity
     * @throws IOException 
     */
    public void writeJson(HttpServletRequest request, HttpServletResponse response, Object...entity) throws IOException{
    	if (entity == null){
            write(request, response, EMPTY_ENTITY);
        }else{
        	String json = JsonUtil.toJSONString(entity);
            write(request, response, "{\"status\": 1, \"data\": ", json, "}");
        }
    }
    /**
     * 对象返回
     * @param entity
     * @throws IOException
     */
    public void writerEntity(HttpServletRequest request, HttpServletResponse response, Map<String, Object> map) throws IOException{
    	StringBuilder builder = new StringBuilder();
    	for (String key : map.keySet()) {
    		if (builder.length() > 0) {
				builder.append(", ");
			}
			builder.append("\"").append(key).append("\": ");
			Object o = map.get(key);
			boolean b = (o instanceof String);
			if (b) {
				builder.append(JSON.toJSONString(o));
			}else {
				if (o instanceof Integer || o instanceof Long) {
					builder.append(o);
				}else {
					builder.append(JsonUtil.toJSONString(o));
				}
			}
		}
    	builder.insert(0, "{\"status\": 1, \"data\": {");
    	builder.append("}}");
    	map.clear();
    	map = null;
    	write(request, response, builder.toString());
    }
}
