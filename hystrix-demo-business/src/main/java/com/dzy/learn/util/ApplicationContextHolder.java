package com.dzy.learn.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class ApplicationContextHolder implements ApplicationContextAware {

	public static ApplicationContext APPLICATION_CONTEXT = null;
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		APPLICATION_CONTEXT = applicationContext;
	}
	
	public static ApplicationContext getApplicationContext() {
		return APPLICATION_CONTEXT;
	}

}
