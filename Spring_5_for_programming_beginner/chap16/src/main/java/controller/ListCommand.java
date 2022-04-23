package controller;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

public class ListCommand {

	@DateTimeFormat(pattern = "yyyyMMddHH")
	private LocalDateTime from;
	@DateTimeFormat(pattern = "yyyyMMddHH")
	private LocalDateTime to;

	public final LocalDateTime getFrom() {
		return from;
	}

	public final void setFrom(LocalDateTime from) {
		this.from = from;
	}

	public final LocalDateTime getTo() {
		return to;
	}

	public final void setTo(LocalDateTime to) {
		this.to = to;
	}

}
