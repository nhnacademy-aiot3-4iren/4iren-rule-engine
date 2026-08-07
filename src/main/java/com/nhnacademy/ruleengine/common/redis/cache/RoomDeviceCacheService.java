package com.nhnacademy.ruleengine.common.redis.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.ruleengine.domain.nodeconfig.dto.ExternalRoomDeviceInfo;
import com.nhnacademy.ruleengine.domain.nodeconfig.external.RoomDeviceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Slf4j
@Service
public class RoomDeviceCacheService {
    private static final Duration TTL = Duration.ofMinutes(10);

    private final RedisTemplate<String, Object> redisTemplate;//TODO 생성자 주입하기
    private final RoomDeviceClient roomDeviceClient;
    private final ObjectMapper objectMapper;

    public RoomDeviceCacheService(
            @Qualifier("externalRoomDeviceRedisTemplate")RedisTemplate<String, Object> redisTemplate,
            RoomDeviceClient roomDeviceClient,
            ObjectMapper objectMapper
            ){
        this.redisTemplate = redisTemplate;
        this.roomDeviceClient = roomDeviceClient;
        this.objectMapper = objectMapper;
    }

    public List<ExternalRoomDeviceInfo> getRoomDevices(Long roomId){
        String key = buildKey(roomId);

        Object cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            log.info("redis cache");
            return objectMapper.convertValue(
                    cached,
                    new TypeReference<List<ExternalRoomDeviceInfo>>() {}
            );

        }

        List<ExternalRoomDeviceInfo> devices;

        try {
            devices = roomDeviceClient.getRoomDevices(roomId);
        } catch (Exception e) {
            log.warn("External API unavailable. Using dummy payload.", e);
            devices = getDummyDevices();
        }
        redisTemplate.opsForValue().set(key, devices, TTL);
        return devices;
    }

    public String buildKey(Long roomId){
        return "room:%d:devices".formatted(roomId);
    }

    //TODO 테스트용 추후 삭제
    private List<ExternalRoomDeviceInfo> getDummyDevices() {
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
                        """,new TypeReference<List<ExternalRoomDeviceInfo>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}