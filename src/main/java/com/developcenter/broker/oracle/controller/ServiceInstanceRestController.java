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
import com.developcenter.broker.oracle.model.request.CreateInstanceRequest;
import com.developcenter.broker.oracle.service.ServiceBindingService;
import com.developcenter.broker.oracle.service.ServiceInstanceService;

@Controller
@RequestMapping("/v2/service_instances/{id}")
class ServiceInstanceRestController {
  @Autowired
  private ServiceInstanceService service;

  @Autowired 
  ServiceBindingService bindingService;
  
  private Log logger = LogFactory.getLog(ServiceInstanceRestController.class);
  
	@RequestMapping(method = RequestMethod.PUT)
	@ResponseBody
	Map update(@PathVariable String id, final HttpServletRequest request,
			HttpServletResponse response) {
		try {
			/**
			 * 获取body 参数
			 */
			CreateInstanceRequest paramRequest = Util.getObjectFromPutMethod(
					request, new TypeReference<CreateInstanceRequest>() {
					});

			if (null == paramRequest) {
				logger.error("ServiceInstanceRestController paramRequest is null");
				response.setStatus(403);
				return new HashMap<String, String>();
			}
			 ServiceInstance instance=new ServiceInstance(id,paramRequest.getPlan_id());
			if (!service.isExists(instance.getId())) {
				service.create(instance);
			} else {
				logger.error("ServiceInstanceRestController create instance faile ,instance has exist id="
						+ id);
				response.setStatus(409);
				return new HashMap<String, String>();
			}
		} catch (Exception e) {
			logger.error(
					"ServiceInstanceRestController create service instance exception",
					e);
			logger.error("ServiceInstanceRestController create service instance id="
					+ id);
			response.setStatus(500);
			return new HashMap<String, String>();
		}
		response.setStatus(201);
		return new HashMap<String, String>();
	}

	@RequestMapping(method = RequestMethod.DELETE)
	@ResponseBody
	Map destroy(@PathVariable String id, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			ServiceInstance serviceInstance=service.getServiceInstanceById(id);
			/**
			 * 检查是否存在该serviceId
			 */
			if (null==serviceInstance) {
				logger.error("ServiceInstanceRestController delete service instance instance does not exist.id= "+id);
				response.setStatus(500);
				return null;
			}
			
			/**
			 * 检查是否已经被绑定
			 */
		   ServiceBinding serviceBinding=	bindingService.getServiceBindingByInstanceId(id);
			if(null!=serviceBinding)
			{
				service.deleteInstance(serviceInstance);
			}
			else
			{
				logger.error("ServiceInstanceRestController delete service instance instance does not exist.id= "+id);
			}
			
			
		} catch (Exception e) {
			logger.error(
					"ServiceInstanceRestController delete service instance exception",
					e);
			logger.error("ServiceInstanceRestController delete service instance id="
					+ id);
			response.setStatus(500);
			return null;
		}
		return new HashMap<String, String>();
	}
}
