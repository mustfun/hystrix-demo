package com.dzy.learn.model;

public class HystrixCommandConfig {

	// 命令组
	private String groupKey;
	
	// 命令
	private String commandKey;
	
	// 线程池
	private String threadPoolKey;
	
	// 线程池核心线程数，默认值：10
	private String coreThreadSize;
	
	// 请求等待队列，默认值：-1
	private String maxQueueSize;
	
	// 隔离策略，THREAD和SEMAPHORE，默认使用THREAD模式
	private String strategy;
	
	// 超时时间，THREAD模式下，超时可以中断，SEMAPHORE模式下，等待执行完成，默认值：1000
	private String timeoutInMilliseconds;
	
	// 信号量最大并发度，SEMAPHORE模式有效，默认值：10
	private String executionMaxConcurrentRequests;
	
	// fallback最大并发度，SEMAPHORE模式有效，默认值：10
	private String fallbackMaxConcurrentRequests;
	
	// 熔断触发的最小个数/10s，默认值：20
	private String requestVolumeThreshold;
	
	// 熔断后多少毫秒开启尝试，默认值：5000
	private String sleepWindowInMilliseconds;
	
	// 失败率达到多少百分比后熔断，默认值：50
	private String errorThresholdPercentage;

	public String getGroupKey() {
		return groupKey;
	}

	public void setGroupKey(String groupKey) {
		this.groupKey = groupKey;
	}

	public String getCommandKey() {
		return commandKey;
	}

	public void setCommandKey(String commandKey) {
		this.commandKey = commandKey;
	}

	public String getThreadPoolKey() {
		return threadPoolKey;
	}

	public void setThreadPoolKey(String threadPoolKey) {
		this.threadPoolKey = threadPoolKey;
	}

	public String getCoreThreadSize() {
		return coreThreadSize;
	}

	public void setCoreThreadSize(String coreThreadSize) {
		this.coreThreadSize = coreThreadSize;
	}

	public String getMaxQueueSize() {
		return maxQueueSize;
	}

	public void setMaxQueueSize(String maxQueueSize) {
		this.maxQueueSize = maxQueueSize;
	}

	public String getStrategy() {
		return strategy;
	}

	public void setStrategy(String strategy) {
		this.strategy = strategy;
	}

	public String getTimeoutInMilliseconds() {
		return timeoutInMilliseconds;
	}

	public void setTimeoutInMilliseconds(String timeoutInMilliseconds) {
		this.timeoutInMilliseconds = timeoutInMilliseconds;
	}

	public String getExecutionMaxConcurrentRequests() {
		return executionMaxConcurrentRequests;
	}

	public void setExecutionMaxConcurrentRequests(String executionMaxConcurrentRequests) {
		this.executionMaxConcurrentRequests = executionMaxConcurrentRequests;
	}

	public String getFallbackMaxConcurrentRequests() {
		return fallbackMaxConcurrentRequests;
	}

	public void setFallbackMaxConcurrentRequests(String fallbackMaxConcurrentRequests) {
		this.fallbackMaxConcurrentRequests = fallbackMaxConcurrentRequests;
	}

	public String getRequestVolumeThreshold() {
		return requestVolumeThreshold;
	}

	public void setRequestVolumeThreshold(String requestVolumeThreshold) {
		this.requestVolumeThreshold = requestVolumeThreshold;
	}

	public String getSleepWindowInMilliseconds() {
		return sleepWindowInMilliseconds;
	}

	public void setSleepWindowInMilliseconds(String sleepWindowInMilliseconds) {
		this.sleepWindowInMilliseconds = sleepWindowInMilliseconds;
	}

	public String getErrorThresholdPercentage() {
		return errorThresholdPercentage;
	}

	public void setErrorThresholdPercentage(String errorThresholdPercentage) {
		this.errorThresholdPercentage = errorThresholdPercentage;
	}
	
}
