# Chapter 10 : 스프링 MVC 프레임워크 동작 방식

## 스프링 MVC 핵심 구성 요소

* DispatcherServlet
  * 웹 브라우저로부터 요청이 들어오면 그 요청을 처리하기 위한 컨트롤러 객체를 검색한다.
  * 이때 직접 컨트롤러를 검색하지 않고 HandlerMapping이라는 빈 객체에게 컨트롤러 검색을 요청한다.
  * 컨트롤러 객체를 전달받았다고 해서 바로 컨트롤러 객체의 메서드를 실행할 수는 없다. 
  * @Controller 애노테이션을 이용해 구현한 컨트롤러뿐만 아니라 Controller 인터페이스를 구현한 컨트롤러, 특수 목적으로 사용되는 HttpRequestHandler 인터페이스를 구현한 클래스를 동일한 방식으로 실행할 수 있도록 만들어졌는데, 이를 위해 HandlerAdapter 빈을 사용한다.

* HandlerMapping
  * 클라이언트의 요청 경로를 이용해서 이를 처리할 컨트롤러 빈 객체를 DispatcherServlet에 전달한다.
  * 예를 들어 웹 요청 경로가 "/hello"라면 등록된 컨트롤러 빈 중에서 "/hello" 요청 경로를 처리할 컨트롤러를 반환한다.
  
* HandlerAdapter
  * DispatcherServlet이 HandlerMapping이 찾아준 컨트롤러 객체를 처리하기 위해 HandlerAdapter 빈에게 요청 처리를 위임한다.
  * 컨트롤러의 알맞은 메서드를 호출해서 요청을 처리하고 그 결과를 DispatcherServlet에 반환한다.
  * 이때 컨트롤러의 처리 결과를 ModelAndView라는 객체로 변환해서 DispatcherServlet에 반환한다.
  
* ViewResolver
  * HandlerAdapter로부터 ModelAndView를 받으면 DispatcherServlet은 결과를 보여줄 뷰를 찾기 위해 ViewResolver 빈 객체를 사용한다.
  * ModelAndView는 컨트롤러가 반환한 뷰 이름을 담고 있는데 ViewResolver는 이 뷰 이름에 해당하는 View 객체를 찾거나 생성해서 반환한다.
  * 응답을 생성하기 위해 JSP를 사용하는 ViewResolver는 매번 새로운 View 객체를 생성해서 DispatcherServlet에 반환한다.
  
* DispatcherServlet은 ViewResolver가 반환한 View 객체에게 응답 결과 생성을 요청한다.
  * JSP를 사용하는 경우 View 객체는 JSP를 실행함으로써 웹 브라우저에 전송할 응답 결과를 생성하고 모든 과정이 끝이 난다.
  
* 전체적인 처리 과정
  * 웹 브라우저 -> DispatcherServlet -> HandlerMapping -> DispatcherServlet -> HandlerAdapter -> 컨트롤러 -> HandlerAdapter -> DispatcherServlet -> ViewResolver -> DispatcherServlet -> View -> JSP
  
### 컨트롤러와 핸들러

* 클라이언트의 요청을 실제로 처리하는 곳은 컨트롤러고 DispatcherServlet은 클라이언트의 요청을 전달받는 창구 역할을 한다.

* 보통 @Controller 애노테이션을 붙인 클래스를 이용해서 클라이언트의 요청을 처리하지만 원한다면 자신이 직접 만든 클래스를 이용해서 클라이언트의 요청을 처리할 수도 있다.
  * 즉 DispatcherServlet 입장에서는 클라이언트 요청을 처리하는 객체의 타입이 반드시 @Controller를 적용한 클래스일 필요는 없다.
  * 실제로 스프링이 클라이언트의 요청을 처리하기 위해 제공하는 타입 중에는 HttpRequestHandler도 존재한다.
  * 이 때문에 스프링 MVC는 웹 요청을 실제로 처리하는 객체를 Handler라고 표현한다. 즉, @Controller 적용 객체나 Controller 인터페이스를 구현한 객체는 모두 스프링 MVC 입장에서는 핸들러가 된다.
  * 따라서 특정 요청 경로를 처리해주는 핸들러를 찾아주는 객체를 HandlerMapping이라고 부른다.
  
* DispatcherServlet은 핸들러 객체의 실제 타입에 상관없이 실행 결과를 ModelAndView라는 타입으로만 받을 수 있으면 된다.
  * 그런데 핸들러의 실제 구현 타입에 따라 ModelAndView를 반환하는 객체도 있고, 그렇지 않은 객체도 있다.
  * 따라서 핸들러의 처리 결과를 ModelAndView로 변환해주는 객체가 필요한데, 그게 바로 HandlerAdapter이다.
  
