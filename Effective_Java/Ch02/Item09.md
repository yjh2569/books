# try-finally보다는 try-with-resources를 사용하라

* 자바 라이브러리에는 InputStream, OutputStream, java.sql.Connection 등 close 메서드를 호출해 직접 닫아줘야 하는 자원이 많다.
* 이는 클라이언트가 놓치기 쉬워 성능 문제로 이어지기도 한다.
* 위와 같은 현상을 미리 방지하기 위해 try-finally가 쓰였다.

> try-finally를 사용하는 예시

```
static String firstLineOfFile(String path) throws IOException {
    BufferedReader br = new BufferedReader(new FileReader(path));
    try {
        return br.readline();
    } finally {
        br.close();
    }
}
```

* 이와 같은 방법은 자원을 둘 이상 사용할 때 지저분해질 수 있다.

```
static void copy(String src, String dst) throws IOException {
    InputStream in = new FileInputStream(src);
    try {
        OutputStream out = new FileOutputStream(dst);
        try {
            byte[] buf = new byte[BUFFER_SIZE];
            int n;
            while ((n = in.read(buf)) >= 0) out.write(buf, 0, n);
        } finally {
            out.close(); 
        } 
    } finally {
        in.close();
    }
}
```

* 또한 예외가 try 블록과 finally 블록 모두에서 발생할 수 있는데, 기기의 물리적인 오류로 인해 firstLineOfFile 메서드 내 readLine 메서드가 예외를 던지고, 같은 이유로 close 메서드도 실패할 수 있다. 이런 상황에서 두 번째 예외가 첫 번째 예외를 완전히 집어삼켜 첫 번째 예외에 대한 정보가 남지 않게 된다.

* 이러한 문제들은 자바 7에서 try-with-resources로 모두 해결되었다.
* 이 구조는 해당 자원이 close 메서드 하나만 있는 AutoCloseable 인터페이스를 구현하기만 하면 된다.

> try-with-resources를 사용하는 예시

```
static String firstLineOfFile(String path) throws IOException {
    try (BufferedReader br = new BufferedReader(new FileReader(path))) {
        return br.readline();
    }
}
```

```
static void copy(String src, String dst) throws IOException {
    
    try (InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst)) {
        byte[] buf = new byte[BUFFER_SIZE];
        int n;
        while ((n = in.read(buf)) >= 0) out.write(buf, 0, n);
    }
}
```

* 위 버전에서 firstLineOfFile 메서드 내에 readLine과 close 호출 양쪽에서 예외가 발생하면 close에서 발생한 예외는 숨겨지고 readLine에서 발생한 예외가 기록된다.
* try-with-resources에서도 catch 절을 쓸 수 있다. 이를 통해 다수의 예외를 처리할 수 있다.

> try-with-resources를 catch 절과 함께 사용하는 예시

```
static String firstLineOfFile(String path, String defaultVal) {
    try (BufferedReader br = new BufferedReader(new FileReader(path))) {
        return br.readline();
    } catch (IOException e) {
        return defaultVal;
    }
}
```
