package com.developcenter.broker.oracle.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.developcenter.broker.oracle.common.Cache;
import com.developcenter.broker.oracle.common.ConfigUtil;
import com.developcenter.broker.oracle.common.OracleDbManager;
import com.developcenter.broker.oracle.common.Util;
import com.developcenter.broker.oracle.model.ServiceBinding;
import com.developcenter.broker.oracle.model.ServiceInstance;

/**
 * Author: Sridharan Kuppa sridharan.kuppa@gmail.com Date: 12/12/13
 */
@Service
@Transactional
public class ServiceBindingService implements EnvironmentAware {
	@Autowired
	JdbcTemplate jdbcTemplate;

	@Autowired
	ServiceInstanceService instanceService;

	Environment environment;
	private Log logger = LogFactory.getLog(ServiceBindingService.class);

	@Override
	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}

	/**
	 * 
	 * @param bindingId
	 * @param serviceInstance
	 *            将bindingId 与serviceInstance 绑定
	 * @return
	 */
	public ServiceBinding constructServiceBinding(String bindingId,String appId,
			ServiceInstance serviceInstance) {

		ServiceBinding serviceBinding = new ServiceBinding();
		serviceBinding.setBindingid(bindingId);
		serviceBinding.setDate(Util.getSysTime());
		serviceBinding.setInstanceid(serviceInstance.getId());
		serviceBinding.setPassword(Util.getRandomString(10));
		serviceBinding.setUsername("C##" + serviceInstance.getInstancename()); // 用户名与
		serviceBinding.setAppid(appId);																		// 表空间名，表空间文件一致
		serviceBinding.setUrl(ConfigUtil.getInstance().getOracleUrl());
		return serviceBinding;
	}

	/**
	 * 
	 * @param binding
	 * @throws Exception
	 */
	public void create(ServiceBinding binding, ServiceInstance serviceInstance)
			throws Exception {

		try {

			String userName = binding.getUsername();
			String password = binding.getPassword();
			String date = binding.getDate();
			String host = binding.getHost();
			String port = binding.getPort();
			String url = binding.getUrl();
			String instanceId = binding.getInstanceid();
			String bindingId = binding.getBindingid();

			String sql = "INSERT INTO servicebinding (username,password,host,port,url,date,instanceid,bindingid,appid) VALUES(?,?,?,?,?,?,?,?,?)";
			Object[] params = new Object[] { userName, password, host, port,
					url,date, instanceId, bindingId,binding.getAppid() };
			int[] types = new int[] { Types.VARCHAR, Types.VARCHAR,
					Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR,
					Types.VARCHAR, Types.VARCHAR, Types.VARCHAR };

			jdbcTemplate.update(sql, params, types);

			String quota = Cache.getSizeByPlanId(serviceInstance.getPlanid());

			/**
			 * oracle 操作
			 */
			StringBuilder tableSpaceSql = new StringBuilder();
			tableSpaceSql.append(" create user ").append(userName)
					.append(" identified by ").append(password)
					.append(" default tablespace ")
					.append(serviceInstance.getInstancename())
					.append(" quota ").append(quota).append(" on ")
					.append(serviceInstance.getInstancename());
			/**
			 * 授予角色
			 */
			String roleName=ConfigUtil.getInstance().getOracleRoleName();
			
			StringBuilder roleSql=new StringBuilder().append("grant ").append(roleName).append(" to ").append(userName);
			
			Connection connection = null;
			OracleDbManager dbManager=new OracleDbManager();
			try {
				connection = dbManager.getConnection();
				connection.setAutoCommit(false);
				PreparedStatement stmt = connection
						.prepareStatement(tableSpaceSql.toString());
				stmt.executeUpdate();
				stmt = connection
						.prepareStatement(roleSql.toString());
				stmt.executeUpdate();
				connection.commit();
			} catch (Exception e) {
				logger.error(
						"ServiceInstanceService create user in oracle failed exception",
						e);
				deleteServiceBindingByBindingId(bindingId);
			} finally {
				if (null != connection) {
					try {
						connection.close();
					} catch (SQLException e) {
						logger.error(
								"ServiceInstanceService create tablespace close oracle connection failed",
								e);
					}
				}
			}

		} catch (Exception e) {

			logger.error(
					"ServiceInstanceService create tablespace Undefined Exception",
					e);

			throw e;
		}
	}

	/**
	 * 查看是否已经存在
	 * @param instanceId
	 * @return
	 */
	public ServiceBinding getServiceBindingByInstanceId(String instanceId) {
		String sql = "SELECT * FROM servicebinding WHERE instanceid = ? and deleted='N'";
		Object[] params = new Object[] { instanceId };
		int[] types = new int[] { Types.VARCHAR };
		List<ServiceBinding> serviceBindings=jdbcTemplate.query(sql, params,
				types, new RowMapper<ServiceBinding>(){
			public ServiceBinding mapRow(ResultSet rs, int rowNum)
					throws SQLException {
				ServiceBinding serviceBinding = new ServiceBinding();
				serviceBinding.setAppid(rs.getString("appid"));
				serviceBinding.setBindingid(rs.getString("bindingid"));
				serviceBinding.setDate(rs.getString("date"));
				serviceBinding.setHost(rs.getString("host"));
				serviceBinding.setInstanceid(rs.getString("instanceid"));
				serviceBinding.setPassword(rs.getString("password"));
				serviceBinding.setPort(rs.getString("port"));
				serviceBinding.setUrl(rs.getString("url"));
				serviceBinding.setUsername(rs.getString("username"));
				return serviceBinding;
			}});
		if(null==serviceBindings||serviceBindings.isEmpty())
		{
			logger.error("ServiceBindingService serviceBinding does not exist instanceId="+instanceId);
			return null;
		}
		return serviceBindings.get(0);
	}

	/**
	 * 
	 * @param bindingId
	 * @return
	 */
	public ServiceBinding getServiceBindingByBindingId(String bindingId) {
		String sql = "SELECT * FROM servicebinding WHERE bindingid = ?";
		Object[] params = new Object[] { bindingId };
		int[] types = new int[] { Types.VARCHAR };

		List<ServiceBinding> serviceBindings=jdbcTemplate.query(sql, params,
				types, new RowMapper<ServiceBinding>(){
			public ServiceBinding mapRow(ResultSet rs, int rowNum)
					throws SQLException {
				ServiceBinding serviceBinding = new ServiceBinding();
				serviceBinding.setAppid(rs.getString("appid"));
				serviceBinding.setBindingid(rs.getString("bindingid"));
				serviceBinding.setDate(rs.getString("date"));
				serviceBinding.setHost(rs.getString("host"));
				serviceBinding.setInstanceid(rs.getString("instanceid"));
				serviceBinding.setPassword(rs.getString("password"));
				serviceBinding.setPort(rs.getString("port"));
				serviceBinding.setUrl(rs.getString("url"));
				serviceBinding.setUsername(rs.getString("username"));
				return serviceBinding;
			}});
		if(null==serviceBindings||serviceBindings.isEmpty())
		{
			logger.error("ServiceBindingService serviceBinding does not exist bindingId="+bindingId);
			return null;
		}
		return serviceBindings.get(0);
	}

	private void deleteServiceBindingByBindingId(String bindingId) {

		String sql = "update servicebinding set deleted='Y' where bindingid=?";
		Object[] params = new Object[] { bindingId };
		int[] types = new int[] { Types.VARCHAR };
		jdbcTemplate.update(sql, params, types);
	}

	/**
	 *  删除 broker 数据，oracle 删除用户
	 * @param bindingId
	 * @throws Exception 
	 */
	public void delete(ServiceBinding serviceBinding) throws Exception {
		
		
		deleteServiceBindingByBindingId(serviceBinding.getBindingid());
		
		
		String dropUserSql = "drop user " + serviceBinding.getUsername()+" cascade";
		Connection connection = null;
		OracleDbManager dbManager=new OracleDbManager();
		try {
		connection = dbManager.getConnection();
	    PreparedStatement stmt = connection.prepareStatement(dropUserSql);
	    stmt.executeUpdate();
		} catch (Exception e) {
			
			//TODO oracle 数据库多了一个垃圾表空间 
			logger.error(
					"ServiceInstanceService delete user in oracle failed exception",
					e);
			throw e;
		} finally {
			if (null != connection) {
				try {
					connection.close();
				} catch (SQLException e) {
					logger.error(
							"ServiceInstanceService delete user close oracle connection failed",
							e);
				}
			}
		}
	}

}
