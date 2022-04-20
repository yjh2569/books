# Chapter 14 : MVC 4 : 날짜 값 변환, @PathVariable, 익셉션 처리

## 커맨드 객체 Date 타입 프로퍼티 변환 처리 : @DateTimeFormat

* 입력 받은 시간을 LocalDateTime 타입으로 변환해야 하는 경우 기본 데이터 타입과는 달리 자동으로 이루어지지 않는다.
  * 클래스 내 시간을 나타내는 필드에 @DateTimeFormat 애노테이션을 적용하면 된다.
  * 커맨드 객체에 @DateTimeFormat이 적용되어 있으면 @DateTimeFormat에서 지정한 형식을 이용해서 문자열을 LocalDateTime 타입으로 변환한다.
  * pattern 속성값으로 "yyyyMMddHH"를 주면 "2022042020"의 문자열을 "2022년 4월 20일 20시" 값을 갖는 LocalDateTime 객체로 변환해준다.
  

## 변환 처리에 대한 이해

* 스프링 MVC는 요청 매핑 애노테이션 적용 메서드와 DispatcherServlet 사이를 연결하기 위해 RequestMappingHandlerAdaptor 객체를 사용하는데, 이 핸들러 어댑터 객체는 요청 파라미터와 커맨드 객체 사이의 변환 처리를 위해 WebDataBinder를 이용한다.
  * WebDataBinder는 커맨드 객체를 생성하고, 커맨드 객체의 프로퍼티와 같은 이름을 갖는 요청 파라미터를 이용해서 프로퍼티 값을 생성한다.
  * WebDataBinder는 직접 타입을 변환하지 않고 ConversionService에 그 역할을 위임한다.
  * @EnableWebMvc 애노테이션을 사용하면 DefaultFormattingConversionService를 ConversionService로 사용한다.
  * 위 서비스는 기본 데이터 타입뿐만 아니라 @DateTimeFormat 애노테이션을 사용한 시간 관련 타입 변환 기능을 제공한다.
  * 따라서 커맨드로 사용할 클래스에 @DateTimeFormat 애노테이션만 붙이면 지정한 형식의 문자열을 시간 타입 값으로 받을 수 있다.
  

* WebDataBinder는 \<form:input\>에도 사용된다.
  * path 속성에 지정한 프로퍼티 값을 String으로 변환해서 \<input\> 태그의 value 속성값으로 생성한다.
  * 프로퍼티 값을 String으로 변환할 때 WebDataBinder의 ConversionService를 사용한다.


## @PathVariable을 이용한 경로 변수 처리

* 경로의 일부가 고정되어 있지 않고 달라질 때 @PathVariable 애노테이션을 사용해 가변 경로를 처리할 수 있다.
  * 매핑 경로에 '{경로변수}'와 같이 중괄호로 둘러 쌓인 부분을 경로 변수라고 부른다.
  * '{경로변수}'에 해당하는 값은 같은 경로 변수 이름을 지정한 @PathVariable 파라미터에 전달된다.
  
## 컨트롤러 익셉션 처리하기

* @PathVariable을 이용한 처리에서 만약 없는 ID를 경로변수로 처리한다면 MemberNotFoundException이 발생하고, 숫자가 아닌 문자를 경로 변수로 사용하면 Long 타입으로 변환할 수 없어 400 에러가 발생한다.
  * MemberNotFoundException은 try-catch로 잡은 뒤 안내 화면을 보여주는 뷰를 만들면 해결 가능하나, 400 에러는 이러한 방법이 불가능하다.
  * 이때 사용할 수 있는 게 @ExceptionHandler 애노테이션이다.
  * 같은 컨트롤러에 @ExceptionHandler 애노테이션을 적용한 메서드가 존재하면 그 메서드가 예외를 처리한다.
  * 따라서 컨트롤러에서 발생한 예외를 직접 처리하고 싶다면 @ExceptionHandler 애노테이션을 적용한 메서드를 구현하면 된다.
  

### @ControllerAdvice를 이용한 공통 예외 처리

* 다수의 컨트롤러에서 동일 타입의 예외가 발생하는 경우 @ControllerAdvice 애노테이션을 이용해서 불필요한 중복을 막을 수 있다.
  * @ControllerAdvice 애노테이션이 적용된 클래스는 지정한 범위의 컨트롤러에 공통으로 사용될 설정을 지정할 수 있다.
  * @ControllerAdvice 적용 클래스가 동작하려면 해당 클래스를 스프링에 빈으로 등록해야 한다.
  

### @ExceptionHandler 적용 메서드의 우선 순위

* @ControllerAdvice 클래스 내 @ExceptionHandler 메서드보다, 컨트롤러 클래스에 있는 @ExceptionHandler 메서드가 우선 순위가 높다.

* @ControllerAdvice 애노테이션은 공통 설정을 적용할 컨트롤러 대상을 지정하기 위해 다음과 같은 속성을 제공한다.
  * value, basePackages(String\[\]) : 공통 설정을 적용할 컨트롤러가 속하는 기준 패키지
  * annotations(Class<? extends Annotation>[]) : 특정 애노테이션이 적용된 컨트롤러 대상
  * assignableTypes(Class<?>\[\]) : 특정 타입 또는 그 하위 타입인 컨트롤러 대상
  

### @ExceptionHandler 애노테이션 적용 메서드의 파라미터와 리턴 타입

* @ExceptionHandler 애노테이션을 붙인 메서드는 다음 파라미터를 가질 수 있다.
  * HttpServletRequest, HttpServletResponse, HttpSession
  * Model
  * Exception
  
* 리턴 가능한 타입은 다음과 같다.
  * ModelAndView
  * String(뷰 이름)
  * (@ResponseBody 애노테이션을 붙인 경우) 임의 객체
  * ResponseEntity