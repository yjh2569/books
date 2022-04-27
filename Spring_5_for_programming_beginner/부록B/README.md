# 스프링 부트 소개

* 스프링 부트의 starter 모듈은 다음 두 가지를 제공한다.
  * 메이븐 의존 설정 추가
  * 기본 설정 추가
* spring-boot-start-web 모듈은 spring-webmvc, JSON, Validator, 내장 톰캣 등 웹 개발에 필요한 의존을 설정하고, 스프링 MVC를 위한 다양한 구성 요소(DispatcherServlet, 디폴트 서블릿, Jackson 등)에 대한 설정을 자동 생성하는 기능을 제공한다.
  * 필요한 설정을 자동으로 등록하므로 개발자는 일부 설정만 추가로 작업하면 된다.

* @SpringBootApplication 애노테이션을 붙인 클래스를 SpringApplication.run()을 이용해 실행하면 여러 설정을 자동으로 처리한다.
  * 웹 starter를 사용하면 웹 관련 자동 설정 기능을 활성화하고 JDBC starter를 사용하면 DB 관련 자동 설정 기능을 활성화한다.
  * 필요한 대부분의 설정을 자동으로 생성하므로 개발자는 필요한 것만 골라서 설정하면 된다.
  * 컴포넌트 스캔 기능도 활성화된다. 컴포넌트 스캔 대상 애노테이션을 붙인 클래스를 빈으로 등록한다.

* application.properties 파일은 설정 정보를 담는다.
  * JDBC URL이나 웹 캐시 시간과 같은 설정을 변경하고 싶을 때 이 프로퍼티 파일을 사용한다.

* DB 연동과 관련된 설정도 spring-boot-starter-jdbc와 mysql-connector-java의 두 가지 의존만 추가하면 된다.
  * spring-boot-starter-jdbc 모듈은 JDBC 연결에 필요한 DataSource, JdbcTemplate, 트랜잭션 관리자 등을 자동으로 설정한다.
  * DataSource를 생성할 때 필요한 JDBC URL 정보는 application.properties 파일에서 읽어온다.
  * spring-boot-starter-jdbc 모듈은 JdbcTemplate를 빈으로 등록하기 때문에 @Autowired 애노테이션을 이용해서 JdbcTemplate 빈을 주입받을 수 있다.

* 스프링 부트를 이용하면 실행 가능한 패키지도 쉽게 만들 수 있다.
```
mvnw package
```
  * 위 명령어를 실행하면 target 폴더에 jar 파일이 생성된다. 이 파일은 스프링 부트 플러그인이 만든 실행 가능한 jar 파일이다.
  * 아래 명령어를 통해 이 jar 파일을 실행할 수 있다.
  ```
  java -jar target/(jar파일이름).jar
  ```
  * 이 명령어를 실행하면 내장 톰캣을 이용해 웹 어플리케이션을 구동한다.
