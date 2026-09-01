package com.nhnacademy.ruleengine.common.interceptor;

import com.nhnacademy.ruleengine.common.external.dto.RoomManagementAccessResponse;
import com.nhnacademy.ruleengine.common.external.service.RoomManagementCacheService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class RoomManagementAuthInterceptor implements HandlerInterceptor {
    private final ObjectProvider<RoomManagementCacheService> roomManagementCacheServiceProvider;
    private static final String X_USER_ID = "X-User-Id";
    private static final String X_USER_ROLE = "X-User-Role";

    //ADMIN 이상의 권한 정의(OWNER,ADMIN)
    private static final Set<String> PRIVILEGED_ROLES = Set.of("OWNER", "ADMIN");

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String userIdStr = request.getHeader(X_USER_ID);
        String userRole = request.getHeader(X_USER_ROLE);

        //필수 헤더 누락 검증
        if(userIdStr == null || userRole == null || userIdStr.isEmpty() || userRole.isEmpty()){
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "인증 헤더가 누락되었습니다.");
            return false;
        }

        // X-User-Role 권한 기준 검증 (ADMIN 이상인 OWNER, ADMIN만 통과)
        if (!PRIVILEGED_ROLES.contains(userRole)) {
            response.sendError(HttpStatus.FORBIDDEN.value(), "접근 권한이 없습니다. (ADMIN 이상 필요)");
            return false;
        }

        //현재 요청 URL 에서 room-id 추출
        Map<String, String> pathVariables = ( Map<String, String>)request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (pathVariables == null || !pathVariables.containsKey("room-id")) {
            response.sendError(HttpStatus.BAD_REQUEST.value(), "요청 경로에 roomId가 존재하지 않습니다.");
            return false;
        }

    try {

        Long roomId = Long.parseLong(pathVariables.get("room-id"));
        Long userId = Long.parseLong(userIdStr);

        //외부 API 호출
        RoomManagementAccessResponse apiResponse = roomManagementCacheServiceProvider.getObject().getManagementAllowed(roomId, userId);

            if (apiResponse != null && apiResponse.allowed()) {
                return true;//최종 검증 성공 -> 컨트롤러로 진입 허용
            } else {
                response.sendError(HttpStatus.FORBIDDEN.value(), "해당 강의실의 관리팀 멤버가 아닙니다");
                return false;
            }
        } catch (NumberFormatException e) {
            response.sendError(HttpStatus.BAD_REQUEST.value(), "잘못된 형식의 ID 값입니다.");
            return false;
        } catch (Exception e) {
            // 외부 서버 통신 실패 시 에러 처리 (Fail-Close)
            response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "관리팀 정보 검증 중 서버 오류가 발생했습니다.");
            return false;
        }

    }
}
