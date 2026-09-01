package com.nhnacademy.ruleengine.domain.nodeconfig.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.ruleengine.common.external.dto.RoomManagementAccessResponse;
import com.nhnacademy.ruleengine.common.external.service.RoomManagementCacheService;
import com.nhnacademy.ruleengine.common.exception.invalid.InvalidNodeException;
import com.nhnacademy.ruleengine.domain.flow.dto.SensorMetaInfo;
import com.nhnacademy.ruleengine.domain.nodeconfig.dto.*;
import com.nhnacademy.ruleengine.domain.nodeconfig.dto.NodeConfigValidationResponse.NodeConfigError;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.MeasurementType;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.NodeType;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.Operator;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.NodeConfig;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.condition.ThresholdNodeConfig;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.logical.OrNodeConfig;
import com.nhnacademy.ruleengine.domain.nodeconfig.service.NodeConfigService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NodeConfigController.class)
class NodeConfigControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private NodeConfigService nodeConfigService;

    @MockitoBean
    private RoomManagementCacheService roomManagementCacheService;

    private static final Long ROOM_ID = 1L;
    private static final Long NODE_ID = 1L;
    private static final Long USER_ID = 1L;

    private static final String NODE_CONFIG_URL =
            "/api/rule/rooms/{room-id}/node-config/{node-id}";
    private static final String VALIDATE_URL =
            "/api/rule/rooms/{room-id}/validate-config";

    @BeforeEach
    void setUp() {
        given(roomManagementCacheService.getManagementAllowed(ROOM_ID, USER_ID))
                .willReturn(new RoomManagementAccessResponse(true));
    }

    private RequestPostProcessor authHeaders() {
        return request -> {
            request.addHeader("X-User-Id", USER_ID.toString());
            request.addHeader("X-User-Role", "ADMIN");
            return request;
        };
    }

    private NodeConfig sampleThresholdConfig() {
        return new ThresholdNodeConfig(
                NodeType.THRESHOLD,
                0, 0,
                MeasurementType.TEMPERATURE,
                "°C",
                Operator.GT,   // 실제 enum 값으로 교체
                25.0
        );
    }

    private SensorMetaInfo sampleSensorMeta() {
        return new SensorMetaInfo(MeasurementType.TEMPERATURE, "온도", "실내 온도","C");
    }



    @Test
    @DisplayName("노드 설정 검증 - 유효한 설정이면 valid:true 반환")
    void validateNodeConfig_valid() throws Exception {
        NodeConfigValidateRequest request = new NodeConfigValidateRequest(
                sampleThresholdConfig()
        );

        given(nodeConfigService.validate(eq(ROOM_ID), any(NodeConfigValidateRequest.class)))
                .willReturn(NodeConfigValidationResponse.success());

        mockMvc.perform(post(VALIDATE_URL, ROOM_ID)
                        .with(authHeaders())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors").isEmpty());
    }

    @Test
    @DisplayName("노드 설정 검증 - 유효하지 않은 설정이면 valid:false + errors 반환")
    void validateNodeConfig_invalid() throws Exception {
        NodeConfigValidateRequest request = new NodeConfigValidateRequest(
                sampleThresholdConfig()
        );

        List<NodeConfigError> errors = List.of(
                NodeConfigError.of("nodeConfig.threshold", "threshold 값은 0보다 커야 합니다"),
                NodeConfigError.of("nodeConfig.measurementType", "measurementType은 필수입니다")
        );

        given(nodeConfigService.validate(eq(ROOM_ID), any(NodeConfigValidateRequest.class)))
                .willReturn(NodeConfigValidationResponse.failure(errors));

        mockMvc.perform(post(VALIDATE_URL, ROOM_ID)
                        .with(authHeaders())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.message").value("노드 설정을 확인해주세요."))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[0].field").value("nodeConfig.threshold"))
                .andExpect(jsonPath("$.errors[0].detailMessage").value("threshold 값은 0보다 커야 합니다"))
                .andExpect(jsonPath("$.errors[1].field").value("nodeConfig.measurementType"))
                .andExpect(jsonPath("$.errors[1].detailMessage").value("measurementType은 필수입니다"));
    }
    @Test
    @DisplayName("노드 설정 검증 - nodeConfig null이면 400")
    void validateNodeConfig_nullNodeConfig_400() throws Exception {
        String body = "{\"nodeConfig\": null}";

        willThrow(new InvalidNodeException())
                .given(nodeConfigService)
                .validate(eq(ROOM_ID), any(NodeConfigValidateRequest.class));

        mockMvc.perform(post(VALIDATE_URL, ROOM_ID)
                        .with(authHeaders())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("노드 설정 검증 - 요청 바디 없으면 400")
    void validateNodeConfig_emptyBody_400() throws Exception {
        mockMvc.perform(post(VALIDATE_URL, ROOM_ID)
                        .with(authHeaders())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("노드 설정 검증 - OR 노드 설정 검증")
    void validateNodeConfig_orNodeConfig_valid() throws Exception {
        NodeConfig orConfig = new OrNodeConfig(
                NodeType.OR,
                0, 0
        );
        NodeConfigValidateRequest request = new NodeConfigValidateRequest(orConfig);

        given(nodeConfigService.validate(eq(ROOM_ID), any(NodeConfigValidateRequest.class)))
                .willReturn(NodeConfigValidationResponse.success());

        mockMvc.perform(post(VALIDATE_URL, ROOM_ID)
                        .with(authHeaders())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true));
    }
}
