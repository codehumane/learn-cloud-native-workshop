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
- [ ] `@LoadBalanced`를 통한 서비스 호출 로드밸런싱
- [ ] 컨트롤러를 `/reservations`에 매핑하고 `getReservationNames` 메소드 추가한 후 `/names`에 매핑
- [ ] actuator 및 hystrix 의존성 추가
- [ ] `@EnableCircuitBreaker` 선언
- [ ] `@HystrixCommand` 선언하여 폴백 메소드 명시
- [ ] reservation-service 종료시킨 후 `/reservations/names` 접근하여 폴백 여부 확인
- [ ] actifactId를 `hystrix-dashboard`로 하여 새로운 서비스 생성
- [ ] `bootstrap.properties`에서 식별자를 `hystrix-dashboard`로 명시하고 config server에서 가리키도록 함
- [ ] `@EnableHystrixDashboard` 선언 후 실행

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

## `@LoadBalanced`를 통한 서비스 호출 로드밸런싱

> Add a REST service called ReservationApiGatewayRestController that uses the @Autowired @LoadBalanced RestTemplate rt to make a load-balanced call to a service in the registry using Ribbon.

## 컨트롤러를 `/reservations`에 매핑하고 `getReservationNames' 메소드 추가한 후 `/names`에 매핑

> Map the controller itself to /reservations and then create a new controller handler method, getReservationNames, that's mapped to /names.
> In the getReservationNames handler, make a call to http://reservation-service/reservations using the RestTemplate#exchange method, specifying the return value with a ParameterizedTypeReference<Resources<Reservation>>> as the final argument to the RestTemplate#exchange method.
> Take the results of the call and map them from Reservation to Reservation#getReservationName. Then, confirm that http://localhost:9999/reservations/names returns the names.

## actuator 및 hystrix 의존성 추가

> Add org.springframework.boot:spring-boot-starter-actuator and org.springframework.cloud:spring-cloud-starter-hystrix to the reservation-client

## `@EnableCircuitBreaker` 선언

> Add @EnableCircuitBreaker to our DemoApplication configuration class

## `@HystrixCommand` 선언하여 폴백 메소드 명시

> Add @HystrixCommand around any potentially shaky service-to-service calls, like getReservationNames, specifying a fallback method that returns an empty collection.

## reservation-service 종료시킨 후 `/reservations/names` 접근하여 폴백 여부 확인

> Test that everything works by killing the reservation-service and revisiting the /reservations/names endpoint

## actifactId를 `hystrix-dashboard`로 하여 새로운 서비스 생성

> Go to the Spring Initializr and stand up a new service - with an artifactId of hystrix-dashboard - that uses Eureka Discovery, Config Client, and the Hystrix Dashboard.

## `bootstrap.properties`에서 식별자를 `hystrix-dashboard`로 명시하고 config server에서 가리키도록 함

> Identify it is as hystrix-dashboard in bootstrap.properties and point it to config server.

## `@EnableHystrixDashboard` 선언 후 실행

> Annotate it with @EnableHystrixDashboard and run it. You should be able to load it at http://localhost:8010/hystrix.html. It will expect a heartbeat stream from any of the services with a circuit breaker in them. Give it the address from the reservation-client: http://localhost:9999/hystrix.stream