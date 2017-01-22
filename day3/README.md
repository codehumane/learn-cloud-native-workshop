# The Config Server

Josh Long의  '[Cloud Native Java Workshop](https://github.com/joshlong/cloud-native-workshop#2-making-a-spring-boot-application-production-ready)' 중 3일차인 '[The Config Server](https://github.com/joshlong/cloud-native-workshop#3-the-config-server)' 과정을 따라해 보았다. 간단히 기억할 만한 것들을 문서로 함께 기록함.

## 과정 간단 소개

> The 12 Factor manifesto talks about externalizing that which changes from one environment to another - hosts, locators, passwords, etc. - from the application itself. Spring Boot readily supports this pattern, but it's not enough. In this lab, we'll look at how to centralize, externalize, and dynamically update application configuration with the Spring Cloud Config Server.

Spring Cloud Config Server를 활용하여 어플리케이션 설정을 중앙화<sup>centralize</sup>, 외부화<sup>externalize</sup>, 동적으로 업데이트<sup>dynamically update</sup> 하는 방법에 대해 알아본다. [12 Factor manifesto의 한글 버전](https://12factor.net/ko/)도 존재한다. 함께 참고하자.

## 전체 절차

- [x] Config Server 구축
- [ ] 

## 참고 리소스

- [Cloud Native Workshop > The Config Server - README](https://github.com/joshlong/cloud-native-workshop#3-the-config-server)
- [Cloud Native Workshop > The Config Server - Git Repository](https://github.com/joshlong/cloud-native-workshop/tree/master/labs/3)

## 따라하기

- 전체 절차에서 소개한 내용을 하나씩 따라해봄
- 최종 결과물은 [여기](https://github.com/codehumane/codehumane/tree/master/learning/cloud-native-workshop/day3)를 참고

### Config Server 구축

- [Spring Initializr](http://start.spring.io/)에 접근하여 아래 내용 입력과 함께 프로젝트 생성
    + Group: `codehumane`
    + Artifact: `config-service`
    + Project: `Gradle`
    + Spring Boot Version: `1.4.3`
    + Dependencies: `Config Server`
- Josh Long의 [bootiful-microservices-config git repository](https://github.com/joshlong/bootiful-microservices-config)를 clone
- config-service 프로젝트의 `application.properties`에 아래 내용 명시
    + `server.port`는 config-service가 실행될 포트
    + `spring.cloud.config.server.git.uri`는 `bootiful-microservices-config`를 clone한 git repository의 경로

```properties
server.port=8888
spring.cloud.config.server.git.uri=https://github.com/codehumane/spring-cloud-configs.git
```

- config-service의 루트 어플리케이션에 @EnableConfigServer 추가

```java
package codehumane;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@EnableConfigServer
@SpringBootApplication
public class ConfigServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConfigServiceApplication.class, args);
    }
}
```

- Config Server를 참고하여 아래와 같이 reservation-service에 Spring Cloud BOM 추가

```gradle
dependencyManagement {
    imports {
        mavenBom "org.springframework.cloud:spring-cloud-dependencies:Camden.SR4"
    }
}
```

