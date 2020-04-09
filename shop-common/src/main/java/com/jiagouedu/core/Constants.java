package com.jiagouedu.core;

public class Constants {

    public static final class RedisKeyPrefix {
        public static final String PRODUCT_STOCK = "product_stock";
        public static final String PRODUCT= "redis_product";
        public static final String USER_DEFAULT_ADDRESS  = "user_default_address";
        public static final String MIAOSHA_ORDER = "miaosha_order";
        public static final String MIAOSHA_ORDER_WAIT = "miaosha_order_wait";
        public static final String MIAOSHA_VERIFY_CODE = "miaosha_verify_code";
        public static final String MIAOSHA_ORDER_TOKEN = "miaosha_order_token";
    }

    public static final class EhcacheKeyPrefix {
        public static final String PRODUCT = "ehcache_product";
    }

    public static final class ProductType {
        public static final String PRODUCT_MIAOSHA = "product_miaosha";
        public static final String PRODUCT_HOT = "product_hot";
    }

    public static final class ZookeeperPathPrefix {
        public static final String PRODUCT_SOLD_OUT = "product_sold_out";
    }


}
