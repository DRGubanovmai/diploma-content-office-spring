package com.example.diplomacontentofficespring.service.service.transform;

import java.io.InputStream;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.input.ProxyInputStream;

/**
 * Хелпер для обработки MS Office документа.
 *
 * @author Alexander Korznikov
 * @since 0.1.0
 */
@Slf4j
public class OfficeHelper {

	/**
	 * Так как мы работаем с ZIP entry, а не с полноценными стримом, её нельзя закрывать. По этому, делаем открытие
	 * ридера через ProxyInputStream, чтоб перекрыть метод close(), иначе ридер закрывает поток и провоцирует исключения дальше.
	 *
	 * @param is - input stream с XML.
	 * @return - ридер событий.
	 * @throws XMLStreamException - что-то пошло не так.
	 */
	public static XMLEventReader createNonClosableXMLEventReader(XMLInputFactory inputFactory, InputStream is) throws XMLStreamException {
		return inputFactory.createXMLEventReader(new ProxyInputStream(is) {
			@Override
			public void close() {
				// Do nothing! Без сопливых закроем!
			}
		});
	}
}
