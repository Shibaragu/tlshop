package com.jiagouedu.web.util;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.File;

public class Constants {
	public static final String SF_FILE_SEPARATOR = System.getProperty("file.separator");//文件分隔符
	public static final String SF_LINE_SEPARATOR = System.getProperty("line.separator");//行分隔符
	public static final String SF_PATH_SEPARATOR = System.getProperty("path.separator");//路径分隔符
	public static final String QRCODE_PATH ;
	//此处为支付二维码 存放地址 自行定义
	static {
		HttpServletRequest req = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
		String fileUrl=req.getServletContext().getRealPath("/")+SF_FILE_SEPARATOR+"qrcode";
		File file=new File(fileUrl);
		if (!file.exists()){
			file.mkdir();
		}
		QRCODE_PATH =fileUrl;
		System.out.println("QRCODE_PATH:"+QRCODE_PATH);
	}
	
}
