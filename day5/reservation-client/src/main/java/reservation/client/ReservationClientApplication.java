package reservation.client;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.Bean;

@EnableDiscoveryClient
@SpringBootApplication
@EnableZuulProxy
public class ReservationClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReservationClientApplication.class, args);
    }

    @Bean
    CommandLineRunner runner(DiscoveryClient dc) {
        return args ->
                dc.getInstances("reservation-service")
                        .forEach(si -> System.out.println(String.format(
                                "Eureka Client 정보 - serviceId:%s, host:%s, port:%s",
                                si.getServiceId(), si.getHost(), si.getPort())
                        ));
    }
}

class Reservation {

    private Long id;
    private String reservationName;

    public Long getId() {
        return id;
    }

    public String getReservationName() {
        return reservationName;
    }
}