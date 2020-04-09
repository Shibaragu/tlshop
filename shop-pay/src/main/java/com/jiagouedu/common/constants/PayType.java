package com.jiagouedu.common.constants;
/**
 * 支付类型
 * 创建者 悟空老师 245553999
 * 创建时间	2019年1月16日
 *
 */
public enum PayType {
	/**支付类型*/
	ALI("支付宝",(short)1),WECHAT("微信",(short)2);
	
	private Short code;
	private String name;
	
	private PayType(String name, Short code) {
		this.name = name;
		this.code = code;
	}

	public static String getName(Short code, String name) {
		for (PayType c : PayType.values()) {
			if (c.getCode() == code) {
				return c.name;
			}
		}
		return null;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public short getCode() {
		return code;
	}

	public void setCode(short code) {
		this.code = code;
	}
}
