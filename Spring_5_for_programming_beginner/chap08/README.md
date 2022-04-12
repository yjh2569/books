# DB 연동

## JDBC 프로그래밍의 단점을 보완하는 스프링

* 일반적인 JDBC 프로그래밍은 DB 연동을 위한 코드가 계속 반복되는 문제가 있다.
  * 이러한 구조적인 반복을 줄이기 위해 스프링은 템플릿 메서드 패턴과 전략 패턴을 함께 사용한 JdbcTemplate 클래스를 제공한다.
  
* 스프링은 트랜잭션 관리를 쉽게 해준다.
  * JDBC API로 트랜잭션을 처리하려면 Connection의 setAutoCommit(false)을 이용해서 자동 커밋을 비활성화하고 commit()과 rollback() 메서드를 이용해 트랜잭션을 커밋하거나 롤백해야 한다.
  * 스프링에서는 트랜잭션을 적용하고 싶은 메서드에 @Transactional 애노테이션을 붙이기만 하면 된다. 그러면 커밋과 롤백 처리를 스프링이 알아서 처리한다.
  
## DataSource 설정

* 스프링이 제공하는 DB 연동 기능은 DataSource를 사용해서 DB Connection을 구한다.
  * DB 연동에 사용할 DataSource를 스프링 빈으로 등록하고 DB 연동 기능을 구현한 빈 객체는 DataSource를 주입받아 사용한다.
  
* Tomcat JDBC 모듈은 javax.sql.DataSource를 구현한 DataSource 클래스를 제공한다.
  * 이 클래스를 스프링 빈으로 등록해서 DataSource로 사용할 수 있다.
  
### Tomcat JDBC의 주요 프로퍼티

* Tomcat JDBC 모듈의 DataSource 클래스는 커넥션 풀 기능을 제공하는 DataSource 구현 클래스이다.
  * DataSource 클래스는 커넥션을 몇 개 만들지 지정할 수 있는 메서드를 제공한다.

* Tomcat JDBC DataSource 클래스의 주요 프로퍼티
  * setInitialSize(int) : 커넥션 풀 초기화 시 초기 커넥션 개수 지정. 기본값은 10
  * setMaxActive(int) : 커넥션 풀에서 가져올 수 있는 최대 커넥션 개수 지정. 기본값은 10
  * setMaxIdle(int) : 커넥션 풀에 유지할 수 있는 최대 커넥션 개수 지정. 기본값은 maxActive와 같음
  * setMinIdle(int) : 커넥션 풀에 유지할 수 있는 최소 커넥션 개수 지정. 기본값은 initialSize에서 가져옴
  * setMaxWait(int) : 커넥션 풀에서 커넥션을 가져올 때 대기할 최대 시간을 밀리초 단위로 지정. 기본값은 30000밀리초(30초)
  * setMaxAge(long) : 최초 커넥션 연결 후 커넥션의 최대 유효 시간을 밀리초 단위로 지정. 기본값은 0(유효 시간 없음)
  * setValidationQuery(String) : 커넥션이 유효한지 검사할 때 사용할 쿼리 지정. 언제 검사할지는 별도 설정으로 지정. 기본값은 null(검사 수행 X)
    * "select 1"이나 "select 1 from dual"과 같은 쿼리를 주로 사용한다.
  * setValidationQueryTimeout(int) : 검사 쿼리의 최대 실행 시간을 초 단위로 지정. 이 시간을 초과하면 검사 실패로 간주.
    * 0 이하로 지정하면 비활성화하며, 기본값은 -1
  * setTestOnBorrow(boolean) : 풀에서 커넥션을 가져올 때 검사 여부 지정. 기본값은 false
  * setTestOnReturn(boolean) : 풀에서 커넥션을 반환할 때 검사 여부 지정. 기본값은 false
  * setTestWhileIdle(boolean) : 커넥션이 풀에 유효 상태로 있는 동안에 검사 여부 지정. 기본값은 false
  * setMinEvictableIdleTimeMillis(int) : 커넥션 풀에 유휴 상태로 유지할 최소 시간을 밀리초 단위로 지정.
    * testWhileIdle이 true이면 유휴 시간이 이 값을 초과한 커넥션을 풀에서 제거. 기본값은 60000밀리초(60초).
  * setTimeBetweenEvictionRunsMillis(int) : 커넥션 풀의 유휴 커넥션을 검사할 주기를 밀리초 단위로 지정. 기본값은 5000밀리초(5초).
    * 이 값을 1초 이하로 설정하면 안 된다.
    
