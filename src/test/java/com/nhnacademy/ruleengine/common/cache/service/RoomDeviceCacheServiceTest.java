package com.nhnacademy.ruleengine.common.cache.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.ruleengine.common.cache.repository.RoomDeviceCacheRepository;
import com.nhnacademy.ruleengine.common.external.client.RoomDeviceClient;
import com.nhnacademy.ruleengine.domain.nodeconfig.dto.ExternalRoomDeviceInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomDeviceCacheServiceTest {

    @Mock private RoomDeviceCacheRepository cacheRepository;
    @Mock private RoomDeviceClient roomDeviceClient;

    @Spy private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private RoomDeviceCacheService roomDeviceCacheService;

    @Test
    @DisplayName("캐시에 디바이스 정보가 있을 경우 외부 API 호출 없이 캐시 데이터 반환")
    void getRoomDevices_CacheHit() {
        List<ExternalRoomDeviceInfo> cachedList = List.of(new ExternalRoomDeviceInfo(1L, "eui", "name", null));
        when(cacheRepository.get(1L)).thenReturn(cachedList);

        List<ExternalRoomDeviceInfo> result = roomDeviceCacheService.getRoomDevices(1L);

        assertThat(result).isEqualTo(cachedList);
        verify(roomDeviceClient, never()).getRoomDevices(any());
    }

    @Test
    @DisplayName("캐시 미스 시 외부 API를 호출하고 결과를 캐시에 저장 후 반환")
    void getRoomDevices_CacheMiss_ClientSuccess() {
        when(cacheRepository.get(1L)).thenReturn(null);
        List<ExternalRoomDeviceInfo> apiList = List.of(new ExternalRoomDeviceInfo(1L, "eui", "name", null));
        when(roomDeviceClient.getRoomDevices(1L)).thenReturn(apiList);

        List<ExternalRoomDeviceInfo> result = roomDeviceCacheService.getRoomDevices(1L);

        assertThat(result).isEqualTo(apiList);
        verify(cacheRepository).set(1L, apiList);
    }

    @Test
    @DisplayName("캐시 미스 및 외부 API 호출 실패 시 더미 데이터 반환")
    void getRoomDevices_CacheMiss_ClientFail_ReturnsDummy() {
        when(cacheRepository.get(1L)).thenReturn(null);
        when(roomDeviceClient.getRoomDevices(1L)).thenThrow(new RuntimeException("API Down"));

        List<ExternalRoomDeviceInfo> result = roomDeviceCacheService.getRoomDevices(1L);

        assertThat(result).isNotEmpty();
        assertThat(result.getFirst().devEui()).isEqualTo("24e124126d152862");
    }
}