package com.jiagouedu.message.schedule;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component  
public class AnnotationQuartz {  
	
	
    @Scheduled(cron = "0/30 * * * * ?")
    public void processInitMsg() { 
    	//TODO 回调消息发送服务中心接口用消息业务id查找对应的业务数据是否生成，如果生成则直接将消息改成已发送状态并投递消息到mq
    }  
    
    @Scheduled(cron = "0/60 * * * * ?")
    public void processSentMsg() {  
    	//TODO 定时处理未完成的消息，如果已经几次查找消息都还是处于未完成状态，则很可能消息没有被消费者接收成功，由于消费者有幂等性实现，则可重发消息给mq
    }  
} 