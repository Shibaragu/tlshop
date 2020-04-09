package com.jiagouedu.common.utils;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.demo.trade.config.Configs;
import com.alipay.demo.trade.service.AlipayTradeService;
import com.alipay.demo.trade.service.impl.AlipayTradeServiceImpl;

/**
 * 配置公共参数
 * 创建者 悟空老师 245553999
 * 创建时间	2019年1月16日
 */
public final class AliPayConfig {
	
	 /**
     * 私有的默认构造子，保证外界无法直接实例化
     */
    private AliPayConfig(){};
    
	/**
     * 收款方账号
     */
    public static final String SELLER_ID = "zhifu@52itstyle.com";
	 /**
     * 参数类型
     */
    public static String PARAM_TYPE = "json";
    /**
     * 编码
     */
    public static String CHARSET = "UTF-8";
    /**
     * 类级的内部类，也就是静态的成员式内部类，该内部类的实例与外部类的实例
     * 没有绑定关系，而且只有被调用到才会装载，从而实现了延迟加载
     */
    private static class SingletonHolder{
        /**
         * 静态初始化器，由JVM来保证线程安全
         */
		private  static AlipayClient alipayClient = new DefaultAlipayClient(
													Configs.getOpenApiDomain(), Configs.getAppid(),
													Configs.getPrivateKey(), PARAM_TYPE, CHARSET,
													Configs.getAlipayPublicKey());
		
		private  static AlipayTradeService tradeService = new AlipayTradeServiceImpl.ClientBuilder().build();
    }
    /**
     * 支付宝APP请求客户端实例
     * @Author  悟空老师
     * @return  AlipayClient
     * @Date	2018年7月27日
     * 更新日志
     * 2018年7月27日  悟空老师 首次创建
     *
     */
    public static AlipayClient getAlipayClient(){
       AlipayClient alipayClient = new DefaultAlipayClient(ConfigUtilZfb.serverUrl, ConfigUtilZfb.appId,ConfigUtilZfb.privateKey, PARAM_TYPE,CHARSET, ConfigUtilZfb.alipayPublicKey,"RSA2");
       return alipayClient;
       //      return SingletonHolder.alipayClient;
   }
    /**
     * 电脑端预下单
     * @Author  悟空老师
     * @return  AlipayTradeService
     * @Date	2018年7月27日
     * 更新日志
     * 2018年7月27日  悟空老师 首次创建
     *
     */
    public static AlipayTradeService getAlipayTradeService(){
        return SingletonHolder.tradeService;
    }

}
