# Making a Spring Boot application Production Ready

Josh Long의  '[Cloud Native Java Workshop](https://github.com/joshlong/cloud-native-workshop#2-making-a-spring-boot-application-production-ready)' 중 2일차인 '[Making a Spring Boot application Production Ready](https://github.com/joshlong/cloud-native-workshop#2-making-a-spring-boot-application-production-ready)' 과정을 따라해 보았다. 간단히 기억할 만한 것들을 문서로 함께 기록함.

## 과정 간단 소개

> In this lab, we'll look at how Spring Boot is optimized for the continuous delivery of applications into production.

코드의 완성과 프로덕션 반영은 완전히 다르며, 이 사이의 여정은 어느 누구의 예상보다 길다고 한다. 이 과정에서는 Spring Boot가 프로덕션 환경에서의 지속적 배포를 어떻게 돕는지 살펴본다.

## 전체 절차

- [x] `org.springframework.boot`:`spring-boot-starter-actuator` 추가
- [x] 사용자가 정의한 `HealthIndicator`를 통해 `HealthEndpoint` 커스터마이징
- [x] `./bin/graphite.sh` 실행
- [x] 2개의 환경 변수 `GRAPHITE_HOST` (`export GRAPHITE_HOST="$DOCKER_IP"`) and `GRAPHITE_PORT` (`2003`) 설정 (변수 설정 후 IDE를 재시작해야 할지도 모름)
- [x] `GraphiteReporter` @Bean 추가
- [ ] Add `io.dropwizard.metrics`:`metrics-graphite`
- [ ] Build an executable `.jar` (UNIX-specific) using the `<executable/>` configuration flag
- [ ] Add the HAL browser - `org.springframework.data`:`spring-data-rest-hal-browser` and view the Actuator endpoints using that
- [ ] Configure Maven resource filtering and the Git commit ID plugin in the `pom.xml` in all existing and subsequent `pom.xml`s, or extract out a common parent `pom.xml` that all modules may extend.
- [ ] Add `info.build.artifact=${project.artifactId}` and `info.build.version=${project.version}` to application.properties.
- [ ] Introduce a new `@RepositoryEventHandler` and `@Component`. Provide handlers for `@HandleAfterCreate`, `@HandleAfterSave`, and `@HandleAfterDelete`. Extract common counters to a shared method
- [ ] Add a semantic metric using `CounterService` and observe the histogram in Graphite

## 따라하기

아래 3가지 리소스를 기반으로 학습을 진행함

