# Service Registration and Discovery

Josh Long의  '[Cloud Native Java Workshop](https://github.com/joshlong/cloud-native-workshop#2-making-a-spring-boot-application-production-ready)' 중 3일차인 '[Service Registration and Discovery](https://github.com/joshlong/cloud-native-workshop#4-service-registration-and-discovery)' 과정을 따라해 보았다. 간단히 기억할 만한 것들을 문서로 함께 기록함.

## 과정 간단 소개

> 클라우드에서 서비스의 수명은 종종 일시적이며, 이러한 서비스의 호스트와 포트를 염려하지 않고 추상적으로 통신할 수 있는 것은 중요하다. 언뜻 보기에는 이것이 DNS의 사용 사례처럼 보일지 모르겠지만, DNS는 몇 가지 중요한 한계점을 가진다. 응답은 가능하지만 DNS 매핑을 기다리는 서비스가 있는지 어떻게 알 수 있는가? DNS 보다 좀 더 정교한 로드밸런싱(전형적인 로드밸런서가 다룰 수 있는 라운드로빈 등의)을 어떻게 지원하는가? 대부분의 클라우드 환경 바깥에서 DNS가 리졸브되기 위해 필요한 부가적 홉<sup>hop</sup>을 피하려면 어떻게 해야 하는가? 이러한 한계들과 그 외의 것들을 위해, 우리는 DNS와 결합되어 있지는 않지만 DNS 효과가 나는 것을 필요로 하게 된다. 여기서는  service registry와 스프링 클라우드의 `DiscoveryClient` 추상화를 사용한다.

## 전체 절차

- [x] 프로젝트 초기화
- [x] Config Server 사용하도록 설정
- [x] `reservation-service`의 Eureka Server 사용을 위한 의존성 추가
- [x] @EnableDiscoveryClient 추가 및 확인
- [x] Spring Initializr를 이용하여 `reservation-client`라는 새로운 모듈 생성 및 설정
- [x] `DiscoveryClient` api를 이용한 다른 Eureka 서비스 조회

## 따라하기

전체 절차를 하나씩 따라하며 기록

## 프로젝트 초기화

- [Spring Initializr](http://start.spring.io/) 접속
- Group명은 `codehumane`, Artifact는 `eureka-service`, Dependencies는 `Eureka Server` 추가
- `Eureka Server`의 추가는 `org.springframework.cloud:spring-cloud-starter-eureka-server` 의존성 추가를 가리킴
- `EurekaServiceApplication`에 `@EnableEurekaServer` 추가

## Config Server 사용하도록 설정

- `build.gradle`에 일일이 추가하는 대신, Spring Initializr에서 `Config Client`도 함께 지정
- 완성된 `build.gradle`은 [여기](https://github.com/codehumane/learn-cloud-native-workshop/blob/master/day4/eureka-service/build.gradle) 참고

## `reservation-service`의 Eureka Server 사용을 위한 의존성 추가

- `reservation-service`의 `build.grade`에 아래 의존성 추가

```gradle
depdendencies {
    compile('org.springframework.cloud:spring-cloud-starter-eureka')
}
```

## @EnableDiscoveryClient 추가 및 확인

- `reservation-service`의 Main 클래스에 `@EnableDiscoveryClient` 추가
- 프로세스 시작
- `http://localhost:8761`(Eureka Server)에 접속하면, `RESERVATION-SERVICE`가 나타남을 확인할 수 있음

## Spring Initializr를 이용하여 `reservation-client`라는 새로운 모듈 생성 및 설정

- Dependencies에 `Config Client`, `Eureka Discovery`, `Web` 명시하여 새로운 모듈 생성
- Main 클래스에 `@EnableDiscoveryClient` 추가
- `application.properties`에 아래 내용 명시

```gradle
spring.cloud.config.uri=http://localhost:8888
spring.application.name=eureka-service
```

- Boot를 실행하면 Eureka Server에 `RESERVATION-CLIENT`가 나타남을 확인할 수 있음

## `DiscoveryClient` api를 이용한 다른 Eureka 서비스 조회

- `ReservationClientApplication` 클래스에 아래 내용 추가

```java
@Bean
CommandLineRunner runner(DiscoveryClient dc) {
    return args ->
            dc.getInstances("reservation-service")
                    .forEach(si -> System.out.println(String.format(
                            "Eureka Client 정보 - serviceId:%s, host:%s, port:%s",
                            si.getServiceId(), si.getHost(), si.getPort())
                    ));
}
```

- 서버를 재시작하면 콘솔에서 아래 내용 확인할 수 있음

```
Eureka Client 정보 - serviceId:RESERVATION-SERVICE, host:192.168.0.3, port:8081
```