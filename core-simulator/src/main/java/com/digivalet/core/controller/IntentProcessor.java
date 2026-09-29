package com.digivalet.core.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.digivalet.core.model.IntentRequest;
import com.digivalet.core.service.CoreIntentService;

@Component
public class IntentProcessor
{
   private static final Logger log =
         LoggerFactory.getLogger(IntentProcessor.class);

   private final CoreIntentService coreIntentService;

   public IntentProcessor(CoreIntentService coreIntentService)
   {
      this.coreIntentService = coreIntentService;
   }

   public void process(IntentRequest request)
   {
      log.info(
            "Processing intent requestId={} roomId={} intent={}",
            request.getRequestId(),
            request.getRoomId(),
            request.getIntent());

      coreIntentService.process(request);
   }
}
