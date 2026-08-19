package com.nhnacademy.ruleengine.engine.executor.runtimestate;

import com.nhnacademy.ruleengine.domain.flow.enums.BranchType;
import org.aspectj.weaver.ast.Or;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrRuntimeStateTest {

    @BeforeEach
    void setUp() {
    }


    @Test
    @DisplayName("초기 상태는 모든 입력이 pending이어야 함.")
    void init_state_all_pending() {
        LogicalInputKey a = new LogicalInputKey(1L, BranchType.TRUE, 10L);
        LogicalInputKey b = new LogicalInputKey(2L, BranchType.TRUE, 10L);

        OrRuntimeState state = new OrRuntimeState(100L, List.of(a, b));

        assertFalse(state.isReady());
        assertFalse(state.isSatisfied());
    }

    @Test
    @DisplayName("입력 하나가 arrived상태로 바뀌었더라도 pending상태인 입력이 하나라도 있으면 ready상태가 못 됨")
    void one_arrived_one_pendding_not_ready(){
        LogicalInputKey a = new LogicalInputKey(1L, BranchType.TRUE, 10L);
        LogicalInputKey b = new LogicalInputKey(2L, BranchType.TRUE, 10L);

        OrRuntimeState state = new OrRuntimeState(100L, List.of(a, b));

        state.markArrived(a, List.of());

        assertFalse(state.isReady());
        assertTrue(state.isSatisfied());
    }

    @Test
    @DisplayName("두개의 입력중 한개가 arrived, 한개가 blocke이면 isReady, isSatisfied 상태임")
    void oneArrived_oneBlocked_isReadyNIsSatisfied () {
        LogicalInputKey a = new LogicalInputKey(1L, BranchType.TRUE, 10L);
        LogicalInputKey b = new LogicalInputKey(2L, BranchType.TRUE, 10L);

        OrRuntimeState state = new OrRuntimeState(100L, List.of(a, b));

        state.markArrived(a, List.of());
        state.markBlocked(b);

        assertTrue(state.isReady());
        assertTrue(state.isSatisfied());

    }

    @Test
    @DisplayName("모든 입력이 blocked상태일 경우 isReady상태이고 isSatisfied는 false이다")
    void allBlocked_isReadey_notSatisfied() {
        LogicalInputKey a = new LogicalInputKey(1L, BranchType.TRUE, 10L);
        LogicalInputKey b = new LogicalInputKey(2L, BranchType.TRUE, 10L);

        OrRuntimeState state = new OrRuntimeState(100L, List.of(a, b));

        state.markBlocked(a);
        state.markBlocked(b);

        assertTrue(state.isReady());
        assertFalse(state.isSatisfied());

    }
}