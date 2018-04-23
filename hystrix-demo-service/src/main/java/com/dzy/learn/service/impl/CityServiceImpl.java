package com.dzy.learn.service.impl;

import com.dzy.learn.dao.mapper.CityMapper;
import com.dzy.learn.model.City;
import com.dzy.learn.service.CityService;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.command.AsyncResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rx.Observable;
import rx.Subscriber;

import java.util.concurrent.Future;

/**
 * Created by dengzhiyuan on 2017/4/6.
 */
@Service
public class CityServiceImpl implements CityService {

    private static final Logger LOG = LoggerFactory.getLogger(CityServiceImpl.class);

    @Autowired
    private CityMapper cityMapper;

    @Override
    @HystrixCommand(fallbackMethod = "defaultCity")
    public City getOne(Integer id) {
        City city = null;
        city = cityMapper.selectByPrimaryKey(id);
        // 模拟取数据时候的异常信息
        try {
            //停顿2-12ms
            Thread.sleep((int) (Math.random() * 10) + 2);
        } catch (InterruptedException e) {
            // 不处理异常情况
        }

        // 40%的概率会去调用fallback,失败
        if (Math.random() > 0.01) {
            LOG.warn("因为代码原因和网络原因执行失败了，需要调用fallback方法");
            throw new RuntimeException("执行getOneCity请求时候随机失败啦----------");
        }

        // 5%的概率导致超时情况发生
        if (Math.random() > 0.95) {
            // 随机触发
            try {
                Thread.sleep((int) (Math.random() * 300) + 25);
            } catch (InterruptedException e) {
                // 超时不作处理
            }
        }
        LOG.warn("执行成功");
        return city;
    }

    public City defaultCity(Integer id){
        return generateTemplateCity();
    }

    private City generateTemplateCity() {
        LOG.info("我是回调方法，我被调用了........");
        City city  =new City();
        city.setId(1000);
        city.setName("我是测试城市");
        city.setCountry("中国");
        return city;
    }

    /**
     * 注意在这里测试一下spring 事务管理 和  sharding-jdbc结合得怎么样
     * @param city
     * @return
     */
    @Override
    @Transactional
    public Integer addOneCity(City city) {
        int insert = cityMapper.insert(city);
        //int i=1/0; 测试事务也成功啦！啦啦啦啦啦
        return insert;
    }

    /**
     * 测试一下写库和读库的读写情况
     * @param city
     * @return
     */
    @Override
    public City saveAndGet(City city) {
        int id=cityMapper.insert(city);
        return  cityMapper.selectByPrimaryKey(2);
        //实际测试sharding-jdbc在写完主库之后，会去从库读
    }


    @Override
    @HystrixCommand(fallbackMethod = "defaultCity")
    public Future<City> getCityFromFuture() {
        return new AsyncResult<City>() {
            @Override
            public City invoke() {
                return generateTemplateCity();
            }
        };
    }

    @Override
    @HystrixCommand(fallbackMethod="defaultCity")
    public Observable<City> getCityFromObserve() {
        return Observable.create(observer -> {
            try {
                if (!observer.isUnsubscribed()) {
                    observer.onNext(generateTemplateCity());
                    observer.onCompleted();
                }
            } catch (Exception e) {
                observer.onError(e);
            }
        });
    }


}
