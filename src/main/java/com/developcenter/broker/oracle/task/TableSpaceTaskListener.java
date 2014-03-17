package com.developcenter.broker.oracle.task;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.developcenter.broker.oracle.common.OracleDbManager;
import com.developcenter.broker.oracle.model.ServiceInstance;

public class TableSpaceTaskListener implements ServletContextListener {
	private ScheduledExecutorService scheduExec = Executors
			.newScheduledThreadPool(1);
	private JdbcTemplate jdbcTemplate;
	
	public void beginScanTask() {
		scheduExec.scheduleWithFixedDelay(new TableSpaceDeleteTask(),
				1000 * 50, 1000 * 120, TimeUnit.MILLISECONDS);
	}

	/**
	 * 删除 tablespace的异步线程
	 * 
	 * @author wang
	 * 
	 */
	public class TableSpaceDeleteTask implements Runnable {
		private Log logger = LogFactory.getLog(TableSpaceDeleteTask.class);
		

		@Override
		public void run() {

			OracleDbManager dbManager = new OracleDbManager();
			Connection connection = null;
			try {
				List<ServiceInstance> unknowServiceInstances = getServiceInstanceUnknownStatus();
				if(null==unknowServiceInstances||unknowServiceInstances.isEmpty())
				{
					return;
				}
				connection = dbManager.getConnection();

				Iterator<ServiceInstance> iter = unknowServiceInstances
						.iterator();
				PreparedStatement stmt = null;
				while (iter.hasNext()) {
					ServiceInstance instance = iter.next();
					String dropSpaceSql = "drop tablespace "
							+ instance.getInstancename()
							+ " including contents and datafiles cascade constraints";
					stmt = connection.prepareStatement(dropSpaceSql);
					stmt.executeUpdate();
					deleteServiceInstanceById(instance.getId(), "Y");
					iter.remove();
				}

			} catch (Exception e) {

				// TODO oracle 数据库多了一个垃圾表空间
				logger.error(
						"ServiceInstanceService delete tablespace in oracle failed exception",
						e);
			} finally {
				if (null != connection) {
					try {
						connection.close();
					} catch (Exception e) {
						logger.error(
								"ServiceInstanceService delete tablespace close oracle connection failed",
								e);
					}
				}
			}
		}

		/**
		 * 删除Service 实例
		 * 
		 * @param instanceId
		 */
		public void deleteServiceInstanceById(String instanceId, String status) {

			/**
			 * 将deleted 标示标为unknow ，由异步进程进行删除
			 */
			String sql = "update serviceinstance set deleted='" + status
					+ "' where id = ?";
			Object[] params = new Object[] { instanceId };
			int[] types = new int[] { Types.VARCHAR };
			jdbcTemplate.update(sql, params, types);
		}

		/**
		 * 根据待删除的instance
		 * 
		 * @param instanceId
		 * @return
		 */
		public List<ServiceInstance> getServiceInstanceUnknownStatus() {
			String sql = "SELECT * FROM serviceinstance WHERE deleted = 'unknow'";

			List<ServiceInstance> serviceInstances = jdbcTemplate.query(sql,
					new RowMapper<ServiceInstance>() {
						public ServiceInstance mapRow(ResultSet rs, int rowNum)
								throws SQLException {
							ServiceInstance instance = new ServiceInstance();
							instance.setDate(rs.getString("date"));
							instance.setId(rs.getString("id"));
							instance.setInstancename(rs
									.getString("instancename"));
							instance.setOrgid(rs.getString("orgid"));
							instance.setPlanid(rs.getString("planid"));
							instance.setServicenodeid(rs
									.getString("servicenodeid"));
							instance.setSpaceid(rs.getString("spaceid"));
							return instance;
						}
					});
			if (null == serviceInstances || serviceInstances.isEmpty()) {
				logger.info("ServiceInstanceService getServiceInstanceById getServiceInstanceUnknownStatus no serviceinstance should be delete");
				return new ArrayList<ServiceInstance>();
			}
			return serviceInstances;
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		
		WebApplicationContext servletContext =  WebApplicationContextUtils.getWebApplicationContext(arg0.getServletContext());
		jdbcTemplate = (JdbcTemplate) servletContext.getBean("jdbcTemplate");
		 
		beginScanTask();

	}

}