* 커넥션의 상태
  * 커넥션 풀은 커넥션을 생성하고 유지한다.
  * 커넥션 풀에 커넥션을 요청하면 해당 커넥션은 활성(active) 상태가 된다.
  * 커넥션을 다시 커넥션 풀에 반환하면 유휴(idle) 상태가 된다.
  * DataSource#getConnection()을 실행하면 커넥션 풀에서 커넥션을 가져와 커넥션이 활성 상태가 된다.
  * 커넥션을 종료하면(close) 커넥션은 풀로 돌아가 유휴 상태가 된다.

* maxActive는 활성 상태가 가능한 최대 커넥션 개수를 지정한다.
  * maxActive개의 커넥션이 활성 상태일 때 커넥션 풀에 다시 커넥션을 요청하면 다른 커넥션이 반환될 때까지 대기한다.
  * 이 대기 시간이 maxWait이다. 대기 시간 내에 풀에 반환된 커넥션이 없으면 예외가 발생한다.
  
* 커넥션 풀은 성능 때문에 사용한다.
  * 커넥션 풀을 사용하면 미리 커넥션을 생성했다가 필요할 때에 커넥션을 꺼내 쓰므로 커넥션을 구하는 시간이 줄어 전체 응답 시간도 짧아진다.
  * 그래서 커넥션 풀을 초기화할 때 최소 수준의 커넥션을 미리 생성하는 것이 좋다. 이때 생성할 커넥션의 개수를 initialSize로 지정한다.
  
* 커넥션 풀에 생성된 커넥션은 지속적으로 재사용되지만 한 커넥션이 영원히 유지되지는 않는다.
  * 일정 시간 내에 쿼리를 실행하지 않으면 연결을 끊기도 한다.
  * 만약 커넥션 풀에 특정 커넥션이 오랜 시간 유휴 상태로 존재한 경우, DBMS는 해당 커넥션의 연결을 끊지만 커넥션은 여전히 풀 속에 남아 있어 해당 커넥션을 풀에서 가져와 사용하면 연결이 끊어진 커넥션이므로 예외가 발생한다.
  * 업무용 시스템과 같이 특정 시간대에 사용자가 없으면 이런 상황이 발생할 수 있다.
  * 이런 문제를 방지하려면 커넥션 풀의 커넥션이 유효한지 주기적으로 검사해야 한다.
  * 이와 관련된 속성이 testWhileIdle, minEvictableIdleTimeMillis, timeBetweenEvictionRunsMillis이다.

## JdbcTemplate을 이용한 쿼리 실행

* 스프링을 사용하면 DataSource나 Connection, Statement, ResultSet을 직접 사용하지 않고 JdbcTemplate을 이용해서 편리하게 쿼리를 실행할 수 있다.

* JdbcTemplate 객체를 생성하려면 DataSource를 주입받아야 한다.

### JdbcTemplate을 이용한 조회 쿼리 실행

* JdbcTemplate 클래스는 SELECT 쿼리 실행을 위한 query() 메서드를 제공한다.
  * List\<T\> query(String sql, RowMapper\<T\> rowMapper)
  * List\<T\> query(String sql, Object[] args, RowMapper\<T\> rowMapper)
  * List\<T\> query(String sql, RowMapper\<T\> rowMapper, Object... args)
  
* query() 메서드는 sql 파라미터로 전달받은 쿼리를 실행하고 RowMapper를 이용해서 ResultSet의 결과를 자바 객체로 변환한다.
  * sql 파라미터가 인덱스 기반 파라미터를 가진 쿼리이면 args 파라미터를 이용해 각 인덱스 파라미터의 값을 지정한다.
  * RowMapper의 mapRow() 메서드는 SQL 실행 결과로 구한 ResultSet에서 한 행의 데이터를 읽어와 자바 객체로 변환하는 매퍼 기능을 구현한다.
  * RowMapper 인터페이스를 구현한 클래스를 작성할 수도 있지만 임의 클래스나 람다식으로 RowMapper의 객체를 생성해서 query() 메서드에 전달할 때도 많다.
  * 쿼리를 실행한 결과가 존재하지 않으면 길이가 0인 List를 반환한다.
  
### 결과가 1행인 경우 사용할 수 있는 queryForObject() 메서드

* queryForObject() 메서드는 쿼리 실행 결과 행이 한 개인 경우에 사용할 수 있는 메서드다.
  * 두 번째 파라미터는 칼럼을 읽어올 때 사용할 타입을 지정한다.
  * 인덱스 파라미터가 존재하면 파라미터의 값을 가변 인자로 전달한다.
  * 실행 결과 칼럼이 두 개 이상이면 RowMapper를 파라미터로 전달해서 결과를 생성할 수 있다.
    * 이는 query()와 사용 방법이 거의 동일하나 리턴 타입이 List가 아닌 RowMapper로 변환해주는 타입이 된다.

