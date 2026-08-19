package com.nhnacademy.ruleengine.engine.executor;


import com.nhnacademy.ruleengine.domain.flow.enums.BranchType;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.NodeType;
import com.nhnacademy.ruleengine.engine.executor.node.NodeExecutorRegistry;
import com.nhnacademy.ruleengine.engine.executor.runtimestate.FlowRuntime;
import com.nhnacademy.ruleengine.engine.executor.runtimestate.LogicalInputKey;
import com.nhnacademy.ruleengine.engine.executor.runtimestate.OrRuntimeState;
import com.nhnacademy.ruleengine.engine.flow.ExecutableFlow;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;

@Slf4j
@Component
@RequiredArgsConstructor
public class FlowExecutor {

    private final NodeExecutorRegistry nodeExecutorRegistry;

    public void execute(ExecutableFlow flow, FlowContext context){
        //FlowRuntime 초기화
        Queue<ExecutionPath> queue = new ArrayDeque<>();//queue 초기
        Map<Long, OrRuntimeState> orStateMap = initializeOrStateMap(flow);//OrRuntimeState 초기화
        FlowRuntime runtime = new FlowRuntime(queue, orStateMap);

        //startNode 바로 다음 노드들 큐에 저장
        enqueueStartNodes(flow, queue);

        //루프
        while(!queue.isEmpty()){
            //실행할 path 한개 꺼내기
            ExecutionPath path = queue.poll();

            ExecutableFlow.ExecutableNode currentNode = flow.nodeMap().get(path.currentNodeId());

            //노드 executor 실행후 결과 반환.
            boolean decision = nodeExecutorRegistry.execute(
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
                    continue;
                }
            }

            BranchType selectedBranch = decision ? BranchType.TRUE : BranchType.FALSE;
            enqueueNextPath(flow, queue, path, currentNode.nodeId(), selectedBranch);

            //현재 노드의 결과 선택되지 않은 branch가 OR로간다면 Or노드로의 경로가 끊긴것이므로 BLOCKED 상태 반영
            BranchType blockedBranch = (selectedBranch == BranchType.TRUE) ?BranchType.FALSE : BranchType.TRUE;

            blockOrInputsOfUnselectedBranch(flow, currentNode.nodeId(),blockedBranch, runtime);

        }
    }

    //OrRuntimeState에 BLOCKED 상태 반영
    private void blockOrInputsOfUnselectedBranch(
            ExecutableFlow flow,
            Long currentNodeId,
            BranchType blockedBranch,
            FlowRuntime runtime
    ) {
        List<Long>  blackedTargets = getNextNodeIds(flow, currentNodeId, blockedBranch);

        //타겟 노드가 없다면 끝노드이므로 그냥 return
        if(blackedTargets == null || blackedTargets.isEmpty()){
            return;
        }

        for(Long targetNodeId: blackedTargets){
            ExecutableFlow.ExecutableNode targetNode = flow.nodeMap().get(targetNodeId);

            if(targetNode.nodeType() != NodeType.OR){
                continue;
            }
            OrRuntimeState orState = runtime.orStateMap().get(targetNodeId);

            LogicalInputKey blockedInput = new LogicalInputKey(
                    currentNodeId,
                    blockedBranch,
                    targetNodeId
            );

            orState.markBlocked(blockedInput);
        }
    }

    //startNodeId 다음 노드들을 큐에 넣음
    private void enqueueStartNodes(ExecutableFlow flow, Queue<ExecutionPath> queue) {
        List<Long> startNextNodes = flow.trueAdjacencyMap()
                .get(flow.startNodeId());

        for(Long nextNodeId : startNextNodes){
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
        List<Long> nextNodeIds = getNextNodeIds(flow, currentNodeId, selectedBranch);

        //다음 노드가 없다면 끝노드이므로 return
        if( nextNodeIds == null || nextNodeIds.isEmpty()){
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
