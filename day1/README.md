# Bootcamp

- [Bootcamp](https://github.com/joshlong/cloud-native-workshop#1-bootcamp)

## REST & Data

### @RepositoryRestResource

> Annotate a Repository with this to customize export mapping and rels.

- [@Entity와 @Repository를 가지고 간단한 REST 어플리케이션을 만드는 예제](https://spring.io/guides/gs/accessing-data-rest/)
- [RepositoryRestResource API](http://docs.spring.io/spring-data/rest/docs/current/api/org/springframework/data/rest/core/annotation/RepositoryRestResource.html)
- 테스트 URL: http://localhost:8080/reservations/search/by-name?rn=Mark

### @RestResource

- [RestResource API](http://docs.spring.io/autorepo/docs/spring-hateoas/0.18.x/api/org/springframework/hateoas/ResourceProcessor.html)
- [Configuring the REST URL path](https://github.com/spring-projects/spring-data-rest/blob/master/src/main/asciidoc/configuring-the-rest-url-path.adoc)

## HATEOAS

> HATEOAS (Hypermedia as the Engine of Application State) is a constraint of the REST application architecture.

- [Understanding HATEOAS](https://spring.io/understanding/HATEOAS)
- [Building a Hypermedia-Driven RESTful Web Service](https://spring.io/guides/gs/rest-hateoas/)
- [Spring HATEOAS - Reference Documentation](http://docs.spring.io/spring-hateoas/docs/current/reference/html/)

### @ResourceProcessor

- [Customizing the JSON output](https://github.com/spring-projects/spring-data-rest/blob/master/src/main/asciidoc/customizing-json-output.adoc)

### @Resource

> A simple {@link Resource} wrapping a domain object and adding links to it.

## Boot Test

### @RunWith

- `@RunWith(SpringRunner.class)` tells JUnit to run using Spring’s testing support
- `SpringRunner` is the new name for `SpringJUnit4ClassRunner`
    + Boot 1.4에서부터 간소화된 이름 ([Testing improvements in Spring Boot 1.4](https://spring.io/blog/2016/04/15/testing-improvements-in-spring-boot-1-4) 참고)

### @SpringBootTest

- `@SpringBootTest` is saying “bootstrap with Spring Boot’s support”
- e.g. load application.properties and give me all the Spring Boot goodness.
- 속성
    + `properties`
    + `webEnvironment`
    + `classes`: a specific configuration to load.

### @WebAppConfiguration

> The presence of @WebAppConfiguration on a test class indicates that a WebApplicationContext should be loaded for the test using a default for the path to the root of the web application. To override the default, specify an explicit resource path via the value() attribute.<br/>
> \- [API Document](http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/test/context/web/WebAppConfiguration.html)

- 보다 자세한 내용은 [여기](https://spring.io/blog/2012/11/07/spring-framework-3-2-rc1-new-testing-features)를 참고

### @AutoConfigureMockMvc
### @TestPropertySource

- [configuration 우선 순위](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html) 참고

### 예제 비교

```java
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {TestConfiguration.class})
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.yml")
```
```java
@RunWith(SpringRunner.class)
@SpringBootTest(classes = CloudNativeWorkshopApplication.class)
@WebAppConfiguration
```

## 그 외

- CommandLineRunnder
- [Not War but Jar!](https://blog.mimacom.com/introduction-to-spring-boot/)
- [vaadin gradle 설정](https://github.com/spring-guides/gs-crud-with-vaadin/blob/master/complete/build.gradle)

## Questions:

- What is Spring? Spring, fundamentally, is a dependency injection container. This detail is unimportant. What is important is that once Spring is aware of all the objects - beans - in an application, it can provide services to them to support different use cases like persistence, web services, web applications, messaging and integration, etc.
- Why .jars and not .wars? We've found that many organizations deploy only one, not many, application to one Tomcat/Jetty/whatever. They need to configure things like SSL, or GZIP compression, so they end up doing that in the container itself and - because they don't want the versioned configuration for the server to drift out of sync with the code, they end up version controlling the application server artifacts as well as the application itself! This implies a needless barrier between dev and ops which we struggle in every other place to remove.
- How do I access the by-name search endpoint? Follow the links! visit http://localhost:8080/reservations and scroll down and you'll see links that connect you to related resources. You'll see one for search. Follow it, find the relevant finder method, and then follow its link.

