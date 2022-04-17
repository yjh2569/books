# Chapter 12 : MVC 2 : 메시지, 커맨드 객체 검증

## \<spring:message\> 태그로 메시지 출력하기

* 사용자 화면에 보일 문자열은 JSP에 직접 코딩했었는데, 이러한 경우 만약 문자열을 변경하거나, 다국어 지원을 할 때 불편하다.
* 위 문제를 해결하기 위해 뷰 코드에서 사용할 문자열을 언어별로 파일에 보관하고 뷰 코드는 언어에 따라 알맞은 파일에서 문자열을 읽어와 출력한다.
  * 스프링은 자체적으로 이 기능을 제공하고 있기 때문에 각각의 언어별로 알맞은 문자열을 출력하도록 JSP 코드를 구현하는 것이 쉽다.

* 문자열을 별도 파일에 작성하고 JSP 코드에서 이를 사용하기 위한 작업
  * 문자열을 담은 메시지 파일을 작성한다.
  * 메시지 파일에서 값을 읽어오는 MessageSource 빈을 설정한다.
  * JSP 코드에서 \<spring:message\> 태그를 사용해서 메시지를 출력한다.
  
* 메시지 파일은 자바의 프로퍼티 파일 형식으로 작성한다.
  * 프로퍼티 파일을 열 때 한국어를 입력하는 경우 Text Editor를 사용해서 연다. 또한 프로퍼티 파일을 Properties 메뉴를 실행해 UTF-8로 설정한다.
  
* \<spring:message\> 태그는 프로퍼티 파일에서 code와 일치하는 값을 가진 프로퍼티의 값을 출력한다.
  * MessageSource로부터 코드에 해당하는 메시지를 읽어온다.
  
* 다국어 메시지를 지원하려면 각 프로퍼티 파일 이름에 언어에 해당하는 로케일 문자를 추가한다.
  * 예를 들어 한국어와 영어에 대한 메시지를 지원하려면 label_ko.properties, label_en.properties 파일을 사용하면 된다.
  
### 메시지 처리를 위한 MessageSource와 \<spring:message\> 태그

* 스프링은 로케일에 상관없이 일관된 방법으로 문자열을 관리할 수 있는 MessageSource 인터페이스를 정의하고 있다.
  * 특정 로케일에 해당하는 메시지가 필요한 코드는 MessageSource의 getMessage() 메서드를 이용해서 필요한 메시지를 가져와서 사용한다.
  * getMessage() 메서드의 code 파라미터는 메시지를 구분하기 위한 코드이고, locale 파라미터는 지역을 구분하기 위한 Locale이다.
  * 같은 코드라 하더라도 지역에 따라 다른 메시지를 제공할 수 있도록 설계했다. 이 기능을 사용하면 지역에 따라 다른 언어로 메시지를 보여주는 처리를 할 수 있다.

* MessageSource의 구현체로는 자바의 프로퍼티 파일로부터 메시지를 읽어오는 ResourceBundleMessageSource 클래스를 사용한다.
  * 이 클래스는 메시지 코드와 일치하는 이름을 가진 프로퍼티의 값을 메시지로 제공한다.
  * ResourceBundleMessageSource는 자바의 ResourceBundle을 사용하기 때문에 해당 프로퍼티 파일이 클래스 패스에 위치해야 한다. 따라서 클래스 패스에 포함되는 src/main/resources에 프로퍼티 파일을 위치시킨다.
  
* \<spring:message\> 태그는 스프링 설정에 등록된 messageSource 빈을 이용해서 메시지를 구한다.
  * 이 태그를 실행하면 내부적으로 MessageSource의 getMessage() 메서드를 실행해서 필요한 메시지를 구한다.
  
* \<spring:message\> 태그의 code 속성에 지정한 메시지가 존재하지 않으면 에외가 발생한다.

### \<spring:message\> 태그의 메시지 인자 처리

