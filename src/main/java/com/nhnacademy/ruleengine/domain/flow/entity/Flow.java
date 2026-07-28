package com.nhnacademy.ruleengine.domain.flow.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Builder
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


    //템플릿일경우  room_id, is_active null

    public void update(String flowName, String description, Boolean isActive) {
        if (flowName != null) this.flowName = flowName;
        if (description != null) this.description = description;
        if (isActive != null) this.isActive = isActive;
    }
}