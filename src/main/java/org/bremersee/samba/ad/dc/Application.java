package org.bremersee.samba.ad.dc;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * The application.
 */
@SpringBootApplication()
@EnableAspectJAutoProxy
@Slf4j
public class Application {

  /**
   * The entry point of the application.
   *
   * @param args the input arguments
   */
  public static void main(String[] args) {
    var env = System.getenv();
    for (var entry : env.entrySet()) {
      log.info("Environment variable: {}={}", entry.getKey(), entry.getValue());
    }

    SpringApplication.run(Application.class, args);
  }

}
