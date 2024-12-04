package com.hbh.train.business.config;

import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;

import java.util.ArrayList;
import java.util.List;

@EnableCaching
@SpringBootApplication
@ComponentScan("com.hbh")
@MapperScan("com.hbh.train.*.mapper")
@EnableFeignClients("com.hbh.train.business.feign")
public class BusinessApplication {
    private static final Logger LOG = LoggerFactory.getLogger(BusinessApplication.class);
    public static void main(String[] args) {

        SpringApplication app=new SpringApplication(BusinessApplication.class);
        Environment env=app.run(args).getEnvironment();
        LOG.info("启动成功！");
        LOG.info("127.0.0.1:"+env.getProperty("server.port"));
        //限流
        initFlowRules();
        LOG.info("定义限流规则！");
    }
    private static void initFlowRules(){
        List<FlowRule> rules = new ArrayList<>();
        FlowRule rule = new FlowRule();
//        rule.setResource("doConfirm");
//        rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
//        // Set limit QPS to 20.
//        rule.setCount(1);
//        rules.add(rule);
        FlowRuleManager.loadRules(rules);
    }

}
