package com.developcenter.broker.oracle.model.request;

public class ServiceUnbindingRequest {
	private String plan_id;
	private String service_id;
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
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ServiceUnbindingRequest [plan_id=");
		builder.append(plan_id);
		builder.append(", service_id=");
		builder.append(service_id);
		builder.append("]");
		return builder.toString();
	}
	
}
