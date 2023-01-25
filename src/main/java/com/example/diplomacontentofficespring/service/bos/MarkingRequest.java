package com.example.diplomacontentofficespring.service.bos;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.InputStream;
import javax.validation.Valid;
import javax.ws.rs.FormParam;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Входная сущность для маркировки документов офисного формата.
 *
 * @author Yana Kuzmina.
 * @since 0.0.2
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MarkingRequest {

	/**
	 * Последовательность, которую требуется прогнать для восстановления былой копии.
	 */
	@FormParam("seq")
	//@PartType(MediaType.APPLICATION_JSON)
	@Valid
	@Builder.Default
	private ExistedMarkSequence sequence = null;

	/**
	 * Опции, пока пустые.
	 */
	@FormParam("opts")
	//@PartType(MediaType.APPLICATION_JSON)
	@Valid
	private MarkingOptions options;

	/**
	 * {@link InputStream} с контентом, который передается через HTTP протокол и надо модифицировать.
	 */
	@FormParam("content")
	//@PartType(MediaType.APPLICATION_OCTET_STREAM)
	private InputStream content;

	/**
	 * MimeType контента.
	 */
	@FormParam("fileType")
	//@PartType(MediaType.APPLICATION_JSON)
	private String mimeType;
}
