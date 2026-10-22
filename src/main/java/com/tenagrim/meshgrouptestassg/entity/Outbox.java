package com.tenagrim.meshgrouptestassg.entity;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "outbox_notification")
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class Outbox {
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "aggregatetype", nullable = false, length = 255)
    private String aggregatetype;

    @Column(name = "aggregateid", nullable = false, length = 255)
    private String aggregateid;

    @Column(name = "type", nullable = false, length = 255)
    private String type;

    @Type(type = "jsonb")
    @Column(name = "payload", nullable = false, columnDefinition = "jsonb")
    private String payload;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getAggregatetype() { return aggregatetype; }
    public void setAggregatetype(String aggregatetype) { this.aggregatetype = aggregatetype; }

    public String getAggregateid() { return aggregateid; }
    public void setAggregateid(String aggregateid) { this.aggregateid = aggregateid; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