* 주요 queryForObject() 메서드
  * T queryForObject(String sql, Class\<T\> requiredType)
  * T queryForObject(String sql, Class\<T\> requiredType, Object... args)
  * T queryForObject(String sql, RowMapper\<T\> rowMapper)
  * T queryForObject(String sql, RowMapper\<T\> rowMapper, Object... args)

* queryForObject() 메서드를 사용하려면 쿼리 실행 결과는 반드시 한 행이어야 한다.
  * 만약 쿼리 실행 결과 행이 없거나 두 개 이상이면 IncorrectResultSizeDataAcceessException이 발생한다.
  * 행의 개수가 0이면 하위 클래스인 EmptyResultDataAccessException이 발생한다.
  * 따라서 결과 행이 정확히 한 개라는 보장이 없다면 query() 메서드를 사용한다.
  
### JdbcTemplate을 이용한 변경 쿼리 실행

* INSERT, UPDATE, DELETE 쿼리는 update() 메서드를 사용한다.
  * int update(String sql)
  * int update(String sql, Object... args)
  * 쿼리 실행 결과로 변경된 행의 개수를 반환한다.
  
### PreparedStatementCreator를 이용한 쿼리 실행

* PreparedStatement의 set 메서드를 사용해서 직접 인덱스 파라미터의 값을 설정해야 할 때도 있다.
  * 이 경우 PreparedStatementCreator를 인자로 받는 메서드를 이용해서 직접 PreparedStatement를 생성하고 설정해야 한다.

* PreparedStatementCreator 인터페이스는 createPreparedStatement(Connection con) 메서드를 가지고 있다.
  * 파라미터로 전달받은 Connection을 이용해서 PreparedStatement 객체를 생성하고 인덱스 파라미터를 알맞게 설정한 뒤에 리턴하면 된다.
  
* jdbcTemplate 클래스가 제공하는 메서드 중 PreparedStatementCreator 인터페이스를 파라미터로 갖는 메서드
  * List\<T\> query(PreparedStatementCreator psc, RowMapper\<T\> rowMapper)
  * int update(PreparedStatementCreator psc)
  * int update(PreparedStatementCreator psc, KeyHolder generatedKeyHolder)
    * 이 메서드는 자동 생성되는 키 값을 구할 때 사용한다.
    
### INSERT 쿼리 실행 시 KeyHolder를 이용해서 자동 생성 키 값 구하기

* MySQL의 AUTO_INCREMENT 칼럼은 행이 추가되면 자동으로 값이 할당되는 칼럼으로서 주요키 칼럼에 사용된다.
  * 따라서 INSERT 쿼리에 자동 증가 칼럼에 해당하는 값은 지정하지 않는다.
  * 만약 생성된 키 값을 알고 싶은 경우 KeyHolder를 사용하면 된다.
  
* JdbcTemplate의 update() 메서드는 PreparedStatement를 실행한 후 자동 생성된 키 값을 KeyHolder에 보관한다.
  * KeyHolder에 보관된 키 값은 getKey() 메서드를 이용해서 구한다.
  * getKey() 메서드는 java.lang.Number를 리턴하므로 Number의 intValue(), longValue() 등의 메서드를 사용해서 원하는 타입의 값으로 변환할 수 있다.
  
## 스프링의 익셉션 변환 처리

* SQL 문법이 잘못됐을 때 발생한 메시지를 보면 예외 클래스가 org.spring.framework.jdbc 패키지에 속한 BadSqlGrammarException 클래스임을 알 수 있다.
  * JDBC API를 사용하는 과정에서 SQLException이 발생하면 이 예외를 알맞은 DataAccessException으로 변환해서 발생한다.
  * MySQL용 JDBC 드라이버는 SQL 문법이 잘못된 경우 SQLException을 상속받은 MySQLSyntaxErrorException을 발생시키는데 JdbcTemplate은 이 예외를 DataAccessException을 상속받은 BadSqlGrammarException으로 변환한다.
  * DataAccessException은 스프링이 제공하는 예외 타입으로 데이터 연결에 문제가 있을 떄 스프링 모듈이 발생시킨다.
  * 이는 연동 기술에 상관없이 동일하게 예외를 처리할 수 있도록 하기 위함이다.
  * 연동 기술에 따라 발생하는 예외를 스프링이 제공하는 예외로 변환함으로써 구현 기술에 상관없이 동일한 코드로 예외를 처리할 수 있게 된다.
  
