package com.nhnacademy.ruleengine.domain.nodeconfig.service.impl;

import com.nhnacademy.ruleengine.common.exception.notfound.MeasurementTypeNotFoundException;
import com.nhnacademy.ruleengine.domain.nodeconfig.dto.DeviceInfo;
import com.nhnacademy.ruleengine.domain.nodeconfig.dto.ExternalRoomDeviceInfo;
import com.nhnacademy.ruleengine.domain.nodeconfig.dto.SensorStaticMeta;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.MeasurementType;
import com.nhnacademy.ruleengine.domain.nodeconfig.external.MeasurementMeasurementTypeMapper;
import com.nhnacademy.ruleengine.domain.nodeconfig.service.SensorStaticMetaService;
import com.nhnacademy.ruleengine.common.redis.cache.RoomDeviceCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SensorStaticMetaServiceImpl implements SensorStaticMetaService {
    public final RoomDeviceCacheService roomDeviceCacheService;
    public final MeasurementMeasurementTypeMapper measurementmeasurementTypeMapper;

    @Override
    public List<SensorStaticMeta> getSensorStaticMetaList(Long roomId){
        List<ExternalRoomDeviceInfo> roomDeviceInfoList = roomDeviceCacheService.getRoomDevices(roomId);

        if(roomDeviceInfoList.isEmpty()){
            return List.of();
        }

        List<SensorStaticMeta> sensorStaticMetaList = new ArrayList<>();

        Map<MeasurementType, List<DeviceInfo>> deviceInfoBySensorType = getDeviceInfoByMeasurementType(roomDeviceInfoList);
        Map<MeasurementType, String> unitBySensorType = getUnitByMeasuremrntType(roomDeviceInfoList);

        for(MeasurementType measurementType : deviceInfoBySensorType.keySet()){
            sensorStaticMetaList.add(SensorStaticMeta.of(measurementType, unitBySensorType.get(measurementType), deviceInfoBySensorType.get(measurementType)));
        }

        return sensorStaticMetaList;
    }

    @Override
    public List<DeviceInfo> getDeviceOptionsInRoom(Long roomId) {
        List<ExternalRoomDeviceInfo> roomDeviceInfoList = roomDeviceCacheService.getRoomDevices(roomId);

        if (roomDeviceInfoList.isEmpty()) {
            return List.of();
        }


        return roomDeviceInfoList.stream()
                .map(room -> DeviceInfo.of(room.devEui(), room.deviceName()))
                .distinct()
                .collect(Collectors.toList());
    }
    @Override
    public List<MeasurementType> getMeasurementTypeOptionsInRoom(Long roomId) {
        List<ExternalRoomDeviceInfo> roomDeviceInfoList = roomDeviceCacheService.getRoomDevices(roomId);
        if (roomDeviceInfoList.isEmpty()) {
            return List.of();
        }

        return roomDeviceInfoList.stream()
                .flatMap(room -> room.measurement().keySet().stream())
                .map(measurement -> measurementmeasurementTypeMapper.toMeasurementType(measurement).orElseThrow(()-> new MeasurementTypeNotFoundException()))
                .distinct()
                .collect(Collectors.toList());
    }

    private Map<MeasurementType, String> getUnitByMeasuremrntType(List<ExternalRoomDeviceInfo> roomDeviceInfoList){
        return roomDeviceInfoList.stream()
                .flatMap(room -> room.measurement().entrySet().stream())
                .collect(Collectors.toMap(
                        entry -> measurementmeasurementTypeMapper.toMeasurementType(entry.getKey()).orElseThrow(()->new MeasurementTypeNotFoundException()),
                        Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue,
                        () -> new EnumMap<>(MeasurementType.class)
                ));
    }

    private Map<MeasurementType, List<DeviceInfo>> getDeviceInfoByMeasurementType(List<ExternalRoomDeviceInfo> roomDeviceInfoList){
        return roomDeviceInfoList.stream()
                .flatMap(room -> room.measurement().keySet().stream()//Stream<MeasurementSensorType>
                        .map(sensorType -> Map.entry(//Stream<Map.Entry<MeasurementType, DeviceInfo>>
                                measurementmeasurementTypeMapper.toMeasurementType(sensorType).orElseThrow(()->new MeasurementTypeNotFoundException()),
                                DeviceInfo.of(room.devEui(), room.deviceName())
                        ))
                ).collect(Collectors.groupingBy(//Map<MeasurementType, List<DeviceInfo>>
                        Map.Entry::getKey,
                        ()-> new EnumMap<>(MeasurementType.class),
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList())
                ));
    }
}