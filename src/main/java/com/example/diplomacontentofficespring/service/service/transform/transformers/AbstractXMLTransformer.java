package com.example.diplomacontentofficespring.service.service.transform.transformers;

import com.example.diplomacontentofficespring.service.bos.MarkingSequence;
import com.example.diplomacontentofficespring.service.service.transform.OfficeHelper;
import java.io.InputStream;
import java.io.OutputStream;
import javax.xml.stream.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Абстрактный класс для всех процессесоров.
 *
 * @author Alexander Korznikov
 * @since 0.1.0
 */
@Slf4j
@Data
public abstract class AbstractXMLTransformer<T extends MarkingSequence> {

	/**
	 * Фабрика для создания XMLEventReader.
	 */
	static final XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();

	/**
	 * Фабрика для создания XMLEventWriter.
	 */
	static final XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();

	/**
	 * Фабрика для создания новых XMLEvents.
	 */
	static final XMLEventFactory eventFactory = XMLEventFactory.newInstance();

	/**
	 * Кодировка для XMLEventWriter.
	 */
	static final String ENCODING = "UTF-8";

	/**
	 * Входной поток XML документа.
	 */
	final InputStream input;

	/**
	 * Выходной поток измененного XML документа.
	 */
	final OutputStream output;

	/**
	 * Последовательность, применяемая.
	 */
	T sequence;

	/**
	 * Текущий обработчик входного потока XML.
	 */
	final XMLEventReader reader;

	/**
	 * Текущий выходной поток результирующего XML.
	 */
	final XMLEventWriter writer;

	/**
	 * Конструктор.
	 *
	 * @param input - input stream.
	 * @param output - output stream.
	 * @param sequence - последовательность для маркировки.
	 */
	public AbstractXMLTransformer(final InputStream input, final OutputStream output, T sequence) throws XMLStreamException {
		this.input = input;
		this.output = output;
		this.sequence = sequence;

		reader = OfficeHelper.createNonClosableXMLEventReader(xmlInputFactory, this.input);
		writer = xmlOutputFactory.createXMLEventWriter(this.output, ENCODING);
	}

	/**
	 * Абстарктный метод обработки xml сущности.
	 */
	public abstract void mark() throws XMLStreamException;
}
