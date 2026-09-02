package com.nhnacademy.ruleengine.common.external.service;


import com.nhnacademy.ruleengine.common.external.client.RoomManagementClient;
import com.nhnacademy.ruleengine.common.external.dto.RoomManagementAccessResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class RoomManagementCacheService {

    private final RoomManagementClient managementClient;
    @Cacheable(value = "management-access", key = "#roomId + ':' + #userId", cacheManager = "roomManagementCacheManager")
    public RoomManagementAccessResponse getManagementAllowed(Long roomId, Long userId){
        log.info("management-access:... cache miss, 외부 API 조회");

        try {
            return managementClient.getManagementAccessAllowed(roomId, userId);
        } catch (Exception e){
        log.info("External API unavailable. Using dummy payload.", e);
        return new RoomManagementAccessResponse(true);
    }

    }
}
