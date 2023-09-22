package org.camunda.bpm.spring.boot.example;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.camunda.zeebe.client.api.worker.JobWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ZeebeConfiguration {

  protected static Logger LOG = LoggerFactory.getLogger(ZeebeConfiguration.class);

  @Value("${zeebe.client.cloud.clusterId}")
  private String clusterId;
  @Value("${zeebe.client.cloud.clientId}")
  private String clientId;
  @Value("${zeebe.client.cloud.clientSecret}")
  private String clientSecret;
  @Value("${zeebe.client.cloud.region}")
  private String region;

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
  public JobWorker jobWorker(ZeebeClient zeebeClient) {
    LOG.info("Starting worker");
    return zeebeClient.newWorker()
        .jobType("payment-service")
        .handler(jobHandler())
        .open();
  }

  public JobHandler jobHandler() {
    return (jobClient, job) -> {
      LOG.info("Processing job {}", job);
      jobClient.newCompleteCommand(job.getKey())
          .send()
          .join();
    };
  }

}
