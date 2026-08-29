package com.nhnacademy.ruleengine.common.external.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.ruleengine.common.external.dto.RoomDeviceInfo;
import com.nhnacademy.ruleengine.common.external.client.RoomSensorClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomDeviceCacheService {
//    private final RoomDeviceCacheRepository cacheRepository; // 리포지토리 주입
    private final RoomSensorClient roomSensorClient;
    private final ObjectMapper objectMapper;//추후 더미데이터와 함께 삭제

    @Cacheable(value = "room:devices", key = "#roomId", unless = "#result == null || #result.isEmpty()", cacheManager = "deviceCacheManager")
    public List<RoomDeviceInfo> getRoomDevices(Long roomId) {
        log.info("cache miss roomId = {}, 외부 API 조회", roomId);

        try{
            return roomSensorClient.getRoomDevices(roomId);
        }catch (Exception e){
            log.info("External API unavailable. Using dummy payload.", e);
            return getDummyDevices();
        }

//        List<RoomDeviceInfo> cached = cacheRepository.get(roomId);
//        if (cached != null) {
//            log.info("redis cache hit");
//            return cached;
//        }
//
//        try {
//            List<RoomDeviceInfo> devices = roomDeviceClient.getRoomDevices(roomId);
//            cacheRepository.set(roomId, devices);
//            return devices;
//        } catch (Exception e) {
//            log.warn("External API unavailable. Using dummy payload.", e);
//            return getDummyDevices();
//        }
    }
    //TODO 테스트용 추후 삭제
    private List<RoomDeviceInfo> getDummyDevices() {
        try {
            log.info("getDummyDevices");
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
                              "pressure": "Pa"
                            }
                          }
                        ]
                        """,new TypeReference<List<RoomDeviceInfo>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}