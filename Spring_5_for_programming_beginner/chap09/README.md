# Chapter 9 : 스프링 MVC 시작하기

## 스프링 MVC를 위한 설정

* @EnableWebMvc 애노테이션을 사용하면 내부적으로 다양한 빈 설정을 추가해준다.

* WebMvcConfigurer 인터페이스는 스프링 MVC의 개별 설정을 조정할 때 사용한다.
  * configureDefaultServletHandling() 메서드와 configureViewResolvers() 메서드는 WebMvcConfigurer 인터페이스에 정의된 메서드다.
  * 각각 디폴트 서블릿과 ViewResolver와 관련된 설정을 조절한다.
  
### web.xml 파일에 DispatcherServlet 설정

* 스프링 MVC가 웹 요청을 처리하려면 DispatcherServlet을 통해서 웹 요청을 받아야 한다.
  * 이를 위해 web.xml 파일에 DispatcherServlet을 등록한다.
  
* DispatcherServlet은 초기화 과정에서 contextConfiguration 초기화 파라미터에 지정한 설정 파일을 이용해서 스프링 컨테이너를 초기화한다.

## 코드 구현

### 컨트롤러 구현

* 스프링 MVC 프레임워크에서 컨트롤러는 웹 요청을 처리하고 그 결과를 뷰에 전달하는 스프링 빈 객체이다.
  * 스프링 컨트롤러로 사용될 클래스는 @Controller 애노테이션을 붙여야 하고, @GetMapping 애노테이션이나 @PostMapping 애노테이션과 같은 요청 매핑 애노테이션을 이용해서 처리할 경로를 지정해 주어야 한다.
  
* @GetMapping 애노테이션의 값은 서블릿 컨텍스트 경로(또는 웹 어플리케이션 경로)를 기준으로 한다.
  * 예를 들어 톰캣의 경우 webapps\sp5-chap09 폴더는 웹 브라우저에서 http://host/sp5-chap09 경로에 해당하는데, 이때 sp5-chap09가 컨텍스트 경로가 된다.
  * 컨텍스트 경로가 /sp5-chap09이므로 http://host/sp5-chap09/main/list 경로를 처리하기 위한 컨트롤러는 @GetMapping("/main/list")를 사용해야 한다.
  
* @RequestParam 애노테이션은 HTTP 요청 파라미터를 메서드의 파라미터로 전달받을 수 있게 해준다.
  * @RequestParam 애노테이션의 value 속성은 HTTP 요청 파라미터의 이름을 지정하고 required 속성은 필수 여부를 지정한다.

* Model 객체의 addAttribute() 메서드는 뷰에 전달할 데이터를 지정하기 위해 사용된다.
  * 첫 번째 파라미터는 데이터를 식별하는데 사용되는 속성 이름, 두 번째 파라미터는 속성 이름에 해당하는 값이다.