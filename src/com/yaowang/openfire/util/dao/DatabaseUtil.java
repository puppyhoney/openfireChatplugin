package com.yaowang.openfire.util.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbcp.BasicDataSource;

import com.yaowang.openfire.util.ResourcesLoad;

public class DatabaseUtil {
	protected static final BasicDataSource dataSource = new BasicDataSource();
	
	static{
		Map<String, String> map = ResourcesLoad.load("/conf/database.properties");
		dataSource.setDriverClassName(map.get("datasource.driverClassName"));
		dataSource.setUrl(map.get("datasource.url"));
		dataSource.setUsername(map.get("datasource.username"));
		dataSource.setPassword(map.get("datasource.password"));
	}
	
	public static BasicDataSource getDataSource(){
		return dataSource;
	}
	
	public static Integer update(String sql, List<Object[]> args) throws SQLException{
		Connection conn = dataSource.getConnection();
		try {
			conn.setAutoCommit(false); 
			PreparedStatement pstmt = conn.prepareStatement(sql);
			for (Object[] objects : args) {
				for (int i = 0; i < objects.length; i++) {
					pstmt.setObject(i + 1, objects[i]); 
				}
				//加入批处理 
				pstmt.addBatch(); 
			}
			pstmt.executeBatch(); 
			conn.commit();
		} finally{
			conn.close();
		}
        return args.size();
	}
	
	public static Integer queryForInt(String sql, List<Object> args) throws SQLException{
		Connection conn = dataSource.getConnection();
		try {
			conn.setAutoCommit(false); 
			PreparedStatement pstmt = conn.prepareStatement(sql);
			if (args != null) {
				for (int i = 0; i < args.size(); i++) {
					pstmt.setObject(i + 1, args.get(i)); 
				}
			}
			ResultSet rs= pstmt.executeQuery();
			if (rs.next()) {
				return rs.getInt(1);
			}else {
				return 0;
			}
		} finally{
			conn.close();
		}
	}
	
	public static String queryForString(String sql, List<Object> args) throws SQLException{
		Connection conn = dataSource.getConnection();
		try {
			conn.setAutoCommit(false); 
			PreparedStatement pstmt = conn.prepareStatement(sql);
			if (args != null) {
				for (int i = 0; i < args.size(); i++) {
					pstmt.setObject(i + 1, args.get(i)); 
				}
			}
			ResultSet rs= pstmt.executeQuery();
			if (rs.next()) {
				return rs.getString(1);
			}else {
				return null;
			}
		} finally{
			conn.close();
		}
	}
	
	public static Object[] queryForObjs(String sql, List<Object> args, Integer length) throws SQLException{
		Connection conn = dataSource.getConnection();
		try {
			conn.setAutoCommit(false); 
			PreparedStatement pstmt = conn.prepareStatement(sql);
			if (args != null) {
				for (int i = 0; i < args.size(); i++) {
					pstmt.setObject(i + 1, args.get(i)); 
				}
			}
			ResultSet rs= pstmt.executeQuery();
			Object[] obs = new Object[length];
			if (rs.next()) {
				for (int i = 0; i < obs.length; i++) {
					obs[i] = rs.getObject(i+1);
				}
				return obs;
			}else {
				return null;
			}
		} finally{
			conn.close();
		}
	}
	
	public static List<Object> queryForList(String sql, List<Object> args) throws SQLException{
		Connection conn = dataSource.getConnection();
		try {
			conn.setAutoCommit(false); 
			PreparedStatement pstmt = conn.prepareStatement(sql);
			if (args != null) {
				for (int i = 0; i < args.size(); i++) {
					pstmt.setObject(i + 1, args.get(i)); 
				}
			}
			ResultSet rs= pstmt.executeQuery();
			List<Object> list = new ArrayList<Object>();
			while (rs.next()) {
				list.add(rs.getObject(1));
			}
			return list;
		} finally{
			conn.close();
		}
	}
	
	public static List<Object[]> queryForLists(String sql, List<Object> args, Integer length) throws SQLException{
		Connection conn = dataSource.getConnection();
		try {
			conn.setAutoCommit(false); 
			PreparedStatement pstmt = conn.prepareStatement(sql);
			if (args != null) {
				for (int i = 0; i < args.size(); i++) {
					pstmt.setObject(i + 1, args.get(i)); 
				}
			}
			ResultSet rs= pstmt.executeQuery();
			List<Object[]> list = new ArrayList<Object[]>();
			while (rs.next()) {
				Object[] obs = new Object[length];
				for (int i = 0; i < obs.length; i++) {
					obs[i] = rs.getObject(i+1);
				}
				list.add(obs);
			}
			return list;
		} finally{
			conn.close();
		}
	}
	
	public static Map<Object, Object> queryForMap(String sql, List<Object> args) throws SQLException{
		Map<Object, Object> map = new HashMap<Object, Object>();
		try {
			Connection conn = dataSource.getConnection();
			try {
				conn.setAutoCommit(false); 
				PreparedStatement pstmt = conn.prepareStatement(sql);
				if (args != null) {
					for (int i = 0; i < args.size(); i++) {
						pstmt.setObject(i + 1, args.get(i)); 
					}
				}
				ResultSet rs= pstmt.executeQuery();
				while (rs.next()) {
					map.put(rs.getObject(1), rs.getObject(2));
				}
				return map;
			} finally{
				conn.close();
			}
		} catch (Exception e) {
			return map;
		}
	}
	/**
	 * 随机获取权重
	 * @return
	 */
	public static Object getWeight(){
		return Math.random() * 10;
	}
}
