package com.nhnacademy.ruleengine.domain.nodeconfig.external;

import com.nhnacademy.ruleengine.common.exception.notfound.SensorTypeNotFoundException;
import com.nhnacademy.ruleengine.domain.nodeconfig.dto.DeviceInfo;
import com.nhnacademy.ruleengine.domain.nodeconfig.dto.SensorStaticMeta;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.SensorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SensorStaticMetaService{
    public final RoomDeviceCacheService roomDeviceCacheService;
    public final MeasurementSensorTypeMapper measurementSensorTypeMapper;

    public List<SensorStaticMeta> getSensorStaticMetaList(Long roomId){
        List<ExternalRoomDeviceInfo> devices = roomDeviceCacheService.getRoomDevices(roomId);

        Map<SensorType, String> unitBySensorType = new EnumMap<>(SensorType.class);//센서 타입별 유닛 매핑
        Map<SensorType, List<DeviceInfo>> deviceOptionsBySensorType = new EnumMap<>(SensorType.class);//센서타입별 기기 정보 매핑

        for (ExternalRoomDeviceInfo device : devices) {
            Map<String, String> measurement = device.measurement();

            if (measurement == null || measurement.isEmpty()) {
                continue;
            }

            for (Map.Entry<String, String> entry : measurement.entrySet()) {
                String measurementKey = entry.getKey();
                String unit = entry.getValue();

                measurementSensorTypeMapper.toSensorType(measurementKey)
                        .ifPresent(sensorType -> {
                            unitBySensorType.putIfAbsent(sensorType, unit);

                            deviceOptionsBySensorType
                                    .computeIfAbsent(sensorType, key -> new ArrayList<>())
                                    .add(new DeviceInfo(device.devEui(), device.deviceName()));
                        });
            }
        }

        return deviceOptionsBySensorType.entrySet().stream()
                .map(entry -> new SensorStaticMeta(
                        entry.getKey(),
                        unitBySensorType.get(entry.getKey()),
                        dedupeDeviceOptions(entry.getValue())
                ))
                .sorted(Comparator.comparing(meta -> meta.sensorType().name()))
                .toList();
    }

    private List<DeviceInfo> dedupeDeviceOptions(List<DeviceInfo> deviceOptions) {
        return deviceOptions.stream()
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(
                                DeviceInfo::devEui,
                                option -> option,
                                (existing, replacement) -> existing,
                                LinkedHashMap::new
                        ),
                        map -> new ArrayList<>(map.values())
                ));
    }
}