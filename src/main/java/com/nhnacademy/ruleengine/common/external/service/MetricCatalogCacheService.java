package com.nhnacademy.ruleengine.common.external.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.ruleengine.common.external.client.RoomSensorClient;
import com.nhnacademy.ruleengine.common.external.client.SensorCatalogClient;
import com.nhnacademy.ruleengine.common.external.dto.MetricCatalogInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MetricCatalogCacheService {

    private final RoomSensorClient roomSensorClient;
    private final SensorCatalogClient sensorCatalogClient;
    private final ObjectMapper objectMapper;


    @Cacheable(value = "sensor:catalog", unless = "#result == null || #result.isEmpty()", cacheManager = "sensorCacheManager")
    public List<MetricCatalogInfo> getMetricCatalog(){
        log.info("측정 항목 카탈로그 캐시 미스, 외부 API 조회");

        try{
            return sensorCatalogClient.getMetricCatalog();
        }catch (Exception e){

            log.error("측정 항목 카탈로그 외부 API 호출 실패", e);
            return List.of();
        }

    }
}
