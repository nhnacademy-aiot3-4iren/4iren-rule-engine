package com.nhnacademy.ruleengine.engine.executor.node;

import com.nhnacademy.ruleengine.engine.executor.ExecutionPath;

public record NodeExecutionResult (
        boolean passed,
        ExecutionPath path
) {
    public static NodeExecutionResult of(boolean passed, ExecutionPath path) {
        return new NodeExecutionResult(passed, path);
    }
}
