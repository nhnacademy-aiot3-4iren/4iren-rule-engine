package com.nhnacademy.ruleengine.engine.executor;


import com.nhnacademy.ruleengine.domain.flow.enums.BranchType;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.NodeType;
import com.nhnacademy.ruleengine.engine.executor.node.NodeExecutionResult;
import com.nhnacademy.ruleengine.engine.executor.node.NodeExecutorRegistry;
import com.nhnacademy.ruleengine.engine.executor.runtimestate.FlowRuntime;
import com.nhnacademy.ruleengine.engine.executor.runtimestate.LogicalInputKey;
import com.nhnacademy.ruleengine.engine.executor.runtimestate.OrRuntimeState;
import com.nhnacademy.ruleengine.engine.flow.ExecutableFlow;
import com.nhnacademy.ruleengine.engine.model.AlertEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class FlowExecutor {

    private final NodeExecutorRegistry nodeExecutorRegistry;

    public void execute(FlowContext context){
        ExecutableFlow flow = context.flow();

        //FlowRuntime 초기화
        Queue<ExecutionPath> queue = new ArrayDeque<>();//queue 초기
        Map<Long, OrRuntimeState> orStateMap = initializeOrStateMap(flow);//OrRuntimeState 초기화
        FlowRuntime runtime = new FlowRuntime(queue, orStateMap);

        //OR 전파 중복 실행 방지를 위해 저장
        Set<Long> completedOrNodeIds = new HashSet<>();

        //startNode 바로 다음 노드들 큐에 저장
        enqueueStartNodes(flow, queue);

        log.info("루프 시작");
        //루프
        while(!queue.isEmpty()){
            //실행할 path 한개 꺼내기
            ExecutionPath path = queue.poll();

            ExecutableFlow.ExecutableNode currentNode = flow.nodeMap().get(path.currentNodeId());
            log.info("currentNodeId: " + path.currentNodeId() + "실행 중");

            //이미 완료된 OR노드이면 다시 처리하지 않음
            if(currentNode.nodeType() == NodeType.OR && completedOrNodeIds.contains(currentNode.nodeId())){
                continue;
            }

            //노드 executor 실행후 결과 반환.
            NodeExecutionResult result = nodeExecutorRegistry.execute(
                    currentNode.nodeType(),
                    currentNode,
                    context,
                    path,
                    runtime
            );


            // 현재 노드가 OR 노드인데 OrRuntimeState가 아직 isReady상태가 아닐경우 다음 진행 안함
            if(currentNode.nodeType() == NodeType.OR){
                OrRuntimeState orState = runtime.orStateMap().get(currentNode.nodeId());
                if(!orState.isReady()){
                    log.info("Or노드 도착, isReady 상태 아님: continue");
                    continue;
                }
                completedOrNodeIds.add(currentNode.nodeId());
                log.info("Or노드 도착, isReady상대. currentNodeId: " + currentNode.nodeId());

            }

            //선택되지 않은 branch이후에 OR노드가 있다면 경로가 끊긴것이므로 BLOCKED 상태 전파
            BranchType selectedBranch = result.passed() ? BranchType.TRUE : BranchType.FALSE;
            BranchType blockedBranch = (selectedBranch == BranchType.TRUE) ?BranchType.FALSE : BranchType.TRUE;
            propagateBlockedInputsOfUnselectedBranch(flow, currentNode.nodeId(),blockedBranch, runtime, queue, context, completedOrNodeIds);

            enqueueNextPath(flow, queue, result.path(), currentNode.nodeId(), selectedBranch);
        }
    }

    //선택되지 않은 branch가 죽었다는 사실을, 그 죽은 branch 끝에 도달 가능한 모든 OR 노드에게 전파
    private void propagateBlockedInputsOfUnselectedBranch(
            ExecutableFlow flow,
            Long currentNodeId,
            BranchType blockedBranch,
            FlowRuntime runtime,

            Queue<ExecutionPath> queue,
            FlowContext context,
            Set<Long> completedOrNodeIds) {
        //현재 노드에서 선택되지 않은 브랜치로 갈 수 있었던 첫 노드 가져옴
        log.info("현재 노드에서 선택되지 않은 브랜치로 갈 수 있었던 첫 노드 가져옴, currentNodeId: "+ currentNodeId);
        List<Long> blockedTargets = getNextNodeIds(flow, currentNodeId, blockedBranch);

        //타겟 노드가 없다면 끝노드이므로 그냥 return
        if(blockedTargets == null || blockedTargets.isEmpty()){
            return;
        }


        Deque<BlockedEdgeCursor> stack = new ArrayDeque<>();
        Set<BlockedEdgeCursor> visited = new HashSet<>();

        //탐색 시작점 enqueue
        for(Long targetNodeId : blockedTargets){
            stack.push(new BlockedEdgeCursor(currentNodeId, blockedBranch, targetNodeId));
        }

        //DFS
        while (!stack.isEmpty()) {
            BlockedEdgeCursor cursor = stack.pop();
            log.info("blocked 상태 전파 DFS" + cursor.fromNodeId + " -> " + cursor.toNodeId);
            //방문 노드 기록
            if(!visited.add(cursor)){
                continue;
            }

            ExecutableFlow.ExecutableNode targetNode = flow.nodeMap().get(cursor.toNodeId());

            //OR 입력이면 blocked 반영
            if(targetNode.nodeType() == NodeType.OR){
                OrRuntimeState orState = runtime.orStateMap().get(targetNode.nodeId());

                boolean wasReady = orState.isReady();

                LogicalInputKey blockedInput = new LogicalInputKey(
                        cursor.fromNodeId(),
                        cursor.branchType(),
                        targetNode.nodeId()
                );

                orState.markBlocked(blockedInput);
                log.info("OR노드 만다면 blocked 반영 targetNodeId: " + targetNode.nodeId());

                //blocked반영으로 ready 상태가 된 OR노드는 다시 재평가
                if(!wasReady && orState.isReady() && !completedOrNodeIds.contains(targetNode.nodeId())){
                    reevaluateReadyOrNode(
                            flow,
                            targetNode.nodeId(),
                            runtime,
                            queue,
                            context,
                            completedOrNodeIds
                    );
                }

                continue;
            }

            //OR 노드가 아니면 downstream 전체 탐색
            for(Long nextTrueNodeId : flow.trueAdjacencyMap().getOrDefault(targetNode.nodeId(), List.of())){
                stack.push(new BlockedEdgeCursor(targetNode.nodeId(),BranchType.TRUE, nextTrueNodeId));
            }
            for(Long nextFalseId : flow.falseAdjacencyMap().getOrDefault(targetNode.nodeId(), List.of())){
                stack.push(new BlockedEdgeCursor(targetNode.nodeId(), BranchType.FALSE, nextFalseId));
            }


        }

    }
    //OR 노드 재실행
    private void reevaluateReadyOrNode(
            ExecutableFlow flow,
            Long orNodeId,
            FlowRuntime runtime,
            Queue<ExecutionPath> queue,
            FlowContext context,
            Set<Long> completedOrNodeIds
    ) {
       log.info("blocked반영으로 ready 상태가 된 OR노드는 다시 재평가");
        ExecutableFlow.ExecutableNode orNode = flow.nodeMap().get(orNodeId);
        if(orNode == null || orNode.nodeType() != NodeType.OR){
            return;
        }

        OrRuntimeState orState = runtime.orStateMap().get(orNodeId);
        if(orState == null || !orState.isReady()){
            return;
        }

        boolean passed = orState.isSatisfied();   // 실행기 재호출 없이 상태만 읽음
        BranchType selectedBranch = passed ? BranchType.TRUE : BranchType.FALSE;
        BranchType blockedBranch  = passed ? BranchType.FALSE : BranchType.TRUE;

        List<AlertEvent.NodeResult> mergedHistory = orState.mergeArrivedHistories();
        mergedHistory.add(buildOrNodeResult(orNode));

        ExecutionPath continuedPath = ExecutionPath
                .start(orNodeId, orNodeId, selectedBranch)
                .appendMergedResult(mergedHistory);

        completedOrNodeIds.add(orNodeId);


        log.info("Or노드가 평가됨에 따라 뒤의 Or노드에도 blocked상태 전파");
        propagateBlockedInputsOfUnselectedBranch(flow, orNodeId, blockedBranch, runtime, queue, context, completedOrNodeIds);
        enqueueNextPath(flow, queue, continuedPath, orNodeId, selectedBranch);
    }

    private AlertEvent.NodeResult buildOrNodeResult(ExecutableFlow.ExecutableNode orNode) {
        return new AlertEvent.NodeResult(orNode.nodeType().name(), null, null, null, null, null);
    }

    //blocked 경로 추적
    private record BlockedEdgeCursor(
            Long fromNodeId,
            BranchType branchType,
            Long toNodeId
    ) {
    }

    //startNodeId 다음 노드들을 큐에 넣음
    private void enqueueStartNodes(ExecutableFlow flow, Queue<ExecutionPath> queue) {
        log.info("startNode 다음 노드들 enqueue");
        List<Long> startNextNodes = flow.trueAdjacencyMap()
                .getOrDefault(flow.startNodeId(), List.of());

        for (Long nextNodeId : startNextNodes) {
            queue.offer(ExecutionPath.start(
                    nextNodeId,
                    flow.startNodeId(),
                    BranchType.TRUE
            ));
        }
    }

    //다음에 실행할 노드를 큐에 넣음
    private void enqueueNextPath(
            ExecutableFlow flow,
            Queue<ExecutionPath> queue,
            ExecutionPath currentPath,
            Long currentNodeId,
            BranchType selectedBranch
    ){
        log.info("currentNode " +currentNodeId +  " 다음에 실행할 노드 enqueue");
        List<Long> nextNodeIds = getNextNodeIds(flow, currentNodeId, selectedBranch);

        //다음 노드가 없다면 끝노드이므로 return
        if( nextNodeIds == null || nextNodeIds.isEmpty()){
            log.info("다음 노드 없음, 해당 경로 종료.");
            return;
        }
        nextNodeIds.stream()
                .forEach(
                        nextNodeId -> queue.offer(currentPath.next(nextNodeId, selectedBranch))
                );
    }

    private List<Long> getNextNodeIds(ExecutableFlow flow, Long currentNodeId, BranchType branchType){
        return branchType == BranchType.TRUE ?
                flow.trueAdjacencyMap().getOrDefault(currentNodeId,List.of()) :
                flow.falseAdjacencyMap().getOrDefault(currentNodeId, List.of());
    }

    //플로우 안에 존재하는 모든 OrRuntimeState 초기화
    private Map<Long, OrRuntimeState> initializeOrStateMap(ExecutableFlow flow) {
        log.info("initializeOrStateMap: 플로우 안에 존에하는 OrRuntimeState 초기화");
        Map<Long,OrRuntimeState> result = new HashMap<>();

        for(Map.Entry<Long, ExecutableFlow.ExecutableNode> entry : flow.nodeMap().entrySet()){
            ExecutableFlow.ExecutableNode node = entry.getValue();
            if(node.nodeType() != NodeType.OR){
                continue;
            }

            List<LogicalInputKey> inputKeys = collectOrInputs(flow, node.nodeId());
            result.put(node.nodeId(), new OrRuntimeState(node.nodeId(), inputKeys));
        }

        return result;

    }

    //Or노드로 입력되는 연결 경로 찾기 찾기
    private List<LogicalInputKey> collectOrInputs(ExecutableFlow flow, Long orNodeId) {
        log.info("Or 노드로 입력되는 연결 경로 찾기, orNodeId: " + orNodeId);
        List<LogicalInputKey> inputs = new ArrayList<>();

        for(Map.Entry<Long, List<Long>> entry : flow.trueAdjacencyMap().entrySet() ){
            Long fromNodeId = entry.getKey();
            for(Long toNodeId : entry.getValue()){
                if(orNodeId.equals(toNodeId)){
                    inputs.add(new LogicalInputKey(fromNodeId, BranchType.TRUE, orNodeId));
                }
            }
        }

        for(Map.Entry<Long, List<Long>> entry : flow.falseAdjacencyMap().entrySet()){
            Long fromNodeId = entry.getKey();
            for(Long toNodeId : entry.getValue()){
                if(orNodeId.equals(toNodeId)){
                    inputs.add(new LogicalInputKey(fromNodeId, BranchType.FALSE, orNodeId));
                }
            }
        }
        return inputs;
    }
}
