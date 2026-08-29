package com.nhnacademy.ruleengine.common.external.service;

import com.nhnacademy.ruleengine.common.exception.invalid.InvalidMeasurementTypeException;
import com.nhnacademy.ruleengine.domain.nodeconfig.dto.DeviceInfo;
import com.nhnacademy.ruleengine.domain.nodeconfig.dto.ExternalRoomDeviceInfo;
import com.nhnacademy.ruleengine.domain.nodeconfig.dto.SensorStaticMeta;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.MeasurementType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SensorStaticMetaServiceTest {

    @Mock private RoomDeviceCacheService roomDeviceCacheService;

    @InjectMocks
    private SensorStaticMetaService sensorStaticMetaService;

    @Test
    @DisplayName("방에 디바이스가 없을 경우 빈 메타 리스트 반환")
    void getSensorStaticMetaList_EmptyDevices() {
        when(roomDeviceCacheService.getRoomDevices(1L)).thenReturn(List.of());
        List<SensorStaticMeta> result = sensorStaticMetaService.getSensorStaticMetaList(1L);
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("정상적으로 센서 메타데이터 리스트를 반환하며, 중복된 측정 타입의 단위는 기존 값을 유지한다")
    void getSensorStaticMetaList_Success() {
        ExternalRoomDeviceInfo device1 = new ExternalRoomDeviceInfo(1L, "eui1", "dev1", Map.of("co2", "ppm", "temperature", "C"));
        ExternalRoomDeviceInfo device2 = new ExternalRoomDeviceInfo(1L, "eui2", "dev2", Map.of("co2", "mg/m3"));

        when(roomDeviceCacheService.getRoomDevices(1L)).thenReturn(List.of(device1, device2));


        List<SensorStaticMeta> result = sensorStaticMetaService.getSensorStaticMetaList(1L);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(SensorStaticMeta::measurementType)
                .containsExactlyInAnyOrder(MeasurementType.CO2, MeasurementType.TEMPERATURE);

        SensorStaticMeta co2Meta = result.stream().filter(r -> r.measurementType() == MeasurementType.CO2).findFirst().get();
        assertThat(co2Meta.unit()).isEqualTo("ppm");
    }

    @Test
    @DisplayName("방에 디바이스가 없을 경우 빈 디바이스 옵션 리스트 반환")
    void getDeviceOptionsInRoom_Empty() {
        when(roomDeviceCacheService.getRoomDevices(1L)).thenReturn(List.of());
        List<DeviceInfo> result = sensorStaticMetaService.getDeviceOptionsInRoom(1L);
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("방에 있는 디바이스 옵션 목록을 중복 없이 반환")
    void getDeviceOptionsInRoom_Success() {
        ExternalRoomDeviceInfo device1 = new ExternalRoomDeviceInfo(1L, "eui1", "dev1", Map.of());
        ExternalRoomDeviceInfo device2 = new ExternalRoomDeviceInfo(1L, "eui1", "dev1", Map.of());
        when(roomDeviceCacheService.getRoomDevices(1L)).thenReturn(List.of(device1, device2));

        List<DeviceInfo> result = sensorStaticMetaService.getDeviceOptionsInRoom(1L);

        assertThat(result).hasSize(1); // distinct check
        assertThat(result.getFirst().devEui()).isEqualTo("eui1");
    }

    @Test
    @DisplayName("방에 디바이스가 없을 경우 빈 측정 타입 옵션 리스트 반환")
    void getMeasurementTypeOptionsInRoom_Empty() {
        when(roomDeviceCacheService.getRoomDevices(1L)).thenReturn(List.of());
        List<MeasurementType> result = sensorStaticMetaService.getMeasurementTypeOptionsInRoom(1L);
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("정상적으로 방에 있는 디바이스들의 측정 타입 옵션을 중복 없이 반환")
    void getMeasurementTypeOptionsInRoom_Success() {
        ExternalRoomDeviceInfo device1 = new ExternalRoomDeviceInfo(1L, "eui1", "dev1", Map.of("co2", "ppm"));
        ExternalRoomDeviceInfo device2 = new ExternalRoomDeviceInfo(1L, "eui2", "dev2", Map.of("co2", "ppm", "temperature", "C"));

        when(roomDeviceCacheService.getRoomDevices(1L)).thenReturn(List.of(device1, device2));

        List<MeasurementType> result = sensorStaticMetaService.getMeasurementTypeOptionsInRoom(1L);

        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder(MeasurementType.CO2, MeasurementType.TEMPERATURE);
    }

    @Test
    @DisplayName("알 수 없는 측정 타입이 포함된 경우 예외 발생")
    void getMeasurementTypeOptionsInRoom_ExceptionThrown() {
        ExternalRoomDeviceInfo device = new ExternalRoomDeviceInfo(1L, "eui1", "dev1", Map.of("unknown", "unit"));
        when(roomDeviceCacheService.getRoomDevices(1L)).thenReturn(List.of(device));

        assertThrows(InvalidMeasurementTypeException.class, () ->
                sensorStaticMetaService.getMeasurementTypeOptionsInRoom(1L)
        );
    }

    @Test
    @SuppressWarnings("unchecked")
    @DisplayName("미사용 private 메서드인 getDeviceInfoByMeasurementType 리플렉션 정상 동작 검증")
    void getDeviceInfoByMeasurementType_ReflectionTest_Success() throws Exception {
        ExternalRoomDeviceInfo device = new ExternalRoomDeviceInfo(1L, "eui1", "dev1", Map.of("co2", "ppm"));
        List<ExternalRoomDeviceInfo> list = List.of(device);

        Method method = SensorStaticMetaService.class.getDeclaredMethod("getDeviceInfoByMeasurementType", List.class);
        method.setAccessible(true);

        Map<MeasurementType, List<DeviceInfo>> result = (Map<MeasurementType, List<DeviceInfo>>) method.invoke(sensorStaticMetaService, list);

        assertThat(result).containsKey(MeasurementType.CO2);
        assertThat(result.get(MeasurementType.CO2)).hasSize(1);
        assertThat(result.get(MeasurementType.CO2).getFirst().devEui()).isEqualTo("eui1");
    }

    @Test
    @DisplayName("미사용 private 메서드 내에서 매핑 실패 시 예외 발생 검증")
    void getDeviceInfoByMeasurementType_ReflectionTest_Exception() throws Exception {
        ExternalRoomDeviceInfo device = new ExternalRoomDeviceInfo(1L, "eui1", "dev1", Map.of("unknown", "unit"));
        List<ExternalRoomDeviceInfo> list = List.of(device);

        Method method = SensorStaticMetaService.class.getDeclaredMethod("getDeviceInfoByMeasurementType", List.class);
        method.setAccessible(true);

        InvocationTargetException exception = assertThrows(InvocationTargetException.class, () ->
                method.invoke(sensorStaticMetaService, list)
        );

        assertThat(exception.getTargetException()).isInstanceOf(InvalidMeasurementTypeException.class);
    }
}