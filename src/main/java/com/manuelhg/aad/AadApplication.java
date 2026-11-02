package com.manuelhg.aad;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class AadApplication {

    public static void main(String[] args) {
        SpringApplication.run(AadApplication.class, args);
    }
    //SpringApplication.run es el motor de t0do
    //Con @ComponentScan recorre com.manuelhg.aad y t0do lo marcado con @Component, @Service...
    //Con @EnableAutoConfiguration, configura autom. las librerías en pom.xml
    //Con @Configuration marca la clase como fuente de config. de Spring
}
