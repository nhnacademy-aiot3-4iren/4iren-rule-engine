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
        log.info("강의실 관리 권한 캐시 미스, 외부 API 조회 roomId={}, userId={}", roomId, userId);

        try {
            return managementClient.getManagementAccessAllowed(roomId, userId);
        } catch (Exception e){
//            log.error("강의실 관리 권한 외부 API 호출 실패, 임시 허용 응답 사용 roomId={}, userId={}", roomId, userId, e);
//            return new RoomManagementAccessResponse(true);

            log.error("강의실 관리 권한 외부 API 호출 실패, 권한 검증 불가 roomId={}, userId={}", roomId, userId, e);
            return new RoomManagementAccessResponse(false);

        }

    }
}
