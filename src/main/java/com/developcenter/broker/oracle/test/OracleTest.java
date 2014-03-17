package com.developcenter.broker.oracle.test;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;

public class OracleTest {
	public static void main(String[] args) {
	Connection ct = null;
	CallableStatement cs = null;
	try {
		Class.forName("oracle.jdbc.driver.OracleDriver");
		ct = DriverManager.getConnection(
				"jdbc:oracle:thin:@10.41.2.110:1521:orcl", "system", "Admin123");
		ct.setAutoCommit(false);
		cs = ct.prepareCall("{call yao5(?,?,?,?,?,?)}");
		cs.setString(1, "emp");
		cs.setInt(2, 5);
		cs.setInt(3, 1);
		cs.registerOutParameter(4, oracle.jdbc.OracleTypes.INTEGER);
		cs.registerOutParameter(5, oracle.jdbc.OracleTypes.INTEGER);
		cs.registerOutParameter(6, oracle.jdbc.OracleTypes.CURSOR);
		cs.execute();   
		int geshu = cs.getInt(4);
		int yeshu = cs.getInt(5);
		ResultSet ji1 = (ResultSet) cs.getObject(6);
		System.out.println(geshu);
		System.out.println(yeshu);
		
		while (ji1.next()) {
		
			System.out.println(ji1.getInt(1) + ji1.getString(2));
		}
	} catch (Exception e) {
		e.printStackTrace();
	} finally {
		try {
			cs.close();
			ct.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

}
