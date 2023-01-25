package com.example.diplomacontentofficespring.service.bos;

import javax.ws.rs.FormParam;
import javax.ws.rs.core.MediaType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Запрос на создание файла в файл сервисе.
 *
 * @author Daniil Gubanov
 * @since 0.2.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateFileRequest {

	/**
	 * Мета информация файла.
	 */
	@FormParam("entity")
	//@PartType(MediaType.APPLICATION_JSON)
	private FileEntity entity;

	/**
	 * Массив байт модифицированного файла.
	 */
	@FormParam("file")
	//@PartType(MediaType.MULTIPART_FORM_DATA)
	byte[] bytes;
}
