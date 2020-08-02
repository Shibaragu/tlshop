package com.jiagouedu.core.cache;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import com.alibaba.fastjson.JSON;
import com.jiagouedu.core.util.SpringContextHolder;

public class RedisUtil {

    private static final Logger log = Logger.getLogger(RedisUtil.class);
    private static JedisPool jedisPool = SpringContextHolder.getBean(JedisPool.class);

    /**
     * 获取Jedis实例
     * 
     * @return
     */
    public synchronized static Jedis getJedis() {
        try {
            if (jedisPool != null) {
                Jedis resource = jedisPool.getResource();
                return resource;
            } else {
                return null;
            }
        } catch (Exception e) {
            log.error("Redis缓存获取Jedis实例 出错！", e);
            return null;
        }
    }

    /**
     * 释放jedis资源
     * 
     * @param jedis
     */
    public static void returnResource(final Jedis jedis) {
        if (jedis != null) {
            jedisPool.returnResource(jedis);
        }
    }
    
    /**
     * 获取缓存
     * @param key 键
     * @return 值
     */
    public static String get(String key) {
        String value = null;
        Jedis jedis = null;
        try {
        	jedis = getJedis();
            if (jedis.exists(key)) {
                value = jedis.get(key);
                value = org.apache.commons.lang.StringUtils.isNotBlank(value) && !"nil".equalsIgnoreCase(value) ? value : null;
            }
        } catch (Exception e) {
        	log.warn("Redis缓存查询key值 出错！", e);
        } finally {
            returnResource(jedis);
        }
        return value;
    }
    
    /**
	 * 获取当个对象
	 * */
	public static <T> T get(String prefix, String key,  Class<T> clazz) {
		 Jedis jedis = null;
		 try {
			 jedis =  jedisPool.getResource();
			 //生成真正的key
			 String realKey  = prefix + "_" + key;
			 String  str = jedis.get(realKey);
			 T t =  stringToBean(str, clazz);
			 return t;
		 }finally {
			 returnResource(jedis);
		 }
	}
	
	/**
	 * 获取当个对象
	 * */
	public static <T> T get(String key,  Class<T> clazz) {
		Jedis jedis = null;
		try {
			jedis =  jedisPool.getResource();
			String  str = jedis.get(key);
			T t =  stringToBean(str, clazz);
			return t;
		}finally {
			returnResource(jedis);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T stringToBean(String str, Class<T> clazz) {
		if(str == null || str.length() <= 0 || clazz == null) {
			 return null;
		}
		if(clazz == int.class || clazz == Integer.class) {
			 return (T)Integer.valueOf(str);
		}else if(clazz == String.class) {
			 return (T)str;
		}else if(clazz == long.class || clazz == Long.class) {
			return  (T)Long.valueOf(str);
		}else {
			return JSON.toJavaObject(JSON.parseObject(str), clazz);
		}
	}

    /**
     * 向缓存中设置字符串内容
     * 
     * @param key
     *            key
     * @param value
     *            value
     * @return
     * @throws Exception
     */
    public static boolean set(String key, String value) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            if(jedis != null){
                jedis.set(key, value);
            }
            return true;
        } catch (Exception e) {
            log.error("Redis缓存设置key值 出错！", e);
            return false;
        } finally {
            returnResource(jedis);
        }
    }
    
