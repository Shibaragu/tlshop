
package com.jiagouedu.core.jms;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.jiagouedu.core.Constants.RedisKeyPrefix;
import com.jiagouedu.core.cache.RedisUtil;
import com.jiagouedu.core.util.CommonMethod;
import com.jiagouedu.services.front.order.OrderService;
import com.jiagouedu.services.front.order.bean.Order;
import com.jiagouedu.web.action.front.miaosha.MiaoshaAction;

@Component
public class MiaoshaQueueMessageListener implements MessageListener {

	@Autowired
	private JmsTemplate jmsTemplate;
	@Autowired
	private Destination miaoshaQueue;
	@Autowired
	private OrderService orderService;
	@Autowired
	private ThreadPoolTaskExecutor threadPool;

	public void onMessage(Message message) {
		TextMessage msg = (TextMessage) message;
		String ms = "";
        try {
	        ms = msg.getText();
	        System.out.println("收到消息：" + ms);
        } catch (JMSException e) {
	        e.printStackTrace();
	        throw new RuntimeException("接收mq消息异常");
        }
		//转换成相应的对象
		final Order order = JSONObject.parseObject(ms, Order.class);
		if (order == null) {
			throw new RuntimeException("接收的mq消息有误");
		}
		final int productId = order.getOrderdetail().get(0).getProductID();
		//创建秒杀订单
		threadPool.execute(new Runnable() {
			public void run() {
				try {
	                Order realOrder = orderService.createMiaoshaOrder(order);
	                //设置用户是否已经秒杀成功过某个商品的标记
	                RedisUtil.set(CommonMethod.getMiaoshaOrderRedisKey(order.getAccount(), String.valueOf(productId)), realOrder);
	                //删除排队标记
	                RedisUtil.del(CommonMethod.getMiaoshaOrderWaitFlagRedisKey(order.getAccount(), String.valueOf(productId)));
				} catch (Exception e) {
					e.printStackTrace();
                	//删除排队标记
					RedisUtil.del(CommonMethod.getMiaoshaOrderWaitFlagRedisKey(order.getAccount(), String.valueOf(productId)));
					//还原缓存里的库存并清除内存里的商品售完标记
                	RedisUtil.incr(RedisKeyPrefix.PRODUCT_STOCK + "_" + productId);
        	        if (MiaoshaAction.getProductSoldOutMap().get(productId) != null) {
        	        	MiaoshaAction.getProductSoldOutMap().remove(productId);
                    }
                }
			}
		});
	}
}
