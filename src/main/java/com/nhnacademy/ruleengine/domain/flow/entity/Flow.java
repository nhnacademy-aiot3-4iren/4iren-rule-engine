package com.nhnacademy.ruleengine.domain.flow.entity;

import com.nhnacademy.ruleengine.domain.flowschedule.entity.FlowSchedule;
import com.nhnacademy.ruleengine.domain.templateflow.entity.FlowTemplateSensorType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "flows")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Flow {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "flow_id")
    private Long id;

    @Column(name = "room_id")
    private Long roomId;

    @Column(name = "flow_name", nullable = false, length = 50)
    private String flowName;

    @Column(name = "description")
    private String description;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "is_template", nullable = false)
    private Boolean isTemplate;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "flow", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Node> nodes = new ArrayList<>();

    @OneToMany(mappedBy = "flow", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Connection> connections = new ArrayList<>();

    @OneToMany(mappedBy = "flow", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<FlowSchedule> schedules = new ArrayList<>();

    @OneToMany(mappedBy = "flow", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<FlowTemplateSensorType> flowTemplateSensorTypes = new ArrayList<>();


    //일반 플로우 생성용
    @Builder(builderMethodName = "regularBuilder")
    public Flow(Long roomId, String flowName, String description){
        this.id = null;
        this.createdAt = null;
        this.updatedAt = null;

        this.roomId =roomId;
        this.isActive = true;

        this.flowName = flowName;
        this.description = description;

        this.isTemplate = false;
    }

    //템플릿 플로우 생성용
    //템플릿일경우  room_id, is_active null
    @Builder(builderMethodName = "templateBuilder")
    public Flow( String flowName, String description){
        this.id = null;
        this.createdAt = null;
        this.updatedAt = null;

        this.roomId = null;
        this.isActive = null;

        this.flowName = flowName;
        this.description = description;

        this.isTemplate = true;
    }

    public void update(String flowName, String description, Boolean isActive) {
        if (flowName != null) this.flowName = flowName;
        if (description != null) this.description = description;
        if (isActive != null) this.isActive = isActive;
    }
}