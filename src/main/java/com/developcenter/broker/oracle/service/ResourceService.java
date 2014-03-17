package com.developcenter.broker.oracle.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.developcenter.broker.oracle.common.Cache;
import com.developcenter.broker.oracle.common.ConfigUtil;
import com.developcenter.broker.oracle.common.OracleDbManager;
import com.developcenter.broker.oracle.common.Util;
import com.developcenter.broker.oracle.model.ServiceBinding;
import com.developcenter.broker.oracle.model.ServiceInstance;

@Service
public class ResourceService {
	@Autowired
	private ServiceInstanceService instanceService;

	@Autowired
	ServiceBindingService bindingService;
	private Log logger = LogFactory.getLog(ResourceService.class);

	/**
	 * 连接mysql 。存储broker的业务数据
	 */
	@Autowired
	JdbcTemplate jdbcTemplate;

	public void create(ServiceInstance instance, ServiceBinding binding) throws Exception {
		// TODO 设置回滚机制

		Connection oracleConnection = null;
		Connection brokerDbConnection = null;
		OracleDbManager dbManager = new OracleDbManager();
		
		try {
			brokerDbConnection = jdbcTemplate.getDataSource().getConnection();
			brokerDbConnection.setAutoCommit(false);

			/**
			 * oracle database
			 */
			oracleConnection = dbManager.getConnection();
			oracleConnection.setAutoCommit(false);

			/**
			 * ServiceInstace table
			 */
			String sql1 = "INSERT INTO serviceinstance (id,instancename,planid,date) VALUES(?,?,?,?)";
			PreparedStatement preStatement1 = brokerDbConnection
					.prepareStatement(sql1);
			preStatement1.setString(1, instance.getId());
			preStatement1.setString(2, instance.getInstancename());
			preStatement1.setString(3, instance.getPlanid());
			preStatement1.setString(4, Util.getSysTime());
			preStatement1.executeUpdate();
			/**
			 * service Binding table
			 */
			String userName = binding.getUsername();
			String password = binding.getPassword();
			String date = binding.getDate();
			String host = binding.getHost();
			String port = binding.getPort();
			String url = binding.getUrl();
			String instanceId = binding.getInstanceid();
			String bindingId = binding.getBindingid();
			String sql2 = "INSERT INTO servicebinding (username,password,host,port,url,date,instanceid,bindingid,appid) VALUES(?,?,?,?,?,?,?,?,?)";
			PreparedStatement preStatement2 = brokerDbConnection
					.prepareStatement(sql2);
			preStatement2.setString(1, userName);
			preStatement2.setString(2, password);
			preStatement2.setString(3, host);
			preStatement2.setString(4, port);
			preStatement2.setString(5, url);
			preStatement2.setString(6, date);
			preStatement2.setString(7, instanceId);
			preStatement2.setString(8, bindingId);
			preStatement2.setString(9, binding.getAppid());
			preStatement2.executeUpdate();

			/**
			 * create table space
			 */
			StringBuilder tableSpaceSql = new StringBuilder();
			String tabeSpaceSize = Cache.getSizeByPlanId(instance.getPlanid());
			if (null == tabeSpaceSize) {
				throw new IllegalArgumentException();
			}

			tableSpaceSql.append("create tablespace ")
					.append(instance.getInstancename())
					.append(" logging  datafile '")
					.append(ConfigUtil.getInstance().getOracleDbPath())
					.append(instance.getInstancename()).append(".dbf' size ")
					.append("32m").append(" autoextend on next 256M  maxsize ")
					.append(tabeSpaceSize).append(" autoallocate");

			PreparedStatement stmt = oracleConnection
					.prepareStatement(tableSpaceSql.toString());
			stmt.executeUpdate();

			/**
			 * create user,assign role
			 */
			String quota = Cache.getSizeByPlanId(instance.getPlanid());
			tableSpaceSql = new StringBuilder();
			tableSpaceSql.append(" create user ").append(userName)
					.append(" identified by ").append(password)
					.append(" default tablespace ")
					.append(instance.getInstancename()).append(" quota ")
					.append(quota).append(" on ")
					.append(instance.getInstancename());

			String roleName = ConfigUtil.getInstance().getOracleRoleName();

			StringBuilder roleSql = new StringBuilder().append("grant ")
					.append(roleName).append(" to ").append(userName);

			stmt = oracleConnection.prepareStatement(tableSpaceSql.toString());
			stmt.executeUpdate();
			stmt = oracleConnection.prepareStatement(roleSql.toString());
			stmt.executeUpdate();

			/**
			 * 提交
			 */
			brokerDbConnection.commit();
			oracleConnection.commit();
		} catch (Exception e) {
			logger.error(
					"ResourceService create user in oracle failed exception", e);
			try {
				oracleConnection.rollback();
				brokerDbConnection.rollback();
			} catch (SQLException e1) {
				logger.error(
						"ResourceService RollBack exception", e);
			}
			throw e;

		} finally {
			if (null != brokerDbConnection) {
				try {
					brokerDbConnection.close();
				} catch (SQLException e) {
					logger.error(
							"ResourceService  close broker db connection failed",
							e);
				}
			}
			if (null != oracleConnection) {
				try {
					oracleConnection.close();
				} catch (SQLException e) {
					logger.error(
							"ResourceService  close oracle connection failed",
							e);
				}
			}
		}

	}
	
	public void delete(ServiceInstance instance, ServiceBinding serviceBinding) throws Exception{
		Connection oracleConnection = null;
		Connection brokerDbConnection = null;
		OracleDbManager dbManager = new OracleDbManager();
		
		try {
			/**
			 * db broker connection
			 */
			brokerDbConnection = jdbcTemplate.getDataSource().getConnection();
			brokerDbConnection.setAutoCommit(false);

			/**
			 * oracle database
			 */
			oracleConnection = dbManager.getConnection();
			oracleConnection.setAutoCommit(false);
			
			
			String tagServiceBindingDeleteSql = "update servicebinding set deleted='Y' where bindingid=?";
			PreparedStatement preStatment=brokerDbConnection.prepareCall(tagServiceBindingDeleteSql);
			preStatment.setString(1, serviceBinding.getAppid());
			preStatment.executeUpdate();  
			
			String tagServiceInstanceDeleteSql = "update serviceinstance set deleted='Y' where id = ?";

			PreparedStatement preStatment1=brokerDbConnection.prepareCall(tagServiceInstanceDeleteSql);
			preStatment1.setString(1, instance.getId());
			preStatment1.executeUpdate(); 
			
			String dropUserSql = "drop user " + serviceBinding.getUsername()+" cascade";
			PreparedStatement stmt = oracleConnection.prepareStatement(dropUserSql);
			stmt.executeUpdate();
			brokerDbConnection.commit();
			oracleConnection.commit();
		}
		catch (Exception e) {
			logger.error(
					"ResourceService delete user and db failed exception", e);
			try {
				oracleConnection.rollback();
				brokerDbConnection.rollback();
			} catch (SQLException e1) {
				logger.error(
						"ResourceService RollBack exception", e);
			}
      throw e;
		} finally {
			if (null != brokerDbConnection) {
				try {
					brokerDbConnection.close();
				} catch (SQLException e) {
					logger.error(
							"ResourceService  close broker db connection failed",
							e);
				}
			}
			if (null != oracleConnection) {
				try {
					oracleConnection.close();
				} catch (SQLException e) {
					logger.error(
							"ResourceService  close oracle connection failed",
							e);
				}
			}
		}

	  
	}
}
