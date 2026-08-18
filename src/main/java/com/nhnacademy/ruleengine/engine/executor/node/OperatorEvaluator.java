package com.nhnacademy.ruleengine.engine.executor.node;

import com.nhnacademy.ruleengine.domain.nodeconfig.enums.Operator;

public class OperatorEvaluator {

    public static boolean evaluate(Operator operator, double currentValue, double targetValue) {
        return switch (operator) {
            case GT -> currentValue > targetValue;
            case GTE -> currentValue >= targetValue;
            case LT -> currentValue < targetValue;
            case LTE -> currentValue <= targetValue;
            case EQ -> Double.compare(currentValue, targetValue) == 0;
            case NEQ -> Double.compare(currentValue, targetValue) != 0;
        };
    }
}
