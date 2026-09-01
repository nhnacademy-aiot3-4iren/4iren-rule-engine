package com.nhnacademy.ruleengine.common.external.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.ruleengine.common.external.client.RoomSensorClient;
import com.nhnacademy.ruleengine.common.external.dto.MetricCatalogInfo;
import com.nhnacademy.ruleengine.domain.flow.dto.SensorMetaInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MetricCatalogCacheServiceTest {

    @Mock private RoomSensorClient roomSensorClient;
    @Spy private ObjectMapper objectMapper;


    @InjectMocks
    private MetricCatalogCacheService metricCatalogCacheService;

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("Cache Miss - 외부 API 조회 성공 시, 받아온 센서 카탈로그 리스트를 반환")
    void getMetricCatalog(){
        MetricCatalogInfo metaInfo1 = new MetricCatalogInfo("co2", "이산화탄소 농도", "GAUGE","ACTIVE", "실내 공기 중 이산화탄소 농도","[ppm]","백만분율","ppm");
        MetricCatalogInfo metaInfo2 = new MetricCatalogInfo("humidity", "상대습도", "GAUGE","ACTIVE", "실내 공기의 상대습도","%","퍼센트","%");
        MetricCatalogInfo metaInfo3 = new MetricCatalogInfo("temperature", "온도", "GAUGE","ACTIVE", "실내 공기의 섭씨 온도","Cel","섭씨","°C");

        List<MetricCatalogInfo> apiList = List.of(metaInfo1, metaInfo2, metaInfo3);
        when(metricCatalogCacheService.getMetricCatalog()).thenReturn(apiList);

        List<MetricCatalogInfo> result = metricCatalogCacheService.getMetricCatalog();

        assertThat(result).isEqualTo(apiList);
        verify(roomSensorClient, times(1)).getMetricCatalog();
    }

}