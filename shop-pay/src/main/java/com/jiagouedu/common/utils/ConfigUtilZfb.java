package com.jiagouedu.common.utils;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 * 相关配置参数 
 * 创建者 悟空老师
 * 创建时间 2018年7月31日
 */
public class ConfigUtilZfb {
	private static Configuration configs;
	static String serverUrl;
	static String appId;
	static String privateKey;
	static String alipayPublicKey;
	public static String returnUrl;
	public static String notifyurl;



	public static synchronized void init(String filePath) {
		if (configs != null) {
			return;
		}
		try {
			configs = new PropertiesConfiguration(filePath);
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}

		if (configs == null) {
			throw new IllegalStateException("can`t find file by path:"
					+ filePath);
		}
		serverUrl = configs.getString("serverUrl");
		appId = configs.getString("appId");
		privateKey = configs.getString("privateKey");
		alipayPublicKey = configs.getString("alipayPublicKey");
		returnUrl = configs.getString("returnUrl");
		notifyurl = configs.getString("notifyurl");


	}


}