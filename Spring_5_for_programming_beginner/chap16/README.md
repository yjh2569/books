# Chapter 16 : JSON 응답과 요청 처리

## JSON 개요

* JSON(JavaScript Object Notation)
  * 간단한 형식을 갖는 문자열로 데이터 교환에 주로 사용한다.
  * 중괄호를 사용해 객체를 표현하고, 객체는 (이름, 값) 쌍을 갖는다. 이름과 값은 콜론(:)으로 구분한다.

* 값에는 문자열, 숫자, 불리언, null, 배열, 다른 객체가 올 수 있다.
  * 문자열은 큰따옴표나 작은따옴표 사이에 위치한 값으로 \"(큰따옴표), \n(뉴라인), \r(캐리지 리턴), \t(탭)과 같이 역슬래시를 이용해 특수 문자를 표시할 수 있다.
  * 숫자는 10진수 표기법이나 지수 표기법을 따른다.
  * 불리언 타입 값은 true와 false가 있다.
  * 배열은 대괄호로 표현한다. 대괄호 안에 콤마로 구분한 값 목록을 갖는다.

## Jackson 의존 설정

* Jackson은 자바 객체와 JSON 형식 문자열 간 변환을 처리하는 라이브러리이다.
  * 스프링 MVC에서 Jackson 라이브러리를 이용해서 자바 객체를 JSON으로 변환하려면 클래스 패스에 Jackson 라이브러리를 추가하면 된다.

## @RestController로 JSON 형식 응답

* 스프링 MVC에서 JSON 형식으로 데이터를 응답할 때 @Controller 애노테이션 대신 @RestController 애노테이션을 사용하면 된다.
  * @RestController 애노테이션을 붙인 경우 스프링 MVC는 요청 매핑 애노테이션을 붙인 메서드가 반환한 객체를 알맞은 형식으로 변환해서 응답 데이터로 전송한다.
  * 클래스 패스에 Jackson이 존재하면 JSON 형식의 문자열로 변환해서 응답한다.
* 스프링 4 버전 이후로는 @RestController를 사용하지만, 4 버전 이전에는 @Controller 애노테이션과 @ResponseBody 애노테이션을 사용한다.

### @JsonIgnore를 이용한 제외 처리

* 민감한 데이터는 응답 결과에 포함시키면 안되는데, Jackson이 제공하는 @JsonIgnore 애노테이션을 사용하면 이를 간단히 처리할 수 있다.
  * 커맨드 클래스 내 필드 중 포함시키지 않을 필드에 @JsonIgnore 애노테이션을 붙이면 된다.

### 날짜 형식 변환 처리 : @JsonFormat 사용

* 날짜나 시간을 JSON 값으로 바꾸면 배열이나 숫자가 되는데, 보통 특정 형식을 갖는 문자열로 표현하는 것을 선호한다.
  * Jackson에서 날짜나 시간 값을 특정한 형식으로 표현하는 가장 쉬운 방법은 @JsonFormat 애노테이션을 사용하는 것이다.
  * 예를 들어 ISO-8601(yyyy-MM-ddTHH:mm:ss) 형식으로 변환하고 싶다면 shpae 속성 값으로 Shape.STRING을 갖는 @JsonFormat 애노테이션을 변환 대상에 적용하면 된다.
  * 원하는 형식으로 변환해서 출력하고 싶다면 @JsonFormat 애노테이션의 pattern 속성을 사용한다.

### 날짜 형식 변환 처리 : 기본 적용 설정

* 날짜 형식을 변환할 모든 대상에 동일한 변환 규칙을 적용하고 싶다면 @JsonFormat 애노테이션을 사용하는 대신 스프링 MVC 설정을 변경한다.
* 스프링 MVC는 자바 객체를 HTTP 응답으로 변환할 때 HttpMessageConverter라는 것을 사용한다.
  * 따라서 JSON으로 변환할 때 사용하는 HttpMessageConverter를 새롭게 등록해서 날짜 형식을 원하는 형식으로 변환하도록 설정하면 모든 날짜 형식에 동일한 변환 규칙을 적용할 수 있다.

## @RequestBody로 JSON 요청 처리

