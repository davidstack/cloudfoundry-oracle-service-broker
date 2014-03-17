package com.developcenter.broker.oracle.model.request;

public class ServiceBindingRequest {
	private String plan_id;
	private String service_id;
	private String app_guid;
	public String getPlan_id() {
		return plan_id;
	}
	public void setPlan_id(String plan_id) {
		this.plan_id = plan_id;
	}
	public String getService_id() {
		return service_id;
	}
	public void setService_id(String service_id) {
		this.service_id = service_id;
	}
	public String getApp_guid() {
		return app_guid;
	}
	public void setApp_guid(String app_guid) {
		this.app_guid = app_guid;
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ServiceBindingRequest [plan_id=");
		builder.append(plan_id);
		builder.append(", service_id=");
		builder.append(service_id);
		builder.append(", app_guid=");
		builder.append(app_guid);
		builder.append("]");
		return builder.toString();
	}
	
}
