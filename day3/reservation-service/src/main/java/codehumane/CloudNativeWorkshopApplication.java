package codehumane;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteReporter;
import com.vaadin.annotations.Theme;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.*;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class CloudNativeWorkshopApplication {

    @Bean
    HealthIndicator healthIndicator() {
        return () -> Health.status("health.status.custom").build();
    }

    @Bean
    GraphiteReporter graphiteReporter(
            MetricRegistry registry,
            @Value("${graphite.host}") String host,
            @Value("${graphite.port}") int port) {

        GraphiteReporter reporter = GraphiteReporter.forRegistry(registry)
                .prefixedWith("reservations")
                .build(new Graphite(host, port));
        reporter.start(2, TimeUnit.SECONDS);
        return reporter;
    }

	@Bean
	CommandLineRunner runner(ReservationRepository rr) {
		return args ->
				Arrays.asList("Marten,Josh,Dave,Mark,Mark,Juergen".split(","))
						.forEach(x -> rr.save(new Reservation(x)));

	}

	public static void main(String[] args) {
		SpringApplication.run(CloudNativeWorkshopApplication.class, args);
	}

    @Component
    @RepositoryEventHandler
    public static class ReservationEventHandler {

        private final CounterService counterService;

        public ReservationEventHandler(CounterService counterService) {
            this.counterService = counterService;
        }

        @HandleAfterCreate
        public void create(Reservation p) {
            count("reservations.create", p);
        }

        @HandleAfterSave
        public void save(Reservation p) {
            count("reservations.save", p);
            count("reservations." + p.getId() + ".save", p);
        }

        @HandleAfterDelete
        public void delete(Reservation p) {
            count("reservations.delete", p);
        }

        private void count(String evt, Reservation p) {
            this.counterService.increment(evt);
            this.counterService.increment("meter." + evt);
        }
    }
}

@SpringUI(path = "ui")
@Theme("valo")
class ReservationUI extends UI {

    private final ReservationRepository reservationRepository;

    public ReservationUI(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        Table components = new Table();
        components.setContainerDataSource(new BeanItemContainer<>(
                Reservation.class,
                this.reservationRepository.findAll()
        ));
        components.setSizeFull();
        setContent(components);
    }

}

@RefreshScope
@RestController
class MessageRestController {

    @Value("${message}")
    private String message;

    @RequestMapping("/message")
    String getMessage() {
        return this.message;
    }
}

@Component
class ReservationResourceProcessor implements ResourceProcessor<Resource<Reservation>> {

    @Override
    public Resource<Reservation> process(Resource<Reservation> reservationResource) {
        Reservation reservation = reservationResource.getContent();
        Long id = reservation.getId();
        String url = "http://aws.images.com/" + id + ".jpg";
        reservationResource.add(new Link(url, "profile-photo"));
        return reservationResource;
    }
}

@RepositoryRestResource
interface ReservationRepository extends JpaRepository<Reservation, Long> {

	@RestResource(path = "by-name")
	Collection<Reservation> findByReservationName(@Param("rn") String rn);
}
