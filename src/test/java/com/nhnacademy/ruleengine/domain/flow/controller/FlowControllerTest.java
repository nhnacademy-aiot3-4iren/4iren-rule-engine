package com.nhnacademy.ruleengine.domain.flow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.ruleengine.domain.flow.dto.*;
import com.nhnacademy.ruleengine.domain.flow.service.FlowService;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.*;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.NodeConfig;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.action.AlertNodeConfig;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.condition.ThresholdNodeConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FlowController.class)
class FlowControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private FlowService flowService;

    private static final Long ROOM_ID = 1L;
    private static final Long FLOW_ID = 1L;
    private static final Long TEMPLATE_ID = 1L;


    private NodeConfig thresholdNodeConfig() {
        return new ThresholdNodeConfig(
                NodeType.THRESHOLD,
                0, 0,
                MeasurementType.TEMPERATURE,
                "°C",
                Operator.GT,
                25.0
        );
    }

    private NodeConfig alertNodeConfig() {
        return new AlertNodeConfig(
                NodeType.ALERT,
                100, 0,
                AlertChannel.TELEGRAM,
                "온도 경고",
                AlertType.COMFORT_LIMIT_EXCEEDED,
                300
        );
    }

    private NodeInfo thresholdNodeInfo() {
        return new NodeInfo(
                -1L,
                "온도 임계값",
                NodeType.THRESHOLD,
                thresholdNodeConfig(),
                null
        );
    }

    private NodeInfo alertNodeInfo() {
        return new NodeInfo(
                -2L,
                "알림",
                NodeType.ALERT,
                alertNodeConfig(),
                null
        );
    }

    private ConnectionInfo sampleConnectionInfo() {
        return new ConnectionInfo(-1L, -2L, null); // branchType null → TRUE
    }

    private FlowCreateRequest sampleCreateRequest() {
        return new FlowCreateRequest(
                "테스트 플로우",
                "설명",
                List.of(thresholdNodeInfo(), alertNodeInfo()),
                List.of(sampleConnectionInfo())
        );
    }

    private FlowUpdateRequest sampleUpdateRequest() {
        return new FlowUpdateRequest(
                "수정된 플로우",
                "수정된 설명",
                true,
                List.of(thresholdNodeInfo(), alertNodeInfo()),
                List.of(sampleConnectionInfo())
        );
    }

    private FlowDetailResponse sampleDetailResponse() {
        return FlowDetailResponse.builder()
                .flowId(FLOW_ID)
                .roomId(ROOM_ID)
                .flowName("테스트 플로우")
                .description("설명")
                .isActive(true)
                .nodes(List.of())
                .connections(List.of())
                .createdAt(LocalDateTime.of(2024, 1, 1, 0, 0))
                .updatedAt(LocalDateTime.of(2024, 1, 1, 0, 0))
                .build();
    }


    @Test
    @DisplayName("플로우 생성 - 성공 201")
    void createFlow_success() throws Exception {
        given(flowService.createFlow(eq(ROOM_ID), any(FlowCreateRequest.class)))
                .willReturn(FlowCreateResponse.of(FLOW_ID));

        mockMvc.perform(post("/api/rule/rooms/{room-id}/flows", ROOM_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleCreateRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.flowId").value(FLOW_ID));
    }

    @Test
    @DisplayName("플로우 생성 - flowName 공백이면 400")
    void createFlow_blankFlowName_400() throws Exception {
        FlowCreateRequest invalid = new FlowCreateRequest(
                "",
                "설명",
                List.of(thresholdNodeInfo()),
                List.of(sampleConnectionInfo())
        );

        mockMvc.perform(post("/api/rule/rooms/{room-id}/flows", ROOM_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("플로우 생성 - flowName 50자 초과이면 400")
    void createFlow_flowNameTooLong_400() throws Exception {
        FlowCreateRequest invalid = new FlowCreateRequest(
                "a".repeat(51),
                "설명",
                List.of(thresholdNodeInfo()),
                List.of(sampleConnectionInfo())
        );

        mockMvc.perform(post("/api/rule/rooms/{room-id}/flows", ROOM_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("플로우 생성 - nodes 비어있으면 400")
    void createFlow_emptyNodes_400() throws Exception {
        FlowCreateRequest invalid = new FlowCreateRequest(
                "플로우",
                "설명",
                List.of(),
                List.of(sampleConnectionInfo())
        );

        mockMvc.perform(post("/api/rule/rooms/{room-id}/flows", ROOM_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("플로우 생성 - connections null이면 400")
    void createFlow_nullConnections_400() throws Exception {
        FlowCreateRequest invalid = new FlowCreateRequest(
                "플로우",
                "설명",
                List.of(thresholdNodeInfo()),
                null
        );

        mockMvc.perform(post("/api/rule/rooms/{room-id}/flows", ROOM_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }


    @Test
    @DisplayName("플로우 목록 조회 - 성공 200")
    void getFlowList_success() throws Exception {
        FlowListResponse response = FlowListResponse.of(List.of());
        given(flowService.getFlowList(ROOM_ID)).willReturn(response);

        mockMvc.perform(get("/api/rule/rooms/{room-id}/flows", ROOM_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flowResponseList").isArray());
    }


    @Test
    @DisplayName("플로우 상세 조회 - 성공 200")
    void getFlowDetail_success() throws Exception {
        given(flowService.getFlowDetail(ROOM_ID, FLOW_ID))
                .willReturn(sampleDetailResponse());

        mockMvc.perform(get("/api/rule/rooms/{room-id}/flows/{flow-id}", ROOM_ID, FLOW_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flowId").value(FLOW_ID))
                .andExpect(jsonPath("$.roomId").value(ROOM_ID))
                .andExpect(jsonPath("$.flowName").value("테스트 플로우"))
                .andExpect(jsonPath("$.isActive").value(true));
    }

    @Test
    @DisplayName("강의실별 템플릿 플로우 목록 조회 - 성공 200")
    void getFlowTemplateList_success() throws Exception {
        RoomTemplateListResponse response = new RoomTemplateListResponse(List.of());
        given(flowService.getFlowTemplateList(ROOM_ID)).willReturn(response);

        mockMvc.perform(get("/api/rule/rooms/{room-id}/flows/templates", ROOM_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomTemplateResponseList").isArray());
    }

    @Test
    @DisplayName("템플릿 플로우 상세 조회 - 성공 200")
    void getTemplateFlowDetail_success() throws Exception {
        RoomTemplateDetailResponse response = RoomTemplateDetailResponse.builder()
                .templateName("온도 알림 템플릿")
                .description("온도 임계값 초과 시 알림")
                .nodes(List.of())
                .connections(List.of())
                .build();

        given(flowService.getTemplateFlowDetail(TEMPLATE_ID)).willReturn(response);

        mockMvc.perform(get("/api/rule/rooms/{room-id}/flows/templates/{template-id}",
                        ROOM_ID, TEMPLATE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.templateName").value("온도 알림 템플릿"))
                .andExpect(jsonPath("$.nodes").isArray())
                .andExpect(jsonPath("$.connections").isArray());
    }

    @Test
    @DisplayName("플로우 수정 - 성공 204")
    void updateFlow_success() throws Exception {
        willDoNothing().given(flowService)
                .updateFlow(eq(ROOM_ID), eq(FLOW_ID), any(FlowUpdateRequest.class));

        mockMvc.perform(put("/api/rule/rooms/{room-id}/flows/{flow-id}", ROOM_ID, FLOW_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleUpdateRequest())))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("플로우 수정 - flowName 공백이면 400")
    void updateFlow_blankFlowName_400() throws Exception {
        FlowUpdateRequest invalid = new FlowUpdateRequest(
                "",
                "설명",
                true,
                List.of(thresholdNodeInfo()),
                List.of(sampleConnectionInfo())
        );

        mockMvc.perform(put("/api/rule/rooms/{room-id}/flows/{flow-id}", ROOM_ID, FLOW_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("플로우 수정 - isActive null이면 400")
    void updateFlow_nullIsActive_400() throws Exception {
        FlowUpdateRequest invalid = new FlowUpdateRequest(
                "플로우",
                "설명",
                null,
                List.of(thresholdNodeInfo()),
                List.of(sampleConnectionInfo())
        );

        mockMvc.perform(put("/api/rule/rooms/{room-id}/flows/{flow-id}", ROOM_ID, FLOW_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("플로우 수정 - nodes 비어있으면 400")
    void updateFlow_emptyNodes_400() throws Exception {
        FlowUpdateRequest invalid = new FlowUpdateRequest(
                "플로우",
                "설명",
                true,
                List.of(),
                List.of(sampleConnectionInfo())
        );

        mockMvc.perform(put("/api/rule/rooms/{room-id}/flows/{flow-id}", ROOM_ID, FLOW_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("플로우 삭제 - 성공 204")
    void deleteFlow_success() throws Exception {
        willDoNothing().given(flowService).deleteFlow(ROOM_ID, FLOW_ID);

        mockMvc.perform(delete("/api/rule/rooms/{room-id}/flows/{flow-id}", ROOM_ID, FLOW_ID))
                .andExpect(status().isNoContent());
    }
}