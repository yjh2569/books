# DB 연동

## JDBC 프로그래미으이 단점을 보완하는 스프링

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