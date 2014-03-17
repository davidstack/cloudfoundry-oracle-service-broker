package com.developcenter.broker.oracle.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

public class Util {

	private static final String CHAR_COLLECTIONS="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
	public static String getSysTime()
	{
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		// new Date()为获取当前系统时间
		String time = df.format(new Date());
		
		return time;
	}


	/**
	 * 解析 PUT 方法中的 body
	 * @param request
	 * @param reference
	 * @return
	 */
	public static <T> T getObjectFromPutMethod(final HttpServletRequest request,
			TypeReference<T> reference) {
		BufferedReader in = null;
		T result = null;
		try {
			in = new BufferedReader(new InputStreamReader(
					request.getInputStream()));
			ObjectMapper mapper = new ObjectMapper();
			result = mapper.readValue(in,reference);
		} catch (IOException e) {
			if(in !=null)
			{
				try {
					in.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			e.printStackTrace();
			result=null;
		}

		return result;
	}
//	
//	/**
//	 * 获取输入字符串的摘要信息
//	 * @param inputString
//	 * @return
//	 */
//	public String get16BitDigest(String inputString) {
//		MessageDigest digest = null;
//		String result = null;
//		try {
//			digest = MessageDigest.getInstance("MD5");
//		} catch (NoSuchAlgorithmException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		digest.update(inputString.getBytes());
//		result = new BigInteger(1, digest.digest()).toString(16)
//				.replaceAll("/[^a-zA-Z0-9]+/", "''").substring(0, 16);
//		return result;
//	}

	public static  String getRandomString() {
		Date dt = new Date();
		Long time = dt.getTime();
		return String.valueOf(time);

	}
	
	public static String getRandomString(int bitsNum)
	{
		 String result="";  
	       for(int i=0;i<bitsNum;i++){  
	           int intVal=(int)(Math.random()*52);  
	           result=result+CHAR_COLLECTIONS.charAt(intVal);  
	       }
	       return result;
	}
	public static void main(String[] args) {
		     String result="";  
		       for(int i=0;i<8;i++){  
		           int intVal=(int)(Math.random()*10);  
		           result=result+CHAR_COLLECTIONS.charAt(intVal);  
		       }  
		     System.out.print(result); 

	}
}
