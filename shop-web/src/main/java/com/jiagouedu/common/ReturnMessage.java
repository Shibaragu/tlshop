package com.jiagouedu.common;

/**
 * 返回消息封装
 * 
 * @author aaron.rao
 *
 */
public class ReturnMessage {

	public static final String SUCCESS_CODE = "0";
	public static final String ERROR_CODE = "1";

	private String code;
	private Object data;
	private String msg;

	public boolean isSuccess() {
	    return SUCCESS_CODE.equals(code);
    }
	
	// 默认构造函数，用于从Json还原对象，请勿移除
	public ReturnMessage() {
	}

	public ReturnMessage(String code, Object data, String msg) {
		super();
		this.code = code;
		this.data = data;
		this.msg = msg;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public static ReturnMessage byCode(String code) {
		return new ReturnMessage(code, "", null);
	}

	public static ReturnMessage byCodeMsg(String code, String msg) {
		return new ReturnMessage(code, msg, null);
	}
	
	public static ReturnMessage success(){
		return new ReturnMessage(SUCCESS_CODE, null, null);
	}
	public static ReturnMessage success(Object data){
		return new ReturnMessage(SUCCESS_CODE, data, null);
	}
	
	public static ReturnMessage error(String msg){
		return new ReturnMessage(ERROR_CODE, null, msg);
	}
	
	public static ReturnMessage error(Object data, String msg){
		return new ReturnMessage(ERROR_CODE, data, msg);
	}

}
