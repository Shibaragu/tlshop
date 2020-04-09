package com.jiagouedu.wxpay.service;/* ━━━━━━如来保佑━━━━━━
 * 　　　┏┓　　　┏┓
 * 　　┏┛┻━━━┛┻┓
 * 　　┃　　　━　　　┃
 * 　　┃　┳┛　┗┳　┃
 * 　　┃　　　┻　　　┃
 * 　　┗━┓　　　┏━┛
 * 　　　　┃　　　┗━━━┓
 * 　　　　┃　　　　　　　┣┓
 * 　　　　┃　　　　　　　┏┛
 * 　　　　┗┓┓┏━┳┓┏┛
 * 　　　　　┗┻┛　┗┻┛
 * ━━━━━━永无BUG━━━━━━
 * 图灵学院-悟空老师
 * 以往视频加小乔老师QQ：895900009
 * 悟空老师QQ：245553999
 */

import com.jiagouedu.IWeiXinPayService;
import com.jiagouedu.common.constants.Constants;
import com.jiagouedu.common.utils.*;
import com.jiagouedu.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@Service(value = "weixinPayServiceImpl")
public class WeiXinPayServiceImpl implements IWeiXinPayService{
   private static final Logger logger = LoggerFactory.getLogger(WeiXinPayServiceImpl.class);


   public String weixinPay(Product product) {
      logger.info("订单号：{}生成微信支付码",product.getOutTradeNo());
      String  urlCode =null;
      try {

         //String imgPath= product.getOutTradeNo()+".png";
         // 账号信息
         String key = ConfigUtil.API_KEY; // key
         String trade_type = "NATIVE";// 交易类型 原生扫码支付
         SortedMap<Object, Object> packageParams = new TreeMap<Object, Object>();
         ConfigUtil.commonParams(packageParams);
         packageParams.put("product_id", product.getProductId());// 商品ID
         packageParams.put("body", product.getBody());// 商品描述
         packageParams.put("out_trade_no", product.getOutTradeNo());// 商户订单号
         String totalFee = product.getTotalFee();
         totalFee =  CommonUtil.subZeroAndDot(totalFee);
         packageParams.put("total_fee", totalFee);// 总金额
         packageParams.put("spbill_create_ip",ConfigUtil.IP);// 发起人IP地址
         packageParams.put("notify_url", ConfigUtil.notifyurl);// 回调地址
         packageParams.put("trade_type", trade_type);// 交易类型
         String sign = PayCommonUtil.createSign("UTF-8", packageParams, key);
         packageParams.put("sign", sign);// 签名

         String requestXML = PayCommonUtil.getRequestXml(packageParams);
         System.out.println(requestXML);
         String resXml = HttpUtil.postData(ConfigUtil.UNIFIED_ORDER_URL, requestXML);
         System.out.println(resXml);
         Map map = XMLUtil.doXMLParse(resXml);
         String returnCode = (String) map.get("return_code");
         if("SUCCESS".equals(returnCode)){
            String resultCode = (String) map.get("result_code");
            if("SUCCESS".equals(resultCode)){
               logger.info("订单号：{}生成微信支付码成功",product.getOutTradeNo());
                urlCode = (String) map.get("code_url");
               ConfigUtil.shorturl(urlCode);//转换为短链接

            }else{
               String errCodeDes = (String) map.get("err_code_des");
               logger.info("订单号：{}生成微信支付码(系统)失败:{}",product.getOutTradeNo(),errCodeDes);
            }
         }else{
            String returnMsg = (String) map.get("return_msg");
            logger.info("(订单号：{}生成微信支付码(通信)失败:{}",product.getOutTradeNo(),returnMsg);
         }
      } catch (Exception e) {
         logger.error("订单号：{}生成微信支付码失败(系统异常))",product.getOutTradeNo(),e);
      }
      return urlCode;
   }
}
