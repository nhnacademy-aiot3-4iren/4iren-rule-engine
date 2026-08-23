package com.nhnacademy.ruleengine.engine.executor.node;

import com.nhnacademy.ruleengine.domain.nodeconfig.enums.Operator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class OperatorEvaluatorTest {

    @ParameterizedTest(name = "{0}: {1} vs {2} => {3}")
    @DisplayName("연산자별 비교 결과가 올바르다")
    @CsvSource({
            "GT,  10.0, 5.0,  true",
            "GT,  5.0,  10.0, false",
            "GT,  5.0,  5.0,  false",
            "GTE, 5.0,  5.0,  true",
            "GTE, 4.9,  5.0,  false",
            "LT,  4.0,  5.0,  true",
            "LT,  5.0,  5.0,  false",
            "LTE, 5.0,  5.0,  true",
            "LTE, 5.1,  5.0,  false",
            "EQ,  5.0,  5.0,  true",
            "EQ,  5.0,  5.1,  false",
            "NEQ, 5.0,  5.1,  true",
            "NEQ, 5.0,  5.0,  false"
    })
    void evaluate(Operator operator, double currentValue, double targetValue, boolean expected) {
        boolean result = OperatorEvaluator.evaluate(operator, currentValue, targetValue);

        assertThat(result).isEqualTo(expected);
    }

    @ParameterizedTest(name = "EQ/NEQ는 부동소수점 오차 없이 Double.compare로 비교한다")
    @CsvSource({
            "EQ,  0.1, 0.1, true",
            "NEQ, 0.1, 0.2, true"
    })
    void evaluate_usesDoubleCompareForEquality(Operator operator, double currentValue, double targetValue, boolean expected) {
        boolean result = OperatorEvaluator.evaluate(operator, currentValue, targetValue);

        assertThat(result).isEqualTo(expected);
    }
}