* 핸들러 객체의 실제 타입마다 그에 알맞은 HandlerMapping과 HandlerAdapter가 존재하기 때문에 사용할 핸들러 종류에 따라 해당 HandlerMapping과 HandlerAdapter를 스프링 빈으로 등록해야 한다.
  * 물론 스프링이 제공하는 설정 기능을 사용하면 이 두 종류의 빈을 직접 등록하지 않아도 된다.
  
## DispatcherServlet과 스프링 컨테이너

* web.xml에서 DispatcherServlet은 전달받은 설정 파일(contextConfigLocation을 통해 위치를 알아낸다.)을 이용해서 스프링 컨테이너를 생성한다.
  * HandlerMapping, HandlerAdapter, 컨트롤러, ViewResolver 등의 빈은 DispatcherServlet이 생성한 스프링 컨테이너에서 구한다.
  * 따라서 DispatcherServlet이 사용하는 설정 파일에 이들 빈에 대한 정의가 포함되어 있어야 한다.
  
## @Controller를 위한 HandlerMapping과 HandlerAdapter

* @Controller 적용 객체는 DispatcherServlet 입장에서 한 종류의 핸들러 객체이다.
  * DispatcherServlet은 웹 브라우저의 요청을 처리할 핸들러 객체를 찾기 위해 HandlerMapping을, 핸들러를 실행하기 위해 HandlerAdapter를 사용한다.
  * DispatcherServlet은 스프링 컨테이너에서 HandlerMapping과 HandlerAdapter 타입의 빈을 사용하므로 핸들러에 알맞은 HandlerMapping 빈과 HandlerAdapter 빈이 스프링 설정에 등록되어 있어야 한다.
  * 그런데 9장에서 작성한 예제에서는 HandlerMapping이나 HandlerAdapter 클래스를 빈으로 등록하는 코드는 없고, @EnableWebMvc 애노테이션만 추가했다.
  
* @EnableWebMvc는 매우 다양한 스프링 빈 설정을 추가해주는데, 이 태그가 빈으로 추가해주는 클래스 중에는 @Controller 타입의 핸들러 객체를 처리하기 위한 두 클래스도 포함되어 있다.
  * RequestMappingHandlerMapping과 RequestMappingHandlerAdapter 클래스를 포함한다.
  * RequestMappingHandlerMapping은 @Controller 애노테이션이 적용된 객체의 요청 매핑 애노테이션(@GetMapping) 값을 이용해서 웹 브라우저의 요청을 처리할 컨트롤러 빈을 찾는다.
  * RequestMappingHandlerAdapter는 컨트롤러의 메서드를 알맞게 실행하고 그 결과를 ModelAndView 객체로 변환해서 DispatcherServlet에 반환한다.
  * RequestMappingHandlerAdapter는 컨트롤러 메서드 결과 값이 String 타입이면 해당 값을 뷰 이름으로 갖는 ModelAndView 객체를 생성해서 DispatcherServlet에 반환한다. 
  * 이때 @Controller 클래스 내 메서드의 첫 번째 파라미터로 전달한 Model 객체에 보관된 값도 ModelAndView에 함께 전달한다.
  
## WebMvcConfigurer 인터페이스와 설정

* @EnableWebMvc 애노테이션
  * @Controller 애노테이션을 붙인 컨트롤러를 위한 설정을 생성한다.
  * WebMvcConfigurer 타입의 빈 객체의 메서드를 호출해서 MVC 설정을 추가한다.
  
* ViewResoler 설정을 추가하기 위해 WebMvcConfigurer 타입인 빈 객체의 configureViewResolvers() 메서드를 호출한다.
  * 따라서 WebMvcConfigurer 인터페이스를 구현한 설정 클래스는 configureViewResolvers() 메서드를 재정의해서 알맞은 뷰 관련 설정을 추가하면 된다.

* 스프링 5 버전은 자바 8 버전부터 지원하는 디폴트 메서드를 사용해서 WebMvcConfigurer 인터페이스 메서드에 기본 구현을 제공하고 있다.
  * 기본 구현은 모두 빈 구현으로, 이 인터페이스를 상속한 설정 클래스는 재정의가 필요한 메서드만 구현하면 된다.
  
## JSP를 위한 ViewResolver

* WebMvcConfigurer 인터페이스에 정의된 configureViewResolvers() 메서드는 ViewResolverRegistry 타입의 registry 파라미터를 갖는다.
  * ViewResolverRegistry#jsp() 메서드를 사용하면 JSP를 위한 ViewResolver를 설정할 수 있다.
  
```
@Override
public void configureViewResolvers(ViewResolverRegistry registry) {
    registry.jsp("/WEB-INF/view", ".jsp");
}
```

* 위 설정은 InternalResourceViewResolver 클래스를 이용해서 다음 설정과 같은 빈을 등록한다.

```
@Bean
public ViewResolver viewResolver() {
    InternalResourceViewResolver vr = new InternalResourceViewResolver();
    vr.setPrefix("/WEB-INF/view/");
    vr.setSuffix(".jsp");
    return vr;
}
```

