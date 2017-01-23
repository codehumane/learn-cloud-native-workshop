# The Config Server

Josh Long의  '[Cloud Native Java Workshop](https://github.com/joshlong/cloud-native-workshop#2-making-a-spring-boot-application-production-ready)' 중 3일차인 '[The Config Server](https://github.com/joshlong/cloud-native-workshop#3-the-config-server)' 과정을 따라해 보았다. 간단히 기억할 만한 것들을 문서로 함께 기록함.

## 과정 간단 소개

> The 12 Factor manifesto talks about externalizing that which changes from one environment to another - hosts, locators, passwords, etc. - from the application itself. Spring Boot readily supports this pattern, but it's not enough. In this lab, we'll look at how to centralize, externalize, and dynamically update application configuration with the Spring Cloud Config Server.

Spring Cloud Config Server를 활용하여 어플리케이션 설정을 중앙화<sup>centralize</sup>, 외부화<sup>externalize</sup>, 동적으로 업데이트<sup>dynamically update</sup> 하는 방법에 대해 알아본다. [12 Factor manifesto의 한글 버전](https://12factor.net/ko/)도 존재한다. 함께 참고하자.

## 전체 절차

- [x] Config Server 구축
- [x] Config Client 구축

## 참고 리소스

- [Cloud Native Workshop > The Config Server - README](https://github.com/joshlong/cloud-native-workshop#3-the-config-server)
- [Cloud Native Workshop > The Config Server - Git Repository](https://github.com/joshlong/cloud-native-workshop/tree/master/labs/3)
- [Spring Cloud Config > Quickstart](https://cloud.spring.io/spring-cloud-config/)

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

### Config Client 구축

- 가장 먼저 할일은 `reservation-service`의 `pom.xml`에 아래 내용을 추가하는 것

```xml
<dependencyManagement>
    <dependencies>
      ...
      <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-parent</artifactId>
        <version>Brixton.RELEASE</version>
        <type>pom</type>
        <scope>import</scope>
      </dependency>
    </dependencies>
</dependencyManagement>
```

- 하지만 여기서는 gradle 설정이 필요하므로, `pom.xml`에 추가되는 내용을 먼저 파악하기로 함
- maven의 `dependencyManagement`란 의존성 정보를 중앙화<sup>centralizing</sup>하는 메커니즘으로, Apache Maven Project의 [Introduction to the Dependency Mechanism](https://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html#Dependency_Management)에 예제와 함께 잘 설명되어 있음
- gradle에서는 이를 [Dependency management plugin](https://github.com/spring-gradle-plugins/dependency-management-plugin)이 지원하고 있으며, [Importing a Maven bom](https://github.com/spring-gradle-plugins/dependency-management-plugin) 부분을 참고하면 됨
- 더불어, [Spring Cloud Config > Quickstart](https://cloud.spring.io/spring-cloud-config/)를 함께 참고하여 작성한 내용은 아래와 같음

```gradle
apply plugin: "io.spring.dependency-management"

dependencyManagement {
    dependencies {
        dependency mavenBom 'org.springframework.cloud:spring-cloud-config:1.2.3.BUILD-SNAPSHOT'
    }
}

repositories {
    maven { url 'https://repo.spring.io/libs-snapshot' }
}
dependencies {
    compile('org.springframework.cloud:spring-cloud-starter-config')
}
```

- 그리고 나서, `application.properties`의 내용을 [config git repository](https://github.com/codehumane/spring-cloud-configs/blob/master/reservation-service.properties)로 옮기고, 대신 아래 내용으로 대체
    + 원래 과정에서는 `bootstrap.properties`라는 파일로 대체하라고 함
    + 하지만, 이 경우 `spring.application.name`의 값이 잘 읽히지 않는 문제가 발생함
    + `application.properties` 파일로 하면 정상 작동함

```properties
spring.application.name=reservation-service
spring.cloud.config.uri=http://localhost:8888
```

- `spring.application.name`으로 명시한 것은 [config git repository](https://github.com/codehumane/spring-cloud-configs/blob/master/reservation-service.properties)에서 `reservation-service.properties`를 읽겠다는 의미
- `spring.cloud.config.uri`는 config server의 uri를 가리킴
- 이제 config client에서 설정 값을 잘 불러오는지 확인하기 위해 아래의 코드를 작성

```java
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
```

- 이제 서버를 띄우고 `/message`로 접근해 보면 [config git repository](https://github.com/codehumane/spring-cloud-configs/blob/master/reservation-service.properties)에서 설정한 값을 확인할 수 있음
- @RefreshScope의 의미는 변경되는 값이 있으면 반영하겠다는 의미이며, 아래의 endpoint 호출로 반영 가능함

```shell
curl -X POST http://localhost:8080/admin/refresh
```