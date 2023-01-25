package com.example.diplomacontentofficespring.service.service.transform.processors;

import java.io.InputStream;
import java.io.OutputStream;
import javax.xml.stream.XMLStreamException;

/**
 * Интерфейс для процессинга msOfficeWord в нужном "режиме".
 *
 * Подготовка {@link PrepareDocumentProcessor},
 * маркировка {@link MarkingProcessor},
 *
 * @author Daniil Gubanov
 * @since 0.2.0
 */
public interface DocumentProcessor {
	/**
	 * обработка document.xml
	 */
	void processDocument(InputStream inputStream, OutputStream outputStream) throws XMLStreamException;

	/**
	 * обработка style.xml
	 */
	void processStyles(InputStream inputStream, OutputStream outputStream) throws XMLStreamException;
}
