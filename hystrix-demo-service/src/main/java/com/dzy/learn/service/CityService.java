package com.dzy.learn.service;

import com.dzy.learn.model.City;
import rx.Observable;

import java.util.concurrent.Future;

/**
 * Created by dengzhiyuan on 2017/4/6.
 */
public interface CityService {
    City getOne(Integer id);
    Future<City> getCityFromFuture();
    Observable<City> getCityFromObserve();


    Integer addOneCity(City city);

    City saveAndGet(City city);
}
