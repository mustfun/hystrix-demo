package com.dzy.learn.util;

import com.dzy.learn.annotation.Hystrix;
import com.dzy.learn.enums.HystrixType;
import com.dzy.learn.model.HystrixConfig;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**<p>BeanPostProcessor这个东西，允许你在bean实例化前和实例化后做一些操作
 *
 * @author dengzhiyuan
 * @version 1.0
 * @date 2017/8/9
 * @since 1.0
 * @see BeanPostProcessor
 *
 */
public class HystrixCommandBeanPostProcessor implements BeanPostProcessor {


    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Class<?> aClass = bean.getClass();
        Method[] declaredMethods = aClass.getDeclaredMethods();

        List<HystrixConfig> list=new ArrayList<>();

        if (declaredMethods != null && declaredMethods.length > 0) {
            boolean isHystrix = false;
            for (Method method : declaredMethods) {
                Hystrix hystrix = method.getAnnotation(Hystrix.class);
                //说明方法上的这个注解是有值的
                if (hystrix != null) {
                    HystrixType type = hystrix.type();
                    String config = hystrix.config();
                    HystrixConfig hystrixConfig = new HystrixConfig();
                    hystrixConfig.setConfig(config);

                    if (type == HystrixType.SUCCESS) {
                        hystrixConfig.setMethod(method);
                    } else {
                        hystrixConfig.setFailMethod(method);
                    }

                    list.add(hystrixConfig);
                    isHystrix=true;
                }

            }

            if (isHystrix){
                //开始实例化这个bean
                return new HystrixCommandProxy().getProxy(bean,list);
            }


        }
        return null;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
}
