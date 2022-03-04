package com.coeuy.osp.rmq.adepts.annotation;

import com.coeuy.osp.rmq.adepts.common.InitListener;
import com.coeuy.osp.rmq.adepts.exception.RmqException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 注解自动开启消费处理
 * @author yarnk
 */
@Component
@Slf4j
public class RmqConsumerHandler implements ApplicationContextAware, ApplicationListener<ContextRefreshedEvent> {

    private  ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Resource
    private InitListener initListener;

    @Override
    public void onApplicationEvent(@NonNull ContextRefreshedEvent event) {
        Class<? extends RmqConsumer> annotationClass = RmqConsumer.class;
        Map<String, Object> annotation = applicationContext.getBeansWithAnnotation(annotationClass);
        Set<Map.Entry<String,Object>> entitySet = annotation.entrySet();
        for (Map.Entry<String,Object> entry :entitySet){
            String name = entry.getValue().getClass().getName();
            log.info("load mq consumer: {}",name);
            Class<?> clazz = entry.getValue().getClass();
            RmqConsumer componentDesc = AnnotationUtils.findAnnotation(clazz,RmqConsumer.class);
            if (Objects.isNull(componentDesc)){
                continue;
            }
            Object bean = getBean(clazz);
            String rk = componentDesc.exchange()+componentDesc.queue();
            if (Objects.nonNull(componentDesc.rk()) && !"".equals(componentDesc.rk())){
                rk = componentDesc.rk();
            }
            if (componentDesc.core()<1||componentDesc.max()<1||componentDesc.max()<componentDesc.core()){
                throw new RmqException("Rabbit consumer "+name+" pool params is error! Init task skip now, please check!\n初始化消息监听失败：线程配置错误，请检查注解的线程配置！");
            }
            initListener.init(
                    componentDesc.exchange(),
                    componentDesc.type(),
                    componentDesc.queue(),
                    rk,
                    componentDesc.core(),
                    componentDesc.max(),
                    componentDesc.interval(),
                    componentDesc.delayed(),
                    bean);
        }
    }
    public <T> T getBean(Class<T> tClass) {
        return applicationContext.getBean(tClass);
    }
}