* BadSqlGrammarException은 DataAccessException을 상속받은 하위 타입으로, 실행할 쿼리가 올바르지 않은 경우 사용된다.
  * 스프링은 이 외에도 DuplicateKeyException, QueryTimeoutException 등 DataAccessException을 상속한 다양한 예외 클래스를 제공한다.
  * 각 예외 클래스의 이름은 문제가 발생한 원인을 의미한다. 따라서 이름만으로도 어느 정도 문제 원인을 유추할 수 있다.
  
* DataAccessException은 RuntimeException이므로 필요한 경우에만 예외 처리하면 된다.

## 트랜잭션 처리

* 트랜잭션(transaction) : 여러 쿼리를 논리적으로 하나의 작업으로 묶는 것
  * 한 트랜잭션으로 묶인 쿼리 중 하나라도 실패하면 전체 쿼리를 실패로 간주하고 실패 이전에 실행한 쿼리를 취소한다.
  * 롤백(rollback) : 쿼리 실행 결과를 취소하고 DB를 기존 상태로 되돌리는 것
  * 커밋(commit) : 트랜잭션으로 묶인 모든 쿼리가 성공해서 쿼리 결과를 DB에 실제로 반영하는 것
  
* JDBC는 Connection의 setAutoCommit(false)를 이용해서 트랜잭션을 시작하고 commit()과 rollback()을 이용해서 트랜잭션을 반영(커밋)하거나 취소(롤백)한다.
  * 다만 이와 같은 방식으로 하면 직접 트랜잭션 범위를 관리하기 때문에 개발자가 트랜잭션을 커밋하는 코드나 롤백하는 코드를 누락하기 쉽다.
  * 구조적인 중복이 반복되는 문제도 있다.
  * 스프링이 제공하는 트랜잭션 기능을 사용하면 중복이 없는 매우 간단한 코드로 트랜잭션 범위를 지정할 수 있다.
  
### @Transactional을 이용한 트랜잭션 처리

* 스프링이 제공하는 @Transactional 애노테이션을 사용하면 트랜잭션 범위를 매우 쉽게 지정할 수 있다.
  * 트랜잭션 범위에서 실행하고 싶은 메서드에 @Transactional 애노테이션만 붙이면 된다.
  
* @Transactional 애노테이션이 제대로 동작하려면 두 가지 내용을 스프링 설정에 추가해야 한다.
  * 플랫폼 트랜잭션 매니저(PlatformTransactionManager) 빈 설정
  * @Transactional 애노테이션 활성화 설정
  
* PlatformTransactionManager는 스프링이 제공하는 트랜잭션 매니저 인터페이스이다.
  * 스프링은 구현기술에 상관없이 동일한 방식으로 트랜잭션을 처리하기 위해 이 인터페이스를 사용한다.
  * JDBC는 DataSourceTransactionManager 클래스를 PlatformTransactionManager로 사용한다.
  * dataSource 프로퍼티를 이용해서 트랜잭션 연동에 사용할 DataSource를 지정한다.
  
* @EnableTransactionManagement 애노테이션은 @Transactional 애노테이션이 붙은 메서드를 트랜잭션 범위에서 실행하는 기능을 활성화한다.
  * 등록된 PlatformTransactionManager 빈을 사용해서 트랜잭션을 적용한다.
  * 이후 트랜잭션 범위에서 실행하고 싶은 스프링 빈 객체의 메서드에 @Transactional 애노테이션을 붙이면 된다.
  
### @Transaction과 프록시

* 스프링은 @Transactional 애노테이션을 이용해서 트랜잭션을 처리하기 위해 내부적으로 AOP를 사용한다.
  * 즉, 트랜잭션 처리도 프록시를 통해서 이루어진다.
  
### @Transaction 적용 메서드의 롤백 처리

* 커밋을 수행하는 주체가 프록시 객체였던 것처럼 롤백을 처리하는 주체 또한 프록시 객체이다.

* @Transaction을 처리하기 위한 프록시 객체는 원본 객체의 메서드를 실행하는 과정에서 RuntimeException이 발생하면 트랜잭션을 롤백한다.
  * 별도 설정을 추가하지 않으면 발생한 예외가 RuntimeException일 때 트랜잭션을 롤백한다.
  * Exception 클래스를 직접 구현할 때 RuntimeException을 상속한 이유는 바로 트랜잭션 롤백을 염두해 두었기 때문이다.
  * JdbcTemplate이 발생시키는 DataAccessException 역시 RuntimeException을 상속받고 있어 예외가 발생해도 프록시는 트랜잭션을 롤백한다.
  * SQLException은 RuntimeException이 아니므로 예외가 발생해도 트랜잭션을 롤백하지 않는다. 따라서 이러한 경우에도 롤백하고 싶으면 @Transactional의 rollbackFor 속성을 사용해야 한다.
  
