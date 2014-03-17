package com.developcenter.broker.oracle.controller;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.developcenter.broker.oracle.common.Cache;

/**
 * Author: Sridharan Kuppa sridharan.kuppa@gmail.com
 * Date: 12/12/13
 */
@Controller
class CatalogRestController {
	private Log logger = LogFactory.getLog(CatalogRestController.class);

	@RequestMapping(value = "/v2/catalog", method = RequestMethod.GET)
	@ResponseBody
	synchronized Map getCatalog() {
		logger.info("CatalogRestController getCatalog ="+Cache.getServiceSettingCache());
		return Cache.getServiceSettingCache();
	}

}
