package com.order.orderapi.entity.suberclass;

import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Generated;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.sql.Timestamp;


@Setter
@Getter
public class AuditableEntity extends BaseEntity {

    @CreatedDate
    @Generated
    @Column(name = "created_at", nullable = false)
    private Timestamp creationDatetime;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Timestamp lastModDatetime;

}
