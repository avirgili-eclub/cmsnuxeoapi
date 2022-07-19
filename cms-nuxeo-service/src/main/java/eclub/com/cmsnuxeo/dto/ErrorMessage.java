package eclub.com.cmsnuxeo.dto;


import lombok.Builder;
import lombok.Data;
import org.springframework.validation.FieldError;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@Builder
public class ErrorMessage {
	private String code;
	private List<Map<String,String>> messages;

	public static ErrorMessage formatMessages(String code, List<FieldError> result) {
		List<Map<String, String>> errors = result.stream().map(e -> {
			Map<String, String> error = new HashMap<>();
			error.put(e.getField(), e.getDefaultMessage());
			return error;
		}).collect(Collectors.toList());

		return ErrorMessage.builder()
				.code(code)
				.messages(errors).build();
	}
}
