
package com.jiagouedu.core.jms;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.jiagouedu.core.Constants.RedisKeyPrefix;
import com.jiagouedu.core.Constants.ZookeeperPathPrefix;
import com.jiagouedu.core.cache.RedisUtil;
import com.jiagouedu.core.util.CommonMethod;
import com.jiagouedu.core.util.SnowflakeIdWorker;
import com.jiagouedu.message.Constants;
import com.jiagouedu.message.service.MessageService;
import com.jiagouedu.services.front.order.OrderService;
import com.jiagouedu.services.front.order.bean.Order;
import com.jiagouedu.web.action.front.miaosha.MiaoshaAction;

@Component
public class MiaoshaQueueMessageListener implements MessageListener {
	
	private static final Logger logger = LoggerFactory.getLogger(MiaoshaQueueMessageListener.class);
	@Autowired
	private OrderService orderService;
	@Autowired
	private ThreadPoolTaskExecutor threadPool;
	@Autowired
	private ZooKeeper zooKeeper;
	@Autowired
	private MessageService messageService;

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
					//使用mq的分布式事务需要改造订单的主键id，要事先生成，不能再依赖于mysql的主键id
					Long orderID = SnowflakeIdWorker.generateId();
					order.setId(orderID.toString());
					//发送初始减库存消息
					messageService.receiveMsg(new com.jiagouedu.message.model.Message(orderID.toString(), Constants.BizType.ORDER_PRODUCT_STOCK, JSONObject.toJSONString(order), Constants.MessageStatus.INIT));
	                Order realOrder = orderService.createMiaoshaOrder(order);
	                //发送减库存确认消息
	                messageService.receiveMsg(new com.jiagouedu.message.model.Message(orderID.toString(), Constants.BizType.ORDER_PRODUCT_STOCK, JSONObject.toJSONString(realOrder), Constants.MessageStatus.SENT));
	                RedisUtil.set(CommonMethod.getMiaoshaOrderRedisKey(order.getAccount(), String.valueOf(productId)), realOrder);
				} catch (Exception e) { 
					logger.error("创建订单异常", e);
					//还原缓存里的库存并清除内存里的商品售完标记
                	RedisUtil.incr(RedisKeyPrefix.PRODUCT_STOCK + "_" + productId);
        	        if (MiaoshaAction.getProductSoldOutMap().get(productId) != null) {
        	        	MiaoshaAction.getProductSoldOutMap().remove(productId);
                    }
        	        //修改zk的商品售完标记为false
        	        try {
						if (zooKeeper.exists(ZookeeperPathPrefix.getZKSoldOutProductPath(String.valueOf(productId)), true) != null) {
							zooKeeper.setData(ZookeeperPathPrefix.getZKSoldOutProductPath(String.valueOf(productId)), "false".getBytes(), -1);
						}
					} catch (Exception e1) {
						logger.error("修改zk商品售完标记异常", e1);
					} 
                }finally {
                	//删除排队标记
					RedisUtil.del(CommonMethod.getMiaoshaOrderWaitFlagRedisKey(order.getAccount(), String.valueOf(productId)));
				}
			}
		});
	}
}