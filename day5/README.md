# Edge Services: API gateways (circuit breakers, client-side load balancing)

Josh Long의  '[Cloud Native Java Workshop](https://github.com/joshlong/cloud-native-workshop#2-making-a-spring-boot-application-production-ready)' 중 5일차인 '[Edge Services: API gateways](https://github.com/joshlong/cloud-native-workshop#5-edge-services-api-gateways-circuit-breakers-client-side-load-balancing)' 과정을 따라해 보았다. 간단히 기억할 만한 것들을 문서로 함께 기록함.

# 내용 요약

> Edge services sit as intermediaries between the clients (smart phones, HTML5 applications, etc) and the service. An edge service is a logical place to insert any client-specific requirements (security, API translation, protocol translation) and keep the mid-tier services free of this burdensome logic (as well as free from associated redeploys!)
>
> Proxy requests from an edge-service to mid-tier services with a microproxy. For some classes of clients, a microproxy and security (HTTPS, authentication) might be enough.

Edge 서비스는 클라이언트(스마트 폰, HTML5 응용 프로그램 등)와 서비스 간 중개자 역할을 수행함. Edge 서비스는 클라이언트의 특수한 요구 사항(보안, API 변환, 프로토콜 변환)을 수행하여, 미드 티어 서비스들에게 이러한 로직의 부담(관련된 재배포도 함께)을 덜어주는 논리적 공간임.

마이크로 프록시를 사용하여 Edge 서비스에서 중간 계층 서비스들에 대한 프록시 요청을 처리할 수 있음. 일부 클라이언트의 클래스는 microproxy 및 보안(HTTPS, 인증)으로 충분함.

# 전체 절차

- [x] reservation-client에 `@EnableZullProxy` 추가
- [x] 프록시 주소를 통한 reservation-client 접근
- [x] 서비스로부터의 데이터를 담기 위한 클라이언트 측 DTO 작성
- [x] hateoas 의존성 추가
- [x] `@LoadBalanced RestTemplate`을 사용한 컨트롤러 선언
- [x] `@LoadBalanced RestTemplate`을 사용한 서비스 호출
- [x] actuator 및 hystrix 의존성 추가
- [x] `@EnableCircuitBreaker` 선언
- [x] `@HystrixCommand` 선언하여 폴백 메소드 명시
- [x] `reservation-service` 종료하여 폴백 여부 확인
- [x] `hystrix-dashboard` 서비스 생성
- [x] config server 설정
- [x] `@EnableHystrixDashboard` 선언 후 실행

# 세부 절차

각각의 절차들을 하나씩 따라하며 관련된 내용 기록.

## `reservation-client`에 `@EnableZullProxy` 추가

> Add `org.springframework.cloud:spring-cloud-starter-zuul` and `@EnableZuulProxy` to the `reservation-client`, then run it.

- `reservation-client`에 `org.springframework.cloud:spring-cloud-starter-zuul` 의존성 추가
- `@EnableZuulProxy` 추가
- 이 애노테이션 추가의 의미는 아래 설명을 참고

