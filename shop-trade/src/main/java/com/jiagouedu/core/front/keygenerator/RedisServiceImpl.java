package com.jiagouedu.core.front.keygenerator;/* ━━━━━━如来保佑━━━━━━
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

import io.shardingsphere.core.keygen.KeyGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.support.atomic.RedisAtomicInteger;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
@Service
public class RedisServiceImpl implements KeyGenerator {
   @Autowired
   private RedisTemplate redisTemplate;


   public String generator() {
      return String.valueOf(getId(getOrderIdPrefix(new Date())));
   }

   /**
    * 订单ID前缀
    *
    * @param date
    * @return
    */
   public String getOrderIdPrefix(Date date) {
      Calendar c = Calendar.getInstance();
      c.setTime(date);
      int year = c.get(Calendar.YEAR);
      int day = c.get(Calendar.DAY_OF_YEAR);
      int hour = c.get(Calendar.HOUR_OF_DAY);
      String dayFmt = String.format("%1$03d", day);// 补齐 第一个参数由1$引用（参数在参数列表中的位置） 03d 三位数字不够三位补0
      String hourFmt = String.format("%1$02d", hour);
      return (year - 2000) + dayFmt + hourFmt;
   }



   public Long getId(String prefix) {
      String key = "bit_order_id_" + prefix;
      String orderId = null;
      try {
         RedisAtomicInteger redisAtomicInteger=new RedisAtomicInteger(key,redisTemplate.getConnectionFactory());
         //开关 5分钟 数据加密 归零
         //jedis.expire(key,5*60*60); //先不写
         orderId = prefix + String.format("%1$05d", redisAtomicInteger.getAndIncrement());
         return Long.valueOf(orderId);
      } catch (Exception e) {
         e.printStackTrace();
         System.out.println("从redis中获取订单号失败");
      }finally {

      }
      return null;

   }

   @Override
   public Number generateKey() {
      return getId(getOrderIdPrefix(new Date()))/100;
   }
}
