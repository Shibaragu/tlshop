package com.jiagouedu.common.utils;


import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * 初始化参数 
 * 创建者 悟空老师 245553999
 * 创建时间	2019年1月16日
 *
 */
@Component
public class InitUtil {
	@PostConstruct
	public void init() {
		ConfigUtil.init("wxinfo.properties");
		ConfigUtilZfb.init("zfbinfo.properties");
		SDKConfig.getConfig().loadPropertiesFromSrc();
	}

}
