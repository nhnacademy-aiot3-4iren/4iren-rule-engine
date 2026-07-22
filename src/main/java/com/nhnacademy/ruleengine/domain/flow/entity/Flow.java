package com.nhnacademy.ruleengine.domain.flow.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

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

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "is_template", nullable = false)
    private Boolean isTemplate;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;


    public Flow(Long roomId, String flowName, String description, Boolean isActive, Boolean isTemplate) {
        this.roomId = roomId;
        this.flowName = flowName;
        this.description = description;
        this.isActive = isActive;
        this.isTemplate = isTemplate;
    }


    public void update(String flowName, String description, Boolean isActive) {
        if (flowName != null) this.flowName = flowName;
        if (description != null) this.description = description;
        if (isActive != null) this.isActive = isActive;
    }
}