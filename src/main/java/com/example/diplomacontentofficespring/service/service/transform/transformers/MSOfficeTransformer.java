package com.example.diplomacontentofficespring.service.service.transform.transformers;

import com.example.diplomacontentofficespring.service.bos.CorrelationId;
import com.example.diplomacontentofficespring.service.bos.MarkingOptions;
import com.example.diplomacontentofficespring.service.bos.MarkingSequence;
import com.example.diplomacontentofficespring.service.bos.NewMarkSequence;
import com.example.diplomacontentofficespring.service.exceptions.MicroException;
import com.example.diplomacontentofficespring.service.service.Transformer;
import com.example.diplomacontentofficespring.service.service.transform.processors.MarkingProcessor;
import com.example.diplomacontentofficespring.service.service.transform.processors.OfficeProcessor;
import com.example.diplomacontentofficespring.service.service.transform.processors.ZipProcessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.inject.Inject;
import javax.xml.stream.XMLStreamException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestClientException;

/**
 * Бин в рамках запроса, для маркировки документа.
 *
 * @author Alexander Korznikov
 * @since 0.1.0
 */
@Slf4j
@Data
public class MSOfficeTransformer implements Transformer<InputStream, OutputStream> {


	/**
	 * Именованный канал для публикации событий эмиттером. Куда конкретно будет смотреть этот канал конфигурируется
	 * в настройках системы.
	 */
//	@Inject
//	@Channel("publish-channel")
//	Emitter<MarkingSequence> emitter;

	/**
	 * Идентификатор корреляции, для связи файла и субъекта.
	 */
	CorrelationId correlationId;

	/**
	 * Опции маркировки - зарезервировано.
	 */
	MarkingOptions options;

	/**
	 * Последовательность используемая в маркировке, новая или существующая.
	 */
	MarkingSequence sequence;

	/**
	 * Процессор для обработки документа в нужном "режиме".
	 * Подготовка {@link com.example.diplomacontentofficespring.service.service.transform.processors.PrepareDocumentProcessor},
	 * маркировка {@link MarkingProcessor}
	 */
	OfficeProcessor processor;

	/**
	 * Маркирует документ, и сбрасывает параметры маркировки в очередь.
	 */
	@Override
	public OutputStream make(InputStream input, Object... params) throws MicroException {
		log.info("Got request for transformation for correlation {}", correlationId);
		OutputStream output = (OutputStream) params[0];

		ZipProcessor zipProcessor = new ZipProcessor(sequence, processor);

		try {
			zipProcessor.process(input, output);
		} catch (IOException e) {
			log.error("IOException occurred", e);
			throw new RestClientException("IO Exception occurred while processing", e);
		} catch (XMLStreamException e) {
			log.error("XMLStreamException occurred", e);
			throw new MicroException("Not valid xml format inside", e);
		}


		return output;
	}
}
