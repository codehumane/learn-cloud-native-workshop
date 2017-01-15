# Making a Spring Boot application Production Ready

Josh Long의  '[Cloud Native Java Workshop](https://github.com/joshlong/cloud-native-workshop#2-making-a-spring-boot-application-production-ready)' 중 2일차인 '[Making a Spring Boot application Production Ready](https://github.com/joshlong/cloud-native-workshop#2-making-a-spring-boot-application-production-ready)' 과정을 따라해 보았다. 간단히 기억할 만한 것들을 문서로 함께 기록함.

## 과정 간단 소개

> In this lab, we'll look at how Spring Boot is optimized for the continuous delivery of applications into production.

코드의 완성과 프로덕션 반영은 완전히 다르며, 이 사이의 여정은 어느 누구의 예상보다 길다고 한다. 이 과정에서는 Spring Boot가 프로덕션 환경에서의 지속적 배포를 어떻게 돕는지 살펴본다.

## 전체 절차

- [x] `org.springframework.boot`:`spring-boot-starter-actuator` 추가
- [ ] 사용자가 정의한 `HealthIndicator`를 통해 `HealthEndpoint` 커스터마이징
- [ ] Start `./bin/graphite.sh`
- [ ] Configure two environment variables `GRAPHITE_HOST` (`export GRAPHITE_HOST="$DOCKER_IP"`) and `GRAPHITE_PORT` (`2003`) (you may need to restart your IDE to see these new environment variables)
- [ ] Add a `GraphiteReporter` bean
- [ ] Add `io.dropwizard.metrics`:`metrics-graphite`
- [ ] Build an executable `.jar` (UNIX-specific) using the `<executable/>` configuration flag
- [ ] Add the HAL browser - `org.springframework.data`:`spring-data-rest-hal-browser` and view the Actuator endpoints using that
- [ ] Configure Maven resource filtering and the Git commit ID plugin in the `pom.xml` in all existing and subsequent `pom.xml`s, or extract out a common parent `pom.xml` that all modules may extend.
- [ ] Add `info.build.artifact=${project.artifactId}` and `info.build.version=${project.version}` to application.properties.
- [ ] Introduce a new `@RepositoryEventHandler` and `@Component`. Provide handlers for `@HandleAfterCreate`, `@HandleAfterSave`, and `@HandleAfterDelete`. Extract common counters to a shared method
- [ ] Add a semantic metric using `CounterService` and observe the histogram in Graphite

## 따라하기

아래 2가지 리소스를 기반으로 학습을 진행함
1. [Cloud Native Workshop Hands-On 영상](https://youtu.be/fxB0tVnAi0I?t=2350)
2. [Cloud Native Workshop Github repository](https://github.com/joshlong/cloud-native-workshop/tree/master/labs/2/reservation-service)

### Actuator Endpoints 추가

- Actuator Endpoints는 어플리케이션과의 상호 작용 및 모니터링을 위한 방법을 제공함
- 자세한 설명은 [Spring Boot Endpoints](http://docs.spring.io/spring-boot/docs/current/reference/html/production-ready-endpoints.html)를 참고
- 이 기능을 쓰고 싶다면 `build.gradle`의 `dependencies` 항목에 아래 라인을 추가한다.
    + ```compile('org.springframework.boot:spring-boot-starter-actuator')```
    + classpath를 추가한 것만으로 사용이 가능함
- 서버를 시작한 후, URL 경로로 `/metrics`, `/health` 등의 endpoint 접근이 가능해짐을 확인
- 접근 가능한 모든 endpoint를 보고 싶다면, `/actuator` 경로로 접근
- 주요 endpoint 몇 가지에 대한 설명은 다음과 같다.

endpoint | 설명 | 기본값
-------- | --- | ----
actuator | 다른 모든 endpoint를 HATEOAS 방식으로 보여줌 | true
autoconfig | 모든 자동 설정<sup>auto-configuration</sup> 후보들을 적용/미적용 이유와 함께 보여줌 | true
health | 시스템 헬스<sup>health</sup> 정보를 보여줌 | true
trace | 시스템 트레이스(마지막 100개의 HTTP 요청) 정보 | true