* 프로퍼티 파일에 \{0\}은 인덱스 기반 변수 중 0번 인덱스의 값으로 대치되는 부분을 표시한 것이다.
  * MessageSource의 getMessage() 메서드는 인덱스 기반 변수를 전달하기 위해 Object 배열 타입의 파라미터를 사용한다.
  
* \<spring:message\> 태그를 사용할 때는 arguments 속성을 사용해서 인덱스 기반 변수값을 전달한다.
  * 두 개 이상의 값을 전달해야 할 경우 다음 방법 중 하나를 사용한다.
    * 콤마로 구분한 문자열
    * 객체 배열
    * \<spring:argument\> 태그 사용
    
## 커맨드 객체의 값 검증과 에러 메시지 처리

* 폼 값 검증과 에러 메시지 처리는 애플리케이션 개발 시 중요한 부분 중 하나이다.
* 스프링은 이 두 가지 문제를 처리하기 위해 다음 방법을 제공한다.
  * 커맨드 객체를 검증하고 결과를 에러 코드로 지정
  * JSP에서 에러 코드로부터 메시지를 출력
  
### 커맨드 객체 검증과 에러 코드 지정하기

* 스프링 MVC에서 커맨드 객체의 값이 올바른지 검사하려면 다음의 두 인터페이스를 사용한다.
  * org.springframework.validation.Validator
  * org.springframework.validation.Errors
  
* Validator 인터페이스는 supports() 메서드와 validate() 메서드를 가지고 있다.
  * supports() 메서드는 Validator가 검증할 수 있는 타입인지 검사한다.
  * validate() 메서드는 첫 번째 파라미터로 전달받은 객체를 검증하고 오류 결과를 Errors에 담는 기능을 정의한다.
  
* validate() 메서드는 보통 다음과 같이 구현한다.
  * 검사 대상 객체의 특정 프로퍼티나 상태가 올바른지 검사
  * 올바르지 않다면 Errors의 rejectValue() 메서드를 이용해서 에러 코드 저장
  
* Errors의 rejectValue() 메서드는 첫 번째 파라미터로 프로퍼티의 이름을 전달받고, 두 번째 파라미터로 에러 코드를 전달받는다.
  * JSP 코드에서는 여기서 지정한 에러 코드를 이용해서 에러 메시지를 출력한다.
  
* ValidationUtils 클래스는 객체의 값 검증 코드를 간결하게 작성할 수 있도록 도와준다.
  * 이 클래스의 메서드를 실행할 때 target을 파라미터로 전달받지 않아도 target 객체의 프로퍼티 값을 검사할 수 있는데, 이는 Errors 객체 때문이다.
  * 스프링 MVC에서 Validator를 사용하는 코드는 매핑 애노테이션 적용 메서드에 Errors 타입 파라미터를 전달받고, 이 Errors 객체를 Validator의 validate() 메서드에 두 번째 파라미터로 전달한다.
  * 요청 매핑 애노테이션 적용 메서드의 커맨드 객체 파라미터 뒤에 Errors 타입 파라미터가 위치하면, 스프링 MVC는 메서드를 호출할 때 커맨드 객체와 연결된 Errors 객체를 생성해서 파라미터로 전달한다.
  * 이 Errors 객체는 커맨드 객체의 특정 프로퍼티 값을 구할 수 있는 getFieldValue() 메서드를 제공한다.
  * 따라서 ValidaionUtils의 메서드는 커맨드 객체를 전달받지 않아도 Errors 객체를 이용해서 지정한 값을 구할 수 있다.
  
* 커맨드 객체의 특정 프로퍼티가 아닌 커맨드 객체 자체가 잘못될 수도 있는데, 이런 경우에는 rejectValue() 메서드 대신 reject() 메서드를 사용한다.
  * reject() 메서드는 개별 프로퍼티가 아닌 객체 자체에 에러 코드를 추가하므로 이 에러를 글로벌 에러라고 부른다.
  
* 요청 매핑 애노테이션을 붙인 메서드에 Errors 타입의 파라미터는 반드시 커맨드 객체를 위한 파라미터 다음에 위치해야 한다.
  * 그렇지 않으면 예외가 발생한다.
  
