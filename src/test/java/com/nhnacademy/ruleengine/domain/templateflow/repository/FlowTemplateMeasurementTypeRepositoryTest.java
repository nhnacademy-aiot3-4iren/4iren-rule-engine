package com.nhnacademy.ruleengine.domain.templateflow.repository;

import com.nhnacademy.ruleengine.domain.flow.entity.Flow;
import com.nhnacademy.ruleengine.domain.flow.repository.FlowRepository;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.MeasurementType;
import com.nhnacademy.ruleengine.domain.templateflow.entity.FlowTemplateMeasurementType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@DataJpaTest
@Import(JacksonAutoConfiguration.class)
class FlowTemplateMeasurementTypeRepositoryTest {
    @Autowired
    private FlowTemplateMeasurementTypeRepository flowTemplateMeasurementTypeRepository;

    @Autowired
    private FlowRepository flowRepository;

    private Flow templateFlow1;
    private Flow templateFlow2;
    private Flow nonTemplateFlow;

    private FlowTemplateMeasurementType type1;
    private FlowTemplateMeasurementType type2;
    private FlowTemplateMeasurementType type3;

    @BeforeEach
    void setUp() {
        templateFlow1 = flowRepository.save(Flow.templateBuilder()
                        .flowName("템플릿 플로우1").description("템플릿 플로우1 설명").build());

        templateFlow2 = flowRepository.save(Flow.templateBuilder()
                .flowName("템플릿 플로우2").description("템플릿 플로우2 설명").build());

        nonTemplateFlow = flowRepository.save(Flow.regularBuilder()
                .roomId(1L).isActive(true).flowName("일반 플로우").description("일반 플로우 설명")
                .build());

        type1 = flowTemplateMeasurementTypeRepository.save(
                FlowTemplateMeasurementType.builder()
                        .flow(templateFlow1)
                        .measurementType(MeasurementType.TEMPERATURE)
                        .build()
        );

        type2 = flowTemplateMeasurementTypeRepository.save(
                FlowTemplateMeasurementType.builder()
                        .flow(templateFlow1)
                        .measurementType(MeasurementType.HUMIDITY)
                        .build()
        );

        type3 = flowTemplateMeasurementTypeRepository.save(
                FlowTemplateMeasurementType.builder()
                        .flow(templateFlow2)
                        .measurementType(MeasurementType.CO2)
                        .build()
        );
    }


    @Test
    @DisplayName("findAllByFlow - 해당 플로우의 측정 타입만 반환")
    void findAllByFlow_returnsTypesOfFlow() {
        List<FlowTemplateMeasurementType> result =
                flowTemplateMeasurementTypeRepository.findAllByFlow(templateFlow1);

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(FlowTemplateMeasurementType::getMeasurementType)
                .containsExactlyInAnyOrder(MeasurementType.TEMPERATURE, MeasurementType.HUMIDITY)
                .doesNotContain(MeasurementType.CO2);

    }
    @Test
    @DisplayName("findAllByFlow - 측정 타입 없는 플로우는 빈 리스트 반환")
    void findAllByFlow_noTypes_returnsEmpty() {
        List<FlowTemplateMeasurementType> result =
                flowTemplateMeasurementTypeRepository.findAllByFlow(nonTemplateFlow);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findAllByFlowIn - 여러 템플릿 플로우의 측정 타입 한번에 조회")
    void findAllByFlowIn_returnsAllTypes() {
        List<FlowTemplateMeasurementType> result =
                flowTemplateMeasurementTypeRepository.findAllByFlowIn(
                        List.of(templateFlow1, templateFlow2)
                );

        assertThat(result).hasSize(3);
        assertThat(result)
                .extracting(FlowTemplateMeasurementType::getMeasurementType)
                .containsExactlyInAnyOrder(MeasurementType.TEMPERATURE, MeasurementType.HUMIDITY, MeasurementType.CO2);
    }

    @Test
    @DisplayName("findAllByFlowIn - 빈 리스트 입력 시 빈 결과 반환")
    void findAllByFlowIn_emptyInput_returnsEmpty() {
        List<FlowTemplateMeasurementType> result =
                flowTemplateMeasurementTypeRepository.findAllByFlowIn(List.of());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findAllByFlowIn - 측정 타입 없는 플로우만 포함 시 빈 결과 반환")
    void findAllByFlowIn_flowWithNoTypes_returnsEmpty() {
        List<FlowTemplateMeasurementType> result =
                flowTemplateMeasurementTypeRepository.findAllByFlowIn(
                        List.of(nonTemplateFlow)
                );

        assertThat(result).isEmpty();
    }
    @Test
    @DisplayName("deleteAllByFlow - 해당 플로우 측정 타입 삭제")
    void deleteAllByFlow_deletesTypesOfFlow() {
        flowTemplateMeasurementTypeRepository.deleteAllByFlow(templateFlow1);

        List<FlowTemplateMeasurementType> result =
                flowTemplateMeasurementTypeRepository.findAllByFlow(templateFlow1);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("deleteAllByFlow - 측정 타입 없는 플로우 삭제 시 예외 없음")
    void deleteAllByFlow_noTypes_noException() {
        // when & then: 예외 없이 정상 실행
        assertThatCode(() ->
                flowTemplateMeasurementTypeRepository.deleteAllByFlow(nonTemplateFlow)
        ).doesNotThrowAnyException();
    }
}