package com.nhnacademy.ruleengine.engine.executor.runtimestate;

import com.nhnacademy.ruleengine.engine.model.AlertEvent;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//플로우 실행중 OrNode와 연결된 경로들의 상태를 저장 및 판단
@Getter
public class OrRuntimeState {


    private final Long orNodeId;
    private final Map<LogicalInputKey, LogicalInputStatus> statusMap = new HashMap<>();
    private final Map<LogicalInputKey, List<AlertEvent.NodeResult>> arrivedHistoryMap = new HashMap<>();


    public OrRuntimeState(Long orNodeId, List<LogicalInputKey> inputs) {
        this.orNodeId = orNodeId;
        inputs.stream()
                .forEach(logicalInputKey -> statusMap.put(logicalInputKey, LogicalInputStatus.PENDING));
    }

    //각 경로에서 or노드에 도착시 해당 경로 arrived 처리
    public void markArrived(LogicalInputKey key, List<AlertEvent.NodeResult> history){
        statusMap.put(key,LogicalInputStatus.ARRIVED);
        arrivedHistoryMap.put(key, new ArrayList<>(history));
    }

    //or노드로 향하는 경로가 막힐 경우 해당 경로 blocked 처리
    public void markBlocked(LogicalInputKey key){
        if(statusMap.get(key) == LogicalInputStatus.PENDING){
            statusMap.put(key, LogicalInputStatus.BLOCKED);
        }
    }

    //PENDING 상태인 경로가 없을 경우 or노드 결과(true/false) 판단
    public boolean isReady(){
        return  statusMap.values().stream()
                .noneMatch(status ->status == LogicalInputStatus.PENDING);
    }

    //isReady상태인 경우에만 호출 가능: 하나만 ARRIVED 상태여도 조건 충족
    public boolean isSatisfied(){
        return statusMap.values().stream()
                .anyMatch(status -> status == LogicalInputStatus.ARRIVED);
    }
    //도달한 경로의 history병함 -> arrivedHistoryMap
    //arrivedHistoryMap -> or노드에서 나가는 경로의 PathExecution 새로 반환
    public List<AlertEvent.NodeResult> mergeArrivedHistories(){
        List<AlertEvent.NodeResult> merged = new ArrayList<>();
        arrivedHistoryMap.values().forEach(history-> merged.addAll(history));
        return merged;
    }

}
