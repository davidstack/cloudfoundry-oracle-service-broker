package com.developcenter.broker.oracle.common;

public class Constants {
	public static final int MAX_ACTIVE_CONN = 5;
	public static final String DB2_VALIDATE_SQL = "SELECT COUNT(*) FROM SYSIBM.SYSTABLES";
	public static final String ORACLE_VALIDATE_SQL = "SELECT 1 FROM DUAL";
	public static final String DEFAULT_VALIDATE_SQL = "SELECT 1";
}
