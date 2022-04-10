# Chapter 7 : AOP 프로그래밍

* 스프링 프레임워크의 AOP 기능은 spring-aop 모듈이 제공하는데 spring-context 모듈을 의존 대상에 추가하면 spring-aop 모듈도 함께 의존 대상에 포함된다.
  * 따라서 spring-aop 모듈에 대한 의존을 따로 추가하지 않아도 된다.
* aspectweaver 모듈은 AOP를 설정하는데 필요한 애노테이션을 제공하므로 이 의존을 추가해야 한다.

## 프록시와 AOP

* 프록시 : 핵심 기능의 실행은 다른 객체에 위임하고 부가적인 기능을 제공하는 객체
  * 대상 객체 : 실제 핵심 기능을 실행하는 객체
  > 예제 코드에서 ExeTimeCalculator가 프록시이고 ImplCalculator 객체가 프록시의 대상 객체가 된다.
  
* 프록시는 핵심 기능은 구현하지 않는다.
  * 핵심 기능을 구현하지 않는 대신 여러 객체에 공통으로 적용할 수 있는 기능을 구현한다.
  
### AOP

* AOP의 정의
  * Aspect Oriented Programming : 기능(관심) 지향 프로그래밍
  * 여러 객체에 공통으로 적용할 수 있는 기능을 분리해서 재사용성을 높여주는 프로그래밍 기법
  * 핵심 기능과 공통 기능의 구현을 분리함으로써 핵심 기능을 구현한 코드의 수정 없이 공통 기능을 적용할 수 있게 만들어 준다.

* 핵심 기능에 공통 기능을 삽입하는 방법
  * 컴파일 시점에 코드에 공통 기능을 삽입하는 방법
    * AOP 개발 도구가 소스 코드를 컴파일하기 전에 공통 구현 코드를 소스에 삽입한다.
  * 클래스 로딩 시점에 바이트 코드에 공통 기능을 삽입하는 방법
    * 클래스를 로딩할 때 바이트 코드에 공통 기능을 클래스에 삽입한다.
  * 위 두 가지 방법은 스프링 AOP에서는 지원하지 않으며 AspectJ와 같이 AOP 전용 도구를 사용해서 적용할 수 있다.
  * 런타임에 프록시 객체를 생성해서 공통 기능을 삽입하는 방법
    * 스프링이 제공하는 AOP 방식    
  
* 스프링 AOP는 프록시 객체를 자동으로 만들어준다.
  * 따라서 상위 타입의 인터페이스를 상속받은 프록시 클래스를 직접 구현할 필요가 없다.
  * 단지 공통 기능을 구현한 클래스만 알맞게 구현하면 된다.

* AOP 주요 용어
  * Advice : 언제 공통 관심 기능을 핵심 로직에 적용할 지를 정의하고 있다.
  * Joinpoint : Advice를 적용 가능한 지점을 의미한다.
    * 메서드 호출, 필드 값 변경 등이 Joinpoint에 해당한다.
    * 스프링은 프록시를 이용해서 AOP를 구현하기 때문에 메서드 호출에 대한 Joinpoint만 지원한다.
  * Pointcut : Joinpoint의 부분 집합으로서 실제 Advice가 적용되는 Joinpoint를 나타낸다.
    * 스프링에서는 정규 표현식이나 AspectJ의 문법을 이용해 Pointcut을 정의할 수 있다.
  * Weaving : Advice를 핵심 로직 코드에 적용하는 것
  * Aspect : 여러 객체에 공통으로 적용되는 기능. 트랜잭션이나 보안 등이 이에 해당한다.
  
### Advice의 종류

* Before Advice : 대상 객체의 메서드 호출 전에 공통 기능 실행
* After Returning Advice : 대상 객체의 메서드가 예외 없이 실행된 이후에 공통 기능 실행
* After Throwing Advice : 대상 객체의 메서드를 실행하는 도중 예외가 발생한 경우에 공통 기능 실행
* After Advice : 예외 발생 여부와 상관없이 대상 객체의 메서드 실행 후 공통 기능 실행
* Around Advice : 대상 객체의 메서드 실행 전, 후 또는 예외 발생 시점에 공통 기능 실행
* Around Advice가 가장 널리 사용된다.
  * 대상 객체의 메서드를 실행하기 전/후, 예외 발생 시점 등 다양한 시점에 원하는 기능을 삽입할 수 있기 때문이다.
  * 캐시 기능, 성능 모니터링 기능과 같은 Aspect를 구현할 때 사용한다.
  
## 스프링 AOP 구현

* 전체적인 구현 및 적용 방법
  * Aspect로 사용할 클래스에 @Aspect 애노테이션을 붙인다.
  * @Pointcut 애노테이션으로 공통 기능을 적용할 Pointcut을 정의한다.
  * 공통 기능을 구현한 메서드에 @Around 애노테이션을 적용한다.
  
### @Aspect, @Pointcut, @Around를 이용한 AOP 구현

* @Aspect 애노테이션을 적용한 클래스는 Advice와 Pointcut을 함께 제공한다.

* @Pointcut은 공통 기능을 적용할 대상을 설정한다.
* @Around 애노테이션은 Around Advice를 설정한다.
  * @Around 애노테이션의 값이 publicTarget()인 경우 이는 publicTarget() 메서드에 정의한 Pointcut에 공통 기능을 적용한다는 것을 의미한다.