    public static boolean set(String key, String value, String nxxx, String expx, long time) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            String result = "";
            if(jedis != null){
            	result = jedis.set(key, value, nxxx, expx, time);
                
            }
            if ("OK".equals(result)) {
				return true;
			}
            return false;
        } catch (Exception e) {
            log.error("Redis缓存设置key值 出错！", e);
            return false;
        } finally {
            returnResource(jedis);
        }
    }
    
    /**
     * 设置key失效
     * @param key
     * @param seconds
     */
     public static void expire(String key, int seconds) {
         Jedis jedis = null;
         try {
        	 jedis = getJedis();
             jedis.expire(key, seconds);
         } catch (Exception e) {
        	 log.error("Redis缓存设置key值的超时时间 出错！", e);
         } finally {
             returnResource(jedis);
         }
     }
    
    /**
     * 删除缓存中的对象，根据key
     * @param key
     * @return
     */
    public static boolean del(String key) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            jedis.del(key);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            returnResource(jedis);
        }
    }
    
    
    //*******************key-value****************start
    
    /**
     * 向缓存中设置对象
     * 
     * @param key
     * @param value
     * @return
     */
    public static boolean set(String key, Object value) {
        Jedis jedis = null;
        try {
            String objectJson = JSONObject.fromObject(value).toString();
            jedis = getJedis();
            if (jedis != null) {
                jedis.set(key, objectJson);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            returnResource(jedis);
        }
    }
    
    /**
     * 设置缓存
     * @param key 键
     * @param value 值
     * @param cacheSeconds 超时时间，0为不超时
     * @return
     */
    public static boolean set(String key, String value, int cacheSeconds) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            if (jedis != null) {
            	jedis.set(key, value);
            }
            if (cacheSeconds != 0) {
                jedis.expire(key, cacheSeconds);
            }
            return true;
        } catch (Exception e) {
        	e.printStackTrace();
            return false;
        } finally {
            returnResource(jedis);
        }
    }
    
    //*******************key-value****************end
    
    //*************** 操作list****************start
    /**
     * 向缓存中设置对象 
     * @param key
     * @param list
     * T string calss
     * @return
     */
    public static <T> boolean setList(String key,List<T> list){
        Jedis jedis = null;
        try {
            jedis = getJedis();
            if (jedis != null) {
                for (T vz : list) {
                    if (vz instanceof String) {
                        jedis.lpush(key, (String) vz);
                    } else {
                        String objectJson = JSONObject.fromObject(vz).toString();
                        jedis.lpush(key, objectJson);
                    }
                }
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            returnResource(jedis);
        }
    }
    
    
    //*************** 操作list****************end
    
    //*************** 操作map****************start
    public static <K,V> boolean setMap(String key,Map<String,V> map){
        Jedis jedis = null;
        try {
            jedis = getJedis();
            if (jedis != null) {
                Set<Map.Entry<String, V>> entry = map.entrySet();
                for (Iterator<Map.Entry<String, V>> ite = entry.iterator(); ite.hasNext();) {
                    Map.Entry<String, V> kv = ite.next();
                    if (kv.getValue() instanceof String) {
                        jedis.hset(key, kv.getKey(), (String) kv.getValue());
                    }else if (kv.getValue() instanceof List) {
                        jedis.hset(key, kv.getKey(), JSONArray.fromObject(kv.getValue()).toString());
                    } else {
                        jedis.hset(key, kv.getKey(), JSONObject.fromObject(kv.getValue()).toString());
                    }
                }
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            returnResource(jedis);
        }
    }
    
    public static boolean setMapKey(String key,String mapKey,Object value){
        Jedis jedis = null;
        try {
            jedis = getJedis();
            if (jedis != null) {
                if (value instanceof String) {
                    jedis.hset(key, mapKey, String.valueOf(value));
                } else if (value instanceof List) {
                    jedis.hset(key, mapKey, JSONArray.fromObject(value).toString());
                } else {
                    jedis.hset(key, mapKey, JSONObject.fromObject(value).toString());
                }
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            returnResource(jedis);
        }
    }
    
    /**
     * seconds key和value 保存的有效时间（单位：秒）
     * @return
     */
    public static boolean setMapKeyExpire(String key,String mapKey,Object value, int seconds){
        Jedis jedis = null;
        try {
            jedis = getJedis();
            if (jedis != null) {
                if (value instanceof String) {
                    jedis.hset(key, mapKey, String.valueOf(value));
                } else if (value instanceof List) {
                    jedis.hset(key, mapKey, JSONArray.fromObject(value).toString());
                } else {
                    jedis.hset(key, mapKey, JSONObject.fromObject(value).toString());
                }
                jedis.expire(key, seconds);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            returnResource(jedis);
        }
    }
    
    public static boolean delMapKey(String key,String mapKey){
        Jedis jedis = null;
        try {
            jedis = getJedis();
            jedis.hdel(key, mapKey);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            returnResource(jedis);
        }
    }
    
    //*************** 操作map****************end
    
    //***************计数器应用INCR,DECR****************begin
    //Redis的命令都是原子性的，你可以轻松地利用INCR，DECR命令来构建计数器系统
    
    /**
     * incr(key)：名称为key的string增1操作
     */
    public static Long incr(String key){
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.incr(key);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            returnResource(jedis);
        }
    }
    
    /**
     * incrby(key, integer)：名称为key的string增加integer
     */
    public static boolean incrBy(String key, int value){
        Jedis jedis = null;
        try {
            jedis = getJedis();
            jedis.incrBy(key, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            returnResource(jedis);
        }
    }
    
    /**
     * decr(key)：名称为key的string减1操作
     */
    public static Long decr(String key){
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.decr(key);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            returnResource(jedis);
        }
    }
    
    /**
     * decrby(key, integer)：名称为key的string减少integer
     */
    public static boolean decrBy(String key, int value){
        Jedis jedis = null;
        try {
            jedis = getJedis();
            jedis.decrBy(key, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            returnResource(jedis);
        }
    }
    //***************计数器应用INCR,DECR****************end
    
    //***************使用sorted set(zset)甚至可以构建有优先级的队列系统***************begin
    /**
     * 向名称为key的zset中添加元素member，score用于排序。
     * 如果该元素已经存在，则根据score更新该元素的顺序
     */
    public static boolean zadd(String key, double score, String member){
        Jedis jedis = null;
        try {
            jedis = getJedis();
            jedis.zadd(key, score, member);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            returnResource(jedis);
        }
    }
    
    /**
     * 删除名称为key的zset中的元素member
     */
    public static boolean zrem(String key, String... members){
        Jedis jedis = null;
        try {
            jedis = getJedis();
            jedis.zrem(key, members);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            returnResource(jedis);
        }
    }
    
    //***************使用sorted set(zset)甚至可以构建有优先级的队列系统***************end
    
    //***************sorted set 处理***************************************begin
    //zset 处理
    public static boolean zaddObject(String key, double score, Object value){
        Jedis jedis = null;
        try {
            jedis = getJedis();
            String objectJson = JSONObject.fromObject(value).toString();
            jedis.zadd(key, score, objectJson);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            returnResource(jedis);
        }
    }
    
    //删除 元素
    public static  boolean zremObject(String key, Object value){
        Jedis jedis = null;
        try {
            jedis = getJedis();
            String objectJson = JSONObject.fromObject(value).toString();
            jedis.zrem(key, objectJson);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            returnResource(jedis);
        }
    }
    
    //**************sorted set******************************************end
    
    //***********************Redis Set集合操作**************************begin
    /**
     * sadd:向名称为Key的set中添加元素,同一集合中不能出现相同的元素值。（用法：sadd set集合名称 元素值）
     * @param key
     * @param value
     * @return
     */
    public static boolean sadd(String key, String value) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            jedis.sadd(key, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            returnResource(jedis);
        }
    }

    /**
     * srem:删除名称为key的set中的元素。（用法：srem set集合名称 要删除的元素值）
     * 
     * @param key
     * @param value
     * @return
     */
    public static boolean srem(String key, String value) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            jedis.srem(key, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            returnResource(jedis);
        }
    }

    /**
     * sdiff:返回所有给定key与第一个key的差集。（用法：sdiff set集合1 set集合2）
     * 
     * @param key1
     * @param key2
     * @return
     */
    public static Set<String> sdiff(String key1, String key2) {
        Jedis jedis = null;
        Set<String> diffList = null;
        try {
            jedis = getJedis();
            diffList = jedis.sdiff(key1, key2);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            returnResource(jedis);
        }
        return diffList;
    }

    /**
     * smembers(key) ：返回名称为key的set的所有元素
     * 
     * @param key
     * @return
     */
    public static Set<String> smembers(String key) {
        Jedis jedis = null;
        Set<String> list = null;
        try {
            jedis = getJedis();
            list = jedis.smembers(key);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            returnResource(jedis);
        }
        return list;
    }
    
    //***********************Redis Set集合操作****************************end
}