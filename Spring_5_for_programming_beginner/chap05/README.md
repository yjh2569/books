# Chapter 5 : 컴포넌트 스캔

## @Component 애노테이션으로 스캔 대상 지정

* 스프링이 검색해서 빈으로 등록할 수 있으려면 클래스에 @Component 애노테이션을 붙여야 한다.
  * @Component 애노테이션은 해당 클래스를 스캔 대상으로 표시한다.
  * @Component 애노테이션에 값을 주지 않았다면 클래스 이름의 첫 글자를 소문자로 바꾼 이름을 빈 이름으로 사용한다.
  * @Component 애노테이션에 값을 주면 그 값을 빈 이름으로 사용한다.
  
* @Component 애노테이션을 붙인 클래스를 스캔해서 스프링 빈으로 등록하려면 설정 클래스에 @ComponentScan 애노테이션을 적용해야 한다.
  * basePackages 속성값을 통해 스캔 대상 패키지 목록을 지정할 수 있다. 스캔 대상에 패키지를 등록하면 그 패키지와 하위 패키지를 모두 스캔 대상으로 설정한다.
  * 스캔 대상에 해당하는 클래스 중 @Component 애노테이션이 붙은 클래스의 객체를 생성해서 빈으로 등록한다.
  
## 스캔 대상에서 제외하거나 포함하기

* excludeFilters 속성을 사용하면 스캔할 때 특정 대상을 자동 등록 대상에서 제외할 수 있다.
* @Filter(type = FilterType.~~~, pattern = "...")을 위 속성의 값으로 지정한다.
* FilterType에는 REGEX(정규표현식), AspectJ 등이 있다. AspectJ를 패턴이 동작하기 위해서는 의존 대상에 aspectjweaver 모듈을 추가해야 한다.
* patterns 속성은 String[] 타입이므로 배열을 이용해서 패턴을 한 개 이상 지정할 수 있다.
* 특정 애노테이션을 붙인 타입을 컴포넌트 대상에서 제외할 수도 있다.
  * type을 FilterType.ANNOTATION으로 하고, classes = {AnnotationExample.class, AnnotationExample2.class}로 하면 @AnnotationExample(2)가 붙은 클래스를 컴포넌트 스캔 대상에서 제외한다.
* 특정 타입이나 그 하위 타입을 컴포넌트 스캔 대상에서 제외하려면 ASSIGNABLE_TYPE을 FilterType으로 지정한다. classes 속성에는 제외할 타입 목록을 지정한다.
* 설정할 필터가 두 개 이상이면 @ComponentScan의 excludeFilters 속성에 배열을 사용해서 @Filter 목록을 전달하면 된다.
  
### 기본 스캔 대상

* 다음 애노테이션을 붙인 클래스는 컴포넌트 스캔 대상에 포함된다.
* @Component
* @Controller
* @Service
* @Repository
* @Aspect
* @Configuration
* @Aspect 애노테이션을 제외한 나머지 애노테이션들은 실제로는 @Component 애노테이션에 대한 특수 애노테이션들이다.
* @Controller나 @Repository 애노테이션 등은 컴포넌트 스캔 대상이 될 뿐만 아니라 스프링 프레임워크에서 특별한 기능과 연관되어 있다.
  
## 컴포넌트 스캔에 따른 충돌 처리

### 빈 이름 충돌

* 서로 다른 패키지에 같은 이름의 클래스가 존재하고 두 클래스 모두 @Component 애노테이션을 붙이면 스프링 컨테이너 생성 시 예외가 발생한다.
* 이러한 경우 둘 중 하나에 명시적으로 빈 이름을 지정해서 이름 충돌을 피해야 한다.
  
### 수동 등록한 빈과 충돌

* @Component 애노테이션을 붙인 클래스를 설정 클래스에 같은 이름으로 직접 빈으로 등록하면 수동 등록한 빈이 우선한다. 즉, 해당 타입의 빈은 한 개만 존재한다.
* 만약 수동 등록할 때 다른 이름으로 빈을 등록하면 같은 타입의 빈이 두 개가 생성된다. 따라서 @Qualifier 애노테이션을 사용해 알맞은 빈을 선택해야 한다.