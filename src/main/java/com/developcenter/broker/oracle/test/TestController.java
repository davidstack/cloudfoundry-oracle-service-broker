package com.developcenter.broker.oracle.test;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class TestController {
	 //private Map<Object,Object> settings;

	  @RequestMapping(value="/v2/service/test",method=RequestMethod.GET)
	  @ResponseBody
	  synchronized Map getCatalog() {
		  Map<String,String> result =new HashMap<String,String>();
		  try
	   {
		  String service= System.getenv("VCAP_SERVICES");
		  result.put("value", service);
	   }
	   catch(Exception e)
	   {
		   e.printStackTrace();
	   }
	    return result;
	  }

}
