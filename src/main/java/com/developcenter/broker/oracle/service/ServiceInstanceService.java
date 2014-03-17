package com.developcenter.broker.oracle.service;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import com.developcenter.broker.oracle.common.Cache;
import com.developcenter.broker.oracle.common.ConfigUtil;
import com.developcenter.broker.oracle.common.OracleDbManager;
import com.developcenter.broker.oracle.common.Util;
import com.developcenter.broker.oracle.model.ServiceInstance;

@Service
public class ServiceInstanceService {
  
	private Log logger = LogFactory.getLog(ServiceInstanceService.class);
 
/**
 * 连接mysql 。存储broker的业务数据	
 */
   @Autowired 
   JdbcTemplate jdbcTemplate;


  public boolean isExists(String instanceId) {
	  String sql = "SELECT * FROM serviceinstance WHERE id = ?";
	 Object[] params = new Object[] {instanceId};
	 int[] types = new int[] {Types.VARCHAR};

	 List<Map<String,Object>> databases= jdbcTemplate.queryForList(sql, params, types);
     return databases.size() > 0;
  }

  /**
   * 根据ID 获取实例
   * @param instanceId
   * @return
   */
	public ServiceInstance getServiceInstanceById(String instanceId) {
		String sql = "SELECT * FROM serviceinstance WHERE id = ?";
		Object[] params = new Object[] { instanceId };
		int[] types = new int[] { Types.VARCHAR };
		List<ServiceInstance> serviceInstances = jdbcTemplate.query(sql,
				params, types, new RowMapper<ServiceInstance>() {
					public ServiceInstance mapRow(ResultSet rs, int rowNum)
							throws SQLException {
						ServiceInstance instance = new ServiceInstance();
						instance.setDate(rs.getString("date"));
						instance.setId(rs.getString("id"));
						instance.setInstancename(rs.getString("instancename"));
						instance.setOrgid(rs.getString("orgid"));
						instance.setPlanid(rs.getString("planid"));
						instance.setServicenodeid(rs.getString("servicenodeid"));
						instance.setSpaceid(rs.getString("spaceid"));
						return instance;
					}
				});
		if (null == serviceInstances || serviceInstances.isEmpty()) {
			logger.info("ServiceInstanceService getServiceInstanceById serviceinstance does not exist instanceId="+instanceId);
			return null;
		}
		return serviceInstances.get(0);
	}
 
  /**
   * 创建Service 实例
   * @param instanceId
 * @throws Exception 
   */
  public void create(ServiceInstance instance) throws Exception {
	  
	  //TODO 设置回滚机制
	  String sql = "INSERT INTO serviceinstance (id,instancename,planid,date) VALUES(?,?,?,?)";
	  Object[] params = new Object[] {instance.getId(),instance.getInstancename(),instance.getPlanid(),Util.getSysTime()};
	  int[] types = new int[] {Types.VARCHAR,Types.VARCHAR,Types.VARCHAR,Types.VARCHAR};
	 
	  // 保存 service instance数据到数据库
	  jdbcTemplate.update(sql, params, types);

	   StringBuilder tableSpaceSql = new StringBuilder();
		String tabeSpaceSize = Cache.getSizeByPlanId(instance.getPlanid());
		if(null==tabeSpaceSize)
		{
			throw new IllegalArgumentException();	
		}
		
		tableSpaceSql
				.append("create tablespace ")
				.append(instance.getInstancename())
				.append(" logging  datafile '")
				.append(ConfigUtil.getInstance().getOracleDbPath())
				.append(instance.getInstancename())
				.append(".dbf' size ")
				.append("32m")
				.append(" autoextend on next 256M  maxsize ").append(tabeSpaceSize).append(" autoallocate");
		Connection connection = null;
		OracleDbManager dbManager=new OracleDbManager();
		try {

		connection = dbManager.getConnection();
	    PreparedStatement stmt = connection.prepareStatement(tableSpaceSql
					.toString());
	    stmt.executeUpdate();
		} catch (Exception e) {
			logger.error(
					"ServiceInstanceService create tablespace in oracle failed exception",
					e);
			deleteServiceInstanceById(instance.getId(),"Y");
			throw e;
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
  }

  /**
   * 删除Service 实例
   * @param instanceId
   */
	public void deleteServiceInstanceById(String instanceId,String status) {
		
		/**
		 * 将deleted 标示标为unknow ，由异步进程进行删除
		 */
		String sql = "update serviceinstance set deleted='"+status+"' where id = ?";
		Object[] params = new Object[] { instanceId };
		int[] types = new int[] { Types.VARCHAR };
		jdbcTemplate.update(sql, params, types);
	}
	
	public void deleteInstance(ServiceInstance instance) {
		
		deleteServiceInstanceById(instance.getId(),"unknow");
	}
	
}
