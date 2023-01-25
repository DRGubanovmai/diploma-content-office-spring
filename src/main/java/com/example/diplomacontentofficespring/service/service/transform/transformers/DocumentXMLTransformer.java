package com.example.diplomacontentofficespring.service.service.transform.transformers;

import com.example.diplomacontentofficespring.service.bos.WordSequence;
import com.example.diplomacontentofficespring.service.service.transform.XMLNames;
import com.example.diplomacontentofficespring.service.service.transform.XMLNames.*;
import static com.example.diplomacontentofficespring.service.service.transform.XMLNames.*;
import com.example.diplomacontentofficespring.service.service.transform.processors.PrepareDocumentProcessor;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

/**
 * Процессор для обработки MS Word.
 *
 * @author Alexander Korznikov
 * @since 0.1.0
 */
@Slf4j
public class DocumentXMLTransformer extends RunTagXMLTransformer<WordSequence> {

	/**
	 * Контекст обработки документа при сбрасывании маркеров.
	 */
	PrepareDocumentProcessor.ResetContext resetContext;

	@Builder
	public DocumentXMLTransformer(InputStream input, OutputStream output, WordSequence sequence,
								  PrepareDocumentProcessor.ResetContext resetContext) throws XMLStreamException {
		super(input, output, sequence);
		this.resetContext = resetContext;
	}

	/**
	 * Закрытие тега RUN. Сама обработка происходит при закрытии.
	 * Если внутри был текст с валидными символами, происходит их маркировка.
	 *
	 * @param end - событие окончания.
	 * @return null если событие обработано.
	 * @throws XMLStreamException - что-то не то с XML.
	 */
	public XMLEvent endRun(EndElement end) throws XMLStreamException {
		if (run == null) {
			//Если RUN пустой, значит был закрыт ранее,
			// значит это вложенный и тогда просто забываем про него и выводим то что есть.
			return end;
		}

		if (hasMarkingChars(characters)) {
			log.debug("Marking run with text");
			// Разрезаем по пробелам!
			String lastChars = characters.getData();
			
			findCurrentTextFontsAndStyles(beforeTextEvents);

			while (lastChars.length() > 0) {
				int spacePosition = lastChars.indexOf(' ');
				String currentRange;
				if (spacePosition > 0) {
					currentRange = lastChars.substring(0, spacePosition);
					lastChars = lastChars.substring(currentRange.length());
					outRun(currentRange, end);
				} else if (spacePosition == 0) {
					lastChars = lastChars.substring(1);
					outMark(end);
				} else {
					outRun(lastChars, end);
					lastChars = "";
				}
			}

		} else { // Если RUN не имел текста, или текст был без маркируемых символов.
			// Просто выводим все что было заработано непосильным трудом.
			log.debug("Simple out run");
			out(run);
			out(beforeTextEvents);
			out(end);
		}
		// Закрыть RUN и очистить, нужно вне зависимости от наличия текста.
		clearRun();

		return null;
	}

	/**
	 * Метод для нахождения стилей и фонтов примененных к изначальных тектовым блокам.
	 */
	private void findCurrentTextFontsAndStyles(List<XMLEvent> events) {
		for (XMLEvent event : events) {
			if (event.isStartElement()) {
				StartElement start = event.asStartElement();
				if (RUN_STYLE_TAG.equals(start.getName().getLocalPart())) {
					Iterator<Attribute> iterator = start.getAttributes();
					while (iterator.hasNext()) {
						currentStyles.add(iterator.next());
					}
				} else if (FONT_PROPERTY_TAG.equals(start.getName().getLocalPart())) {
					Iterator<Attribute> iterator = start.getAttributes();
					while (iterator.hasNext()) {
						currentFonts.add(iterator.next());
					}
				}
			}
		}
	}

	/**
	 * Подготовка к маркировке - добавляется проперти с id стиля с 0 спейсингом для пробела.
	 */
	@Override
	protected void outMark(EndElement runEnd) throws XMLStreamException {
		String p = runEnd.getName().getPrefix();
		String t = runEnd.getName().getLocalPart();
		String ns = runEnd.getName().getNamespaceURI();

		out(eventFactory.createStartElement(p, ns, t)); //Тут бы не надо выводить уж прямо все подряд, но может и надо.
		out(eventFactory.createStartElement(p, ns, XMLNames.RUN_PROPERTIES_TAG));

		if (!currentStyles.isEmpty()) {
			out(eventFactory.createStartElement(p, ns, XMLNames.RUN_STYLE_TAG));
			currentStyles.forEach(style -> {
				try {
					out(eventFactory.createAttribute(style.getName(), style.getValue()));
				} catch (XMLStreamException e) {
					log.info("XMLStreamException occurred while creating currentStyles");
				}
			});
			out(eventFactory.createEndElement(p, ns, XMLNames.RUN_STYLE_TAG));
		}
		
		if (!currentFonts.isEmpty()) {
			out(eventFactory.createStartElement(p, ns, FONT_PROPERTY_TAG));
			currentFonts.forEach(font -> {
				try {
					out(eventFactory.createAttribute(font.getName(), font.getValue()));
				} catch (XMLStreamException e) {
					log.info("XMLStreamException occurred while creating currentFonts");
				}
			});
			out(eventFactory.createEndElement(p, ns, FONT_PROPERTY_TAG));
		}

		out(eventFactory.createStartElement(p, ns, XMLNames.RUN_STYLE_TAG));
		out(eventFactory.createAttribute(p, ns, XMLNames.VAL_ATTRIBUTE, EMPTY_STYLE));

		out(eventFactory.createEndElement(p, ns, XMLNames.RUN_STYLE_TAG));
		out(eventFactory.createEndElement(p, ns, XMLNames.RUN_PROPERTIES_TAG));
		out(eventFactory.createStartElement(p, ns, XMLNames.TEXT_RANGE_TAG));
		out(eventFactory.createAttribute("xml", ns, "space", "preserve"));
		out(eventFactory.createCharacters(" "));
		out(eventFactory.createEndElement(p, ns, XMLNames.TEXT_RANGE_TAG));
		out(runEnd);
	}

