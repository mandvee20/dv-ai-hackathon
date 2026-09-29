package com.digivalet.agent.config;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * @author Mandvee Vatsa
 * @date Sep 29, 2026
 */
@Component
public class RoomRegistry {

    private final Map<String, RoomConfig> rooms;

    public RoomRegistry(SimulatorConfigLoader configLoader) {

        this.rooms = configLoader.getConfig()
                .getRooms()
                .stream()
                .collect(Collectors.toMap(
                        RoomConfig::getRoomId,
                        Function.identity()
                ));
    }

    public RoomConfig getRoom(String roomId) {
        return rooms.get(roomId);
    }

    public Map<String, RoomConfig> getRooms() {
        return rooms;
    }
    public Map<String, DeviceConfig> getDeviceMap() {

       return devices.stream()
               .collect(Collectors.toMap(
                       DeviceConfig::getDeviceId,
                       Function.identity()
               ));
   }
}