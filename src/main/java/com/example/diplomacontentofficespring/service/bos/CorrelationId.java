package com.example.diplomacontentofficespring.service.bos;

import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Сущность, представляющая собой идентификатор для сторонних систем для связи пользователя, документа (файла) и
 * созданной копии с этого документа.
 *
 * @author Aksenov Ivan
 * @since 0.0.1
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CorrelationId {

	/**
	 * Идентификатор файла или документа.
	 */
	@NotBlank(message = "File id may not be blank")
	private String fileId;

	/**
	 * Идентификатор сессии или пользователя.
	 */
	@NotBlank(message = "Session id may not be blank")
	private String sessionId;

	@Override
	public String toString() {
		return "CorrelationId{"
				+ "fileId='" + fileId + '\''
				+ ", sessionId='" + sessionId + '\''
				+ '}';
	}
}
