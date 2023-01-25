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
 * Входная сущность для конвертирования документа офисного формата (doc, docx, odt, и др) в pdf.
 * <p>
 *
 * @author Aksenov Ivan
 * @since 0.0.1
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConvertRequest {

	/**
	 * Конструктор, чтоб не собирать через builder().
	 * @param contentId - идентификатор контента на StorageService.
	 * @param options - опции конвертации
	 */
	public ConvertRequest(String contentId, ConvertOptions options) {
		this.contentId = contentId;
		this.options = options;
	}

	/**
	 * Опции для конвертации.
	 */
	@FormParam("opts")
	//@PartType(MediaType.APPLICATION_JSON)
	@Valid
	private ConvertOptions options;

	/**
	 * {@link InputStream} с контентом, который передается через HTTP протокол и надо модифицировать.
	 */
	@FormParam("content")
	//@PartType(MediaType.APPLICATION_OCTET_STREAM)
	private InputStream content;

	/**
	 * id контента в file service.
	 */
	@FormParam("contentId")
	//@PartType(MediaType.APPLICATION_JSON)
	private String contentId;

}
