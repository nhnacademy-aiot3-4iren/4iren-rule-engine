package com.nhnacademy.ruleengine.domain.flow.entity;


import com.nhnacademy.ruleengine.domain.nodeconfig.enums.MeasurementType;
import jakarta.persistence.*;
import lombok.*;

@Getter
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
    private MeasurementType measurementType;


    @Builder
    public FlowTemplateSensorType(Flow flow, MeasurementType measurementType) {
        this.flow = flow;
        this.measurementType = measurementType;
    }
}