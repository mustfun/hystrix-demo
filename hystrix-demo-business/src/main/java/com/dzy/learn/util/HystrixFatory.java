package com.dzy.learn.util;

import com.dzy.learn.model.HystrixCommandConfig;
import com.netflix.hystrix.*;
import com.netflix.hystrix.HystrixCommand.Setter;
import net.sf.cglib.proxy.MethodProxy;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;

public class HystrixFatory {

	// 命令组(同时也是对服务进行分组)
	private final static String GROUP_KEY = "default_group";
	
	// 命令(服务标识)
	private final static String COMMAND_KEY = "default_command";
	
	// 线程池(将服务隔离到不同的线程中，避免共享线程池导致其他服务不可用)
	private final static String THREAD_POOL_KEY = "default_thread_pool";
	
	// 线程池核心线程数，默认值：10 (下面是线程池的一些配置)
	private final static String CORE_THREAD_SIZE = "10";
	
	// 请求等待队列，默认值：-1
	private final static String MAX_QUEUE_SIZE = "-1";
	
	// 隔离策略，THREAD和SEMAPHORE，默认使用THREAD模式
	private final static String STRATEGY = "THREAD";
	
	// 超时时间，THREAD模式下，超时可以中断，SEMAPHORE模式下，等待执行完成，默认值：1000
	private final static String TIMEOUT_IN_MILLISECONDS = "1000";
	
	// 信号量最大并发度，SEMAPHORE模式有效，默认值：10
	private final static String EXECUTION_MAXCONCURRENT_REQUESTS = "10";
	
	// fallback最大并发度，SEMAPHORE模式有效，默认值：10
	private final static String FALLBACK_MAXCONCURRENT_REQUESTS = "10";
	
	// 熔断触发的最小个数/10s，默认值：20
	private final static String REQUEST_VOLUME_THRESHOLD = "20";
	
	// 熔断后多少毫秒开启尝试，默认值：5000
	private final static String SLEEP_WINDOW_IN_MILLISECONDS = "5000";
	
	// 失败率达到多少百分比后熔断，默认值：50
	private final static String ERROR_THRESHOLD_PERCENTAGE = "50";
	
	private HystrixCommandConfig config;
	
	public HystrixFatory() {
		this.config.setGroupKey(GROUP_KEY);
		this.config.setCommandKey(COMMAND_KEY);
		this.config.setCoreThreadSize(CORE_THREAD_SIZE);
		this.config.setErrorThresholdPercentage(ERROR_THRESHOLD_PERCENTAGE);
		this.config.setExecutionMaxConcurrentRequests(EXECUTION_MAXCONCURRENT_REQUESTS);
		this.config.setFallbackMaxConcurrentRequests(FALLBACK_MAXCONCURRENT_REQUESTS);
		this.config.setMaxQueueSize(MAX_QUEUE_SIZE);
		this.config.setRequestVolumeThreshold(REQUEST_VOLUME_THRESHOLD);
		this.config.setSleepWindowInMilliseconds(SLEEP_WINDOW_IN_MILLISECONDS);
		this.config.setStrategy(STRATEGY);
		this.config.setThreadPoolKey(THREAD_POOL_KEY);
		this.config.setTimeoutInMilliseconds(TIMEOUT_IN_MILLISECONDS);
	}
	
