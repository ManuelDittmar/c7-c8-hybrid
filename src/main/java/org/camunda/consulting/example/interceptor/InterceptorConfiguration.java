package org.camunda.consulting.example.interceptor;

import io.grpc.ClientInterceptor;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InterceptorConfiguration {

  @Bean
  public List<ClientInterceptor>  clientInterceptors() {
    return List.of(new OptimizeEventInterceptor());
  }

}
