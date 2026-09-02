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

    @Mock private RoomSensorClient roomSensorClient;

    @Spy private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private RoomDeviceCacheService roomDeviceCacheService;

    private final Long ROOM_ID = 101L;

    @Test
    @DisplayName("비즈니스 검증: 외부 API 조회 성공 시, 받아온 디바이스 리스트를 그대로 정확하게 반환해야 한다")
    void getRoomDevices() {
        List<RoomDeviceInfo> apiList = List.of(new RoomDeviceInfo(ROOM_ID, "eui", "name", null));
        when(roomSensorClient.getRoomDevices(ROOM_ID)).thenReturn(apiList);

        List<RoomDeviceInfo> result = roomDeviceCacheService.getRoomDevices(ROOM_ID);

        assertThat(result).isEqualTo(apiList);
        verify(roomSensorClient, times(1)).getRoomDevices(ROOM_ID);
    }




}