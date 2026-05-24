package com.ecommerce.orderservice.repository;

import com.ecommerce.orderservice.model.OutboxEvents;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface OutboxEventsRepository extends JpaRepository<OutboxEvents, Long> {
        List<OutboxEvents> findByProcessedFalse();
}
