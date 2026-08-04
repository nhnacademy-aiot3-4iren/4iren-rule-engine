package com.nhnacademy.ruleengine.domain.nodeconfig.service;

import com.nhnacademy.ruleengine.domain.nodeconfig.dto.DeviceInfo;
import com.nhnacademy.ruleengine.domain.nodeconfig.dto.SensorStaticMeta;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.MeasurementType;

import java.util.List;

public interface SensorStaticMetaService {
    List<SensorStaticMeta> getSensorStaticMetaList(Long roomId);
    List<DeviceInfo> getDeviceOptionsInRoom(Long roomId);
    List<MeasurementType> getSensorTypeOptionsInRoom(Long roomId);
}
