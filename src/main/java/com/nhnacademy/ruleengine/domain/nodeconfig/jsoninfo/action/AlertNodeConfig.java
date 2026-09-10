package com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.action;

import com.nhnacademy.ruleengine.domain.nodeconfig.enums.AlertType;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.NodeType;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.NodeConfig;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "알림 노드 설정")
public record AlertNodeConfig(

    @Schema(description = "노드 타입", example = "ALERT")
    @NotNull
    NodeType nodeType,

    @Schema(description = "플로우 편집 화면의 x 좌표", example = "700")
    @NotNull
    Integer x,

    @Schema(description = "플로우 편집 화면의 y 좌표", example = "120")
    @NotNull
    Integer y,

//    @NotNull
//    AlertChannel channel,

    @Schema(description = "알림 제목", example = "강의실 온도 경고")
    @NotBlank
    String alertTitle,

    @Schema(description = "알림 유형", example = "COMFORT_LIMIT_EXCEEDED")
    @NotNull
    AlertType alertType,

    @Schema(description = "중복 알림 방지 시간. 같은 실행 이력의 알림을 지정 초 동안 재발송하지 않는다.", example = "300")
    @NotNull
    @Positive
    Integer dedupWindowSec
) implements NodeConfig {

}
