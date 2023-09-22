package org.camunda.bpm.spring.boot.example;

import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.client.api.response.PublishMessageResponse;
import java.util.Optional;
import org.camunda.bpm.client.spring.SpringTopicSubscription;
import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.spring.boot.starter.ClientProperties;
import org.camunda.bpm.client.spring.event.SubscriptionInitializedEvent;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.camunda.bpm.spring.boot.example.services.ZeebeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
public class HandlerConfiguration {

  private ZeebeService zeebeService;

  protected static Logger LOG = LoggerFactory.getLogger(HandlerConfiguration.class);

  protected String workerId;

  public HandlerConfiguration(ClientProperties properties, ZeebeService zeebeService) {
    workerId = properties.getWorkerId();
    this.zeebeService = zeebeService;
  }

  @ExternalTaskSubscription("start-c8-instance")
  @Bean
  public ExternalTaskHandler startC8Instance() {
    return (externalTask, externalTaskService) -> {
      try {
        String bpmnProcessId = externalTask.getVariable("bpmnProcessId");
        Optional<Object> payload = Optional.ofNullable(externalTask.getVariable("payload"));
        Optional<Integer> version = Optional.ofNullable(externalTask.getVariable("version"));
        // Call Service
        ProcessInstanceEvent processInstanceEvent = zeebeService.startInstance(bpmnProcessId, payload, version);
        // Set Variables
        externalTaskService.complete(externalTask, Variables.putValue("processInstanceEvent", processInstanceEvent));
        LOG.info("Started instance for processId: " + bpmnProcessId + " with key: " + processInstanceEvent.getProcessInstanceKey());
      } catch (Exception e) {
        LOG.error("Error starting instance", e);
        externalTaskService.handleFailure(externalTask, workerId, e.getMessage(), 0, 0);
      }
    };
  }

  @ExternalTaskSubscription("message-to-c8")
  @Bean
  public ExternalTaskHandler messageToC8() {
    return (externalTask, externalTaskService) -> {
      String messageName = externalTask.getVariable("messageName");
      String correlationKey = externalTask.getVariable("correlationKey");
      Object payload = externalTask.getVariable("payload");
      try {
        // Call Service
        PublishMessageResponse messageResponse = zeebeService.sendMessage(messageName, correlationKey, payload);
        // Set Variables
        externalTaskService.complete(externalTask, Variables.putValue("messageResponse", messageResponse));
      } catch (Exception e) {
        LOG.error("Error sending message", e);
        externalTaskService.handleFailure(externalTask, workerId, e.getMessage(), 0, 0);
      }
    };
  }

}