### Errors와 ValidationUtils 클래스의 주요 메서드

* reject(), rejectValue() : 에러 코드 추가 메서드
  * 에러 코드에 해당하는 메시지가 \{0\}이나 \{1\}과 같이 인덱스 기반 변수를 포함하고 있는 경우 Object 배열 타입의 errorArgs 파라미터를 이용해서 변수에 삽입될 값을 전달한다.
  * defaultMessage 파라미터를 가진 메서드를 사용하면, 에러 코드에 해당하는 메시지가 없는 경우 예외를 발생시키는 대신 defaultMessage를 출력한다.
  
* ValidationUtils 클래스는 rejectIfEmpty() 메서드와 rejectIfEmptyOrWhitespace() 메서드를 제공한다.
  * rejectIfEmpty() 메서드는 field에 해당하는 프로퍼티 값이 null이거나 빈 문자열인 경우 에러 코드를 추가한다.
  * rejectIfEmptyOrWhitespace() 메서드는 null이거나 빈 문자열, 그리고 공백 문자로만 값이 구성된 경우 에러 코드를 추가한다.
  
### 커맨드 객체의 에러 메시지 출력하기

* 에러 코드를 지정한 이유는 알맞은 에러 메시지를 출력하기 위함이다.
  * Errors에 에러 코드를 추가하면 JSP는 스프링이 제공하는 \<form:errors\> 태그를 사용해서 에러에 해당하는 메시지를 출력할 수 있다.
  * \<form:errors\> 태그의 path 속성은 에러 메시지를 출력할 프로퍼티 이름을 지정한다.
  * 에러 코드가 두 개 이상 존재하면 각 에러 코드에 해당하는 메시지가 출력된다.
  
* 에러 코드에 해당하는 메시지 코드를 찾을 때에는 다음 규칙을 따른다.
  * 에러코드 + "." + 커맨드객체이름 + "." + 필드명
  * 에러코드 + "." + 필드명
  * 에러코드 + "." + 필드타입
  * 에러코드
  
* 프로퍼티 타입이 List나 목록인 경우 다음 순서를 사용해서 메시지 코드를 생성한다.
  * 에러코드 + "." + 커맨드객체이름 + "." + 필드명[인덱스].중첩필드명
  * 에러코드 + "." + 커맨드객체이름 + "." + 필드명.중첩필드명
  * 에러코드 + "." + 필드명[인덱스].중첩필드명
  * 에러코드 + "." + 필드명.중첩필드명
  * 에러코드 + "." + 중첩필드명
  * 에러코드 + "." + 필드타입
  * 에러코드
  
