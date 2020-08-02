/*
package com.jiagouedu.swagger;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@EnableWebMvc
@EnableSwagger2  //使swagger2生效
@Configuration   //配置注解，自动在本类上下文加载一些环境变量信息
@ComponentScan(basePackages = "com.jiagouedu.controller") //需要扫描的包路径
public class SwaggerConfig extends WebMvcConfigurationSupport {

    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2).groupName("所有接口")//总分类标题
                .enable(true)//true或false决定文档是否显示
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.jiagouedu.controller"))
                .paths(PathSelectors.any())
                .build();
    }

    //
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("wukongAPI")
                .version("6.6.6")
                .build();

    }
}
*/
