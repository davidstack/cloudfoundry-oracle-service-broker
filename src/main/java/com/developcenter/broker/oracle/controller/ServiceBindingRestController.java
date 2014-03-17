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
import com.developcenter.broker.oracle.model.request.ServiceBindingRequest;
import com.developcenter.broker.oracle.service.ServiceBindingService;
import com.developcenter.broker.oracle.service.ServiceInstanceService;

@Controller
@RequestMapping("/v2/service_instances/{instanceId}/service_bindings/{bindingId}")
class ServiceBindingRestController {
  @Autowired 
  ServiceBindingService bindingService;
  @Autowired
  private ServiceInstanceService instanceService;
  private Log logger = LogFactory.getLog(ServiceBindingRestController.class);

	/**
	 * 
	 * @param instanceId
	 *            mysql id
	 * @param bindingId
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(method = RequestMethod.PUT)
	@ResponseBody
	Map update(@PathVariable String instanceId,
			@PathVariable String bindingId, HttpServletRequest request,
			HttpServletResponse response) {

		ServiceBindingRequest bodyParams = Util.getObjectFromPutMethod(request,
				new TypeReference<ServiceBindingRequest>() {
				});
		logger.info("binding request params:"+bodyParams);
		if (null == bodyParams) {
			logger.error("ServiceBindingRestController bodyParams is null");
			response.setStatus(403);
			return null;
		}
		
		/**
		 * 查看service instance 是否存在
		 */
		ServiceInstance serviceInstance=instanceService.getServiceInstanceById(instanceId);
		if(null==serviceInstance)
		{
			logger.error("ServiceBinding service instance does not exits");
			response.setStatus(500);
			return null;
		}
		
//		/**
//		 * 查看该service instance 是否已经被绑定
//		 */
//		if(null!=bindingService.getServiceBindingByInstanceId(instanceId))
//		{
//			logger.error("ServiceBinding service instance has been bound");
//			response.setStatus(500);
//			return null;	
//		}
		
		ServiceBinding binding = bindingService.constructServiceBinding(
				bindingId,bodyParams.getApp_guid(),serviceInstance);

		try {
			bindingService.create(binding,serviceInstance);
		} catch (Exception e) {
			logger.error("ServiceBinding service create binding exception",e);
			response.setStatus(500);
			return null;
		}
		Map<String,ServiceBinding> result=new HashMap<String,ServiceBinding>();
		result.put("credentials", binding);
		return result;
	}

	/**
	 * 解除服务
	 * 
	 * @param instanceId
	 * @param bindingId
	 * @return
	 */
	@RequestMapping(method = RequestMethod.DELETE)
	@ResponseBody
	Map destroy(@PathVariable String instanceId,
			@PathVariable String bindingId, HttpServletRequest request,
			HttpServletResponse response) {
//		ServiceUnbindingRequest bodyParams = Util.getObjectFromPutMethod(
//				request, new TypeReference<ServiceUnbindingRequest>() {
//				});
//		if (null == bodyParams) {
//			logger.error("ServiceBindingRestController bodyParams is null");
//			response.setStatus(403);
//			return null;
//		}
		
		ServiceBinding serviceBinding=bindingService.getServiceBindingByBindingId(bindingId);
		
		if(null==serviceBinding)
		{
			logger.info("ServiceBindingController destroy servicebinding serviceBinding doesnot exist ");
			return new HashMap<String, String>();
		}
		
		try
		{
			bindingService.delete(serviceBinding);
		}
		catch(Exception e)
		{
			logger.error("ServiceBindingController destroy servicebinding exception ",e);
			response.setStatus(500);
			return null;
		}
		
		return new HashMap<String, String>();
	}

}
