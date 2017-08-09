### Hystrix-demo
#### master分支
-  test包里面加入Hystrix各种Command基本使用方法
-  在项目里面加入了HystrixCommand实战项目


#### 实战hyystrix
- biz包存放main项目
- business包存放command
- service包存放对数据库访问
- facade不要放代码，自动引入，给外部调用
- 还可以加一个外部调用层，integration之类的


#### dev分支
- 添加断路器注解
- 添加断路器配置
- 支持启动时候cglib方式实例化
- 支持方法调用时候自动拦截，根据情况选择断路器
 
