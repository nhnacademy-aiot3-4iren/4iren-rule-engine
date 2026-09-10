package com.nhnacademy.ruleengine.domain.flowschedule.service;

import com.nhnacademy.ruleengine.common.exception.notfound.FlowNotFoundException;
import com.nhnacademy.ruleengine.common.exception.notfound.FlowScheduleNotFoundException;
import com.nhnacademy.ruleengine.common.exception.invalid.FlowScheduleValidationFailed;
import com.nhnacademy.ruleengine.domain.flow.entity.Flow;
import com.nhnacademy.ruleengine.domain.flow.repository.FlowRepository;
import com.nhnacademy.ruleengine.domain.flowschedule.dto.FlowScheduleCreateRequest;
import com.nhnacademy.ruleengine.domain.flowschedule.dto.FlowScheduleCreateResponse;
import com.nhnacademy.ruleengine.domain.flowschedule.dto.FlowScheduleListResponse;
import com.nhnacademy.ruleengine.domain.flowschedule.dto.FlowScheduleResponse;
import com.nhnacademy.ruleengine.domain.flowschedule.entity.FlowSchedule;
import com.nhnacademy.ruleengine.domain.flowschedule.repository.FlowScheduleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlowScheduleServiceTest {

    @Mock private FlowRepository flowRepository;
    @Mock private FlowScheduleRepository flowScheduleRepository;

    @InjectMocks
    private FlowScheduleService flowScheduleService;

    @Test
    @DisplayName("스케줄 생성")
    void createFlowSchedule_success() {
        Flow mockFlow = mock(Flow.class);
        when(flowRepository.findByIdAndRoomId(1L, 100L)).thenReturn(Optional.of(mockFlow));

        FlowSchedule mockSchedule = mock(FlowSchedule.class);
        when(mockSchedule.getId()).thenReturn(10L);
        when(flowScheduleRepository.findAllByFlowIdAndDayOfWeek(1L, DayOfWeek.MONDAY)).thenReturn(List.of());
        when(flowScheduleRepository.saveAll(anyList())).thenReturn(List.of(mockSchedule));

        FlowScheduleCreateRequest request = sampleCreateRequest();
        FlowScheduleCreateResponse response = flowScheduleService.createFlowSchedule(100L, 1L, request);

        assertThat(response.scheduleIds()).containsExactly(10L);
        verify(flowScheduleRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("flow 없음 생성 실패")
    void createFlowSchedule_flowNotFound() {
        when(flowRepository.findByIdAndRoomId(1L, 100L)).thenReturn(Optional.empty());
        FlowScheduleCreateRequest request = sampleCreateRequest();

        assertThatThrownBy(() -> flowScheduleService.createFlowSchedule(100L, 1L, request))
                .isInstanceOf(FlowNotFoundException.class);
    }

    @Test
    @DisplayName("요청 목록 내부에 겹치는 스케줄이 있으면 생성 실패")
    void createFlowSchedule_overlappedInRequest() {
        Flow mockFlow = mock(Flow.class);
        when(flowRepository.findByIdAndRoomId(1L, 100L)).thenReturn(Optional.of(mockFlow));

        FlowScheduleCreateRequest request = new FlowScheduleCreateRequest(List.of(
                new FlowScheduleCreateRequest.FlowScheduleRequest(
                        DayOfWeek.MONDAY,
                        LocalTime.of(9, 0),
                        LocalTime.of(12, 0)
                ),
                new FlowScheduleCreateRequest.FlowScheduleRequest(
                        DayOfWeek.MONDAY,
                        LocalTime.of(11, 0),
                        LocalTime.of(13, 0)
                )
        ));

        assertThatThrownBy(() -> flowScheduleService.createFlowSchedule(100L, 1L, request))
                .isInstanceOf(FlowScheduleValidationFailed.class);
        verify(flowScheduleRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("스케줄 목록 조회")
    void getFlowScheduleList_success() {
        when(flowRepository.existsByIdAndRoomId(1L, 100L)).thenReturn(true);
        FlowSchedule mockSchedule = mock(FlowSchedule.class);
        when(flowScheduleRepository.findAllByFlowId(1L)).thenReturn(List.of(mockSchedule));

        FlowScheduleListResponse response = flowScheduleService.getFlowScheduleList(1L, 100L);

        assertThat(response.flowId()).isEqualTo(1L);
        assertThat(response.schedules()).hasSize(1);
    }

    @Test
    @DisplayName("flow 없음 목록 조회 실패")
    void getFlowScheduleList_flowNotFound() {
        when(flowRepository.existsByIdAndRoomId(1L, 100L)).thenReturn(false);

        assertThatThrownBy(() -> flowScheduleService.getFlowScheduleList(1L, 100L))
                .isInstanceOf(FlowNotFoundException.class);
    }

    @Test
    @DisplayName("스케줄 상세 조회")
    void getFlowScheduleDetail_success() {
        FlowSchedule mockSchedule = mock(FlowSchedule.class);
        when(flowScheduleRepository.findSchedule(10L, 1L, 100L)).thenReturn(Optional.of(mockSchedule));

        FlowScheduleResponse response = flowScheduleService.getFlowScheduleDetail(100L, 1L, 10L);

        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("스케줄 삭제")
    void deleteFlowSchedule_success() {
        when(flowScheduleRepository.existsFlowSchedule(10L, 1L, 100L)).thenReturn(true);

        flowScheduleService.deleteFlowSchedule(100L, 1L, 10L);

        verify(flowScheduleRepository).deleteById(10L);
    }

    @Test
    @DisplayName("flow 없음 삭제 실패")
    void deleteFlowSchedule_flowNotFound() {
        when(flowScheduleRepository.existsFlowSchedule(10L, 1L, 100L)).thenReturn(false);

        assertThatThrownBy(() -> flowScheduleService.deleteFlowSchedule(100L, 1L, 10L))
                .isInstanceOf(FlowScheduleNotFoundException.class);
    }

    private FlowScheduleCreateRequest sampleCreateRequest() {
        return new FlowScheduleCreateRequest(List.of(
                new FlowScheduleCreateRequest.FlowScheduleRequest(
                        DayOfWeek.MONDAY,
                        LocalTime.of(9, 0),
                        LocalTime.of(18, 0)
                )
        ));
    }
}
