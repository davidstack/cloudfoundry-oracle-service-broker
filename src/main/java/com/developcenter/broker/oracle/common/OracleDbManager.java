package com.developcenter.broker.oracle.common;

import java.sql.Connection;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class OracleDbManager {
	private static Log logger = LogFactory.getLog(OracleDbManager.class);
	private static BasicDataSource bds = new BasicDataSource();

	static
	{
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
		} catch (Exception e) {
			logger.error("OracleDbManager register OracleDriver fail", e);
		}
		bds.setDriverClassName("oracle.jdbc.driver.OracleDriver");
		bds.setUrl(ConfigUtil.getInstance().getOracleUrl());
		bds.setUsername(ConfigUtil.getInstance().getOracleDbUserName());
		bds.setPassword(ConfigUtil.getInstance().getOracleDbPassWord());
		int max = Constants.MAX_ACTIVE_CONN;
		bds.setMaxActive(max);
		bds.setMaxIdle(max / 2);
		bds.setMaxWait(-1);
		String sql = Constants.ORACLE_VALIDATE_SQL;
		bds.setValidationQuery(sql);
	}
	public OracleDbManager() {
	
	}

	public Connection getConnection() throws Exception {

		return bds.getConnection();
	}
	
	
	public static void main(String[] args){
		OracleDbManager dm = new OracleDbManager();
		Connection conn = null;
		try {
			conn = dm.getConnection();
			System.out.println(conn);
			conn.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}