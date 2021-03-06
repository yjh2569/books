# Chapter 11 : MVC 1 : 요청 매핑, 커맨드 객체, 리다이렉트, 폼 태그, 모델

## 요청 매핑 애노테이션을 이용한 경로 매핑

* 웹 어플리케이션 개발은 다음 코드를 작성하는 것이다.
  * 특정 요청 URL을 처리할 코드
  * 처리 결과를 HTML과 같은 형식으로 응답하는 코드
  
* 첫 번째는 @Controller 애노테이션을 사용한 컨트롤러 클래스를 이용해서 구현한다.
  * 컨트롤러 클래스는 요청 매핑 애노테이션을 사용해서 메서드가 처리할 요청 경로를 지정한다.
  * 요청 매핑 애노테이션에는 @RequestMapping, @GetMapping, @PostMapping 등이 있다.
  
* 요청 매핑 애노테이션을 적용한 메서드를 두 개 이상 정의할 수도 있다.
  * 이때 한 컨트롤러 클래스 내 모든 요청 매핑 애노테이션의 경로가 동일한 경로로 시작하는 경우 공통되는 부분의 경로를 담은 @RequestMapping 애노테이션을 클래스에 적용하고 각 메서드는 나머지 경로를 값으로 갖는 요청 매핑 애노테이션을 적용할 수 있다.
  * 이렇게 하면 스프링 MVC는 클래스에 적용한 요청 매핑 애노테이션의 경로와 메서드에 적용한 요청 매핑 애노테이션의 경로를 합쳐서 경로를 찾는다.
  
## GET과 POST 구분: @GetMapping, @PostMapping

* 스프링 MVC는 별도 설정이 없으면 GET과 POST 방식에 상관없이 @RequestMapping에 지정한 경로와 일치하는 요청을 처리한다.
  * GET(POST) 방식 요청만 처리하고 싶다면 @GetMapping(@PostMapping) 애노테이션을 사용해서 제한할 수 있다.
  * 두 애노테이션을 사용하면 같은 경로에 대해 GET과 POST 방식을 각각 다른 메서드가 처리하도록 설정할 수 있다.
  
* @GetMapping, @PostMapping은 스프링 4.3 버전에 추가된 것으로, 이전 버전까지는 @RequestMapping 애노테이션의 method 속성을 사용해서 HTTP 방식을 제한했다.

## 요청 파라미터 접근

* 컨트롤러 메서드에서 요청 파라미터를 사용하는 방법
  * HttpServletRequest를 직접 이용 : HttpServletRequest의 getParameter() 메서드를 이용해서 파라미터 값을 구한다.
  * @RequestParam 애노테이션을 사용 : 요청 파라미터 개수가 몇 개 안 되는 경우 각 요청 파라미터마다 이 애노테이션을 사용해서 값을 구한다.
    * @RequestParam(value="요청_파라미터의_이름", required="필수_여부", defaultValue="요청_파라미터의_값이_없는_경우_Default_값") Type parameter
    * 스프링 MVC는 파라미터 타입에 맞게 String 값을 변환해준다.
    
## 리다이렉트 처리

* 컨트롤러에서 특정 페이지로 리다이렉트시키기 위해서는 "redirect:경로"를 뷰 이름으로 반환하면 된다.
  * "redirect:" 뒤의 문자열이 "/"로 시작하면 웹 어플리케이션을 기준으로 이동 경로를 생성한다.
  * "/"로 시작하지 않으면 현재 경로를 기준으로 상대 경로를 사용한다.
  
## 커맨드 객체를 이용해서 요청 파라미터 사용하기

* 스프링은 요청 파라미터의 개수가 많아지는 경우를 위해 요청 파라미터의 값을 커맨드 객체에 담아주는 기능을 제공한다.
  * 예를 들어 이름이 name인 요청 파라미터의 값을 커맨드 객체의 setName() 메서드를 사용해서 커맨드 객체에 전달하는 기능을 제공한다.
  * 요청 파라미터의 값을 전달받을 수 있는 setter 메서드를 포함하는 객체를 커맨드 객체로 사용하면 된다.
  * 커맨드 객체는 요청 매핑 애노테이션이 적용된 메서드의 파라미터에 위치한다.
  * 스프링 MVC는 메서드에 전달할 객체를 생성하고 그 객체의 setter 메서드를 이용해서 일치하는 요청 파라미터의 값을 전달한다.
  
