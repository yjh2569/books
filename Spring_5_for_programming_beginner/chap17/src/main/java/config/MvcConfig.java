package config;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import interceptor.AuthCheckInterceptor;

@Configuration
@EnableWebMvc
public class MvcConfig implements WebMvcConfigurer{
	
	@Override
	public void configureDefaultServletHandling(
			DefaultServletHandlerConfigurer configurer) {
		configurer.enable();
	}
	
	@Override
	public void configureViewResolvers(ViewResolverRegistry registry) {
		registry.jsp("/WEB-INF/view/", ".jsp");
	}
	
	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		registry.addViewController("/main").setViewName("main");
	}
	
	@Bean
	public MessageSource messageSource() { // 빈의 아이디를 반드시 messageSource로 지정해야 한다.
		ResourceBundleMessageSource ms = new ResourceBundleMessageSource();
		// message 패키지 내 label 프로퍼티 파일을 찾는다.
		// src/main/resources 폴더도 클래스 패스에 포함되고 message 폴더는 message 패키지에 대응한다.
		ms.setBasenames("message.label");
		ms.setDefaultEncoding("UTF-8");
		return ms;
	}
	
//	@Override
//	public Validator getValidator() {
//		return new RegisterRequestValidator();
//	}
	
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(authCheckInterceptor())
			.addPathPatterns("/edit/**");
	}
	
	@Bean
	public AuthCheckInterceptor authCheckInterceptor() {
		return new AuthCheckInterceptor();
	}
	
	@Override
	public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		ObjectMapper objectMapper = Jackson2ObjectMapperBuilder
				.json()
//				.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
//				.simpleDateFormat("yyyyMMddHHmmss")
				.serializerByType(LocalDateTime.class, new LocalDateTimeSerializer(formatter))
				.build();
		converters.add(0, new MappingJackson2HttpMessageConverter(objectMapper));
	}
}
