package com.nhnacademy.ruleengine.domain.templateflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.*;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.NodeConfig;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.action.AlertNodeConfig;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.condition.ThresholdNodeConfig;
import com.nhnacademy.ruleengine.domain.templateflow.dto.*;
import com.nhnacademy.ruleengine.domain.templateflow.service.TemplateFlowService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TemplateFlowController.class)
class TemplateFlowControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    private TemplateFlowService templateFlowService;

    private static final Long ROOM_ID = 100L;
    private static final Long TEMPLATE_ID = 1L;

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("템플릿 플로우생성 - 성공 201")
    void createTemplateFlow_success() throws Exception {
        given(templateFlowService.createTemplateFlow(any(TemplateFlowCreateRequest.class)))
                .willReturn(TemplateFlowCreateResponse.of(TEMPLATE_ID));

        mockMvc.perform(post("/api/rule/templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleCreateRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.templateFlowId").value(TEMPLATE_ID));
    }

    @Test
    @DisplayName("템플릿 플로우 목록 조회 - 성공 200")
    void getTemplateFlowList_success() throws Exception {
        given(templateFlowService.getTemplateList()).willReturn(sampleListResponse());

        mockMvc.perform(get("/api/rule/templates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.templateResponseList").isArray())
                .andExpect(jsonPath("$.templateResponseList[0].templateId").value(TEMPLATE_ID))
                .andExpect(jsonPath("$.templateResponseList[0].templateName").value("템플릿 플로우"))
                .andExpect(jsonPath("$.templateResponseList[0].measurementTypes[0]").value("TEMPERATURE"));

    }


    @Test
    @DisplayName("템플릿 플로우 목록 조회 - 빈 목록이면 빈 배열 반환")
    void getTemplateFlowList_empty() throws Exception {
        given(templateFlowService.getTemplateList())
                .willReturn(new TemplateListResponse(List.of()));

        mockMvc.perform(get("/api/rule/templates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.templateResponseList").isArray())
                .andExpect(jsonPath("$.templateResponseList").isEmpty());
    }


    @Test
    @DisplayName("템플릿 플로우 상세 조회 - 성공 200")
    void getTemplateFlowDetail_success() throws Exception {
        given(templateFlowService.getTemplateDetail(TEMPLATE_ID))
                .willReturn(sampleDetailResponse());

        mockMvc.perform(get("/api/rule/templates/{template-id}", TEMPLATE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.templateId").value(TEMPLATE_ID))
                .andExpect(jsonPath("$.templateName").value("템플릿 플로우"))
                .andExpect(jsonPath("$.description").value("템플릿 플로우 설명"))
                .andExpect(jsonPath("$.nodes").isArray())
                .andExpect(jsonPath("$.nodes[0].nodeId").value(1L))
                .andExpect(jsonPath("$.connections").isArray())
                .andExpect(jsonPath("$.connections[0].branchType").value("TRUE"));
    }

    @Test
    @DisplayName("템플릿 플로우 수정 - 성공 204")
    void updateTemplateFlow_success() throws Exception {
        willDoNothing().given(templateFlowService)
                .updateTemplate(eq(TEMPLATE_ID), any(TemplateFlowUpdateRequest.class));

        mockMvc.perform(put("/api/rule/templates/{template-id}", TEMPLATE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleUpdateRequest())))
                .andExpect(status().isNoContent());
    }
    @Test
    @DisplayName("템플릿 플로우 삭제 - 성공 204")
    void deleteTemplateFlow_success() throws Exception {
        willDoNothing().given(templateFlowService).deleteTemplate(TEMPLATE_ID);

        mockMvc.perform(delete("/api/rule/templates/{template-id}", TEMPLATE_ID))
                .andExpect(status().isNoContent());
    }


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
                "온도 경고",
                AlertType.COMFORT_LIMIT_EXCEEDED,
                300
        );
    }

    private TemplateNodeInfo thresholdNodeInfo() {
        return new TemplateNodeInfo(-1L, "온도 임계값", NodeType.THRESHOLD, thresholdNodeConfig());
    }

    private TemplateNodeInfo alertNodeInfo() {
        return new TemplateNodeInfo(-2L, "알림", NodeType.ALERT, alertNodeConfig());
    }

    private TemplateConnectionInfo sampleConnectionInfo() {
        return new TemplateConnectionInfo(-1L, -2L, null);
    }

    private TemplateFlowCreateRequest sampleCreateRequest() {
        return new TemplateFlowCreateRequest(
                "템플릿 플로우",
                "템플릿 플로우 설명",
                List.of(thresholdNodeInfo(), alertNodeInfo()),
                List.of(sampleConnectionInfo())
        );
    }

    private TemplateFlowUpdateRequest sampleUpdateRequest() {
        return new TemplateFlowUpdateRequest(
                "수정된 템플릿",
                "수정된 설명",
                List.of(thresholdNodeInfo(), alertNodeInfo()),
                List.of(sampleConnectionInfo())
        );
    }

    private TemplateNodeResponse sampleNodeResponse() {
        return TemplateNodeResponse.builder()
                .nodeId(1L)
                .nodeName("온도 임계값")
                .nodeType(NodeType.THRESHOLD)
                .nodeConfig(thresholdNodeConfig())
                .build();
    }

    private TemplateConnectionResponse sampleConnectionResponse() {
        return TemplateConnectionResponse.builder()
                .sourceNodeId(1L)
                .targetNodeId(2L)
                .branchType("TRUE")
                .build();
    }

    private TemplateDetailResponse sampleDetailResponse() {
        return TemplateDetailResponse.builder()
                .templateId(TEMPLATE_ID)
                .templateName("템플릿 플로우")
                .description("템플릿 플로우 설명")
                .nodes(List.of(sampleNodeResponse()))
                .connections(List.of(sampleConnectionResponse()))
                .build();
    }

    private TemplateListResponse sampleListResponse() {
        TemplateResponse templateResponse = TemplateResponse.builder()
                .templateId(TEMPLATE_ID)
                .templateName("템플릿 플로우")
                .description("템플릿 플로우 설명")
                .measurementTypes(List.of(MeasurementType.TEMPERATURE))
                .build();
        return new TemplateListResponse(List.of(templateResponse));
    }
}