> This will turn the Gateway application into a reverse proxy that forwards relevant calls to other services
>
> \- [spring.io Getting Started - Routing and Filtering using Netflix Zuul edge service library](https://spring.io/guides/gs/routing-and-filtering/)

- 즉, `@EnableZuulProxy`의 대상을 리버스 프록시(관련된 요청을 다른 서비스로 전달해 주는)로 만들어줌

## 프록시 주소를 통한 `reservation-client` 접근

> Launch a browser and visit the reservation-client at `http://localhost:9999/reservation-service/reservations`. This is proxying your request to `http://localhost:8000/reservations`.

- `http://localhost:9999/reservation-service/reservations` 접근
- `http://localhost:8081/reservations` 접근 모습과 비교 (가이드와 다르게 현재 실습에서는 8081 포트를 사용중)
- 첫 번째 경로의 요청은 두 번째 경로에 대한 프록시 결과이다.
- 프록시가 받는 요청을 어디로 보낼지 결정하는 방법으로 [설정 파일에 `zuul.routes.xxx`를 명시하기도](https://spring.io/guides/gs/routing-and-filtering/) 함
- 여기서는 eureka에 등록된 어플리케이션 아이디인 `reservation-service`를 URL path의 시작부에 명시하는 방법을 사용하고 있다.

## 서비스로부터의 데이터를 담기 위한 클라이언트 측 DTO 작성

> In the reservation-client, create a client side DTO - named Reservation, perhaps? - to hold the Reservation data from the service. Do this to avoid being coupled between client and service

- `Reservation`이라는 이름의 클라이언트 측 DTO 생성
- `reservation-service`로부터 받은 데이터를 담는 용도이며,
- 클라이언트와 서비스간의 의존성을 낮추기 위함

## hateoas 의존성 추가

> Add org.springframework.boot:spring-boot-starter-hateoas

- `build.gradle`에 `compile('org.springframework.boot:spring-boot-starter-hateoas')` 추가

## `@LoadBalanced RestTemplate`을 사용한 컨트롤러 선언

> Add a REST service called ReservationApiGatewayRestController that uses the @Autowired @LoadBalanced RestTemplate rt to make a load-balanced call to a service in the registry using Ribbon.

- `ReservationApiGatewayRestController` 이름의 REST 컨트롤러 생성
- 아래 코드와 같이 컨트롤러 안에서 사용할 `RestTemplate` Bean을 `@LoadBalanced`와 함께 선언

```java
@Bean
@LoadBalanced
public RestTemplate restTemplate() {
    return new RestTemplate();
}
```

- registry 안의 서비스 호출을 로드밸런싱하기 위함
- 내부적으로 Ribbon이 함께 사용됨
- 자세한 설명은 [Client Side Load Balancing with Ribbon and Spring Cloud](htㄴtps://spring.io/guides/gs/client-side-load-balancing/)에 잘 나와있음

## `@LoadBalanced RestTemplate`을 사용한 서비스 호출

> Map the controller itself to /reservations and then create a new controller handler method, getReservationNames, that's mapped to /names.
> In the getReservationNames handler, make a call to http://reservation-service/reservations using the RestTemplate#exchange method, specifying the return value with a ParameterizedTypeReference<Resources<Reservation>>> as the final argument to the RestTemplate#exchange method.
> Take the results of the call and map them from Reservation to Reservation#getReservationName. Then, confirm that http://localhost:9999/reservations/names returns the names.

- 컨트롤러를 `/reservations`에 매핑하고, `getReservationNames`라는 핸들러 메소드 추가후 `/names`에 매핑
- 핸들러 안에서는 `RestTemplate#exchange` 메소드를 사용하여 `http://reservation-service/reservations`를 호출
- 이 때 반환값을 `ParameterizedTypeReference<Resources<Reservation>>>`로 명시함
- 여기서 `Resources`는 `org.springframework.hateoas`의 `Resources`
- 반환값을 다시 `Reservation#getReservationName`로 매핑
- 코드는 아래와 같음

```java
@RequestMapping("/names")
public Collection<String> getReservationNames() {
    ParameterizedTypeReference<Resources<Reservation>> parameterizedTypeReference =
            new ParameterizedTypeReference<Resources<Reservation>>() {
            };

    ResponseEntity<Resources<Reservation>> exchange = restTemplate.exchange(
            "http://reservation-service:8081/reservations", HttpMethod.GET, null, parameterizedTypeReference);

    return exchange
            .getBody()
            .getContent()
            .stream()
            .map(Reservation::getReservationName)
            .collect(Collectors.toList());
}
```

- `http://localhost:9999/reservations/names` 접근하여 결과 확인
- 로그를 보면 `DynamicServerListLoadBalancer`이 사용되고 있음

## actuator 및 hystrix 의존성 추가

> Add org.springframework.boot:spring-boot-starter-actuator and org.springframework.cloud:spring-cloud-starter-hystrix to the reservation-client

- `reservation-client`의 `build.gradle`에서 `dependencies` 항목에 아래 2줄 추가

```gradle
compile('org.springframework.cloud:spring-cloud-starter-zuul')
compile('org.springframework.cloud:spring-cloud-starter-hystrix')
```

## `@EnableCircuitBreaker` 선언

> Add @EnableCircuitBreaker to our DemoApplication configuration class

- `ReservationClientApplication`에 `@EnableCircuitBreaker` 선언
- Spring Cloud에게 [CircuitBreaker](https://martinfowler.com/bliki/CircuitBreaker.html)를 사용함을 알려주는 작업
- 이렇게하면 Circuit Breaker를 위한 서비스 제공자<sup>supplier</sup>의 monitoring, opening, closing을 제공받을 수 있음
- 구체적인 동작 방식은 아래 소개될 `@HystrixCommand`를 통해 결정됨
- `@EnableCircuitBreaker`의 사용법은 [여기](https://spring.io/guides/gs/circuit-breaker/)에 잘 설명되어 있음
- Circuit Breaker 패턴에 대해 간단히 설명하면,
    - 네트워크를 통해 호출하는 공급자가 문제가 있어 타임아웃이 걸리면, 많은 리소스들이 의미 없이 낭비되곤 함
    - 이러한 catastrophical cascade를 막는 한가지 방법임

## `@HystrixCommand` 선언하여 폴백 메소드 명시

> Add @HystrixCommand around any potentially shaky service-to-service calls, like getReservationNames, specifying a fallback method that returns an empty collection.

- 서비스 대 서비스 호출이 불안정할 수 있는 곳, 여기서는 `getReservationNames` 메소드에 `@HystrixCommand`를 선언
- 이렇게하면 호출 실패시 `fallbackMethod`에 명시된 메소드가 실행됨
- 좀 더 자세한 사용법은 [여기](https://spring.io/guides/gs/circuit-breaker/)를 참고

## `reservation-service` 종료하여 폴백 여부 확인

> Test that everything works by killing the reservation-service and revisiting the /reservations/names endpoint

- `reservation-service`를 종료하고
- `localhost:9999/reservations/names` 접근해보면,
- `@HystrixCommand`의 fallback 메소드의 결과인 빈배열이 출력됨을 확인할 수 있음

## `hystrix-dashboard` 서비스 생성

> Go to the Spring Initializr and stand up a new service - with an artifactId of hystrix-dashboard - that uses Eureka Discovery, Config Client, and the Hystrix Dashboard.

- [Spring Initializr](https://start.spring.io/) 이동하여 아래 내용 입력 후 `Generate Project`
    - `Artifact`: `hystrix-dashboard`
    - `Dependencies`: `Eureka Discovery`, `Config Client`, `Hystrix Dashboard`
- `day5`의 하위 경로로 이동시키고, `day5/settings.gradle`의 `include` 항목에 `hystrix-dashboard` 추가하여 하위 프로젝트로 인식

## config server 설정

> Identify it is as hystrix-dashboard in bootstrap.properties and point it to config server.

- `bootstrap.properties` 대신 `application.properties`를 사용하고 있음
- `application.properties`에 아래 내용 입력

```properties
spring.application.name=hystrix-dashboard
spring.cloud.config.uri=http://localhost:8888
```

- `application.properties`를 쓰는 이유와 각 설정의 의미는 `day3` 내용인 [The Config Server](../day3/README.md)를 참고

## `@EnableHystrixDashboard` 선언 후 실행

> Annotate it with @EnableHystrixDashboard and run it. You should be able to load it at http://localhost:8010/hystrix.html. It will expect a heartbeat stream from any of the services with a circuit breaker in them. Give it the address from the reservation-client: http://localhost:9999/hystrix.stream

- `HystrixDashboardApplication`에 `@EnableHystrixDashboard` 선언
- 서버 실행후 `http://localhost:8010/hystrix.html` 접근
- 최상단 입력창에 `http://localhost:9999/hystrix.stream` 입력 후 `Monitor Stream` 클릭
- `reservation-service`를 중단, 실행을 반복하면서 `http://localhost:9999/reservation/names`를 계속 접근해보면 dashboard의 지표들이 변경됨을 확인할 수 있음