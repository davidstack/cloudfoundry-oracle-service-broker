package com.developcenter.broker.oracle.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.developcenter.broker.oracle.common.Util;
import com.developcenter.broker.oracle.model.ServiceBinding;
import com.developcenter.broker.oracle.model.ServiceInstance;
import com.developcenter.broker.oracle.model.request.ResourceCreateRequest;
import com.developcenter.broker.oracle.service.ResourceService;
import com.developcenter.broker.oracle.service.ServiceBindingService;
import com.developcenter.broker.oracle.service.ServiceInstanceService;

/**
 * 直接申请数据库资源
 * 
 * @author wang
 * 
 */
@Controller
@RequestMapping("/resources/")
public class ResourceController {
	@Autowired
	ResourceService resourceService;

	@Autowired
	ServiceBindingService bindingService;
	
	@Autowired
	private ServiceInstanceService instanceService;
	
	private Log logger = LogFactory.getLog(ResourceController.class);

	/**
	 * 直接申请数据库
	 * 
	 * @param instanceId
	 * @param request
	 * @param response
	 * @return
	 */
	public @RequestMapping(method = RequestMethod.PUT, value = "/service_instances/{instanceId}")
	@ResponseBody
	Map create(@PathVariable String instanceId,HttpServletRequest request, HttpServletResponse response) {

		try {
			ResourceCreateRequest bodyParams = Util.getObjectFromPutMethod(request,
					new TypeReference<ResourceCreateRequest>() {
					});
			ServiceInstance serviceInstance = new ServiceInstance("direct-"
					+ instanceId, bodyParams.getPlan_id());
	
			ServiceBinding binding = bindingService.constructServiceBinding(
					"direct-" + instanceId.toString(), bodyParams.getApp_guid(), serviceInstance);
			resourceService.create(serviceInstance, binding);
			Map<String, ServiceBinding> result = new HashMap<String, ServiceBinding>();
			result.put("credentials", binding);
			return result;
		} catch (Exception e) {
			logger.error(
					"ResourceController create service instance exception", e);
			logger.error("ResourceController create service instance id="
					+ instanceId);
			response.setStatus(500);
			return new HashMap<String, String>();
		}
	}

	/**
	 * 直接删除数据库
	 * 
	 * @param instanceId
	 * @param request
	 * @param response
	 * @return
	 */
	public @RequestMapping(method = RequestMethod.DELETE, value = "/service_instances/{instanceId}")
	@ResponseBody
	Map delete(@PathVariable String instanceId, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			ServiceBinding serviceBinding = bindingService
					.getServiceBindingByBindingId("direct-" + instanceId);
			ServiceInstance serviceInstance = instanceService
					.getServiceInstanceById("direct-" + instanceId);
			
			if (null == serviceBinding) {
				logger.info("ResourceController destroy servicebinding serviceBinding doesnot exist ");
				return new HashMap<String, String>();
			}

			try {
				resourceService.delete(serviceInstance, serviceBinding);
			} catch (Exception e) {
				logger.error(
						"ResourceController destroy servicebinding exception ",
						e);
				response.setStatus(500);
				return null;
			}
		} catch (Exception e) {
			logger.error(
					"ResourceController create service instance exception", e);
			logger.error("ResourceController create service instance id="
					+ instanceId);
			response.setStatus(500);
			return new HashMap<String, String>();
		}
		return new HashMap<String, String>();
	}
}
