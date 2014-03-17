package com.developcenter.broker.oracle.common;

import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ho.yaml.Yaml;

public class Cache {

	private static Map<Object, Object> serviceSettingCache = new ConcurrentHashMap<Object, Object>();
	private static Log logger = LogFactory.getLog(Cache.class);
	static {
		try {
			Yaml yaml = new Yaml();
			ClassLoader loader = Thread.currentThread().getContextClassLoader();
			InputStream is = loader.getResourceAsStream("settings.yml");
			serviceSettingCache = (Map<Object, Object>) yaml.load(is);
		} catch (Exception e) {
			logger.error("Cache get service setting exception", e);
		}
	}

	public static Map<Object, Object> getServiceSettingCache() {
		return serviceSettingCache;
	}
	
	public static List<Map<String,Object>> getServicePlans()
	{
		List<Object> services=getServices();
		if(services==null||services.size()<1)
		{
			logger.error("service in setting.xml is null");
			return null;
		}
		return (List<Map<String, Object>>) ((Map<Object, Object>) services.get(0)).get("plans");
	}
	
	public static List<Object> getServices()
	{
		return (List<Object>) serviceSettingCache.get("services");
	}
	
	/**
	 * 获取plan中的size
	 * @param planId
	 * @return
	 */
	public static String getSizeByPlanId(String planId)
	{
		List<Map<String, Object>> servicePlans=getServicePlans();
		
		Iterator<Map<String,Object>> iter=servicePlans.iterator();
		
		while(iter.hasNext())
		{
			Map<String,Object> plan=iter.next();
			if(planId.equals(plan.get("id")))
			{
			  return (String)plan.get("size");	
			}
		}
		return null;
	}
//	{
//		  "services": [{
//		    "id": "service-guid-here",
//		    "name": "MySQL",
//		    "description": "A MySQL-compatible relational database",
//		    "bindable": true,
//		    "plans": [{
//		      "id": "plan1-guid-here",
//		      "name": "small",
//		      "description": "A small shared database with 100mb storage quota and 10 connections"
//		    },{
//		      "id": "plan2-guid-here",
//		      "name": "large",
//		      "description": "A large dedicated database with 10GB storage quota, 512MB of RAM, and 100 connections"
//		    }]
//		  }]
//		}

}
