# Edge Services: API gateways (circuit breakers, client-side load balancing)

Josh Long의  '[Cloud Native Java Workshop](https://github.com/joshlong/cloud-native-workshop#2-making-a-spring-boot-application-production-ready)' 중 5일차인 '[Edge Services: API gateways](https://github.com/joshlong/cloud-native-workshop#5-edge-services-api-gateways-circuit-breakers-client-side-load-balancing)' 과정을 따라해 보았다. 간단히 기억할 만한 것들을 문서로 함께 기록함.

# 전체 절차

- reservation-client에 `@EnableZullProxy` 추가
- 프록시 주소를 통한 reservation-client 접근
- 서비스로부터의 데이터를 담기 위한 클라이언트 사이드 DTO 작성
- hateoas 의존성 추가
- `@LoadBalanced`를 통한 서비스 호출 로드밸런싱
- 컨트롤러를 `/reservations`에 매핑하고 `getReservationNames' 메소드 추가한 후 `/names`에 매핑
- actuator 및 hystrix 의존성 추가
- `@EnableCircuitBreaker` 선언
- `@HystrixCommand` 선언하여 폴백 메소드 명시
- reservation-service 종료시킨 후 `/reservations/names` 접근하여 폴백 여부 확인
- actifactId를 `hystrix-dashboard`로 하여 새로운 서비스 생성
- `bootstrap.properties`에서 식별자를 `hystrix-dashboard`로 명시하고 config server에서 가리키도록 함
- `@EnableHystrixDashboard` 선언 후 실행

# reservation-client에 `@EnableZullProxy` 추가

> Add org.springframework.cloud:spring-cloud-starter-zuul and @EnableZuulProxy to the reservation-client, then run it.

# 프록시 주소를 통한 reservation-client 접근

> Launch a browser and visit the reservation-client at http://localhost:9999/reservation-service/reservations. This is proxying your request to http://localhost:8000/reservations.

# 서비스로부터의 데이터를 담기 위한 클라이언트 사이드 DTO 작성

> In the reservation-client, create a client side DTO - named Reservation, perhaps? - to hold the Reservation data from the service. Do this to avoid being coupled between client and service

# hateoas 의존성 추가

> Add org.springframework.boot:spring-boot-starter-hateoas

# `@LoadBalanced`를 통한 서비스 호출 로드밸런싱

> Add a REST service called ReservationApiGatewayRestController that uses the @Autowired @LoadBalanced RestTemplate rt to make a load-balanced call to a service in the registry using Ribbon.

# 컨트롤러를 `/reservations`에 매핑하고 `getReservationNames' 메소드 추가한 후 `/names`에 매핑

> Map the controller itself to /reservations and then create a new controller handler method, getReservationNames, that's mapped to /names.
> In the getReservationNames handler, make a call to http://reservation-service/reservations using the RestTemplate#exchange method, specifying the return value with a ParameterizedTypeReference<Resources<Reservation>>> as the final argument to the RestTemplate#exchange method.
> Take the results of the call and map them from Reservation to Reservation#getReservationName. Then, confirm that http://localhost:9999/reservations/names returns the names.

# actuator 및 hystrix 의존성 추가

> Add org.springframework.boot:spring-boot-starter-actuator and org.springframework.cloud:spring-cloud-starter-hystrix to the reservation-client

# `@EnableCircuitBreaker` 선언

> Add @EnableCircuitBreaker to our DemoApplication configuration class

# `@HystrixCommand` 선언하여 폴백 메소드 명시

> Add @HystrixCommand around any potentially shaky service-to-service calls, like getReservationNames, specifying a fallback method that returns an empty collection.

# reservation-service 종료시킨 후 `/reservations/names` 접근하여 폴백 여부 확인

> Test that everything works by killing the reservation-service and revisiting the /reservations/names endpoint

# actifactId를 `hystrix-dashboard`로 하여 새로운 서비스 생성

> Go to the Spring Initializr and stand up a new service - with an artifactId of hystrix-dashboard - that uses Eureka Discovery, Config Client, and the Hystrix Dashboard.

# `bootstrap.properties`에서 식별자를 `hystrix-dashboard`로 명시하고 config server에서 가리키도록 함

> Identify it is as hystrix-dashboard in bootstrap.properties and point it to config server.

# `@EnableHystrixDashboard` 선언 후 실행

> Annotate it with @EnableHystrixDashboard and run it. You should be able to load it at http://localhost:8010/hystrix.html. It will expect a heartbeat stream from any of the services with a circuit breaker in them. Give it the address from the reservation-client: http://localhost:9999/hystrix.stream