package com.nhnacademy.ruleengine.domain.nodeconfig.service;

import com.nhnacademy.ruleengine.domain.nodeconfig.dto.DeviceInfo;
import com.nhnacademy.ruleengine.domain.nodeconfig.dto.SensorStaticMeta;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.SensorType;

import java.util.List;

public interface SensorStaticMetaService {
    List<SensorStaticMeta> getSensorStaticMetaList(Long roomId);
    List<DeviceInfo> getDeviceOptionsInRoom(Long roomId);
    List<SensorType> getSensorTypeOptionsInRoom(Long roomId);
}
