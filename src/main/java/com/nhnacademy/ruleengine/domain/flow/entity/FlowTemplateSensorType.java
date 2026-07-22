package com.nhnacademy.ruleengine.domain.flow.entity;


import com.nhnacademy.ruleengine.domain.flow.enums.SensorType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "flow_template_sensor_types")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class FlowTemplateSensorType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "required_sensor_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "flow_id", nullable = false)
    private Flow flow;

    @Enumerated(EnumType.STRING)
    @Column(name = "sensor_type", nullable = false, length = 20)
    private SensorType sensorType;


    public FlowTemplateSensorType(Flow flow, SensorType sensorType) {
        this.flow = flow;
        this.sensorType = sensorType;
    }
}