* 컨트롤러의 실행 결과를 받은 DispatcherServlet은 ViewResolver에게 뷰 이름에 해당하는 View 객체를 요청한다.
  * 이때 InternalResourceViewResolver는 "prefix+뷰이름+suffix"에 해당하는 경로를 뷰 코드로 사용하는 InternalResourceView 타입의 View 객체를 반환한다.
  * 예를 들어 뷰 이름이 "hello"라면 "/WEB-INF/view/hello.jsp" 경로를 뷰 코드로 사용하는 InternalResourceView 객체를 반환한다.
  * DispatcherServlet이 InternalResourceView 객체에 응답 생성을 요청하면 InternalResourceView 객체는 경로에 지정한 JSP 코드를 실행해서 응답 결과를 생성한다.
  
* Model에 담긴 값은 View 객체에 Map 형식으로 전달된다.
  * DispatcherServlet은 View 객체에 응답 생성을 요청할 때 Map 객체를 View 객체에 전달한다.
  * View 객체는 전달받은 Map 객체에 담긴 값을 이용해서 알맞은 응답 결과를 출력한다.
  * InternalResourceView는 Map 객체에 담겨 있는 키 값을 request.setAttribute()를 이용해서 request의 속성에 저장한다. 그런 뒤 해당 경로의 JSP를 실행한다.
  * 결과적으로 컨트롤러에서 지정한 Model 속성은 request 객체 속성으로 JSP에 전달되기 때문에 JSP에서 모델에 지정한 속성 이름을 사용해 값을 사용할 수 있다.
  
## 디폴트 핸들러와 HandlerMapping의 우선순위

* web.xml에서 DispatcherServlet에 대한 매핑 경로를 '/'으로 설정했다.
* 매핑 경로가 '/'인 경우 .jsp로 끝나는 요청을 제외한 모든 요청을 DispatcherServlet이 처리한다.
  * 즉 /index.html이나 /css/bootstrap.css와 같이 확장자가 .jsp가 아닌 모든 요청을 DispatcherServlet이 처리하게 된다.
  
* 그런데 @EnableWebMvc 애노테이션이 등록하는 HandlerMapping은 @Controller 애노테이션을 적용한 빈 객체가 처리할 수 있는 요청 경로만 대응할 수 있다.
  * 예를 들어 등록된 컨트롤러가 한 개고 그 컨트롤러가 @GetMapping("/hello") 설정을 사용한다면 /hello 경로만 처리할 수 있게 된다.
  * 따라서 /index.html이나 /css/bootstrap.css와 같은 요청을 처리할 수 있는 컨트롤러 객체를 찾지 못해 DispatcherServlet은 404 응답을 전송한다.
  
* 위 문제를 해결하기 위해 WebMvcConfigurer의 configureDefaultServletHandling() 메서드를 사용한다.
  * DefaultServletHandlerConfigure#enable() 메서드는 다음의 두 빈 객체를 추가한다.
    * DefaultServletHttpRequestHandler : 클라이언트의 모든 요청을 WAS가 제공하는 디폴트 서블릿에 전달한다.
    * 예를 들어 /index.html에 대한 처리를 DefaultServletHttpRequestHandler에 요청하면 이 요청을 다시 디폴트 서블릿에 전달해서 처리하도록 한다.
    * SimpleUrlHandlerMapping : 모든 경로("/**")를 DefaultServletHttpRequestHandler를 이용해서 처리하도록 설정한다.
    
* 위와 같이 메서드를 추가하면 웹 브라우저의 요청이 들어왔을 때 DispatcherServlet은 다음과 같은 방식으로 요청을 처리한다.
  * RequestMappingHandlerMapping을 사용해서 요청을 처리할 핸들러를 검색한다. -> 존재하면 해당 컨트롤러를 이용해서 요청을 처리한다.
  * 존재하지 않으면 SimpleUrlHandlerMapping을 사용해서 요청을 처리할 핸들러를 검색한다.
    * DefaultServletHandlerConfigurer#enable() 메서드가 등록한 SimpleUrlHandlerMapping은 "/**" 경로에 대해 DefaultServletHttpRequestHandler를 반환한다.
    * DispatcherServlet은 DefaultServletHttpRequestHandler에 처리를 요청한다.
    * DefaultServletHttpRequestHandler는 디폴트 서블릿에 처리를 위임한다.

* 예를 들어 "/index.html" 경로로 요청이 들어오면 이에 해당하는 컨트롤러를 찾지 못하므로 디폴트 서블릿이 /index.html 요청을 처리하게 된다.

* DefaultServletHandlerConfigurer#enable()이 등록하는 SimpleUrlHandlerMapping의 우선순위가 가장 낮기 때문에 enable()을 설정하면 별도 설정이 없는 모든 요청 경로를 디폴트 서블릿이 처리한다.