1. [Cloud Native Workshop Hands-On Youtube](https://youtu.be/fxB0tVnAi0I?t=2350)
2. [Cloud Native Workshop Github repository](https://github.com/joshlong/cloud-native-workshop/tree/master/labs/2/reservation-service)
3. [Spring Boot Actuator: Production-ready features](http://docs.spring.io/spring-boot/docs/current/reference/html/production-ready.html)

### Actuator Endpoints 추가

- 어플리케이션과의 상호 작용 및 모니터링을 위한 방법을 제공함
- 자세한 설명은 [Spring Boot Endpoints](http://docs.spring.io/spring-boot/docs/current/reference/html/production-ready-endpoints.html)를 참고
- 이 기능을 쓰고 싶다면 `build.gradle`의 `dependencies` 항목에 아래 라인을 추가
    + `compile('org.springframework.boot:spring-boot-starter-actuator')`
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

### HealthIndicator 커스터마이징

- 위에서 소개된 Endpoint들은 커스터마이징 가능하며, 자신만의 Endpoint를 등록할 수도 있다.
- 2가지 방법이 존재함
    + 설정 파일의 수정
    + @Bean 등록
- 여기서는 Actuator 접근 경로와 Health Endpoint의 내용을 바꾸어본다.
- 먼저, endpoint 접근 prefix를 `/admin`으로 설정하는 작업
    + `src/main/resources/application.properties` 파일 열기
    + `management.context-path=/admin` 설정 추가
- 다음으로, 사용자 정의 `HealthIndicator`를 등록
    + `org.springframework.boot.actuate.health.HealthIndicator` 구현체를 `@Bean`으로 등록

```java
@Bean
HealthIndicator healthIndicator() {
    return new HealthIndicator() {
        @Override
        public Health health() {
            return Health.status("health.status.custom").build();
        }
    };
}
```

- 어플리케이션을 재시작하면 아래 2가지 변경 사항이 확인됨
    + endpoint 경로가 `/health` 대신 `/admin/health` 경로로 바뀜
    + `healthIndicator`의 `status` 항목 값이 `health.status.custom`로 바뀜

### Graphite 실행

- 아래의 shell 스크립트를 작성

```shell
#!/bin/bash
docker run --name cna-graphite -p 80:80 -p 2003:2003 -p 8125:8125/udp hopsoft/graphite-statsd
```

- shell이 하는 일을 살펴보면,
    + `cna-graphite`라는 컨테이너명으로 실행
    + 컨테이너와 호스트의 포트를 tcp에 대해서는 80:80 및 2003:2003, udp에 대해서는 8125:8125으로 대응
    + 실행할 이미지명은 `hopsoft/graphite-statsd`
    + 이미지가 로컬에 없으니 dockerhub로부터 가져오리라 예상
- graphite에 대한 설명은 [여기](https://matt.aimonetti.net/posts/2013/06/26/practical-guide-to-graphite-monitoring/)에 잘 나와있음(Spring Boot 문서에서도 이 글을 언급함). 설명 하나를 발췌하면,

> Graphite is used to store and render time-series data. In other words, you collect metrics and Graphite allows you to create pretty graphs easily.

- 즉, 시계열 데이터를 저장하고 보여줌. 또한 메트릭스를 수집하고 예쁜 그래프를 쉽게 만들도록 도와줌.
- [graphite 홈페이지](http://graphiteapp.org/)도 함께 참고
- 각 포트에 대한 설명은 아래 표 참고

포트  | 설명
---- | ---
80   | 그래프를 보여주는 웹 화면 접속에 사용됨
2003 | Carbon의 [plaintext protocol](http://graphite.readthedocs.io/en/latest/feeding-carbon.html#the-plaintext-protocol)로 데이터를 받기 위한 포트
8125 | [StatsD](https://github.com/etsy/statsd/blob/master/docs/server.md)가 보내는 데이터를 받기 위한 포트

### GRAPHITE_HOST 및 GRAPHITE_PORT 환경 변수 설정

- 우선 `$DOCKER_IP`를 구함
    + 이게 뭘까를 잠시 고민. graphite에 접근하기 위한 IP로 예상되며, 아래 명령어를 통해 IP 획득
    + `docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}} cna-graphite`
    + `172.17.0.2`인 것을 확인
- 그리고 나서 `GRAPHITE_HOST` 및 `GRAPHITE_PORT`를 설정
    + Zsh를 사용하고 있으므로 `vi ~/.zshrc`로 파일 열기
    + 아래 두 라인 추가

```shell
export GRAPHITE_HOST=$(docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' cna-graphite)
export GRAPHITE_PORT=2003
```

- 도커 컨테이너의 ip 변경이 잦으므로, 상대적으로 고정적인 컨테이너명(`./bin/graphite.sh` 참고)을 이용하여 변수에 실행문 할당
    + 단점은 컨테이너가 실행된 후에 zsh 세션을 시작해야 한다는 것
- udp 포트인 2003은 그대로 명시 (바뀔 가능성 매우 적음)
- 환경 변수 반영을 위해 IntelliJ 재시작

### `GraphiteReporter` @Bean 추가

- graphite로 데이터를 보내주기 위한 @Bean으로 예상됨
- 우선, gradle에 아래 의존성 추가

```gradle
compile('io.dropwizard.metrics:metrics-graphite')
```

- 아래와 같이 @Bean 추가

```java
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
```

- 코드를 간단히 살펴보면,
    + `GraphiteReporter`는 Graphite에게 측정<sup>metric</sup> 값들을 보내주는 보고자
    + `MetricRegistry`는 Spring Boot의 측정 값들이 등록되는 곳
        * 이 `MetricRegistry`의 측정값들이 `/metrics` endpoint를 통해 노출됨
        * [Spring Boot 레퍼런스 Dropwizard Metrics](https://docs.spring.io/spring-boot/docs/current/reference/html/production-ready-metrics.html#production-ready-dropwizard-metrics) 참조
    + `Graphite`는 `GraphiteReporter`의 보고 대상
    + 그리고 `GraphiteReporter`는 보고를 2분 간격으로 하도록 설정되어 실행됨
    + 참고로, host와 port의 값은 `@Value`를 통해 각각 `GRAPHITE_HOST`와 `GRAPHITE_PORT`의 값으로 할당됨
        * 설정값이 @Value에 할당되는 과정은 [Externalized Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html) 문서를 참고
