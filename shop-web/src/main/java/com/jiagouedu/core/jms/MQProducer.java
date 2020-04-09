package com.jiagouedu.core.jms;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.jiagouedu.services.front.order.bean.Order;

@Component
public class MQProducer {
	
	private JmsTemplate jmsTemplate;
	
	public JmsTemplate getJmsTemplate() {
		return jmsTemplate;
	}
	
	@Autowired
	public void setJmsTemplate(JmsTemplate jmsTemplate) {
		this.jmsTemplate = jmsTemplate;
	}

	public void sendMessage(final Order order) {
		jmsTemplate.send(new MessageCreator() {
			public Message createMessage(Session session) throws JMSException {
				return session.createTextMessage(JSONObject.toJSONString(order));
			}
		});
	}

}
