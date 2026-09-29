package com.precheckin.simulator.service;


import com.precheckin.simulator.dto.PreCheckinRequest;
import com.precheckin.simulator.entity.PreCheckin;

public interface PreCheckinService
{
    PreCheckin createPreCheckin(PreCheckinRequest request);
}
