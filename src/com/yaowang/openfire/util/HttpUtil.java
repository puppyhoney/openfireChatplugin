/**
 * 
 */
package com.yaowang.openfire.util;

import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;

/**
 * @author shenl
 *
 */
public class HttpUtil {
	
	/** 发送GET请求并获取服务器端返回值 
	 * @throws  
	 * @throws Exception
	 */
	public static String handleGet(String strUrl, Integer timeOut) throws ConnectTimeoutException, Exception {
		String result = null;
		//实例化get请求
		HttpGet request = new HttpGet(strUrl);
		//实例化客户端
		DefaultHttpClient client = new DefaultHttpClient();
		if (timeOut != null) {
			client.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, timeOut);
		}
		//执行该请求,得到服务器端的响应内容
		HttpResponse response = client.execute(request);
		if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
			//把响应结果转成String
			result = EntityUtils.toString(response.getEntity(), "UTF-8");
		}
		return result;
	}
	
	public static String handleGet(String strUrl) throws Exception {
		return handleGet(strUrl, null);
	}
	
	/** 携带一个params数据发送Post请求到指Url */
	public static String handlePost(String strUrl, List<NameValuePair> params) throws ConnectTimeoutException, Exception {
		CloseableHttpClient client = HttpClients.custom().build();
		HttpPost request = new HttpPost(strUrl);
		addHead(request);
		request.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
		CloseableHttpResponse response = client.execute(request);
		//把响应结果转成String
		String data = EntityUtils.toString(response.getEntity(), "UTF-8");
		
		if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
			response.close();
			return data;
		}else {
			response.close();
			return "";
		}
	}
	
	protected static void addHead(HttpRequestBase request){
		request.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		request.addHeader("Accept-Encoding", "gzip,deflate,sdch");
		request.addHeader("Accept-Language", "zh-CN,zh;q=0.8");
		request.addHeader("Cache-Control:", "no-cache");
		request.addHeader("Connection", "keep-alive");
		request.addHeader("Pragma", "no-cache");
		request.addHeader("Upgrade-Insecure-Requests", "1");
		request.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.80 Safari/537.36");
	}
	
	/**
	 * 判断字符串非空
	 * @param s
	 * @return
	 */
	public static boolean isNotEmpty(String s){
		if (s == null || "".equals(s)){
			return false;
		}
		return true;
	}
	/**
	 * 判断字符串为空
	 * @param s
	 * @return
	 */
	public static boolean isEmpty(String s){
		if (s == null || "".equals(s)){
			return true;
		}
		return false;
	}
	/**
	 * 按字节截取
	 * @param str
	 * @param len
	 * @return
	 */
	public static String cutOff(String str , int len){
		if (str.getBytes().length < len){
			return str;
		}
		char[] charArr = str.toCharArray();
		int charLength = charArr.length;  //字符数组
		StringBuilder reStr = new StringBuilder("");  //截取之后的字符串
		int k = 0;         //计算字节数
        for(int i=0; i < charLength && k < len ; i++){
            String s = String.valueOf(charArr[i]);  
            byte[] getBytes = s.getBytes();    
            k += getBytes.length;  
            if(k<=len){      //处理如："a我"，2的情况，只输出"a",而不是"a我"  
                reStr.append(charArr[i]);  
            }                     
        }    
		return reStr.toString()+"...";
	}
	/**
	 * url添加参数
	 * @param params
	 * @param name
	 * @param value
	 * @return
	 */
	public static List<NameValuePair> addParams(List<NameValuePair> params, final String name, final String value){
		params.add(new NameValuePair() {
			
			@Override
			public String getValue() {
				return value;
			}
			
			@Override
			public String getName() {
				return name;
			}
		});
		return params;
	}
	
}
