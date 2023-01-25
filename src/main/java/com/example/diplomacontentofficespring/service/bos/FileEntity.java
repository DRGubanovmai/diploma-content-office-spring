package com.example.diplomacontentofficespring.service.bos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Dto с информацией о файле для использования в file-сервисе.
 *
 * @author Daniil Gubanov
 * @since 0.2.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class FileEntity {

	/**
	 * Идентификатор контента.
	 */
	String id;
}
