package com.example.diplomacontentofficespring.service.service.transform.transformers;

import com.example.diplomacontentofficespring.service.bos.Style;
import com.example.diplomacontentofficespring.service.bos.WordSequence;
import com.example.diplomacontentofficespring.service.service.transform.XMLNames;
import com.example.diplomacontentofficespring.service.service.transform.processors.PrepareDocumentProcessor;
import java.io.IOException;
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
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Процессер для обработки styles.xml файла.
 *
 * @author Alexander Korznikov
 * @since 0.1.0
 */
@Slf4j
public class StyleXMLTransformer extends AbstractXMLTransformer<WordSequence> {

	/**
	 * Флаг для пометки, что последующие эвенты не нужно записывать в аутпут.
	 */
	boolean isSkippable;

	/**
	 * Контекст обработки документа при сбрасывании маркеров.
	 */
	@Getter
	PrepareDocumentProcessor.ResetContext resetContext;

	/**
	 * События до w:name.
	 */
	List<XMLEvent> beforeNameTagEvent;

	/**
	 * Конструктор.
	 */
	@Builder
	public StyleXMLTransformer(InputStream input, OutputStream output, WordSequence sequence) throws XMLStreamException {
		super(input, output, sequence);
		beforeNameTagEvent = new ArrayList<>();
		resetContext = new PrepareDocumentProcessor.ResetContext();
	}

	/**
	 * Обработчик styles.xml файла.
	 * По сути записывает строку со стилем из последовательности в аутпут стрим.
	 */
	@Override
	public void mark() throws XMLStreamException {
		while (reader.hasNext()) {
			//Поймать событие окончания </styles> и создать ещё пару тройку событий!
			// Но для документа надо по другому!
			XMLEvent event = reader.nextEvent();

			// если </styles>
			if (event.isEndElement()) {
				EndElement el = event.asEndElement();
				if (XMLNames.STYLES_TAG.equals(el.getName().getLocalPart())) {
					for (Style customStyle : sequence.getStyles()) {
						if (customStyle.getStyleId().equals(XMLNames.EMPTY_STYLE)) {
							continue;
						}
						log.info("🅰  Add custom style with id {}", customStyle.getStyleId());
						try {
							customStyle.printOut(output);
						} catch (IOException e) {
							log.error("Can't printout new styles into XML output.", e);
							throw new XMLStreamException("Can't output new styles", e);
						}
					}
				}
			}
			writer.add(event);
		}
		writer.close();
	}

	/**
	 * Обработчик style.xml файла для удаления кастомных стилей в нем.
	 */
	public void prepare() throws XMLStreamException {
		while (reader.hasNext()) {
			XMLEvent event = reader.nextEvent();

			if (event.isStartElement()) {
				StartElement start = event.asStartElement();

				if (XMLNames.STYLE_TAG.equals(start.getName().getLocalPart())) {
					log.debug("Start style tag");
					event = startStyleTag(start);
				} else if (XMLNames.NAME_TAG.equals(start.getName().getLocalPart())) {
					log.debug("Start name tag");
					event = startNameTag(start);
				} else {
					log.debug("Start other tag");
					event = startEvent(start);
				}

			} else if (event.isEndElement()) {
				EndElement end = event.asEndElement();
				if (XMLNames.STYLE_TAG.equals(end.getName().getLocalPart())) {
					log.debug("End style tag");
					event = endStyle(end);
				} else if (XMLNames.STYLES_TAG.equals(end.getName().getLocalPart())) {
					log.debug("Add 0 spacing style");
					Style style = new Style(XMLNames.EMPTY_STYLE, 0);
					try {
						style.printOut(output);
					} catch (IOException e) {
						log.error("Can't printout new styles into XML output.", e);
						throw new XMLStreamException("Can't output new styles", e);
					}
				} else {
					log.debug("End other tag");
					event = endEvent(end);
				}
			}

			if (event != null) {
				log.debug("add event {}", event);
				writer.add(event);
			}
		}
		writer.close();
	}

	/**
	 * Обработка начала тэга w:name при которой ищутся наши кастомные стили
	 * и сохраняются id измененные r7office. Если их внутри данного тега их нет, то
	 * добавляем все, что запомнили до этого и ставим флаг на пропуск в false.
	 */
	private XMLEvent startNameTag(StartElement start) throws XMLStreamException {
		if (isSkippable) {
			Iterator<Attribute> attributeIterator = start.getAttributes();
			while (attributeIterator.hasNext()) {
				Attribute styleAttribute = attributeIterator.next();
				if (styleAttribute.getValue().equals("et_0")
						|| styleAttribute.getValue().equals("et_1")
						|| styleAttribute.getValue().equals("et_2")
						|| styleAttribute.getValue().equals("et_3")) {
					saveStyleId();
					return null;
				}
			}
		}
		isSkippable = false;
		addBeforeNameTagEvents();
		return start;
	}

	/**
	 * Метод для запоминания id наших стилей изменных r7office в контекст.
	 */
	private void saveStyleId() {
		XMLEvent event = beforeNameTagEvent.get(beforeNameTagEvent.size() - 1);
		if (event.isStartElement()) {
			StartElement start = event.asStartElement();
			if (XMLNames.STYLE_TAG.equals(start.getName().getLocalPart())) {
				log.debug("Start style tag");
				Iterator<Attribute> attributeIterator = start.getAttributes();
				while (attributeIterator.hasNext()) {
					Attribute styleAttribute = attributeIterator.next();
					if (styleAttribute.getName().getLocalPart().equals("styleId")) {
						resetContext.getStyleIds().add(styleAttribute.getValue());
					}

				}
			}
		}
	}

	/**
	 * Добавление эвентов запомненных до тэга w:name в output.
	 */
	private void addBeforeNameTagEvents() throws XMLStreamException {
		for (XMLEvent event : beforeNameTagEvent) {
			writer.add(event);
		}
		beforeNameTagEvent.clear();
	}

	/**
	 * Начало тэга w:style проставляем флаг на пропуск в true и запоминаем этот эвент.
	 */
	private XMLEvent startStyleTag(StartElement start) {
		if (!isSkippable) {
			isSkippable = true;
			beforeNameTagEvent.add(start);
		}
		return null;
	}

	/**
	 * Метод для обработки события закрытия тегов внутри <w:style></w:style>.
	 *
	 * @return null, если тег не нужно записывать в output или event без изменений
	 */
	private XMLEvent endEvent(EndElement end) {
		if (isSkippable) {
			beforeNameTagEvent.add(end);
			return null;
		}
		return end;
	}

	/**
	 * Метод для обработки события открытия тегов внутри <w:style></w:style>.
	 *
	 * @return null, если тег не нужно записывать в output или event без изменений
	 */
	private XMLEvent startEvent(StartElement start) {
		if (isSkippable) {
			beforeNameTagEvent.add(start);
			return null;
		}
		return start;
	}

	/**
	 * Метод для обработки события закрытия тега <w:style></w:style>.
	 * Если уже обрабываем нужный пропускаем и сбрасываем флаг обратно.
	 *
	 * @return null, если тег не нужно записывать в output или event без изменений
	 */
	private XMLEvent endStyle(EndElement end) {
		if (isSkippable) {
			isSkippable = false;
			return null;
		}
		return end;
	}
}
