package com.digivalet.agent.config;
/**
 * @author Mandvee Vatsa
 * @date Sep 29, 2026
 */
public class DvcConfig
{
   private String ip;
   private int port;
   private HealthConfig health;

   public String getIp() {
       return ip;
   }

   public void setIp(String ip) {
       this.ip = ip;
   }

   public int getPort() {
       return port;
   }

   public void setPort(int port) {
       this.port = port;
   }

   public HealthConfig getHealth() {
       return health;
   }

   public void setHealth(HealthConfig health) {
       this.health = health;
   }
}
