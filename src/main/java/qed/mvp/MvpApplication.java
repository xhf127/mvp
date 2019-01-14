package qed.mvp;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class MvpApplication {

    public static void main(String[] args) {
        SpringApplicationBuilder build = new SpringApplicationBuilder(MvpApplication.class);
        build.headless(false).run(args);
    }

}
