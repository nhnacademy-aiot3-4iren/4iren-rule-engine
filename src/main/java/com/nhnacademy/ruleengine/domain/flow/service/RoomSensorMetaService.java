package com.nhnacademy.ruleengine.domain.flow.service;

import com.nhnacademy.ruleengine.common.external.dto.MetricCatalogInfo;
import com.nhnacademy.ruleengine.common.external.dto.RoomDeviceInfo;
import com.nhnacademy.ruleengine.common.external.service.MetricCatalogCacheService;
import com.nhnacademy.ruleengine.common.external.service.RoomDeviceCacheService;
import com.nhnacademy.ruleengine.domain.flow.dto.SensorMetaInfo;
import com.nhnacademy.ruleengine.domain.nodeconfig.dto.DeviceInfo;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.MeasurementType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomSensorMetaService {
    private final RoomDeviceCacheService roomDeviceCacheService;
    private final MetricCatalogCacheService metricCatalogCacheService;

    //강의실에서 측정 가능한 데이터 메타 정보
    public List<SensorMetaInfo> getSensorMetaList(Long roomId){
        List<RoomDeviceInfo> roomDeviceInfoList = roomDeviceCacheService.getRoomDevices(roomId);
        List<MetricCatalogInfo> metricCatalogInfoList = metricCatalogCacheService.getMetricCatalog();


        if(roomDeviceInfoList.isEmpty()){
            return List.of();
        }

        Map<String, MetricCatalogInfo> catalogMap = metricCatalogInfoList.stream()
                .collect(Collectors.toMap(
                        catalog -> catalog.metricCode().toUpperCase(), // "co2" → "CO2"
                        Function.identity(),
                        (oldValue, newValue) -> oldValue
                ));

        List<SensorMetaInfo> sensorMetaInfoList = roomDeviceInfoList.stream()
                .flatMap(room -> room.measurement().keySet().stream())
                .distinct()
                .map(key->{
                    Optional<MeasurementType> measurementType = MeasurementType.findByExternalCode(key);
                    if (measurementType.isEmpty()) {
                        log.warn("룰 엔진에서 지원하지 않는 측정 타입 제외 roomId={}, measurementType={}", roomId, key);
                        return null;
                    }

                    MetricCatalogInfo catalogInfo = catalogMap.get(key.toUpperCase());

                    if(catalogInfo == null){
                        log.warn("측정 항목 카탈로그에 존재하지 않는 측정 타입 제외 roomId={}, measurementType={}", roomId, key);
                        return null;
                    }

                    return SensorMetaInfo.of(measurementType.get(), catalogInfo);
                })
                .filter(Objects::nonNull)
                .toList();

        log.info("강의실 센서 메타 조회 완료 roomId={}, sensorMetaCount={}", roomId, sensorMetaInfoList.size());
        return sensorMetaInfoList;
    }


    //roomId rlwns 측정 가능한 List<MeasurementType>
    public List<MeasurementType> getMeasurementTypeOptionsInRoom(Long roomId) {
        List<RoomDeviceInfo> roomDeviceInfoList = roomDeviceCacheService.getRoomDevices(roomId);
        if (roomDeviceInfoList.isEmpty()) {
            log.info("강의실 측정 타입 옵션 조회 완료 roomId={}, measurementTypeCount=0", roomId);
            return List.of();
        }

        List<MeasurementType> measurementTypes = roomDeviceInfoList.stream()
                .flatMap(room -> room.measurement().keySet().stream())
                .flatMap(measurementType -> MeasurementType.findByExternalCode(measurementType).stream())
                .distinct()
                .toList();
        log.info("강의실 측정 타입 옵션 조회 완료 roomId={}, measurementTypeCount={}", roomId, measurementTypes.size());
        return measurementTypes;
    }



}