* measure() 메서드의 ProceedingJoinPoint 타입 파라미터는 프록시 대상 객체의 메서드를 호출할 때 사용한다.
  * proceed() 메서드를 사용해서 실제 대상 객체의 메서드를 호출한다.
  * 이 메서드를 호출하면 대상 객체의 메서드가 실행되므로 이 코드 이전과 이후에 공통 기능을 위한 코드를 위치시키면 된다.

* ProceedingJoinPoint의 기타 메서드들
  * getSignature() : 호출한 메서드의 시그니처(메서드 이름 + 파라미터)를 반환
    * Signature 인터페이스는 다음 메서드를 제공한다.
      * getName() : 호출되는 메서드의 이름을 구한다.
      * toLongString() : 호출되는 메서드를 완전하게 표현한 문장을 구한다.(메서드의 리턴 타입, 파라미터 타입 모두 표시)
      * toShortString() : 호출되는 메서드를 축약해서 표현한 문장을 구한다.(기본 구현은 메서드의 이름만을 구한다.)
  * getTarget() : 호출한 메서드의 대상 객체 반환
  * getArgs() : 호출한 메서드의 인자 목록 반환
  
* @Aspect 애노테이션을 붙인 클래스를 공통 기능으로 적용하려면 @EnableAspectJAutoProxy 애노테이션을 설정 클래스에 붙여야 한다.
  * 이 애노테이션을 추가하면 스프링은 @Aspect 애노테이션이 붙은 빈 객체를 찾아서 빈 객체의 @Pointcut 설정과 @Around 설정을 사용한다.
  
## 프록시 생성 방식

* 스프링은 AOP를 위한 프록시 객체를 생성할 때 실제 생성할 빈 객체가 인터페이스를 상속하면 인터페이스를 이용해 프록시를 생성한다.
* 빈 객체가 인터페이스를 상속할 때 인터페이스가 아닌 클래스를 이용해서 프록시를 생성하고 싶다면 @EnableAspectJAutoProxy의 proxyTargetClass 속성을 true로 지정한다.

### execution 명시자 표현식

* execution 명시자는 Advice를 적용할 메서드를 지정할 때 사용한다.
* 기본 형식은 다음과 같다.
  * 수식어패턴은 생략 가능하고 public, protected 등이 온다. 스프링 AOP는 public 메서드에만 적용할 수 있기 때문에 사실상 public만 의미있다.
  * 각 패턴은 '*'을 이용해 모든 값을 표현할 수 있다.
  * '..'을 이용해 0개 이상이라는 의미를 표현할 수 있다.
```
execution(수식어패턴? 리턴타입패턴 클래스이름패턴?메서드이름패턴(파라미터패턴))
```

* execution 명시자 예시
  * execution(public void set*(..)) : 리턴 타입이 void이고, 메서드 이름이 set으로 시작하며, 파라미터가 0개 이상인 메서드 호출
    * 파라미터 부분에 '..'을 사용해 파라미터가 0개 이상인 것을 표현
  * execution(* chap07.*.*()) : chap07 패키지의 타입에 속한 파라미터가 없는 모든 메서드 호출
  * execution(* chap07..*.*(..)) : chap07 패키지 및 하위 패키지에 있는, 파라미터가 0개 이상인 메서드 호출
    * 패키지 부분에 '..'을 사용해 해당 패키지 또는 하위 패키지를 표현했다.
  * execution(Long chap07.Calculator.factorial(..)) : 리턴 타입이 Long인 Calculator 타입의 factorial() 메서드 호출
  * execution(* get*(*)) : 이름이 get으로 시작하고 파라미터가 한 개인 메서드 호출
  * execution(* get*(*, *)) : 이름이 get으로 시작하고 파라미터가 두 개인 메서드 호출
  * execution(* read*(Integer, ..)) : 메서드 이름이 read로 시작하고, 첫 번째 파라미터 타입이 Integer이며, 한 개 이상의 파라미터를 갖는 메서드 호출

### Advice 적용 순서

* 어떤 Aspect가 먼저 적용될지는 스프링 프레임워크나 자바 버전에 따라 달라질 수 있기 때문에 적용 순서가 중요하다면 직접 순서를 지정해야 한다.
* 이럴 때 @Order 애노테이션을 사용한다.
  * @Aspect 애노테이션과 함께 @Order 애노테이션을 클래스에 붙이면 @Order 애노테이션에 지정한 값에 따라 적용 순서를 결정한다.
  * @Order 애노테이션의 값이 작으면 먼저 적용하고 크면 나중에 적용한다.
  
### @Around의 Pointcut 설정과 @Pointcut 재사용

* @Pointcut 애노테이션이 아닌 @Around 애노테이션에 execution 명시자를 직접 지정할 수도 있다.
* 만약 같은 Pointcut을 여러 Advice가 함께 사용한다면 공통 Pointcut을 재사용할 수도 있다.
  * 다른 클래스에 위치한 @Around 애노테이션에서 publicTarget() 메서드의 Pointcut을 사용하고 싶다면 publicTarget() 메서드를 public으로 바꾸면 된다.
  * 그리고 해당 Pointcut의 완전한 클래스 이름을 포함한 메서드 이름을 @Around 애노테이션에서 사용하면 된다.
  
* 이를 활용해 여러 Aspect에서 공통으로 사용하는 Pointcut이 있다면 별도 클래스에 Pointcut을 정의하고, 각 Aspect 클래스에서 해당 Pointcut을 사용하도록 구성하면 Pointcut 관리가 편해진다.
  * @Pointcut을 설정한 CommonPointcut은 빈으로 등록할 필요가 없다. @Around 애노테이션에서 해당 클래스에 접근 가능하기만 하면 된다.