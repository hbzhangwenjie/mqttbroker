package com.zwj.mqtt;

import java.util.concurrent.CountDownLatch;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

/**
 * @Author: zwj
 * @Date: 2019-11-18 10:56
 */
@SpringBootApplication
public class Bootstarp {

    @Bean(name = "daemonLatch")
    public CountDownLatch closeLatch() {
        return new CountDownLatch(1);
    }

    public static void main(String[] args) throws InterruptedException {
        ApplicationContext ctx = new SpringApplication(Bootstarp.class).run(args);
        CountDownLatch closeLatch = (CountDownLatch) ctx.getBean("daemonLatch");
        Runtime.getRuntime().addShutdownHook(new Thread(closeLatch::countDown));
        closeLatch.await();
    }

}
