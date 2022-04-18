# Chapter 13 : MVC 3 : 세션, 인터셉터, 쿠키

## 컨트롤러에서 HttpSession 사용하기

* 로그인 상태를 유지하는 방법은 HttpSession을 이용하는 방법과 쿠키를 이용하는 방법이 있다.

* 컨트롤러에서 HttpSession을 사용하는 방법
  * 요청 매핑 애노테이션 적용 메서드에 HttpSession 파라미터를 추가한다.
  * 요청 매핑 애노테이션 적용 메서드에 HttpServletRequest 파라미터를 추가하고 HttpServletRequest를 이용해서 HttpSession을 구한다.
  
* 요청 매핑 애노테이션 적용 메서드에 HttpSession 파라미터가 존재하면 스프링 MVC는 컨트롤러의 메서드를 호출할 때 HttpSession 객체를 파라미터로 전달한다.
  * HttpSession을 생성하기 전이면 새로운 HttpSession을 생성하고 그렇지 않으면 기존에 존재하는 HttpSession을 전달한다.
* HttpServletRequest의 getSession() 메서드를 이용할 수도 있다.
  * 필요한 시점에만 HttpSession을 생성할 수 있다.
  
## 인터셉터 사용하기

* 비밀번호 변경 폼의 경우 로그인하지 않은 상태에서 출력되는 것은 이상하기 때문에 HttpSession에 로그인 정보가 있는지 확인해 없으면 리다이렉트하도록 수정할 수 있다.
  * 하지만 실제 웹 애플리케이션에서는 비밀번호 변경 기능 외 더 많은 기능에 로그인 여부를 확인해야 하는데, 각 기능을 구현한 컨트롤러 코드마다 세션 확인 코드를 삽입하는 것은 많은 중복을 일으킨다.
  
* 따라서 다수의 컨트롤러에 대해 동일한 기능을 적용할 때 사용할 수 있는 것이 HandlerInterceptor이다.

### HandlerInterceptor 인터페이스 구현하기

* org.springframework.web.HandlerInterceptor 인터페이스를 사용하면 다음 세 지점에 공통 기능을 넣을 수 있다.
  * 컨트롤러(핸들러) 실행 전 : preHandle()
  * 컨트롤러(핸들러) 실행 후, 아직 뷰를 실행하기 전 : postHandle()
  * 뷰를 실행한 이후 : afterCompletion()
  
* preHandle() 메서드를 사용하면 다음 작업이 가능하다.
  * 로그인하지 않은 경우 컨트롤러를 실행하지 않음
  * 컨트롤러를 실행하기 전에 컨트롤러에서 필요로 하는 정보 생성
  * 리턴 타입은 boolean으로 false를 리턴하면 컨트롤러(또는 다음 HandlerInterceptor)를 실행하지 않는다.
* postHandle() 메서드는 컨트롤러가 정상적으로 실행된 이후에 추가 기능을 구현할 때 사용한다.
  * 컨트롤러가 예외를 발생하면 postHandle() 메서드는 실행되지 않는다.
* afterCompletion() 메서드는 뷰가 클라이언트에 응답을 전송한 뒤에 실행된다.
  * 컨트롤러 실행 과정에서 예외 발생 시 이 메서드의 네 번째 파라미터로 전달된다. 예외가 발생하지 않으면 네 번째 파라미터는 null이 된다.
  * 따라서 컨트롤러 실행 이후 예기치 안헥 발생한 예외를 로그로 남기거나 실행 시간을 기록하는 등의 후처리를 하기에 적합한 메서드이다.
  
* HandlerInterceptor 인터페이스의 각 메서드는 아무 기능도 구현하지 않은 디폴트 메서드이므로 모두 구현할 필요가 없다. 필요한 메서드만 재정의하면 된다.

### HandlerInterceptor 설정하기

* WebMvcConfigurer#addInterceptors() 메서드는 인터셉터를 설정하는 메서드이다.
* InterceptorRegistry#addInterceptor() 메서드는 HandlerInterceptor 객체를 설정한다.
* InterceptorRegistry#addInterceptor() 메서드는 InterceptorRegistration 객체를 반환하는데 이 객체의 addPathPatterns() 메서드는 인터셉터를 적용할 경로 패턴을 지정한다.
  * 이 경로는 Ant 경로 패턴을 사용한다.
  * 두 개 이상 경로 패턴을 지정하려면 각 경로 패턴을 콤마로 구분해서 지정한다.
  
* Ant 경로 패턴
  * '*': 0개 또는 그 이상의 글자
  * '?': 1개 글자
  * '**': 0개 또는 그 이상의 폴더 경로
  
* addPathPatterns() 메서드에 지정한 경로 패턴 중 일부를 제외하고 싶다면 excludePathPatterns() 메서드를 사용한다.

## 컨트롤러에서 쿠키 사용하기

* 스프링 MVC에서 쿠키를 사용하는 방법 중 하나는 @CookieValue 애노테이션을 사용하는 것이다.
  * @CookieValue 애노테이션은 요청 매핑 애노테이션 적용 메서드의 Cookie 타입 파라미터에 적용한다.
* 쿠키를 생성하려면 HttpServletResponse 객체가 필요하므로 메서드의 파라미터로 HttpServletResponse 타입을 추가해야 한다.