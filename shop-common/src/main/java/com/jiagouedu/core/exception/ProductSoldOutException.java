package com.jiagouedu.core.exception;

/**
 * 商品售完异常
 * @author aaron.rao
 *
 */
public class ProductSoldOutException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/**
	 * @param string
	 */
	public ProductSoldOutException(String arg0) {
		super(arg0);
	}

}