## 뷰 JSP 코드에서 커맨드 객체 사용하기

* 스프링 MVC는 커맨드 객체의 (첫 글자를 소문자로 바꾼) 클래스 이름과 동일한 속성 이름을 사용해서 커맨드 객체를 뷰에 전달한다.
  * 커맨드 객체의 클래스 이름이 RegisterRequest인 경우 JSP 코드는 registerRequest라는 이름을 사용해서 커맨드 객체에 접근할 수 있다.
  
## @ModelAttribute 애노테이션으로 커맨드 객체 속성 이름 변경

* 커맨드 객체에 접근할 때 사용할 속성 이름을 변경하고 싶다면 커맨드 객체로 사용할 파라미터에 @ModelAttribute 애노테이션을 적용하면 된다.
  * @ModelAttribute 애노테이션은 모델에서 사용할 속성 이름을 값으로 설정한다. 이 값으로 뷰 코드에서 커맨드 객체에 접근할 수 있다.
  
## 커맨드 객체와 스프링 폼 연동

* 기본적으로 비어 있는 폼에 이전에 입력했던 값을 다시 보여주고자 할 때 커맨드 객체의 값을 폼에 채워주면 된다.

* 스프링 MVC가 제공하는 커스텀 태그를 사용하면 좀 더 간단하게 커맨드 객체의 값을 출력할 수 있다.
  * <form:form> 태그와 <form:input> 태그를 제공해 이 두 태그를 사용하면 커맨드 객체의 값을 폼에 출력할 수 있다.
  
* \<form:form\> 태그의 속성
  * action : \<form\> 태그의 action 속성과 동일한 값 사용
  * modelAttribute : 커맨드 객체의 속성 이름을 지정한다. 설정하지 않으면 "command"를 기본값으로 사용한다.

* \<form:input\> : \<input\> 태그 생성
  * path로 지정한 커맨드 객체의 프로퍼티를 \<input\> 태그의 value 속성값으로 사용한다.
  
* 단, \<form:form\> 태그를 사용하려면 커맨드 객체가 반드시 존재해야 한다.
  * 따라서 특별히 커맨드 객체가 필요없는 경우에도 커맨드 객체를 강제로 추가해야 한다.

## 컨트롤러 구현 없는 경로 매핑

* 단순히 요청 경로와 뷰 이름을 연결해주기 위해 컨트롤러 클래스를 만드는 것은 성가신 일이다.
  * 이때 WebMvcConfigurer 인터페이스의 addViewControllers() 메서드를 사용하면 이를 해결할 수 있다.
  
## 주요 에러 발생 상황

### 요청 매핑 애노테이션과 관련된 주요 예외

* 404 에러 : 요청 경로를 처리할 컨트롤러가 존재하지 않거나 WebMvcConfigurer를 이용한 실정이 없는 경우 발생한다.
  * 요청 경로가 올바른지 확인한다.
  * 컨트롤러에 설정한 경로가 올바른지 확인한다.
  * 컨트롤러 클래스를 빈으로 등록했는지 확인한다.
  * 컨트롤러 클래스에 @Controller 애노테이션을 적용했는지 확인한다.
* 뷰 이름에 해당하는 JSP 파일이 존재하지 않아도 404 에러가 발생한다.
  * 컨트롤러에서 반환하는 뷰 이름에 해당하는 JSP 파일이 존재하는지 확인해야 한다.
* 지원하지 않는 전송 방식(method)을 사용한 경우 405 에러가 발생한다.
  * 예를 들어 POST 방식만 처리하는 요청 경로를 GET 방식으로 연결하는 경우 발생한다.
  
### @RequestParam이나 커맨드 객체와 관련된 주요 예외

* @RequestParam 애노테이션을 필수로 설정한 상태에서 요청에서 이에 해당하는 파라미터를 전송하지 않는 경우 400 에러를 응답으로 전송한다.
  * 요청 파라미터의 값을 @RequestParam이 적용된 파라미터의 타입으로 변환할 수 없는 경우에도 같은 에러가 발생한다.
  * 요청 파라미터의 값을 커맨드 객체에 복사하는 과정에서도 동일하게 발생한다.
  * 콘솔에 출력된 로그 메시지를 참고하면 도움이 된다.
  
## 커맨드 객체 : 중첩 및 콜렉션 프로퍼티

