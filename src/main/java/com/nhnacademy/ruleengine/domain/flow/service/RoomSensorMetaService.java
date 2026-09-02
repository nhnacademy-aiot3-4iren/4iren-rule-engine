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
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomSensorMetaService {
    private final RoomDeviceCacheService roomDeviceCacheService;
    private final MetricCatalogCacheService metricCatalogCacheService;

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
                    MeasurementType measurementType = MeasurementType.fromString(key);
                    MetricCatalogInfo catalogInfo = catalogMap.get(key.toUpperCase());

                    if(catalogInfo == null){
                        log.error("MetricCatalog에 존재하지 않는 측정 타입, key: ", key);
                        return null;
                    }

                    return SensorMetaInfo.of(measurementType, catalogInfo);
                })
                .filter(Objects::nonNull)
                .toList();

        return sensorMetaInfoList;
    }


    public List<DeviceInfo> getDeviceOptionsInRoom(Long roomId) {
        List<RoomDeviceInfo> roomDeviceInfoList = roomDeviceCacheService.getRoomDevices(roomId);

        if (roomDeviceInfoList.isEmpty()) {
            return List.of();
        }


        return roomDeviceInfoList.stream()
                .map(room -> DeviceInfo.of(room.devEui(), room.deviceName()))
                .distinct()
                .toList();
    }

    public List<MeasurementType> getMeasurementTypeOptionsInRoom(Long roomId) {
        List<RoomDeviceInfo> roomDeviceInfoList = roomDeviceCacheService.getRoomDevices(roomId);
        if (roomDeviceInfoList.isEmpty()) {
            return List.of();
        }

        return roomDeviceInfoList.stream()
                .flatMap(room -> room.measurement().keySet().stream())
                .map(MeasurementType::fromString)
                .distinct()
                .toList();
    }


    private Map<MeasurementType, List<DeviceInfo>> getDeviceInfoByMeasurementType(List<RoomDeviceInfo> roomDeviceInfoList){
        return roomDeviceInfoList.stream()
                .flatMap(room -> room.measurement().keySet().stream()//Stream<MeasurementType>
                        .map(measurementType -> Map.entry(//Stream<Map.Entry<MeasurementType, DeviceInfo>>
                                MeasurementType.fromString(measurementType),
                                DeviceInfo.of(room.devEui(), room.deviceName())
                        ))
                ).collect(Collectors.groupingBy(//Map<MeasurementType, List<DeviceInfo>>
                        Map.Entry::getKey,
                        ()-> new EnumMap<>(MeasurementType.class),
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList())
                ));
    }
}