package com.developcenter.broker.oracle.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import com.developcenter.broker.oracle.model.ServiceNode;

//TODO 后续增加 选择servicenode ，进行servicenode的负载均衡
public class ServiceNodePool {

	private static  Map<String,ServiceNode> serviceNodesInfo=new HashMap<String,ServiceNode>();
	
	@Autowired 
	JdbcTemplate jdbcTemplate;
	
	/**
	 * 将数据保存到数据库中
	 */
	@SuppressWarnings("unchecked")
	public ServiceNodePool()
	{
		
		String nodesInfo=ConfigUtil.getInstance().getServiceNodes();
		
		ObjectMapper objMappper = new ObjectMapper();
		List<ServiceNode> nodes =new ArrayList<ServiceNode>();
		try {
			nodes=objMappper.readValue(nodesInfo, nodes.getClass());
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		List<Object[]> batchArgs =new ArrayList<Object[]>();
		for(int i=0;i<nodes.size();i++)
		{
			serviceNodesInfo.put(nodes.get(i).getId(), nodes.get(i));
			
			Object[] objets=new Object[2];
			objets[0]=nodes.get(i).getId();
			objets[1]=nodes.get(i).toString();
			batchArgs.add(objets);
		}
		
		/**
		 * 将service node数据保存到数据库
		 */
		String sql = "INSERT INTO servicenodes (id,info) VALUES(?,?)";
		jdbcTemplate.batchUpdate(sql,batchArgs);

	}
}
