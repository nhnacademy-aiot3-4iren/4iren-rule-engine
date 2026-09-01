package com.nhnacademy.ruleengine.domain.nodeconfig.validator.impl;

import com.nhnacademy.ruleengine.domain.nodeconfig.enums.AlertChannel;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.AlertType;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.NodeType;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.action.AlertNodeConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class AlertNodeConfigValidatorTest {
    private final AlertNodeConfigValidator validator = new AlertNodeConfigValidator();

    @Test
    @DisplayName("지원 타입은 ALERT이다")
    void supportsNodeType() {
        assertThat(validator.supportsNodeType()).isEqualTo(NodeType.ALERT);
    }

    @Test
    @DisplayName("정상 설정인 경우 빈 에러 반환")
    void validate_success() {
        AlertNodeConfig config = new AlertNodeConfig(NodeType.ALERT, 0, 0, AlertChannel.TELEGRAM, "Title", AlertType.VENTILATION_RECOMMEND, 300);
        assertThat(validator.validate(config, List.of())).isEmpty();
    }

    @Test
    @DisplayName("채널 정보가 없으면 에러 반환")
    void validate_missingChannel() {
        AlertNodeConfig config = new AlertNodeConfig(NodeType.ALERT, 0, 0, null, "Title", AlertType.VENTILATION_RECOMMEND, 300);
        assertThat(validator.validate(config, List.of())).isNotEmpty();
    }

    @Test
    @DisplayName("알림 제목이 비어있으면 에러 반환")
    void validate_missingTitle() {
        AlertNodeConfig config = new AlertNodeConfig(NodeType.ALERT, 0, 0, AlertChannel.TELEGRAM, "   ", AlertType.VENTILATION_RECOMMEND, 300);
        assertThat(validator.validate(config, List.of())).isNotEmpty();
    }

    @Test
    @DisplayName("중복 감지 시간이 없으면 에러 반환")
    void validate_missingDedupWindowSec() {
        AlertNodeConfig config = new AlertNodeConfig(NodeType.ALERT, 0, 0, AlertChannel.TELEGRAM, "Title", AlertType.VENTILATION_RECOMMEND, null);
        assertThat(validator.validate(config, List.of())).contains("dedupWindowSec은 필수입니다");
    }
}