* 스프링 MVC는 커맨드 객체가 리스트 타입의 프로퍼티를 가졌거나 중첩 프로퍼티를 가진 경우에도 요청 파라미터의 값을 알맞게 커맨드 객체에 설정해주는 기능을 제공하고 있다.
  * HTTP 요청 파라미터의 이름이 "프로퍼티이름[인덱스]" 형식이면 List 타입 프로퍼티의 값 목록으로 처리한다.
  * HTTP 요청 파라미터의 이름이 "프로퍼티이름.프로퍼티이름"과 같은 형식이면 중첩 프로퍼티 값을 처리한다.
  
## Model을 통해 컨트롤러에서 뷰에 데이터 전달하기

* 컨트롤러에서 뷰로 응답 화면을 구성하는데 필요한 데이터를 생성해서 전달하는 경우 Model을 사용한다.
  * 요청 매핑 애노테이션이 적용된 메서드의 파라미터에 Model을 추가한다.
  * Model 파라미터의 addAttribute() 메서드로 뷰에서 사용할 데이터를 전달한다.
  
### ModelAndView를 통한 뷰 선택과 모델 전달

* 지금까지 구현한 컨트롤러의 특징
  * Model을 이용해서 뷰에 전달할 데이터 설정
  * 결과를 보여줄 뷰 이름을 반환 
* ModelAndView를 사용하면 이 두 가지를 한 번에 처리할 수 있다.
  * 요청 매핑 애노테이션을 적용한 메서드는 String 타입 대신 ModelAndView를 반환할 수 있다.
  * ModelAndView는 모델과 뷰 이름을 함께 제공한다.
  
### GET 방식과 POST 방식에 동일 이름 커맨드 객체 사용하기

* <form:form> 태그를 사용하려면 커맨드 객체가 반드시 존재해야 한다.
  * 이를 위해 Model에 직접 객체를 추가했으나, 커맨드 객체를 파라미터로 추가하면 좀 더 간단해진다.
  * 이름을 명시적으로 지정하려면 @ModelAttribute 애노테이션을 사용한다.
  
## 주요 폼 태그 설명

* 스프링 MVC는 HTML 폼과 커맨드 객체를 연동하기 위한 JSP 태그 라이브러리를 제공한다.
* 이 두 태그 외에도 \<select\>를 위한 태그와 체크박스나 라디오 버튼을 위한 커스텀 태그도 제공한다.
  
### \<form:form\>

* method 속성과 action 속성을 지정하지 않으면 method 속성값은 "post"로 설정되고 action 속성값은 현재 요청 URL로 설정된다.
* 생성된 \<form\> 태그의 id 속성값으로 입력 폼의 값을 저장하는 커맨드 객체의 이름을 사용한다.
  * 커맨드 객체 이름이 기본값인 "command"가 아니면 modelAttribute 속성값으로 커맨드 객체의 이름을 설정해야 한다.
* 기타 속성(form 태그와 동일)
  * action : 폼 데이터를 전송할 URL
  * enctype : 전송될 데이터의 인코딩 타입
  * method : 전송 방식

### \<input\> 관련 커스텀 태그

* \<input\> 관련 커스텀 태그의 종류
  * \<form:input\> : text 타입의 \<input\> 태그
  * \<form:password\> : password 타입의 \<input\> 태그
  * \<form:hidden\> : hidden 타입의 \<input\> 태그
* \<form:input\> 커스텀 태그는 path 속성을 사용해 연결할 커맨드 객체의 프로퍼티를 지정한다.
  * 이 path 속성은 \<input\> 태그의 id와 name 속성에 대응하고, value 속성에는 path 속성으로 지정한 커맨드 객체의 프로퍼티 값이 출력된다.
  * 이는 \<form:password\>, \<form:hidden\>도 동일하게 적용된다.
  
### \<select\> 관련 커스텀 태그

* \<select\> 관련 커스텀 태그
  * \<form:select\> : \<select\> 태그를 생성한다. \<option\> 태그를 생성할 때 필요한 콜렉션을 전달받을 수도 있다.
  * \<form:options\> : 지정한 콜렉션 객체를 이용해 \<option\> 태그를 생성한다.
  * \<form:option\> : \<option\> 태그 한 개를 생성한다.
