package com.nhnacademy.ruleengine.domain.flow.repository;

import com.nhnacademy.ruleengine.domain.flow.entity.Flow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
@Import(JacksonAutoConfiguration.class)
class FlowRepositoryTest {

    @Autowired
    private FlowRepository flowRepository;

    private Flow activeFlow;
    private Flow inactiveFlow;
    private Flow templateFlow;

    @BeforeEach
    void setUp() {
        activeFlow = flowRepository.save(Flow.regularBuilder().roomId(1L).isActive(true).flowName("활성 플로우").description("활성 상태의 플로우").build());

        inactiveFlow = flowRepository.save(Flow.regularBuilder().roomId(1L).flowName("비활성 플로우").description("비활성 상태의 플로우").isActive(false).build());

        templateFlow = flowRepository.save(Flow.templateBuilder().flowName("템플릿 플로우").description("템플릿 플로우 설명").build());
    }

    @Test
    void deleteInBatch() {
    }

    @Test
    @DisplayName("findByIdAndRoomId - 존재하는 플로우 조회 성공")
    void findByIdAndRoomId_exists_returnsFlow(){
        Optional<Flow> result = flowRepository.findByIdAndRoomId(activeFlow.getId(), 1L);

        assertThat(result).isPresent();
        assertThat(result.get().getFlowName()).isEqualTo("활성 플로우");
    }

    @Test
    @DisplayName("findByIdAndRoomId - roomId 불일치 시 empty 반환")
    void findByIdAndRoomId_wrongRoomId_returnsEmpty() {
        Optional<Flow> result = flowRepository.findByIdAndRoomId(activeFlow.getId(), 999L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("existByIdAndRoomId - 존재하면 true")
    void existsByIdAndRoomId_exists_returnTrue(){
        boolean result = flowRepository.existsByIdAndRoomId(activeFlow.getId(), 1L);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("existByIdAndRoomId - 존재하지 않으면 false")
    void existByIdAndeRoomId_notExist_returnFalse(){
        boolean result = flowRepository.existsByIdAndRoomId(999L, 1L);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("findAllByRoomId - roomId에 해당하는 모든 플로우 반환")
    void findAllByRoomId_returnsAllFlows() {
        List<Flow> result = flowRepository.findAllByRoomId(1L);

        assertThat(result.size()).isEqualTo(2);
    }


    @Test
    @DisplayName("findAllByRoomId - 다른 roomId는 조회 안됨")
    void findAllByRoomId_differentRoomId_returnsEmpty() {
        List<Flow> result = flowRepository.findAllByRoomId(999L);

        assertThat(result).isEmpty();
    }


    @Test
    @DisplayName("findAllByIsTemplate - 템플릿 플로우만 반환")
    void findAllByIsTemplate_true_returnsTemplateFlows() {
        List<Flow> result = flowRepository.findAllByIsTemplate(true);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getFlowName()).isEqualTo("템플릿 플로우");
        assertThat(result.getFirst().getIsTemplate()).isTrue();
    }

    @Test
    @DisplayName("findAllByRoomIdAndIsActiveTrueAndIsTemplateFalse - 활성 + 비템플릿만 반환")
    void findAllByRoomIdAndIsActiveTrueAndIsTemplateFalse_returnsCorrectFlows() {
        List<Flow> result = flowRepository
                .findAllByRoomIdAndIsActiveTrueAndIsTemplateFalse(1L);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getFlowName()).isEqualTo("활성 플로우");
    }

}