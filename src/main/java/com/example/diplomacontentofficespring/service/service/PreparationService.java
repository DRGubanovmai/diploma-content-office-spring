package com.example.diplomacontentofficespring.service.service;

import com.example.diplomacontentofficespring.service.bos.ConvertOptions;
import com.example.diplomacontentofficespring.service.bos.CorrelationId;
import com.example.diplomacontentofficespring.service.bos.MarkingOptions;
import com.example.diplomacontentofficespring.service.bos.MarkingRequest;
import com.example.diplomacontentofficespring.service.exceptions.MicroException;
import com.example.diplomacontentofficespring.service.resource.CustomStreamingOutput;
import com.example.diplomacontentofficespring.service.service.transform.processors.PrepareDocumentProcessor;
import com.example.diplomacontentofficespring.service.service.transform.transformers.MSOfficeTransformer;
import java.io.InputStream;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.StreamingOutput;
import lombok.extern.slf4j.Slf4j;

/**
 * Сервис подготовки документа к маркировке.
 *
 * @author Daniil Gubanov
 * @since 0.2.0
 */
@Slf4j
public class PreparationService {

	/**
	 * Конфигурация.
	 */
	//TODO: получить конфиги.
	@Inject
	OfficeServiceConfig config;

	/**
	 * Трансформер документов.
	 */
	@Inject
	MSOfficeTransformer transformer;

	/**
	 * Magic number for zip file format.
	 */
	private static final byte[] ZIP_FORMAT_SIGNATURE = new byte[] {0x50, 0x4b, 0x03, 0x04};

	/**
	 * Odt mimetype.
	 */
	private static final String ODT_MIME_TYPE = "application/vnd.oasis.opendocument.text";

	/**
	 * Метод генерации подготовленного к маркировке документа.
	 *
	 * @param request       - запрос на маркировку.
	 * @param correlationId - идентификтаор корреляции (файл и субъект).
	 * @return - маркированный документ.
	 */
	public StreamingOutput prepare(@NotNull(message = "Mark request may not be null") MarkingRequest request,
								   CorrelationId correlationId, ConvertOptions options) throws MicroException {
		return prepare(request.getContent(), request.getOptions(), correlationId);
	}

	/**
	 * Основной метод подготовки Docx к трансформации.
	 *
	 * @param input         - входной документ.
	 * @param options       - опции, пока пустые.
	 * @param correlationId - идентификатор корреляции (файл и субъект).
	 * @return - выходной поток с маркированным документом в формате Docx.
	 */
	public StreamingOutput prepare(InputStream input, MarkingOptions options, CorrelationId correlationId) {
		transformer.setCorrelationId(correlationId);
		transformer.setOptions(options);
		transformer.setProcessor(new PrepareDocumentProcessor());

		return CustomStreamingOutput.<InputStream>builder()
				.transformer(transformer)
				.input(input)
				.build();
	}
}