* \<form:select\> 커스텀 태그를 사용하면 뷰에 전달한 모델 객체를 갖고 간단하게 \<select\>와 \<option\> 태그를 생성할 수 있다.
  * path 속성은 커맨드 객체의 프로퍼티 이름을 입력하고, items 속성에는 \<option\> 태그를 생성할 때 사용할 콜렉션 객체를 지정한다.
  * 콜렉션 객체의 값을 이용해서 \<option\> 태그의 value 속성과 텍스트를 설정한다.
* \<form:select\> 안에 \<form:options\> 태그를 중첩해서 사용할 수 있다.
  * \<form:options\> 태그의 items 속성에 값 목록으로 사용할 모델 이름을 설정한다.
  * 주로 콜렉션에 없는 값을 \<option\> 태그로 추가할 때 사용한다.
* \<form:option\> 태그는 \<option\> 태그를 직접 지정할 때 사용한다.
  * 몸체 내용을 입력하지 않으면 value 속성에 지정한 값을 텍스트로 사용한다.
  * label 속성을 사용하면 그 값을 텍스트로 사용한다.
  
* \<option\> 태그를 생성하는데 사용할 콜렉션 자체가 String이 아닐 수도 있다.
  * 콜렉션에 저장된 객체의 특정 프로퍼티를 사용해야 하는 경우 itemValue 속성과 itemLabel 속성을 사용한다.
  
* 커맨드 객체의 프로퍼티 값과 일치하는 값을 갖는 \<option\>을 자동으로 선택해준다.
  * selected 속성을 추가하는 방식으로 선택한다.
  
### 체크박스 관련 커스텀 태그

* checkbox 타입의 \<input\> 태그와 관련된 커스텀 태그
  * \<form:checkboxes\> : 커맨드 객체의 특정 프로퍼티와 관련된 checkbox 타입의 \<input\> 태그 목록을 생성한다.
  * \<form:checkbox\> : 커맨드 객체의 특정 프로퍼티와 관련된 한 개의 checkbox 타입 \<input\> 태그를 생성한다.
* \<form:checkboxes\> 커스텀 태그는 items 속성을 이용해 값으로 사용할 콜렉션을, path 속성으로 커맨드 객체의 프로퍼티를 지정한다.
* \<option\> 태그와 마찬가지로 콜렉션에 저장된 객체가 String이 아니면 itemValue 속성과 itemLabel 속성을 이용해서 값과 텍스트로 사용할 객체의 프로퍼티를 지정한다.
* \<form:checkbox\> 커스텀 태그는 연결되는 값 타입에 따라 처리 방식이 달라진다.
  * \<form:checkbox\>은 연결되는 프로퍼티 값이 true이면 "checked" 속성을 설정한다. 또한 생성되는 \<input\> 태그의 value 속성값은 "true"가 된다.
  * 프로퍼티가 배열이나 Collection일 경우 해당 콜렉션에 값이 포함되어 있다면 "checked" 속성을 설정한다.
  
### 라디오버튼 관련 커스텀 태그

* radio 타입의 \<input\> 태그를 위한 커스텀 태그
  * \<form:radiobuttons\> : 커맨드 객체의 특정 프로퍼티와 관련된 radio 타입의 \<input\> 태그 목록을 생성한다.
  * \<form:radiobutton\> : 커맨드 객체의 특정 프로퍼티와 관련된 한 개의 radio 타입 \<input\> 태그를 생성한다.
* items 속성에 값으로 사용할 콜렉션을 전달받고 path 속성에 커맨드 객체의 프로퍼티를 지정한다.

### \<textarea\> 태그를 위한 커스텀 태그

* 스프링은 \<form:textarea\> 커스텀 태그를 제공하고 있다.
  * 이 태그를 이용하면 커맨드 객체와 관련된 \<textarea\> 태그를 생성할 수 있다.
  
### CSS 및 HTML 태그와 관련된 공통 속성

* 스프링 커스텀 태그는 HTML의 CSS 및 이벤트 관련 속성을 제공하고 있다.
* CSS와 관련된 속성
  * cssClass : HTML의 class 속성값
  * cssErrorClass : 폼 검증 에러가 발생했을 때 사용할 HTML의 class 속성값(스프링은 폼 검증 기능을 제공한다.)
  * cssStyle : HTML의 style 속성값
* HTML 태그가 사용하는 다음 속성도 사용 가능하다.
  * id, title, dir
  * disabled, tabindex
  * onfocus, onblur, onchange
  * onclick, ondblclick
  * onkeydown, onkeypress, onkeyup
  * onmousedown, onmousemove, onmouseup
  * onmouseout, onmouseover