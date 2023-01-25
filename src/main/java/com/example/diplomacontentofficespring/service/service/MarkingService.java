package com.example.diplomacontentofficespring.service.service;

import com.example.diplomacontentofficespring.service.bos.*;
import com.example.diplomacontentofficespring.service.exceptions.MicroException;
import com.example.diplomacontentofficespring.service.resource.CustomStreamingOutput;
import com.example.diplomacontentofficespring.service.service.transform.processors.MarkingProcessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.StreamingOutput;
import lombok.extern.slf4j.Slf4j;
import com.example.diplomacontentofficespring.service.service.transform.transformers.MSOfficeTransformer;

/**
 * Сервис маркировки документов.
 *
 * @author Alexander Korznikov
 * @since 0.1.0
 */
@Slf4j
public class MarkingService {

	/**
	 * Конфигурация.
	 */
	//TODO: cahnge config
	@Inject
	OfficeServiceConfig config;

	/**
	 * Трансформер документов.
	 */
	@Inject
	MSOfficeTransformer transformer;

	/**
	 * Mapper для конвертирования сиквенса.
	 */
	private static final ObjectMapper MAPPER = new ObjectMapper();

	/**
	 * Метод генерации маркированного документа.
	 *
	 * @param request       - запрос на маркировку.
	 * @param correlationId - идентификтаор корреляции (файл и субъект).
	 * @return - маркированный документ.
	 * @throws MicroException - что-то пошло не так.
	 */
	public StreamingOutput process(@NotNull(message = "Mark request may not be null") MarkingRequest request,
	                               @Valid CorrelationId correlationId) throws MicroException, JsonProcessingException {
		if (request.getSequence() == null) {
			request.setSequence(NewMarkSequence.builder()
					.styleSpacings(config.docxSpacings())
					.inlineSpacings(config.pptxSpacings())
					.shifts(config.pptxShifts())
					.build());
			log.debug("MarkSequence is null, created new one. \n{}\n", MAPPER.writeValueAsString(request.getSequence()));
		}

		return process(request.getContent(), request.getOptions(), request.getSequence(), correlationId);

	}

	/**
	 * Основной метод трансформации Docx.
	 *
	 * @param input         - входной документ.
	 * @param options       - опции, пока пустые.
	 * @param sequence      - последовательность (новая или существующая).
	 * @param correlationId - идентификатор корреляции (файл и субъект).
	 * @return - выходной поток с маркированным документом в формате Docx.
	 */
	public StreamingOutput process(InputStream input, MarkingOptions options, MarkingSequence sequence, CorrelationId correlationId) {
		MarkingProcessor markingProcessor = new MarkingProcessor();
		markingProcessor.setSequence(sequence);
		transformer.setCorrelationId(correlationId);
		transformer.setOptions(options);
		transformer.setSequence(sequence);
		transformer.setProcessor(markingProcessor);

		return CustomStreamingOutput.<InputStream>builder()
				.transformer(transformer)
				.input(input)
				.build();
	}
}