```
// 여러 예외 타입을 지정하고 싶으면 배열로 지정한다.
@Transactional(rollbackFor = SQLException.class)
public void someMethod() {
    ...
}
```

* noRollbackFor 속서은 지정한 예외가 발생하도 롤백시키지 않고 커밋할 예외 타입을 지정할 때 사용한다.

### @Transactional의 주요 속성

* 보통 이들 속성을 사용할 일은 별로 없다.
* value(String) : 트랜잭션을 관리할 때 사용할 PlatformTransactionManager 빈의 이름 지정
* propagation(Propagation) : 트랜잭션 전파 타입 지정. 기본값은 Propagation.REQUIRED
* isolation(Isolation) : 트랜잭션 격리 레벨 지정. 기본값은 Isolation.DEFAULT
* timeout(int) : 트랜잭션 제한 시간 지정. 기본값은 -1로 데이터베이스의 타임아웃 시간을 사용. 초 단위로 지정

* Transactional 애노테이션의 value 속성값이 없으면 등록된 빈 중 타입이 PlatformTransactionManager인 빈을 사용한다.

* Propagation 열거 타입에 정의되어 있는 값
  * REQUIRED : 메서드 수행 시 트랜잭션 필요. 트랜잭션이 존재하지 않으면 새로 생성
  * MANDATORY : 메서드 수행 시 트랜잭션 필요. 하지만 트랜잭션이 존재하지 않을 경우 예외 발생
  * REQUIRES_NEW : 항상 새로운 트랜잭션 시작. 진행 중인 트랜잭션이 존재하면 기존 트랜잭션 일시 중지 후 새 트랜잭션 시작
  * SUPPORTS : 메서드가 트랜잭션을 필요로 하지는 않지만, 진행 중인 트랜잭션이 존재하면 트랜잭션 사용
  * NOT_SUPPORTED : 메서드가 트랜잭션을 필요로 하지 않음. 진행 중인 트랜잭션이 존재하면 메서드가 실행되는 동안 트랜잭션 일시 중지
  * NEVER : 메서드가 트랜잭션을 필요로 하지 않음. 만약 진행 중인 트랜잭션이 존재하면 예외 발생
  * NESTED : 진행 중인 트랜잭션이 존재하면 기존 트랜잭션에 중첩된 트랜잭션에서 메서드 실행. 진행 중인 트랜잭션이 없으면 REQUIRED와 동일하게 동작
  
* Isolation 열거 타입에 정의된 값
  * DEFAULT : 기본 설정
  * READ_UNCOMMITED : 다른 트랜잭션이 커밋하지 않은 데이터 읽기 가능
  * READ_COMMITED : 다른 트랜잭션이 커밋한 데이터 읽기 가능
  * REPEATABLE_READ : 처음에 읽어 온 데이터와 두 번째 읽어 온 데이터가 동일한 값을 가짐
  * SERIALIZABLE : 동일한 데이터에 대해서 동시에 두 개 이상의 트랜잭션 수행 불가

### @EnableTransactionManagement 애노테이션의 주요 속성

* proxyTargetClass : 클래스를 사용해 프록시를 생성할지 여부 지정. 기본값은 false로 인터페이스를 이용해 프록시 생성
* order : AOP 적용 순서 지정. 기본값은 가장 낮은 우선순위에 해당하는 int의 최댓값

### 트랜잭션 전파

* Propagation 열거 타입은 트랜잭션 전파와 관련이 있다.

* 만약 두 @Transactional 애노테이션이 붙은 메서드에 대해 한 메서드 내에 다른 메서드가 있는 경우 트랜잭션 실행 중 또 다른 트랜잭션이 시작되려 할 수 있다.
  * 이 경우 propagation 속성 값이 REQUIRED면 기존에 실행 중인 트랜잭션을 그대로 사용한다. 즉 하나의 트랜잭션으로 묶어서 실행한다.
  * propagation 속성 값이 REQUIRED_NEW라면 기존 트랜잭션이 존재하는지 여부에 상관없이 항상 새로운 트랜잭션을 시작한다. 따라서 메서드 내 다른 메서드가 트랜잭션을 새로 생성한다.
  
* JdbcTemplate은 진행 중인 트랜잭션이 존재하면 해당 트랜잭션 범위에서 쿼리를 실행한다.
  * 따라서 @Transactional 애노테이션이 붙은 메서드 내에서의 메서드들에게는 굳이 @Transactional을 붙이지 않아도 하나의 트랜잭션으로 처리한다.