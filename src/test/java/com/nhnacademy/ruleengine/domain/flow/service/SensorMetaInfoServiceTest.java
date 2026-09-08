package com.nhnacademy.ruleengine.domain.flow.service;

import com.nhnacademy.ruleengine.common.external.dto.MetricCatalogInfo;
import com.nhnacademy.ruleengine.common.external.dto.RoomDeviceInfo;
import com.nhnacademy.ruleengine.common.external.service.MetricCatalogCacheService;
import com.nhnacademy.ruleengine.common.external.service.RoomDeviceCacheService;
import com.nhnacademy.ruleengine.domain.flow.dto.SensorMetaInfo;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.MeasurementType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SensorMetaInfoServiceTest {

    @Mock private RoomDeviceCacheService roomDeviceCacheService;
    @Mock private MetricCatalogCacheService metricCatalogCacheService;
    @InjectMocks
    private RoomSensorMetaService roomSensorMetaService;

    @Test
    @DisplayName("방에 디바이스가 없을 경우 빈 메타 리스트 반환")
    void getSensorStaticMetaList_EmptyDevices() {
        when(roomDeviceCacheService.getRoomDevices(1L)).thenReturn(List.of());
        MetricCatalogInfo metaInfo1 = new MetricCatalogInfo("co2", "이산화탄소 농도", "GAUGE","ACTIVE", "실내 공기 중 이산화탄소 농도","[ppm]","백만분율","ppm");
        MetricCatalogInfo metaInfo2 = new MetricCatalogInfo("humidity", "상대습도", "GAUGE","ACTIVE", "실내 공기의 상대습도","%","퍼센트","%");

        when(metricCatalogCacheService.getMetricCatalog()).thenReturn(List.of(metaInfo1, metaInfo2));

        List<SensorMetaInfo> result = roomSensorMetaService.getSensorMetaList(1L);
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("정상적으로 센서 메타데이터 리스트를 반환하며, 중복된 측정 타입의 단위는 기존 값을 유지한다")
    void getSensorStaticMetaList_Success() {
        RoomDeviceInfo device1 = new RoomDeviceInfo(1L, "eui1", "dev1", Map.of("co2", "ppm", "temperature", "C", "door", ""));
        RoomDeviceInfo device2 = new RoomDeviceInfo(1L, "eui2", "dev2", Map.of("co2", "mg/m3"));

        MetricCatalogInfo metaInfo1 = new MetricCatalogInfo("co2", "이산화탄소 농도", "GAUGE","ACTIVE", "실내 공기 중 이산화탄소 농도","[ppm]","백만분율","ppm");
        MetricCatalogInfo metaInfo2 = new MetricCatalogInfo("humidity", "상대습도", "GAUGE","ACTIVE", "실내 공기의 상대습도","%","퍼센트","%");
        MetricCatalogInfo metaInfo3 = new MetricCatalogInfo("temperature", "온도", "GAUGE","ACTIVE", "실내 공기의 섭씨 온도","Cel","섭씨","°C");
        MetricCatalogInfo metaInfo4 = new MetricCatalogInfo("door", "문 열림 여부", "STATE","ACTIVE", "현재 문 열림/닫힘 상태","1","단위없음","");

        when(roomDeviceCacheService.getRoomDevices(1L)).thenReturn(List.of(device1, device2));
        when(metricCatalogCacheService.getMetricCatalog()).thenReturn(List.of(metaInfo1, metaInfo2, metaInfo3, metaInfo4));

        List<SensorMetaInfo> result = roomSensorMetaService.getSensorMetaList(1L);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(SensorMetaInfo::measurementType)
                .containsExactlyInAnyOrder(MeasurementType.CO2, MeasurementType.TEMPERATURE);

        SensorMetaInfo co2Meta = result.stream().filter(r -> r.measurementType() == MeasurementType.CO2).findFirst().get();
        assertThat(co2Meta.symbol()).isEqualTo("ppm");
    }


    @Test
    @DisplayName("방에 디바이스가 없을 경우 빈 측정 타입 옵션 리스트 반환")
    void getMeasurementTypeOptionsInRoom_Empty() {
        when(roomDeviceCacheService.getRoomDevices(1L)).thenReturn(List.of());
        List<MeasurementType> result = roomSensorMetaService.getMeasurementTypeOptionsInRoom(1L);
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("정상적으로 방에 있는 디바이스들의 측정 타입 옵션을 중복 없이 반환")
    void getMeasurementTypeOptionsInRoom_Success() {
        RoomDeviceInfo device1 = new RoomDeviceInfo(1L, "eui1", "dev1", Map.of("co2", "ppm"));
        RoomDeviceInfo device2 = new RoomDeviceInfo(1L, "eui2", "dev2", Map.of("co2", "ppm", "temperature", "C", "door", ""));

        when(roomDeviceCacheService.getRoomDevices(1L)).thenReturn(List.of(device1, device2));

        List<MeasurementType> result = roomSensorMetaService.getMeasurementTypeOptionsInRoom(1L);

        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder(MeasurementType.CO2, MeasurementType.TEMPERATURE);
    }

    @Test
    @DisplayName("룰 엔진에서 지원하지 않는 외부 측정 타입은 옵션에서 제외")
    void getMeasurementTypeOptionsInRoom_UnsupportedTypeIgnored() {
        RoomDeviceInfo device = new RoomDeviceInfo(1L, "eui1", "dev1", Map.of("unknown", "unit"));
        when(roomDeviceCacheService.getRoomDevices(1L)).thenReturn(List.of(device));

        List<MeasurementType> result = roomSensorMetaService.getMeasurementTypeOptionsInRoom(1L);

        assertThat(result).isEmpty();
    }

}
