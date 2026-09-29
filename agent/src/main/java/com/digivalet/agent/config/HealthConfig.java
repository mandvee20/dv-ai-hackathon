package com.digivalet.agent.config;
/**
 * @author Mandvee Vatsa
 * @date Sep 29, 2026
 */
public class HealthConfig
{

   private int threadCount;
   private String memoryUsage;
   private boolean memoryLeak;

   public int getThreadCount() {
       return threadCount;
   }

   public void setThreadCount(int threadCount) {
       this.threadCount = threadCount;
   }

   public String getMemoryUsage() {
       return memoryUsage;
   }

   public void setMemoryUsage(String memoryUsage) {
       this.memoryUsage = memoryUsage;
   }

   public boolean isMemoryLeak() {
       return memoryLeak;
   }

   public void setMemoryLeak(boolean memoryLeak) {
       this.memoryLeak = memoryLeak;
   }
}
