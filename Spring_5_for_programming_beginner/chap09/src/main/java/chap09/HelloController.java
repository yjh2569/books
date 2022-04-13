package chap09;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller // 스프링 MVC에서 컨트롤러로 사용
public class HelloController {
	
	// 메서드가 처리할 요청 경로 지정(GET 메서드에 대한 매핑 설정)
	@GetMapping("/hello")
	public String hello(Model model, // 컨트롤러의 처리 결과를 뷰에 전달할 떄 사용
			// HTTP 요청 파라미터의 값을 메서드의 파라미터로 전달할 때 사용한다.
			@RequestParam(value = "name", required = false) String name) {
		// "greeting"이라는 모델 속성에 값을 설정
		model.addAttribute("greeting", "안녕하세요, "+name);
		return "hello"; // 컨트롤러의 처리 결과를 보여줄 뷰 이름으로 "hello"를 사용
	}
}
