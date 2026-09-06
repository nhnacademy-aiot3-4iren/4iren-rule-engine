package com.nhnacademy.ruleengine.common.external.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.ruleengine.common.external.client.RoomSensorClient;
import com.nhnacademy.ruleengine.common.external.dto.RoomDeviceInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomDeviceCacheService {
    private final RoomSensorClient roomSensorClient;
    private final ObjectMapper objectMapper;//추후 더미데이터와 함께 삭제

    @Cacheable(value = "room:devices", key = "#roomId", unless = "#result == null || #result.isEmpty()", cacheManager = "sensorCacheManager")
    public List<RoomDeviceInfo> getRoomDevices(Long roomId) {
        log.info("강의실 장비 목록 캐시 미스, 외부 API 조회 roomId={}", roomId);

        try{
            return roomSensorClient.getRoomDevices(roomId);
        }catch (Exception e){
            log.error("강의실 장비 목록 외부 API 호출 실패, 더미 데이터 사용 roomId={}", roomId, e);
            return getDummyDevices();
        }

    }
    //TODO 테스트용 추후 삭제
    private List<RoomDeviceInfo> getDummyDevices() {
        try {
            log.info("테스트용 강의실 장비 더미 데이터 생성");
            return objectMapper.readValue("""
                        [
                          {
                            "roomId": 101,
                            "devEui": "24e124126d152862",
                            "deviceName": "EM500-CO2-152862",
                            "measurement": {
                              "co2": "ppm",
                              "temperature": "°C",
                              "humidity": "%",
                              "pressure": "Pa"
                            }
                          },
                          {
                            "roomId": 101,
                            "devEui": "24e124128c067999",
                            "deviceName": "AM107-067999",
                            "measurement": {
                              "tvoc": "ppb",
                              "illumination": "lux",
                              "co2": "ppm",
                              "temperature": "°C",
                              "humidity": "%",
                              "pressure": "Pa"
                            }
                          },
                          {
                            "roomId": 101,
                            "devEui": "24e124128c140101",
                            "deviceName": "AM107-140101",
                            "measurement": {
                              "tvoc": "ppb",
                              "illumination": "lux",
                              "co2": "ppm",
                              "temperature": "°C",
                              "humidity": "%",
                              "pressure": "Pa",
                              "door" : ""
                            }
                          }
                        ]
                        """,new TypeReference<List<RoomDeviceInfo>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
