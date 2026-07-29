package com.nhnacademy.ruleengine.domain.nodeconfig.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/")
public class NodeConfigController {

   /*
    플로우 구성 화면 내에서 (저장이든 편집이든) 노드의 상세설정을 누르면 NodeType에 맞는 nodeConfig설정 화면이 나올것
    1. 판단 노드인 경우
    sensorType를 반드시 포함하고 있으며. 강의실 내에 측정가능한 sensorType를 조회하는 api 필요함

    2. 행동 노드인 경우

    */
}
