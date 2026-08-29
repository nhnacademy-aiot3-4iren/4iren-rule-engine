package com.nhnacademy.ruleengine.common.external.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.ruleengine.common.external.client.RoomSensorClient;
import com.nhnacademy.ruleengine.common.external.dto.RoomDeviceInfo;
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

//    @Mock private RoomDeviceCacheRepository cacheRepository;
    @Mock private RoomSensorClient roomSensorClient;

    @Spy private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private RoomDeviceCacheService roomDeviceCacheService;

    private final Long ROOM_ID = 101L;

    @Test
    @DisplayName("비즈니스 검증: 외부 API 조회 성공 시, 받아온 디바이스 리스트를 그대로 정확하게 반환해야 한다")
    void getRoomDevices_CacheHit() {
        List<RoomDeviceInfo> apiList = List.of(new RoomDeviceInfo(ROOM_ID, "eui", "name", null));
//        when(cacheRepository.get(1L)).thenReturn(cachedList);
        when(roomSensorClient.getRoomDevices(ROOM_ID)).thenReturn(apiList);

        List<RoomDeviceInfo> result = roomDeviceCacheService.getRoomDevices(ROOM_ID);

        assertThat(result).isEqualTo(apiList);
        verify(roomSensorClient, times(1)).getRoomDevices(ROOM_ID);
    }


    @Test
    @DisplayName("비즈니스 검증: 외부 API 호출 실패 시, 더미 데이터 반환")
    void getRoomDevices_ClientFail_ReturnsDummyData() {
        // Given (외부 통신 장비가 에러를 던지며 다운된 상황을 정의)
        when(roomSensorClient.getRoomDevices(ROOM_ID)).thenThrow(new RuntimeException("External API Connection Refused"));

        // When (서비스 메서드 실행 -> try-catch에 의해 예외가 포획되고 getDummyDevices()가 호출됨)
        List<RoomDeviceInfo> result = roomDeviceCacheService.getRoomDevices(ROOM_ID);

        // Then (더미 데이터 구조 내부의 핵심 값들이 누락 없이 역직렬화되었는지 최종 확인)
        assertThat(result).hasSize(3); // 하드코딩 텍스트 내의 원소 개수는 3개
        assertThat(result.get(0).devEui()).isEqualTo("24e124126d152862");
        assertThat(result.get(1).deviceName()).isEqualTo("AM107-067999");
        assertThat(result.get(2).roomId()).isEqualTo(101);

        // 에러 상황이었지만 비즈니스 예외 방어막에 의해 정상적인 리스트 객체가 생성되었음을 확정
        assertThat(result).doesNotContainNull();
    }
}