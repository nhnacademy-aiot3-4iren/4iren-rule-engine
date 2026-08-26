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
    public static ExecutionPath start(Long currentNodeId, Long fromNodeId, BranchType fromBranchType){
        return new ExecutionPath(currentNodeId, fromNodeId, fromBranchType, List.of());
    }

    //NodeExecutor.execute 실행 후 해당 노드에서의 결과를 history 추가.
    public ExecutionPath append(AlertEvent.NodeResult nodeResult){
        //결과에 따라서 여러 노드로 분산될 수 있는거라면 불변 객체를 반환해야함.
        List<AlertEvent.NodeResult> copied = new ArrayList<>(history);
        copied.add(nodeResult);
        return new ExecutionPath(currentNodeId, fromNodeId, fromBranchType, copied);
    }


    //FlowExecutore에서 다음 경로를 enqueue할 때 사용. 이전에 실행한 노드의 다음 노드로 향하는 ExecutionPath 객체를 새로 생성
    public ExecutionPath next(Long nextNodeId, BranchType fromBranchType) {
        return new ExecutionPath(nextNodeId, currentNodeId, fromBranchType, new ArrayList<>(history));
    }

    //or 노드에서 판단에 따라 다음 노드로 향할 때. or노드에 도착한 경로들의 history를 병합해서 출력함
    public ExecutionPath appendMergedResult(List<AlertEvent.NodeResult> nodeResult){
        //결과에 따라서 여러 노드로 분산될 수 있는거라면 불변 객체를 반환해야함.
        List<AlertEvent.NodeResult> copied = new ArrayList<>(nodeResult);
        return new ExecutionPath(currentNodeId, fromNodeId, fromBranchType, copied);
    }
}