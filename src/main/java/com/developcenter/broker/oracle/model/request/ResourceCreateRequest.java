package com.developcenter.broker.oracle.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties
public class ResourceCreateRequest {
	private String plan_id;
	private String app_guid;
	public String getPlan_id() {
		return plan_id;
	}
	public void setPlan_id(String plan_id) {
		this.plan_id = plan_id;
	}
	public String getApp_guid() {
		return app_guid;
	}
	public void setApp_guid(String app_guid) {
		this.app_guid = app_guid;
	}
	
}
