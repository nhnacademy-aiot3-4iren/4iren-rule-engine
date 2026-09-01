package com.nhnacademy.ruleengine.domain.flowschedule.controller;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.ruleengine.common.external.dto.RoomManagementAccessResponse;
import com.nhnacademy.ruleengine.common.external.service.RoomManagementCacheService;
import com.nhnacademy.ruleengine.domain.flowschedule.dto.*;
import com.nhnacademy.ruleengine.domain.flowschedule.service.FlowScheduleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FlowScheduleController.class)
class FlowScheduleControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private FlowScheduleService flowScheduleService;

    @MockitoBean
    private RoomManagementCacheService roomManagementCacheService;

    private static final Long ROOM_ID = 1L;
    private static final Long FLOW_ID = 1L;
    private static final Long SCHEDULE_ID = 1L;
    private static final Long USER_ID = 1L;

    private static final String BASE_URL =
            "/api/rule/rooms/{room-id}/flows/{flow-id}/schedules";
    private static final String DETAIL_URL =
            "/api/rule/rooms/{room-id}/flows/{flow-id}/schedules/{schedule-id}";

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

    @Test
    @DisplayName("스케줄 생성 - 성공 201")
    void createFlowSchedule_success() throws Exception {
        given(flowScheduleService.createFlowSchedule(
                eq(ROOM_ID), eq(FLOW_ID), any(FlowScheduleCreateRequest.class)))
                .willReturn(FlowScheduleCreateResponse.of(SCHEDULE_ID));

        mockMvc.perform(post(BASE_URL, ROOM_ID, FLOW_ID)
                        .with(authHeaders())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleCreateRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.scheduleId").value(SCHEDULE_ID));
    }

    @Test
    @DisplayName("스케줄 생성 - dayOfWeek null이면 400")
    void createFlowSchedule_nullDayOfWeek_400() throws Exception {
        FlowScheduleCreateRequest invalid = new FlowScheduleCreateRequest(
                null,
                LocalTime.of(9, 0, 0),
                LocalTime.of(18, 0, 0)
        );

        mockMvc.perform(post(BASE_URL, ROOM_ID, FLOW_ID)
                        .with(authHeaders())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("스케줄 생성 - startTime null이면 400")
    void createFlowSchedule_nullStartTime_400() throws Exception {
        FlowScheduleCreateRequest invalid = new FlowScheduleCreateRequest(
                DayOfWeek.MONDAY,
                null,
                LocalTime.of(18, 0, 0)
        );

        mockMvc.perform(post(BASE_URL, ROOM_ID, FLOW_ID)
                        .with(authHeaders())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("스케줄 생성 - endTime null이면 400")
    void createFlowSchedule_nullEndTime_400() throws Exception {
        FlowScheduleCreateRequest invalid = new FlowScheduleCreateRequest(
                DayOfWeek.MONDAY,
                LocalTime.of(9, 0, 0),
                null
        );

        mockMvc.perform(post(BASE_URL, ROOM_ID, FLOW_ID)
                        .with(authHeaders())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }


    @Test
    @DisplayName("스케줄 목록 조회 - 성공 200")
    void getFlowScheduleList_success() throws Exception {
        given(flowScheduleService.getFlowScheduleList(FLOW_ID, ROOM_ID))
                .willReturn(sampleListResponse());

        mockMvc.perform(get(BASE_URL, ROOM_ID, FLOW_ID)
                        .with(authHeaders()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flowId").value(FLOW_ID))
                .andExpect(jsonPath("$.schedules").isArray())
                .andExpect(jsonPath("$.schedules[0].scheduleId").value(SCHEDULE_ID))
                .andExpect(jsonPath("$.schedules[0].dayOfWeek").value("MONDAY"))
                .andExpect(jsonPath("$.schedules[0].startTime").value("09:00:00"))
                .andExpect(jsonPath("$.schedules[0].endTime").value("18:00:00"));
    }

    @Test
    @DisplayName("스케줄 목록 조회 - 스케줄 없으면 빈 배열 반환")
    void getFlowScheduleList_empty() throws Exception {
        given(flowScheduleService.getFlowScheduleList(FLOW_ID, ROOM_ID))
                .willReturn(FlowScheduleListResponse.builder()
                        .flowId(FLOW_ID)
                        .schedules(List.of())
                        .build());

        mockMvc.perform(get(BASE_URL, ROOM_ID, FLOW_ID)
                        .with(authHeaders()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.schedules").isEmpty());
    }

    @Test
    @DisplayName("스케줄 상세 조회 - 성공 200")
    void getFlowScheduleDetail_success() throws Exception {
        given(flowScheduleService.getFlowScheduleDetail(ROOM_ID, FLOW_ID, SCHEDULE_ID))
                .willReturn(sampleScheduleResponse());

        mockMvc.perform(get(DETAIL_URL, ROOM_ID, FLOW_ID, SCHEDULE_ID)
                        .with(authHeaders()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scheduleId").value(SCHEDULE_ID))
                .andExpect(jsonPath("$.dayOfWeek").value("MONDAY"))
                .andExpect(jsonPath("$.startTime").value("09:00:00"))
                .andExpect(jsonPath("$.endTime").value("18:00:00"));
    }
    @Test
    @DisplayName("스케줄 삭제 - 성공 204")
    void deleteFlowSchedule_success() throws Exception {
        willDoNothing().given(flowScheduleService)
                .deleteFlowSchedule(ROOM_ID, FLOW_ID, SCHEDULE_ID);

        mockMvc.perform(delete(DETAIL_URL, ROOM_ID, FLOW_ID, SCHEDULE_ID)
                        .with(authHeaders()))
                .andExpect(status().isNoContent());
    }

    private FlowScheduleCreateRequest sampleCreateRequest() {
        return new FlowScheduleCreateRequest(
                DayOfWeek.MONDAY,
                LocalTime.of(9, 0, 0),
                LocalTime.of(18, 0, 0)
        );
    }

    private FlowScheduleResponse sampleScheduleResponse() {
        return FlowScheduleResponse.builder()
                .scheduleId(SCHEDULE_ID)
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(9, 0, 0))
                .endTime(LocalTime.of(18, 0, 0))
                .build();
    }

    private FlowScheduleListResponse sampleListResponse() {
        return FlowScheduleListResponse.builder()
                .flowId(FLOW_ID)
                .schedules(List.of(sampleScheduleResponse()))
                .build();
    }

}
