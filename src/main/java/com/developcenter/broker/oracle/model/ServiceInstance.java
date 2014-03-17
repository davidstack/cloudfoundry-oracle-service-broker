package com.developcenter.broker.oracle.model;

import com.developcenter.broker.oracle.common.Util;

public class ServiceInstance {

	static String SERVICE_NAME_PREFIX = "cf_";

	/**
	 * 保存服务实例ID，以及ServiceName
	 * @param id
	 */
	public ServiceInstance(String id,String planId) {
		this.id = id;
		this.setInstancename(SERVICE_NAME_PREFIX+Util.getRandomString());
		this.planid=planId;
	}
	
	public ServiceInstance() {
		
	}

	private String instancename;
	
	private String id;

	private String planid;

	private String orgid;

	private String spaceid;

	private String date;
	private String servicenodeid;

	/**
	 * 是否被删除  Y 表示被删除，N表示未被删除
	 */
	private String deleted;
	
	public String getInstancename() {
		return instancename;
	}

	public void setInstancename(String instancename) {
		this.instancename = instancename;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPlanid() {
		return planid;
	}

	public void setPlanid(String planid) {
		this.planid = planid;
	}

	public String getOrgid() {
		return orgid;
	}

	public void setOrgid(String orgid) {
		this.orgid = orgid;
	}

	public String getSpaceid() {
		return spaceid;
	}

	public void setSpaceid(String spaceid) {
		this.spaceid = spaceid;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getServicenodeid() {
		return servicenodeid;
	}

	public void setServicenodeid(String servicenodeid) {
		this.servicenodeid = servicenodeid;
	}

	public String getDeleted() {
		return deleted;
	}

	public void setDeleted(String deleted) {
		this.deleted = deleted;
	}

}