	/**
	 * Обработчик document.xml файла
	 * Подготавливает файл к маркировке, разбивая текст по пробелам и добавляя к ним стиль с 0 спейсингом.
	 */
	public void prepare() throws XMLStreamException {
		while (reader.hasNext()) {
			XMLEvent event = reader.nextEvent();

			if (event.isStartElement()) {
				//Запоминаем текущий открытый элемент.
				StartElement start = event.asStartElement();

				if (RUN_TAG.equals(start.getName().getLocalPart())) {
					log.debug("Start Run");
					event = startRun(start);
				} else if (TEXT_RANGE_TAG.equals(start.getName().getLocalPart())) {
					log.debug("\t\tStart Text");
					event = startText(start);
				} else if (RUN_STYLE_TAG.equals(start.getName().getLocalPart())) {
					log.debug("Start run style tag");
					event = startRunStyleTag(start);
				} else {
					log.debug("\tStart Other");
					event = startEvent(start);
				}
			} else if (event.isEndElement()) {
				EndElement end = event.asEndElement();
				if (RUN_TAG.equals(end.getName().getLocalPart())) {
					//Апогей всего и вся
					log.debug("End Run");
					event = endRun(end);
				} else if (TEXT_RANGE_TAG.equals(end.getName().getLocalPart())) {
					log.debug("\t\tEnd Text");
					event = endText(end);
				} else {
					log.debug("\tEnd Other");
					event = endEvent(end);
				}
			} else if (event.isCharacters()) {
				log.debug("\t\tChars!");
				event = characterEvent(event.asCharacters());
			}

			if (event != null) { //Будем обнулять если не хотим выводить!
				log.debug(">> {}", event);
				writer.add(event);
			}
		}
		writer.close();
	}


	/**
	 * обработчик document.xml файла
	 * Маркировка — замена id стиля с 0 спейсингом(et_0) на другие. Сам et_0 тоже включен в маркировку.
	 */
	@Override
	public void mark() throws XMLStreamException {
		while (reader.hasNext()) {
			XMLEvent event = reader.nextEvent();

			if (event.isStartElement()) {
				//Запоминаем текущий открытый элемент.
				StartElement start = event.asStartElement();
				if (RUN_STYLE_TAG.equals(start.getName().getLocalPart())) {
					event = markRunStyleTag(start);
				}
			}
			if (event != null) { //Будем обнулять если не хотим выводить!
				log.debug(">> {}", event);
				writer.add(event);
			}
		}
		writer.close();
	}

	/**
	 * обработка <w:rStyle></w:rStyle> тега, поиск нужного id и замена его из последовательности.
	 */
	private XMLEvent markRunStyleTag(StartElement start) {
		String prefix = start.getName().getPrefix();
		String ns = start.getNamespaceURI(prefix);
		String lp = start.getName().getLocalPart();

		Iterator<Attribute> attributeIterator = start.getAttributes();
		while (attributeIterator.hasNext()) {
			Attribute styleAttribute = attributeIterator.next();

			if (styleAttribute.getValue().equals(EMPTY_STYLE)) {
				styleAttribute = eventFactory.createAttribute(prefix, ns, XMLNames.VAL_ATTRIBUTE, sequence.nextStyle());

				List<Attribute> attrs = new ArrayList<>();
				attrs.add(styleAttribute);

				return eventFactory.createStartElement(prefix, ns,
						lp, attrs.iterator(), null);
			}
		}

		return start;
	}

	/**
	 * Обработчик события начала <w:rStyle></w:rStyle> для последующей демаркировки.
	 * Путем замены id кастомных стилей на стиль с 0 спейсингом(et_0).
	 */
	private XMLEvent startRunStyleTag(StartElement start) {
		Iterator<Attribute> attributeIterator = start.getAttributes();
		while (attributeIterator.hasNext()) {
			Attribute styleAttribute = attributeIterator.next();

			if (!resetContext.getStyleIds().isEmpty()) {
				if (resetContext.getStyleIds().contains(styleAttribute.getValue())) {
					log.debug("Find our custom styles in document.xml file");
					return null;
				}
			}
		}
		addEvent(start);
		return null;
	}
}
