package com.jiagouedu.message;
public class Constants {
	
    public static final class MessageStatus {
    	//初始状态
    	public static final int INIT= 0;
    	//已发送状态
    	public static final int SENT = 1;
    	//处理完成状态
    	public static final int END = 2;
    }
    
    public static final class BizType {
    	public static final String ORDER_PRODUCT_STOCK = "order_product_stock";
    }
}