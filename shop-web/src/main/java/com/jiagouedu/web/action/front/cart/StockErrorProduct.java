package com.jiagouedu.web.action.front.cart;

/**
 * 库存检查错误消息对象
 */
public class StockErrorProduct{
	String id;//商品ID
	String tips;//错误消息
	
	public StockErrorProduct(){}
	
	public StockErrorProduct(String id,String tips){
		this.id = id;
		this.tips = tips;
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getTips() {
		return tips;
	}
	
	public void setTips(String tips) {
		this.tips = tips;
	}
}
