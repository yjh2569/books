package main;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import chap07.Calculator;
import chap07.RecCalculator;
import config.AppCtx;
import config.AppCtxWithCache;

public class MainAspectWithCache {

	public static void main(String[] args) {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AppCtxWithCache.class);
		
		
		// "calculator" 빈의 실제 타입은 Calculator를 상속한 프록시 타입이므로 
		// RecCalculator로 타입 변환을 할 수 없기 때문에 예외 발생
//		RecCalculator cal = ctx.getBean("calculator", RecCalculator.class);
		Calculator cal = ctx.getBean("calculator", Calculator.class);
		cal.factorial(7);
		cal.factorial(7);
		cal.factorial(5);
		cal.factorial(5);
		ctx.close();

	}

}
