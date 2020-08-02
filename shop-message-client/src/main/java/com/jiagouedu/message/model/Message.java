package com.jiagouedu.message.model;

import java.io.Serializable;

public class Message  implements Serializable {

	private Integer id;
	private String bizId;
	private String bizType;
	private String bizData;
	private int status;
	
	public Message(String bizId, String bizType, String bizData, int status) {
		super();
		this.bizId = bizId;
		this.bizType = bizType;
		this.bizData = bizData;
		this.status = status;
	}
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getBizId() {
		return bizId;
	}
	public void setBizId(String bizId) {
		this.bizId = bizId;
	}
	public String getBizType() {
		return bizType;
	}
	public void setBizType(String bizType) {
		this.bizType = bizType;
	}
	public String getBizData() {
		return bizData;
	}
	public void setBizData(String bizData) {
		this.bizData = bizData;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	
}
