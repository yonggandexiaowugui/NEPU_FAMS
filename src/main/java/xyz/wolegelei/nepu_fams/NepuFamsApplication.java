package xyz.wolegelei.nepu_fams;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableAspectJAutoProxy(exposeProxy = true)
@EnableAsync
@EnableTransactionManagement
public class NepuFamsApplication {

    public static void main(String[] args) {
        SpringApplication.run(NepuFamsApplication.class, args);
    }

}
