package com.jiagouedu.core.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * 
 * 本软件版权归哈尔滨福特威尔科技有限公司享有，未经许可，
 * 禁止任何单位或者个人用作复制、修改、抄录、传播、研发、投入生产、管理等方式使用，
 * 或与其它产品捆绑使用、销售。凡有上述侵权行为的单位和个人，
 * 必须立即停止侵权行为并对侵权行为产生的一切不良后果承担法律责任。
 *
 * @类名称: SerializationUtil
 * @类描述: 序列化工具
 * @作者:李巍
 * @创建时间:2018年1月18日 下午7:55:11
 */
public class SerializationUtil {
	
	/***
	 * 序列化
	 * @param object
	 * @return
	 * @throws Exception
	 */
	public static byte[] serialize(Object object) throws Exception {
		ObjectOutputStream oos = null;
		ByteArrayOutputStream baos = null;
		try{
			baos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(baos);
			oos.writeObject(object);
			byte[] bytes = baos.toByteArray();
			return bytes;
			
		}catch(Exception e){
			throw e;
		}finally{
			if(oos != null){
				oos.close();
			}
			if(baos != null){
				baos.close();
			}
		}
	}
	
	/***
	 * 反序列化
	 * @param bytes
	 * @return
	 * @throws Exception
	 */
	public static Object deserialize(byte[] bytes) throws Exception{
		ByteArrayInputStream bais = null ;
		try{
			bais = new ByteArrayInputStream(bytes);
			ObjectInputStream ois = new ObjectInputStream(bais);
			return ois.readObject();
			
		}catch(Exception e){
			throw e;
		}finally {
			if(bais != null){
				bais.close();
			}
		}
	}
	
	

}
