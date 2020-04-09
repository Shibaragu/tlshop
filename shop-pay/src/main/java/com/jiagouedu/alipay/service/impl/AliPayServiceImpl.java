package com.jiagouedu.alipay.service.impl;


import com.alipay.api.AlipayApiException;

import com.alipay.api.internal.util.AlipaySignature;

import com.alipay.api.request.AlipayTradePagePayRequest;

import com.alipay.demo.trade.config.Configs;


import com.jiagouedu.IAliPayService;
import com.jiagouedu.common.constants.Constants;
import com.jiagouedu.common.constants.PayAlipayType;
import com.jiagouedu.common.utils.AliPayConfig;
import com.jiagouedu.common.utils.ConfigUtilZfb;
import com.jiagouedu.model.Product;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service(value = "aliPayServiceImpl")
public class AliPayServiceImpl implements IAliPayService {
   private static final Logger logger = LoggerFactory.getLogger(AliPayServiceImpl.class);
   //spring_boot实现方式 自行获取  回调参数notify_url


   @Override
   public String aliPayPc(Product product) {
      logger.info("支付宝下单");
      AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
      alipayRequest.setReturnUrl(ConfigUtilZfb.returnUrl);//前台通知
      alipayRequest.setNotifyUrl(ConfigUtilZfb.notifyurl);//后台回调
      JSONObject bizContent = new JSONObject();
      bizContent.put("out_trade_no", product.getOutTradeNo());
      bizContent.put("total_amount", product.getTotalFee());//订单金额:元
      bizContent.put("subject", product.getSubject());//订单标题
      bizContent.put("seller_id", Configs.getPid());//实际收款账号，一般填写商户PID即可
      bizContent.put("product_code", PayAlipayType.FAST_INSTANT_TRADE_PAY);//电脑网站支付
      bizContent.put("body", product.getBody());
      String biz = bizContent.toString().replaceAll("\"", "'");
      alipayRequest.setBizContent(biz);
      logger.info("业务参数:", alipayRequest.getBizContent());
      String form = Constants.FAIL;
      try {
         form = AliPayConfig.getAlipayClient().pageExecute(alipayRequest).getBody();
      } catch (AlipayApiException e) {
         logger.error("支付宝构造表单失败", e);
      }
      return form;
   }

   @Override
   public Boolean checkSign(Map<String, String> params) {
      try {
         return AlipaySignature.rsaCheckV1(params, null, "UTF-8");
      } catch (AlipayApiException e) {
         logger.error("支付宝验证签名失败", e);
         return false;
      }
   }
}
