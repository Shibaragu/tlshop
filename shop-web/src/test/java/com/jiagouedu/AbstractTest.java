package com.jiagouedu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

/** 
 * 带有spring容器环境的测试类
 *
 * @author aaron.rao
 * @date: 2016年1月29日 下午2:57:15
 * @version 1.0
 * @since JDK 1.7
 */
@ContextConfiguration({ "classpath*:/applicationContext*.xml" })
public abstract class AbstractTest extends AbstractJUnit4SpringContextTests {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 根据类型获取bean
     * 
     * @param clazz bean类型
     * @return bean
     * @see ApplicationContext#getBean(Class)
     */
    public <T> T getBean(Class<T> clazz) {
        return applicationContext.getBean(clazz);
    }

}