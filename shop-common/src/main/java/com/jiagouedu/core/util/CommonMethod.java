package com.jiagouedu.core.util;

import com.jiagouedu.core.Constants.RedisKeyPrefix;

public class CommonMethod {

	public static String getMiaoshaOrderRedisKey(String accountId, String productId) {
		return RedisKeyPrefix.MIAOSHA_ORDER + "_" + accountId + "_" + productId;
	}

	public static String getMiaoshaOrderWaitFlagRedisKey(String accountId, String productId) {
		return RedisKeyPrefix.MIAOSHA_ORDER_WAIT + "_" + accountId + "_" + productId;
	}

	public static String getMiaoshaTokenRedisKey(String accountId, String productId) {
		return RedisKeyPrefix.MIAOSHA_ORDER_TOKEN + "_" + accountId + "_" + productId;
	}
	
	public static String getMiaoshaVerifyCodeRedisKey(String accountId, String productId) {
		return RedisKeyPrefix.MIAOSHA_VERIFY_CODE + "_" + accountId + "_" + productId;
	}
}
