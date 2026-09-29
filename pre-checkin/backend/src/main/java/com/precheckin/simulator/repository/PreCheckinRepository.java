package com.precheckin.simulator.repository;

import com.precheckin.simulator.entity.PreCheckin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PreCheckinRepository
        extends JpaRepository<PreCheckin, Long>
{
}
