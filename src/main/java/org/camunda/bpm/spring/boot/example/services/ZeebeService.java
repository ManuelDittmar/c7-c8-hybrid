package org.camunda.bpm.spring.boot.example.services;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.command.CreateProcessInstanceCommandStep1.CreateProcessInstanceCommandStep2;
import io.camunda.zeebe.client.api.command.CreateProcessInstanceCommandStep1.CreateProcessInstanceCommandStep3;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.client.api.response.PublishMessageResponse;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class ZeebeService {

  private ZeebeClient zeebeClient;

  public ZeebeService(ZeebeClient zeebeClient) {
    this.zeebeClient = zeebeClient;
  }

  public ProcessInstanceEvent startInstance(String bpmnProcessId, Optional<Object> payload, Optional<Integer> version) {

    CreateProcessInstanceCommandStep2 step1 = zeebeClient.newCreateInstanceCommand().bpmnProcessId(bpmnProcessId);
    CreateProcessInstanceCommandStep3 step2;
    if(version.isPresent()) {
      step2 = step1.version(version.get());
    } else {
      step2 = step1.latestVersion();
    }
    if(payload.isPresent()) {
      return step2.variables(Map.of("payload",payload.get())).send().join();
    } else {
      return step2.send().join();
    }
  }
  public PublishMessageResponse sendMessage(String messageName,String correlationKey, Object payload) {
    return zeebeClient.newPublishMessageCommand()
        .messageName(messageName)
        .correlationKey(correlationKey)
        .variables(payload)
        .send()
        .join();
  }

}
