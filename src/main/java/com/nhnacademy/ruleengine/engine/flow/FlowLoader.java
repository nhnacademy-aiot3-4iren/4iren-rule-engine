package com.nhnacademy.ruleengine.engine.flow;

import com.nhnacademy.ruleengine.domain.flow.entity.Connection;
import com.nhnacademy.ruleengine.domain.flow.entity.Flow;
import com.nhnacademy.ruleengine.domain.flow.entity.Node;
import com.nhnacademy.ruleengine.domain.flow.repository.ConnectionRepository;
import com.nhnacademy.ruleengine.domain.flow.repository.FlowRepository;
import com.nhnacademy.ruleengine.domain.flow.repository.NodeRepository;
import com.nhnacademy.ruleengine.domain.flowschedule.entity.FlowSchedule;
import com.nhnacademy.ruleengine.domain.flowschedule.repository.FlowScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class FlowLoader {
    private final FlowRepository flowRepository;
    private final NodeRepository nodeRepository;
    private final ConnectionRepository connectionRepository;
    private final FlowScheduleRepository flowScheduleRepository;
    private final FlowGraphBuilder flowGraphBuilder;
//    private final FlowCacheRepository flowCacheRepository;

    @Cacheable(value = "flow:room", key = "#roomId", unless = "#result == null || #result.isEmpty()", cacheManager = "flowCacheManager")
    public List<ExecutableFlow> load(Long roomId){
        log.info("cache miss roomId = {}, DB 조회", roomId);
        return loadFromDatabase(roomId);

//        //캐시 확인
//        List<ExecutableFlow> cached = flowCacheRepository.get(roomId);
//        if(cached != null){
//            log.info("cache hit roomId = {}", roomId);
//            return cached;
//        }
//
//        log.info("cache miss roomId = {}, DB 조회", roomId);
//        List<ExecutableFlow> executableFlows  =  loadFromDatabase(roomId);
//
//        //활성 플로우 없으면 바로 반환(캐시저장 x)
//        if(executableFlows.isEmpty()){
//            log.info("활성 플로우 없음 roomId={}", roomId);
//            return executableFlows;
//        }
//
//        //redis 저장
//        try{
//            flowCacheRepository.set(roomId,executableFlows);
//        }catch(Exception e){
//            log.warn("캐시 저장 실패 roomId={} error={}", roomId, e.getMessage());
//        }
//        return executableFlows;
    }
    private List<ExecutableFlow> loadFromDatabase(Long roomId){
        List<Flow> flows = flowRepository.findAllByRoomIdAndIsActiveTrueAndIsTemplateFalse(roomId);
        if (flows.isEmpty()) {
            return Collections.emptyList();
        }

        //플로우 ID 목록 추출
        List<Long> flowIds = flows.stream()
                .map(Flow :: getId)
                .toList();

        //연관 데이터 한번에 조회
        List<Node> allNodes = nodeRepository.findAllByFlowIdIn(flowIds);
        List<Connection> allConnections = connectionRepository.findAllByFlowIdIn(flowIds);
        List<FlowSchedule> allSchedules = flowScheduleRepository.findAllByFlowIdIn(flowIds);

        //플로우 그루핑
        Map<Long, List<Node>> nodesByFlowId = groupByFlowId(allNodes, node -> node.getFlow().getId());
        Map<Long, List<Connection>> connectionsByFlowId = groupByFlowId(allConnections, conn -> conn.getFlow().getId());
        Map<Long, List<FlowSchedule>> schedulesByFlowId = groupByFlowId(allSchedules, fs-> fs.getFlow().getId());

        //플로우별 ExecutableFlow 조립
        return flows.stream()
                .map(flow ->
                    buildSafely(
                            flow,
                            nodesByFlowId.getOrDefault(flow.getId(), Collections.emptyList()),
                            connectionsByFlowId.getOrDefault(flow.getId(), Collections.emptyList()),
                            schedulesByFlowId.getOrDefault(flow.getId(), Collections.emptyList())
                    )
                ).filter(Objects::nonNull)
                .toList();
    }
    private ExecutableFlow buildSafely(
            Flow flow,
            List<Node> nodes,
            List<Connection> connections,
            List<FlowSchedule> schedules
    ){
        try{
            return flowGraphBuilder.build(flow, nodes, connections, schedules);
        }catch (Exception e){
            log.error("플로우 그래프 조립 실패 flowId = {} errors={}", flow.getId(), e.getMessage());
            return null;
        }
    }

    private <T> Map<Long, List<T>> groupByFlowId(List<T> list, Function<T, Long> flowIdExtractor) {
        return list.stream()
                .collect(Collectors.groupingBy(flowIdExtractor));
    }
}
