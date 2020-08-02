package com.jiagouedu;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

/**
 * Description: TODO {一句话描述类是干什么的}<br/>
 * @author aaron.rao
 * @date: 2016年1月29日 下午2:51:02
 * @version 1.0
 * @since JDK 1.7
 */
public class AccountTest extends AbstractTest {
    
    @Test
    public void doLoginTest() throws Exception {
    	for (int i = 1; i <= 50; i++) {
    		List<NameValuePair> formparams = new ArrayList<NameValuePair>();  
            formparams.add(new BasicNameValuePair("account", "zhuge"+i));  
            formparams.add(new BasicNameValuePair("password", "123456"));  
            HttpEntity reqEntity = new UrlEncodedFormEntity(formparams, "utf-8");  
        
            HttpClient client = new DefaultHttpClient();  
            HttpPost post = new HttpPost("http://localhost:8080/shop-web/account/doLogin");  
            post.setEntity(reqEntity);  
            HttpResponse response = client.execute(post);  
        
            if (response.getStatusLine().getStatusCode() == 200) {  
                HttpEntity resEntity = response.getEntity();  
                String message = EntityUtils.toString(resEntity, "utf-8");  
                System.out.println(message);  
            } else {  
                System.out.println("请求失败");  
            } 
        }
    }
}