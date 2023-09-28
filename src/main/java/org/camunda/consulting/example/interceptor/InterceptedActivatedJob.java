package org.camunda.consulting.example.interceptor;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class InterceptedActivatedJob {

  private long key;
  private String type;
  private String customHeaders;
  private long processInstanceKey;
  private String bpmnProcessId;
  private int processDefinitionVersion;
  private long processDefinitionKey;
  private String elementId;
  private long elementInstanceKey;
  private String worker;
  private int retries;
  private long deadline;
  private String variables;
}
