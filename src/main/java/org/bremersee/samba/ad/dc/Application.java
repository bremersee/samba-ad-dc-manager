package org.bremersee.samba.ad.dc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * The application.
 */
@SpringBootApplication()
@EnableAspectJAutoProxy
public class Application {

  /**
   * The entry point of the application.
   *
   * @param args the input arguments
   */
  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

}
