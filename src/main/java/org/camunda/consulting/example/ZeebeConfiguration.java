package org.camunda.consulting.example;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.camunda.zeebe.client.api.worker.JobWorker;
import java.util.Optional;
import org.camunda.consulting.example.services.CamundaPlatformService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ZeebeConfiguration {

  protected static Logger LOG = LoggerFactory.getLogger(ZeebeConfiguration.class);

  private CamundaPlatformService camundaPlatformService;

  @Value("${zeebe.client.cloud.clusterId}")
  private String clusterId;
  @Value("${zeebe.client.cloud.clientId}")
  private String clientId;
  @Value("${zeebe.client.cloud.clientSecret}")
  private String clientSecret;
  @Value("${zeebe.client.cloud.region}")
  private String region;

  public ZeebeConfiguration(CamundaPlatformService camundaPlatformService) {
    this.camundaPlatformService = camundaPlatformService;
  }

  @Bean
  public ZeebeClient zeebeClient() {
    return ZeebeClient.newCloudClientBuilder()
        .withClusterId(clusterId)
        .withClientId(clientId)
        .withClientSecret(clientSecret)
        .withRegion(region)
        .build();
  }

  @Bean
  public JobWorker jobWorkerC7Starter(ZeebeClient zeebeClient) {
    JobWorker jobWorker = zeebeClient.newWorker()
        .jobType("start-c7-instance")
        .handler(startC7Instance())
        .open();
    LOG.info("Worker started and receiving jobs");
    return jobWorker;
  }

  @Bean
  public JobWorker jobWorkerC7Message(ZeebeClient zeebeClient) {
    JobWorker jobWorker = zeebeClient.newWorker()
        .jobType("message-to-c7")
        .handler(messageToC7())
        .open();
    LOG.info("Worker started and receiving jobs");
    return jobWorker;
  }

  @Bean
  public JobWorker jobWorkerC7Signal(ZeebeClient zeebeClient) {
    JobWorker jobWorker = zeebeClient.newWorker()
        .jobType("signal-to-c7")
        .handler(signalToC7())
        .open();
    LOG.info("Worker started and receiving jobs");
    return jobWorker;
  }

  public JobHandler startC7Instance() {
    return (jobClient, job) -> {
      try {
        String bpmnProcessId = job.getVariablesAsMap().get("bpmnProcessId").toString();
        Optional<Object> payload = Optional.ofNullable(job.getVariablesAsMap().get("payload").toString());
        Optional<Integer> version = Optional.ofNullable(job.getVariablesAsMap().get("version") == null ? null : Integer.parseInt(job.getVariablesAsMap().get("version").toString()));
        camundaPlatformService.startInstance(bpmnProcessId, payload, version);
        LOG.info("Started instance for processId: " + bpmnProcessId);
        jobClient.newCompleteCommand(job.getKey()).send().join();
      } catch (Exception e) {
        LOG.error("Error starting instance", e);
        jobClient.newFailCommand(job.getKey()).retries(job.getRetries() - 1).errorMessage(e.getMessage()).send().join();
      }
    };
  }

  public JobHandler messageToC7() {
    return (jobClient, job) -> {
      try {
        String messageName = job.getVariablesAsMap().get("messageName").toString();
        String correlationKey = job.getVariablesAsMap().get("correlationKey").toString();
        Optional<Object> payload = Optional.ofNullable(job.getVariablesAsMap().get("payload").toString());
        camundaPlatformService.sendMessage(messageName, correlationKey, payload);
        LOG.info("Sent message: " + messageName + " with correlationKey: " + correlationKey);
        jobClient.newCompleteCommand(job.getKey()).send().join();
      } catch (Exception e) {
        LOG.error("Error sending message", e);
        jobClient.newFailCommand(job.getKey()).retries(job.getRetries() - 1).errorMessage(e.getMessage()).send().join();
      }
    };
  }

  public JobHandler signalToC7() {
    return (jobClient, job) -> {
      try {
        String signalName = job.getVariablesAsMap().get("signalName").toString();
        Optional<Object> payload = Optional.ofNullable(job.getVariablesAsMap().get("payload").toString());
        camundaPlatformService.sendSignal(signalName, payload);
        LOG.info("Sent signal: " + signalName + " with payload: " + payload);
        jobClient.newCompleteCommand(job.getKey()).send().join();
      } catch (Exception e) {
        LOG.error("Error sending signal", e);
        jobClient.newFailCommand(job.getKey()).retries(job.getRetries() - 1).errorMessage(e.getMessage()).send().join();
      }
    };
  }




}
