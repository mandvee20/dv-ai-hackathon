package com.digivalet.agent.config;
/**
 * @author Mandvee Vatsa
 * @date Sep 29, 2026
 */
public class OperationConfig
{
   private String result;
   private String exception;
   private String message;
   private String description;

   public String getResult() {
       return result;
   }

   public void setResult(String result) {
       this.result = result;
   }

   public String getException() {
       return exception;
   }

   public void setException(String exception) {
       this.exception = exception;
   }

   public String getMessage() {
       return message;
   }

   public void setMessage(String message) {
       this.message = message;
   }

   public String getDescription() {
       return description;
   }

   public void setDescription(String description) {
       this.description = description;
   }
}
