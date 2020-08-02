package com.jiagouedu.web.action.front.paygate;/* ━━━━━━如来保佑━━━━━━
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

import com.jiagouedu.IAliPayService;
import com.jiagouedu.IWeiXinPayService;
import com.jiagouedu.core.front.SystemManager;
import com.jiagouedu.model.Product;
import com.jiagouedu.services.front.order.OrderService;
import com.jiagouedu.services.front.order.bean.Order;
import com.jiagouedu.services.front.orderpay.OrderpayService;
import com.jiagouedu.services.front.ordership.OrdershipService;
import com.jiagouedu.web.util.Constants;
import com.jiagouedu.web.util.ZxingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("pay")
public class PayAction {
   private Logger logger = LoggerFactory.getLogger(getClass());
   @Autowired
   SystemManager systemManager;
   @Autowired
   private OrderService orderService;
   @Autowired
   private OrdershipService ordershipService;
   @Autowired
   private OrderpayService orderpayService;
   @Autowired
   private IAliPayService aliPayService;
   @Autowired
   private IWeiXinPayService weixinPayService;


   @RequestMapping("/pcpay")
   public String pcPay(String orderId, ModelMap map) {
      logger.info("支付宝支付");
      Order order = orderService.selectById(orderId);
      if(order == null) {
         throw new NullPointerException("根据订单号查询不到订单信息！");
      }
      Product product=new Product();//支付product
      product.setProductId(order.getId());
      product.setSubject(order.getRemark());
      product.setOutTradeNo(order.getId());
      product.setBody(order.getRemark());
      product.setTotalFee(order.getAmount());
      String form  =  aliPayService.aliPayPc(product);
      map.addAttribute("form", form);
      return "paygate/alipay/pay";
   }

   @RequestMapping("/wxpay")
   public String wxpay(String orderId, ModelMap map) {
      logger.info("微信支付");
      Order order = orderService.selectById(orderId);
      if(order == null) {
         throw new NullPointerException("根据订单号查询不到订单信息！");
      }
      String imgPath= Constants.QRCODE_PATH+ Constants.SF_FILE_SEPARATOR+order.getId()+".png";
      Product product=new Product();//支付product
      product.setProductId(order.getId());
      product.setSubject(order.getRemark());
      product.setOutTradeNo(order.getId());
      product.setBody(order.getRemark());
      product.setTotalFee(order.getAmount());
      String urlCode  =  weixinPayService.weixinPay(product);
      ZxingUtils.getQRCodeImge(urlCode, 256, imgPath);// 生成二维码
      map.addAttribute("img", "../qrcode/"+order.getId()+".png");
      return "paygate/weixinpay/pay";
   }

   /***
    * 后台回调
    * @param request
    * @param response
    * @return
    */
   @RequestMapping("/notify")
   @ResponseBody
   public String notify(HttpServletRequest request, HttpServletResponse response) {
      logger.info("支付完成");
      Map<String, String> params = new HashMap<String, String>();
      String  message = "success";
      // 取出所有参数是为了验证签名
      Enumeration<String> parameterNames = request.getParameterNames();
      while (parameterNames.hasMoreElements()) {
         String parameterName = parameterNames.nextElement();
         params.put(parameterName, request.getParameter(parameterName));
      }
      //验证签名 校验签名
      String outtradeno=null;
      boolean signVerified = false;
      signVerified = aliPayService.checkSign(params);
           if (signVerified) {
         logger.info("支付宝验证签名成功！");
         // 若参数中的appid和填入的appid不相同，则为异常通知
            outtradeno = params.get("out_trade_no");
            //在数据库中查找订单号对应的订单，并将其金额与数据库中的金额对比，若对不上，也为异常通知
            String status = params.get("trade_status");
            if (status.equals("WAIT_BUYER_PAY")) { // 如果状态是正在等待用户付款
               logger.info(outtradeno + "订单的状态正在等待用户付款");
            } else if (status.equals("TRADE_CLOSED")) { // 如果状态是未付款交易超时关闭，或支付完成后全额退款
               logger.info(outtradeno + "订单的状态已经关闭");
            } else if (status.equals("TRADE_SUCCESS") || status.equals("TRADE_FINISHED")) { // 如果状态是已经支付成功
               logger.info("(支付宝订单号:"+outtradeno+"付款成功)");
               Order order = orderService.selectById(outtradeno);
               if(order == null) {
                  throw new NullPointerException("根据订单号查询不到订单信息！");
               }
               Order orderDto=new Order();
               orderDto.setId(order.getId());
               orderDto.setStatus(Order.order_paystatus_y);
               logger.info("订单支付成功:",order.getId());
               orderService.update(orderDto);
               request.setAttribute("orderid",order.getId());
            }
      } else { // 如果验证签名没有通过
         message =  "failed";
         logger.info("验证签名失败！");
      }

     return  message;//return "paygate/alipay/finish";
   }


}
