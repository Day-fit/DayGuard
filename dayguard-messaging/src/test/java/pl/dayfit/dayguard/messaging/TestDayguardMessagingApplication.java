package pl.dayfit.dayguard.messaging;

import org.springframework.boot.SpringApplication;

public class TestDayguardMessagingApplication {

    public static void main(String[] args) {
        SpringApplication.from(DayguardMessagingApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