* 예를 들어 errors.rejectValue("email, "required") 코드로 "email" 프로퍼티에 "required" 에러 코드를 추가했고 커맨드 객체 이름이 "registerRequest"인 경우 다음 순서대로 메시지 코드를 검색한다.
  * required.registerRequest.email
  * required.email
  * required.String
  * required
  * 이 중에서 먼저 검색되는 메시지 코드를 사용한다.
  
* 커맨드 객체에 추가한 글로벌 에러 코드는 다음 순서대로 메시지 코드를 검색한다.
  * 에러코드 + "." + 커맨드객체이름
  * 에러코드
  
* 메시지를 찾을 때에는 MessageSource를 사용하므로 에러 코드에 해당하는 메시지를 메시지 프로퍼티 파일에 추가해주어야 한다.

### \<form:errors\> 태그의 주요 속성

* \<form:errors\> 커스텀 태그는 프로퍼티에 추가한 에러 코드 개수만큼 에러 메시지를 출력한다. 다음 두 속성을 사용해 각 에러 메시지를 구분해서 표시한다.
  * element : 각 에러 메시지를 출력할 때 사용할 HTML 태그. 기본값은 span
  * delimiter : 각 에러 메시지를 구분할 때 사용할 HTML 태그. 기본값은 \<br/\>
  
## 글로벌 범위 Validator와 컨트롤러 범위 Validator

* 스프링 MVC는 모든 컨트롤러에 적용할 수 있는 글로벌 Validator와 단일 컨트롤러에 적용할 수 있는 Validator를 설정하는 방법을 제공한다.
  * 이를 사용하면 @Valid 애노테이션을 사용해서 커맨드 객체에 검증 기능을 적용할 수 있다.
  
### 글로벌 범위 Validator 설정과 @Valid 애노테이션

* 글로벌 범위 Validator는 모든 컨트롤러에 적용할 수 있는 Validator이다. 글로벌 범위 Validator를 적용하려면 다음 두 가지를 설정하면 된다.
  * 설정 클래스에서 WebMvcConfigurer의 getValidator() 메서드가 Validator 구현 객체를 반환하도록 구현
  * 글로벌 범위 Validator가 검증할 커맨드 객체에 @Valid 애노테이션 적용
  
* 스프링 MVC는 WebMvcConfigurer 인터페이스의 getValidator() 메서드가 반환한 객체를 글로벌 범위 Validator로 사용한다.
  * 글로벌 범위 Validator를 지정하면 @Valid 애노테이션을 사용해서 Validator를 적용할 수 있다.
  * 커맨드 객체에 해당하는 파라미터에 @Valid 애노테이션을 붙이면 글로벌 범위 Validator가 해당 타입을 검증할 수 있는지 확인한다.
  * 검증 가능하면 실제 검증을 수행하고 그 결과를 Errors에 저장한다. 이는 요청 처리 메서드 실행 전에 적용된다.
  * 단, @Valid 애노테이션을 사용할 때 반드시 Errors 타입 파라미터가 있어야 한다.
  
### @InitBinder 애노테이션을 이용한 컨트롤러 범위 Validator

* @InitBinder 애노테이션을 이용하면 컨트롤러 범위 Validator를 설정할 수 있다.

* 컨트롤러에서 커맨드 객체 파라미터에 @Valid 애노테이션을 적용하고 있다면 어떤 Validator가 커맨드 객체를 검증할지를 initBinder() 메서드가 결정한다.
  * @InitBinder 애노테이션을 적용한 메서드는 WebDataBinder 타입 파라미터를 갖는데 이의 setValidator() 메서드를 이용해 컨트롤러 범위에 적용할 Validator를 설정할 수 있다.
  * 이렇게 설정한 Validator는 @Valid 애노테이션을 붙인 커맨드 객체 검증 시 사용한다.
  
* @InitBinder가 붙은 메서드는 컨트롤러의 요청 처리 메서드를 실행하기 전에 매번 실행된다.

* WebDataBinder는 내부적으로 글로벌 범위 Validator를 기본으로 포함하지만, 만약 setValidator() 메서드를 실행하면 기존의 Validator를 삭제하고 파라미터로 전달받은 Validator를 목록에 추가한다.
  * 따라서 setValidator() 메서드를 사용하면 글로벌 범위 Validator 대신 컨트롤러 범위 Validator를 사용하게 된다.
  * 다만, addValidator() 메서드를 통해 기존 Validator 뒤에 새 Validator를 추가하는데, 이때 기존에 있던 Validator를 먼저 적용한다.
  
## Bean Validation을 이용한 값 처리

* Bean Validation 스펙 내 애노테이션에는 @Valid뿐만 아니라 @NotNull, @Digits, @Size 등의 애노테이션을 정의하고 있다.
  * 이 애노테이션을 사용하면 Validator 작성 없이 애노테이션만으로 커맨드 객체의 값 검증을 처리할 수 있다.
  
* Bean Validation이 제공하는 애노테이션을 이용해 커맨드 객체의 값을 검증하는 방법
  * Bean Validation과 관련된 의존을 설정에 추가한다.
  * 커맨드 객체에 @NotNull, @Digits 등의 애노테이션을 이용해서 검증 규칙을 설정한다.
  
* OptionalValidatorFactoryBean 클래스를 빈으로 등록하면 Bean Validation 애노테이션을 적용한 커맨드 객체를 검증할 수 있다.
  * @EnableWebMvc 애노테이션을 사용하면 이를 글로벌 범위 Validator로 등록하므로 추가로 설정할 것은 없다.
  * 단, 스프링 MVC는 별도로 설정한 글로벌 범위 Validator가 없을 때에 OptionalValidatorFactoryBean을 글로벌 범위 Validator로 사용하므로 글로벌 범위 Validator가 있다면 해당 설정을 삭제한다.
  
* 오류 메시지의 경우 별도 설정을 하지 않으면 Bean Validation 프로바이더가 제공하는 기본 에러 메시지가 출력된다.
  * 원하는 에러 메시지를 사용하려면 다음 규칙을 따르는 메시지 코드를 메시지 프로퍼티 파일에 추가한다.
    * 애노테이션이름.커맨드객체모델명.프로퍼티명
    * 애노테이션이름.프로퍼티명
    * 애노테이션이름
    
### Bean Validation의 주요 애노테이션

* @AssertTrue, @AssertFalse
  * 값이 true인지 또는 false인지 검사한다. 
  * null은 유효하다고 판단한다.
* @DecimalMax, @DecimalMin
  * 주요 속성 : String value(최대값 또는 최소값), boolean inclusive(지정값 포함 여부, 기본값 : true)
  * 지정한 값보다 작거나 같은지 또는 크거나 같은지 검사한다. 
  * inclusive가 false면 value로 지정한 값은 포함하지 않는다. 
  * null은 유효하다고 판단한다.
* @Max, @Min
  * 주요 속성 : long value
  * 지정한 값보다 작거나 같은지 또는 크거나 같은지 검사한다. 
  * null은 유효하다고 판단한다.
* @Digits
  * 주요 속성 : int integer(최대 정수 자릿수), int fraction(최대 소수점 자릿수)
  * 자릿수가 지정한 크기를 넘지 않는지 검사한다.
  * null은 유효하다고 판단한다.
* @Size
  * 주요 속성 : int min(최소 크기, 기본값 : 0), int max(최대 크기, 기본값 : 정수 최대값)
  * 길이나 크기가 지정한 값 범위에 있는지 검사한다.
  * null은 유효하다고 판단한다.
* @Null, @NotNull
  * 값이 null인지 또는 null이 아닌지 검사한다.
* @Pattern
  * 주요 속성 : String regexp(정규 표현식)
  * 값이 정규 표현식에 일치하는지 검사한다.
  * null은 유효하다고 판단한다.
* @NotNull을 제외한 나머지 애노테이션은 검사 대상 값이 null인 경우 유효하다고 판단하기 때문에 필수 입력 값을 검사할 때는 @NotNull과 @Size를 함께 사용해야 한다.

* 아래 애노테이션들은 Bean Validation 2.0에 추가됐다.

* @NotEmpty : 문자열이나 배열의 경우 null이 아니고 길이가 0이 아닌지, 콜렉션의 경우 null이 아니고 크기가 0이 아닌지 검사한다.
* @NotBlank : null이 아니고 최소한 한 개 이상의 공백아닌 문자를 포함하는지 검사한다.
* @Positive, @PositiveOrZero : 양수인지 혹은 0 또는 양수인지 검사한다. null은 유효하다고 판단한다.
* @Negative, @NegativeOrZero : 음수인지 혹은 0 또는 음수인지 검사한다. null은 유효하다고 판단한다.
* @Email : 이메일 주소가 유효한지 검사한다. null은 유효하다고 판단한다.
* @Future, @FutureOrPresent : 해당 시간이 미래 시간인지, 혹은 현재 또는 미래 시간인지 검사한다. null은 유효하다고 판단한다.
* @Past, @PastOrPresent : 해당 시간이 과거 시간인지, 혹은 현재 또는 과거 시간인지 검사한다. null은 유효하다고 판단한다.