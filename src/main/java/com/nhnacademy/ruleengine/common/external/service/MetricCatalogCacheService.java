package com.nhnacademy.ruleengine.common.external.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.ruleengine.common.external.client.RoomSensorClient;
import com.nhnacademy.ruleengine.common.external.dto.MetricCatalogInfo;
import com.nhnacademy.ruleengine.common.external.dto.RoomDeviceInfo;
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
    private final ObjectMapper objectMapper;

    @Cacheable(value = "sensor:catalog", unless = "#result == null || #result.isEmpty()")
    public List<MetricCatalogInfo> getMetricCatalog(){
        log.info("cache miss, 외부 API 조회");

        try{
            return roomSensorClient.getMetricCatalog();
        }catch (Exception e){
            log.info("외부 API 호출 실패, 더미데이터 사용", e);
            return getDummyCatalog();
        }

    }
    private List<MetricCatalogInfo> getDummyCatalog() {
        try {
            return objectMapper.readValue("""
                    [
                      {
                        "metricCode": "co2",
                        "displayName": "이산화탄소 농도",
                        "metricKind": "GAUGE",
                        "status": "ACTIVE",
                        "description": "실내 공기 중 이산화탄소 농도",
                        "ucumCode": "[ppm]",
                        "unitDisplayName": "백만분율",
                        "symbol": "ppm"
                      },
                      {
                        "metricCode": "humidity",
                        "displayName": "상대습도",
                        "metricKind": "GAUGE",
                        "status": "ACTIVE",
                        "description": "실내 공기의 상대습도",
                        "ucumCode": "%",
                        "unitDisplayName": "퍼센트",
                        "symbol": "%"
                      },
                      {
                        "metricCode": "illumination",
                        "displayName": "조도",
                        "metricKind": "GAUGE",
                        "status": "ACTIVE",
                        "description": "실내 조도",
                        "ucumCode": "lx",
                        "unitDisplayName": "럭스",
                        "symbol": "lux"
                      },
                      {
                        "metricCode": "pressure",
                        "displayName": "기압",
                        "metricKind": "GAUGE",
                        "status": "ACTIVE",
                        "description": "실내 기압",
                        "ucumCode": "Pa",
                        "unitDisplayName": "파스칼",
                        "symbol": "Pa"
                      },
                      {
                        "metricCode": "temperature",
                        "displayName": "온도",
                        "metricKind": "GAUGE",
                        "status": "ACTIVE",
                        "description": "실내 공기의 섭씨 온도",
                        "ucumCode": "Cel",
                        "unitDisplayName": "섭씨",
                        "symbol": "°C"
                      },
                      {
                        "metricCode": "door",
                        "displayName": "문 열림 여부",
                        "metricKind": "STATE",
                        "status": "ACTIVE",
                        "description": "현재 문 열림/닫힘 상태",
                        "ucumCode": "1",
                        "unitDisplayName": "단위없음",
                        "symbol": ""
                      },
                      {
                        "metricCode": "tvoc",
                        "displayName": "총휘발성유기화합물",
                        "metricKind": "GAUGE",
                        "status": "ACTIVE",
                        "description": "실내 총휘발성유기화합물(TVOC) 농도",
                        "ucumCode": "[ppb]",
                        "unitDisplayName": "십억분율",
                        "symbol": "ppb"
                      }
                    ]
                    """, new TypeReference<List<MetricCatalogInfo>> (){});
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
