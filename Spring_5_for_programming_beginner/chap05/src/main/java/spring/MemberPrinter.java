package spring;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;

public class MemberPrinter {
	private DateTimeFormatter dateTimeFormatter;
	
//	@Autowired(required = false)
//	private DateTimeFormatter dateTimeFormatter;
//	
//	@Autowired
//	private Optional<DateTimeFormatter> formatterOpt;
//	...
//	DateTimeFormatter dateTimeFormatter = formatterOpt.orElse(null);
//	if (dateTimeFormatter == null) {
//	...
//	
//	@Autowired
//	@Nullable
//	private DateTimeFormatter dateTimeFormatter;
	
	public MemberPrinter() {
		dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일");
	}
	
	public void print(Member member) {
		if (dateTimeFormatter == null) {
			System.out.printf(
				"회원 정보: 아이디=%d, 이메일=%s, 이름=%s, 등록일=%tF\n",
				member.getId(), member.getEmail(), 
				member.getName(), member.getRegisterDateTime());
		} else {
			System.out.printf(
				"회원 정보: 아이디=%d, 이메일=%s, 이름=%s, 등록일=%s\n",
				member.getId(), member.getEmail(), 
				member.getName(), dateTimeFormatter.format(member.getRegisterDateTime()));
		}	
	}
	
	// @Autowired 필수 여부 지정
	
	// 1. required = false
	@Autowired(required = false)
	public void setDateFormatter(DateTimeFormatter dateTimeFormatter) {
		this.dateTimeFormatter = dateTimeFormatter;
	}
	
	// 2. Optional 활용
//	@Autowired
//	public void setDateFormatter(Optional<DateTimeFormatter> formatterOpt) {
//		if (formatterOpt.isPresent()) {
//			this.dateTimeFormatter = formatterOpt.get();
//		} else {
//			this.dateTimeFormatter = null;
//		}
//	}
	
	// 3. Nullable 애노테이션 사용
//	@Autowired
//	public void setDateFormatter(@Nullable DateTimeFormatter dateTimeFormatter) {
//		this.dateTimeFormatter = dateTimeFormatter;
//	}
}