	public HystrixFatory(HystrixCommandConfig config) {
		this.config = config;
		
		if (StringUtils.isEmpty(this.config.getGroupKey())) {
			this.config.setGroupKey(GROUP_KEY);
		}
		if (StringUtils.isEmpty(this.config.getCommandKey())) {
			this.config.setCommandKey(COMMAND_KEY);
		}
		if (StringUtils.isEmpty(this.config.getCoreThreadSize())) {
			this.config.setCoreThreadSize(CORE_THREAD_SIZE);
		}
		if (StringUtils.isEmpty(this.config.getErrorThresholdPercentage())) {
			this.config.setErrorThresholdPercentage(ERROR_THRESHOLD_PERCENTAGE);
		}
		if (StringUtils.isEmpty(this.config.getExecutionMaxConcurrentRequests())) {
			this.config.setExecutionMaxConcurrentRequests(EXECUTION_MAXCONCURRENT_REQUESTS);
		}
		if (StringUtils.isEmpty(this.config.getFallbackMaxConcurrentRequests())) {
			this.config.setFallbackMaxConcurrentRequests(FALLBACK_MAXCONCURRENT_REQUESTS);
		}
		if (StringUtils.isEmpty(this.config.getMaxQueueSize())) {
			this.config.setMaxQueueSize(MAX_QUEUE_SIZE);
		}
		if (StringUtils.isEmpty(this.config.getRequestVolumeThreshold())) {
			this.config.setRequestVolumeThreshold(REQUEST_VOLUME_THRESHOLD);
		}
		if (StringUtils.isEmpty(this.config.getSleepWindowInMilliseconds())) {
			this.config.setSleepWindowInMilliseconds(SLEEP_WINDOW_IN_MILLISECONDS);
		}
		if (StringUtils.isEmpty(this.config.getStrategy())) {
			this.config.setStrategy(STRATEGY);
		}
		if (StringUtils.isEmpty(this.config.getThreadPoolKey())) {
			this.config.setThreadPoolKey(THREAD_POOL_KEY);
		}
		if (StringUtils.isEmpty(this.config.getTimeoutInMilliseconds())) {
			this.config.setTimeoutInMilliseconds(TIMEOUT_IN_MILLISECONDS);
		}
	}
	
	public HystrixCommand<Object> create(MethodProxy methodProxy, Object obj, Object[] params, Method failMethod, Object target) {
		
		HystrixCommand<Object> command = new HystrixCommand<Object>(Setter
				.withGroupKey(HystrixCommandGroupKey.Factory.asKey(config.getGroupKey()))
				.andCommandKey(HystrixCommandKey.Factory.asKey(config.getCommandKey()))
				.andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
						.withExecutionTimeoutInMilliseconds(Integer.parseInt(config.getTimeoutInMilliseconds()))
						.withExecutionIsolationStrategy(config.getStrategy().equalsIgnoreCase("semaphore") ? HystrixCommandProperties.ExecutionIsolationStrategy.SEMAPHORE : HystrixCommandProperties.ExecutionIsolationStrategy.THREAD)
						.withRequestCacheEnabled(true)
						.withCircuitBreakerRequestVolumeThreshold(Integer.parseInt(config.getRequestVolumeThreshold()))
						.withCircuitBreakerSleepWindowInMilliseconds(Integer.parseInt(config.getSleepWindowInMilliseconds()))
						.withCircuitBreakerErrorThresholdPercentage(Integer.parseInt(config.getErrorThresholdPercentage()))
						.withFallbackIsolationSemaphoreMaxConcurrentRequests(Integer.parseInt(config.getFallbackMaxConcurrentRequests()))
						.withExecutionIsolationSemaphoreMaxConcurrentRequests(Integer.parseInt(config.getExecutionMaxConcurrentRequests())))
				.andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey(config.getThreadPoolKey()))
				.andThreadPoolPropertiesDefaults(HystrixThreadPoolProperties.Setter()
						.withCoreSize(Integer.parseInt(config.getCoreThreadSize()))
						.withMaxQueueSize(Integer.parseInt(config.getMaxQueueSize())))) {

			@Override
			protected Object run() throws Exception {
				try {
					Object result = methodProxy.invokeSuper(obj, params);
					return result;
				} catch (Throwable e) {
					e.printStackTrace();
				}
				
				return null;
			}

			@Override
			protected Object getFallback() {
				try {
					if (failMethod != null) {
						Object result = failMethod.invoke(target, params);
						return result;
					}
				} catch (Throwable e) {
					e.printStackTrace();
				}
				
				return super.getFallback();
			}
			
		};
		
		return command;
	}
}
