package com.nhnacademy.ruleengine.engine.executor.runtimestate;

public enum LogicalInputStatus {
    PENDING,//아직 조건 노드의 이전 노드를 실행중인 상태
    ARRIVED,//조건 노드에 도달한 상태
    BLOCKED//노드 연결이 끊거거나 다른 방향으로 분기하여 조건 노드에 도달하는 경로가 막힘
}
