package com.nhnacademy.ruleengine.common.external.service;

import com.nhnacademy.ruleengine.domain.nodeconfig.dto.DeviceInfo;
import com.nhnacademy.ruleengine.domain.nodeconfig.dto.ExternalRoomDeviceInfo;
import com.nhnacademy.ruleengine.domain.nodeconfig.dto.SensorStaticMeta;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.MeasurementType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SensorStaticMetaService {
    public final RoomDeviceCacheService roomDeviceCacheService;


    public List<SensorStaticMeta> getSensorStaticMetaList(Long roomId){
        List<ExternalRoomDeviceInfo> roomDeviceInfoList = roomDeviceCacheService.getRoomDevices(roomId);

        if(roomDeviceInfoList.isEmpty()){
            return List.of();
        }

        List<SensorStaticMeta> sensorStaticMetaList = new ArrayList<>();

        List<MeasurementType> measurementTypeOptionsInRoom = getMeasurementTypeOptionsInRoom(roomId);
        Map<MeasurementType, String> unitByMeasurementType = getUnitByMeasurementType(roomDeviceInfoList);

        for(MeasurementType measurementType : measurementTypeOptionsInRoom){
            sensorStaticMetaList.add(SensorStaticMeta.of(measurementType, unitByMeasurementType.get(measurementType)));
        }

        return sensorStaticMetaList;
    }


    public List<DeviceInfo> getDeviceOptionsInRoom(Long roomId) {
        List<ExternalRoomDeviceInfo> roomDeviceInfoList = roomDeviceCacheService.getRoomDevices(roomId);

        if (roomDeviceInfoList.isEmpty()) {
            return List.of();
        }


        return roomDeviceInfoList.stream()
                .map(room -> DeviceInfo.of(room.devEui(), room.deviceName()))
                .distinct()
                .toList();
    }

    public List<MeasurementType> getMeasurementTypeOptionsInRoom(Long roomId) {
        List<ExternalRoomDeviceInfo> roomDeviceInfoList = roomDeviceCacheService.getRoomDevices(roomId);
        if (roomDeviceInfoList.isEmpty()) {
            return List.of();
        }

        return roomDeviceInfoList.stream()
                .flatMap(room -> room.measurement().keySet().stream())
                .map(MeasurementType::fromString)
                .distinct()
                .toList();
    }

    private Map<MeasurementType, String> getUnitByMeasurementType(List<ExternalRoomDeviceInfo> roomDeviceInfoList){
        return roomDeviceInfoList.stream()
                .flatMap(room -> room.measurement().entrySet().stream())
                .collect(Collectors.toMap(
                        entry -> MeasurementType.fromString(entry.getKey()),
                        Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue,
                        () -> new EnumMap<>(MeasurementType.class)
                ));
    }

    private Map<MeasurementType, List<DeviceInfo>> getDeviceInfoByMeasurementType(List<ExternalRoomDeviceInfo> roomDeviceInfoList){
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