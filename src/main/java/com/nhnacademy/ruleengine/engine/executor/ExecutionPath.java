package com.nhnacademy.ruleengine.engine.executor;

import com.nhnacademy.ruleengine.domain.flow.enums.BranchType;
import com.nhnacademy.ruleengine.engine.model.AlertEvent;

import java.util.ArrayList;
import java.util.List;

//분기된 경로별 실행 상태 추적 및 저장
public record ExecutionPath(
        Long currentNodeId,
        Long fromNodeId,
        BranchType fromBranchType,
        List<AlertEvent.NodeResult> history
) {
    //ExecutionPath 첫 저장
    public static ExecutionPath start(Long currentNodeId, Long arrivedFromNodeId, BranchType arrivedBranchType){
        return new ExecutionPath(currentNodeId, arrivedFromNodeId, arrivedBranchType, List.of());
    }

    //경로 history 추가.
    public ExecutionPath append(AlertEvent.NodeResult nodeResult){
        //결과에 따라서 여러 노드로 분산될 수 있는거라면 불변 객체를 반환해야함.
        List<AlertEvent.NodeResult> copied = new ArrayList<>(history);
        copied.add(nodeResult);
        return new ExecutionPath(currentNodeId, fromNodeId, fromBranchType, copied);
    }

    //다음 노드로 이동할때의 상태 객체를 새로 생성
    public ExecutionPath next(Long nextNodeId, BranchType arrivedBranchType) {
        return new ExecutionPath(nextNodeId, currentNodeId, arrivedBranchType, new ArrayList<>(history));
    }
}