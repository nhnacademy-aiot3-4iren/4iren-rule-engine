package com.nhnacademy.ruleengine.domain.nodeconfig.service.impl;

import com.nhnacademy.ruleengine.common.exception.notfound.SensorTypeNotFoundException;
import com.nhnacademy.ruleengine.domain.nodeconfig.dto.DeviceInfo;
import com.nhnacademy.ruleengine.domain.nodeconfig.dto.ExternalRoomDeviceInfo;
import com.nhnacademy.ruleengine.domain.nodeconfig.dto.SensorStaticMeta;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.SensorType;
import com.nhnacademy.ruleengine.domain.nodeconfig.external.MeasurementSensorTypeMapper;
import com.nhnacademy.ruleengine.domain.nodeconfig.service.SensorStaticMetaService;
import com.nhnacademy.ruleengine.domain.nodeconfig.service.cache.RoomDeviceCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SensorStaticMetaServiceImpl implements SensorStaticMetaService {
    public final RoomDeviceCacheService roomDeviceCacheService;
    public final MeasurementSensorTypeMapper measurementSensorTypeMapper;

    @Override
    public List<SensorStaticMeta> getSensorStaticMetaList(Long roomId){
        List<ExternalRoomDeviceInfo> roomDeviceInfoList = roomDeviceCacheService.getRoomDevices(roomId);

        if(roomDeviceInfoList.isEmpty()){
            return List.of();
        }

        List<SensorStaticMeta> sensorStaticMetaList = new ArrayList<>();

        Map<SensorType, List<DeviceInfo>> deviceInfoBySensorType = getDeviceInfoBySensorType(roomDeviceInfoList);
        Map<SensorType, String> unitBySensorType = getUnitBySensorType(roomDeviceInfoList);

        for(SensorType sensorType: deviceInfoBySensorType.keySet()){
            sensorStaticMetaList.add(SensorStaticMeta.of(sensorType, unitBySensorType.get(sensorType), deviceInfoBySensorType.get(sensorType)));
        }

        return sensorStaticMetaList;
    }

    @Override
    public List<DeviceInfo> getDeviceOptionsInRoom(Long roomId) {
        List<ExternalRoomDeviceInfo> roomDeviceInfoList = roomDeviceCacheService.getRoomDevices(roomId);


        return List.of();
    }
    @Override
    public List<SensorType> getSensorTypeOptionsInRoom(Long roomId) {
        List<ExternalRoomDeviceInfo> devices = roomDeviceCacheService.getRoomDevices(roomId);
        return List.of();
    }

    private Map<SensorType, String> getUnitBySensorType(List<ExternalRoomDeviceInfo> roomDeviceInfoList){
        return roomDeviceInfoList.stream()
                .flatMap(room -> room.measurement().entrySet().stream())
                .collect(Collectors.toMap(
                        entry -> measurementSensorTypeMapper.toSensorType(entry.getKey()).orElseThrow(()->new SensorTypeNotFoundException(entry.getKey())),
                        Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue,
                        () -> new EnumMap<>(SensorType.class)
                ));


        //        return roomDeviceInfo.measurement().entrySet().stream()
//                .collect(Collectors.toMap(
//                        e->measurementSensorTypeMapper.toSensorType(e.getKey()).orElseThrow(()->new SensorTypeNotFoundException(e.getKey())),
//                        e->e.getValue(),
//                        (oldValue, newValue) -> oldValue,
//                        ()-> new EnumMap<>(SensorType.class)
//                ));
    }

    private Map<SensorType, List<DeviceInfo>> getDeviceInfoBySensorType(List<ExternalRoomDeviceInfo> roomDeviceInfoList){
        return roomDeviceInfoList.stream()
                .flatMap(room -> room.measurement().keySet().stream()//Stream<MeasurementSensorType>
                        .map(sensorType -> Map.entry(//Stream<Map.Entry<SensorType, DeviceInfo>>
                                measurementSensorTypeMapper.toSensorType(sensorType).orElseThrow(()->new SensorTypeNotFoundException(sensorType)),
                                DeviceInfo.from(room.devEui(), room.deviceName())
                        ))
                ).collect(Collectors.groupingBy(//Map<SensorType, List<DeviceInfo>>
                        Map.Entry::getKey,
                        ()-> new EnumMap<>(SensorType.class),
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList())
                ));
    }


//    public List<SensorStaticMeta> getSensorStaticMetaList(Long roomId){
//        List<ExternalRoomDeviceInfo> devices = roomDeviceCacheService.getRoomDevices(roomId);
//
//
//        Map<SensorType, String> unitBySensorType = new EnumMap<>(SensorType.class);//센서 타입별 유닛 매핑
//        Map<SensorType, List<DeviceInfo>> deviceInfoBySensorType = new EnumMap<>(SensorType.class);//센서타입별 기기 정보 매핑
//
//        for (ExternalRoomDeviceInfo device : devices) {
//            Map<String, String> measurement = device.measurement();
//
//            if (measurement == null || measurement.isEmpty()) {
//                continue;
//            }
//
//            for (Map.Entry<String, String> entry : measurement.entrySet()) {
//                String measurementKey = entry.getKey();
//                String unit = entry.getValue();
//
//                measurementSensorTypeMapper.toSensorType(measurementKey)
//                        .ifPresent(sensorType -> {
//                            unitBySensorType.putIfAbsent(sensorType, unit);
//
//                            deviceInfoBySensorType
//                                    .computeIfAbsent(sensorType, key -> new ArrayList<>())
//                                    .add(new DeviceInfo(device.devEui(), device.deviceName()));
//                        });
//            }
//        }
//
//        return deviceInfoBySensorType.entrySet().stream()
//                .map(entry -> new SensorStaticMeta(
//                        entry.getKey(),
//                        unitBySensorType.get(entry.getKey()),
//                        dedupeDeviceOptions(entry.getValue())
//                ))
//                .sorted(Comparator.comparing(meta -> meta.sensorType().name()))
//                .toList();
//    }
//
//    private List<DeviceInfo> dedupeDeviceOptions(List<DeviceInfo> deviceOptions) {
//        return deviceOptions.stream()
//                .collect(Collectors.collectingAndThen(
//                        Collectors.toMap(
//                                DeviceInfo::devEui,
//                                option -> option,
//                                (existing, replacement) -> existing,
//                                LinkedHashMap::new
//                        ),
//                        map -> new ArrayList<>(map.values())
//                ));
//    }
}