* JSON 형식으로 전송된 요청 데이터를 커맨드 객체로 전달받으려면 커맨드 객체에 @RequestBody 애노테이션을 붙이기만 하면 된다.
* 스프링 MVC가 JSON 형식으로 전송된 데이터를 올바르게 처리하려면 요청 컨텐츠 타입이 application/json이어야 한다.
  * 보통 POST 방식의 폼 데이터는 쿼리 문자열인 "p1=v1&p2=v2"로 전송되는데 이때 컨텐츠 타입은 application/x-www-form-urlencoded이다.
  * 쿼리 문자열 대신 JSON 형식을 사용하려면 application/json 타입으로 데이터를 전송할 수 있는 별도 프로그램이 필요하다.

### JSON 데이터의 날짜 형식 다루기

* 별도 설정을 하지 않으면 시간대가 없는 JSR-8601 형식의 문자열을 LocalDateTime과 Date로 변환한다.
* 특정 패턴을 가진 문자열을 LocalDateTime이나 Date 타입으로 변환하고 싶다면 @JsonFormat 애노테이션의 pattern 속성을 사용해서 패턴을 지정한다.
  * 특정 속성이 아니라 해당 타입을 갖는 모든 속성에 적용하고 싶다면 스프링 MVC 설정을 추가하면 된다.

### 요청 객체 검증하기

* JSON 형식으로 전송한 데이터를 변환한 객체도 동일한 방식으로 @Valid 애노테이션이나 별도 Validator를 이용해 검증할 수 있다.
  * @Valid 애노테이션을 사용한 경우 검증에 실패하면 400(Bad Request) 상태 코드를 응답한다.
  * Validator를 사용할 경우 직접 상태 코드를 처리해야 한다.

## ResponseEntity로 객체 리턴하고 응답 코드 지정하기

* HttpServletResponse를 이용해 에러 응답을 하면 JSON 형식이 아닌 서버가 기본으로 제공하는 HTML을 응답 결과로 제공한다.
  * HTML이 아닌 JSON 형식의 응답 데이터를 전송해야 API 호출 프로그램이 일관된 방법으로 응답을 처리할 수 있을 것이다.

### ResponseEntity를 이용한 응답 데이터 처리

* ResponseEntity를 사용하면 정상인 경우와 비정상인 경우 모두 JSON 응답을 전송할 수 있다.
  * 스프링 MVC는 리턴 타입이 ResponseEntity이면 ResponseEntity의 body로 지정한 객체를 사용해서 변환을 처리한다.
  * ResponseEntity를 생성하는 기본 방법은 status와 body를 이용해서 상태 코드와 JSON으로 변환할 객체를 지정하는 것이다.
    * 상태 코드는 HttpStatus 열거 타입에 정의된 값을 이용해서 정의한다.
  * 200 응답 코드와 몸체 데이터를 생성할 경우 ok() 메서드를 이용해 한 번에 생성할 수도 있다.
  * 몸체 내용이 없다면 body를 지정하지 않고 build()로 바로 생성한다.
  * 몸체가 없을 때 status() 대신 사용할 수 있는 메서드
    * noContent() : 204
    * badRequest() : 400
    * notFound() : 404

### @ExceptionHandler 적용 메서드에서 ResponseEntity로 응답하기

* @ExceptionHandler 애노테이션을 적용한 메서드에서 에러 응답을 처리하도록 구현하면 중복을 없앨 수 있다.
* @RestControllerAdvice 애노테이션을 이용해서 에러 처리 코드를 별도 클래스로 분리할 수도 있다.
  * @ControllerAdvice 애노테이션과 동일하지만 응답을 JSON이나 XML과 같은 형식으로 변환한다.

### @Valid 에러 결과를 JSON으로 응답하기

* @Valid 애노테이션을 붙인 커맨드 객체가 값 검증에 실패하면 400 상태 코드를 응답하는데, 이때 HttpServletResponse를 이용해서 상태 코드를 응답했을 때와 마찬가지로 HTML 응답을 전송한다.
  * JSON 형식 응답을 제공하고 싶다면 Errors 타입 파라미터를 추가해서 직접 에러 응답을 생성하면 된다.
  * @RequestBody 애노테이션을 붙인 경우 @Valid 애노테이션을 붙인 객체의 검증에 실패했을 때 Errors 타입 파라미터가 존재하지 않으면 MethodArgumentNotValidException이 발생한다. 따라서 이 예외를 활용해 @ExceptionHandler 애노테이션을 이용해서 검증 실패 시 에러 응답을 생상해도 된다.
