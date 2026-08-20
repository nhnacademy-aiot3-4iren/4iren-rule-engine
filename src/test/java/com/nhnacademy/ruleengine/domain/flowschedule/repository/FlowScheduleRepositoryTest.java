package com.nhnacademy.ruleengine.domain.flowschedule.repository;

import com.nhnacademy.ruleengine.domain.flow.entity.Flow;
import com.nhnacademy.ruleengine.domain.flow.repository.FlowRepository;
import com.nhnacademy.ruleengine.domain.flowschedule.entity.FlowSchedule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(JacksonAutoConfiguration.class)
class FlowScheduleRepositoryTest {

    @Autowired
    private FlowScheduleRepository flowScheduleRepository;

    @Autowired
    private FlowRepository flowRepository;

    private Flow flow1;
    private Flow flow2;
    private FlowSchedule schedule1;
    private FlowSchedule schedule2;

    @BeforeEach
    void setUp() {
        flow1 = flowRepository.save(Flow.regularBuilder()
                .roomId(1L).isActive(true).flowName("플로우1").description("플로우1 설명").build());

        flow2 = flowRepository.save(Flow.regularBuilder()
                .roomId(1L).isActive(true).flowName("플로우2").description("플로우2 설명").build());

        schedule1 = flowScheduleRepository.save(FlowSchedule.builder().flow(flow1).dayOfWeek(DayOfWeek.MONDAY).startTime(LocalTime.of(9,0)).endTime(LocalTime.of(18, 0)).build());
        schedule2 = flowScheduleRepository.save(FlowSchedule.builder().flow(flow2).dayOfWeek(DayOfWeek.TUESDAY).startTime(LocalTime.of(10, 0)).endTime(LocalTime.of(17, 0)).build());
    }

    @Test
    void deleteInBatch() {
    }

    @Test
    @DisplayName("findAllByFlowId - 해당 플로우 스케줄만 반환")
    void findAllByFlowId_returnsSchedulesOfFlow() {
        List<FlowSchedule> result = flowScheduleRepository.findAllByFlowId(flow1.getId());

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
    }

    @Test
    @DisplayName("existsFlowSchedule - 존재하면 true")
    void existsFlowSchedule_exists_returnsTrue() {
        boolean result = flowScheduleRepository.existsFlowSchedule(
                schedule1.getId(), flow1.getId(), 1L
        );

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("existsFlowSchedule - roomId 불일치 시 false")
    void existsFlowSchedule_wrongRoomId_returnsFalse() {
        boolean result = flowScheduleRepository.existsFlowSchedule(
                schedule1.getId(), flow1.getId(), 999L
        );

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("existsFlowSchedule - flowId 불일치 시 false")
    void existsFlowSchedule_wrongFlowId_returnsFalse() {
        boolean result = flowScheduleRepository.existsFlowSchedule(
                schedule1.getId(), flow2.getId(), 1L
        );

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("findSchedule - 조건 일치 시 스케줄 반환")
    void findSchedule_exists_returnsSchedule() {
        Optional<FlowSchedule> result = flowScheduleRepository.findSchedule(
                schedule1.getId(), flow1.getId(), 1L
        );

        assertThat(result).isPresent();
        assertThat(result.get().getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
    }


    @Test
    @DisplayName("findSchedule - roomId 불일치 시 empty")
    void findSchedule_wrongRoomId_returnsEmpty() {
        Optional<FlowSchedule> result = flowScheduleRepository.findSchedule(
                schedule1.getId(), flow1.getId(), 999L
        );

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findSchedule - flowId 불일치 시 empty")
    void findSchedule_wrongFlowId_returnsEmpty() {
        Optional<FlowSchedule> result = flowScheduleRepository.findSchedule(
                schedule1.getId(), flow2.getId(), 1L
        );

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findAllByFlowIdIn - 여러 플로우 스케줄 한번에 조회")
    void findAllByFlowIdIn_returnsAllSchedules() {
        List<FlowSchedule> result = flowScheduleRepository.findAllByFlowIdIn(
                List.of(flow1.getId(), flow2.getId())
        );

        assertThat(result).hasSize(2);
    }
}