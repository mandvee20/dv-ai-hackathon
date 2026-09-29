package com.digivalet.agent.repository;

import com.digivalet.agent.entity.FailureAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FailureAnalysisRepository
         extends JpaRepository<FailureAnalysis, Long>
{
}
