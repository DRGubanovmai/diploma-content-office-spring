package com.example.diplomacontentofficespring.service.service.transform.transformers;

import com.example.diplomacontentofficespring.service.bos.PowerPointSequence;
import com.example.diplomacontentofficespring.service.service.transform.XMLNames;
import static com.example.diplomacontentofficespring.service.service.transform.XMLNames.*;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import com.example.diplomacontentofficespring.service.bos.Shift;

/**
 * Процессор для обработки MS PowerPoint.
 *
 * @author Daniil Gubanov
 * @since 0.1.0
 */
@Slf4j
public class SlidesXMLTransformer extends RunTagXMLTransformer<PowerPointSequence> {

	/**
	 * Аттрибут для смещения по X.
	 */
	public static final String ATTRIBUTE_X = "x";

	/**
	 * Аттрибут для смещения по Y.
	 */
	public static final String ATTRIBUTE_Y = "y";

	/**
	 * Пробел.
	 */
	public static final String SPACE = " ";

	/**
	 * Размер шрифта.
	 */
	private Attribute size;

	/**
	 * Флаг начала тега spPr.
	 */
	boolean isShapePropertyStart;

	/**
	 * Флаг на начало run tag-a.
	 */
	private boolean isRunStarted;

	@Builder
	public SlidesXMLTransformer(InputStream input, OutputStream output, PowerPointSequence sequence) throws XMLStreamException {
		super(input, output, sequence);
	}

	/**
	 * Реакция на событие появления тега Offset.
	 *
	 * @param run - событие.
	 * @return - событие, может быть null, если в результате выполнения его не нужно выводить.
	 */
	public XMLEvent startOffset(StartElement run) {
		if (!isShapePropertyStart) {
			return run;
		}
		int x = Integer.parseInt(run.getAttributeByName(QName.valueOf(ATTRIBUTE_X)).getValue());
		int y = Integer.parseInt(run.getAttributeByName(QName.valueOf(ATTRIBUTE_Y)).getValue());
		String prefix = run.getName().getPrefix();

		Shift shift = sequence.nextShift();

		List<Attribute> attrs = new ArrayList<>();
		attrs.add(eventFactory.createAttribute(ATTRIBUTE_X, String.valueOf(x + shift.getDx())));
		attrs.add(eventFactory.createAttribute(ATTRIBUTE_Y, String.valueOf(y + shift.getDy())));

		return eventFactory.createStartElement(prefix, run.getNamespaceURI(prefix),
				run.getName().getLocalPart(), attrs.iterator(), null);
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

			findRunProperties(beforeTextEvents);

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
	 * Маркировка - добавляется проперти с свойством пробела.
	 */
	@Override
	protected void outMark(EndElement runEnd) throws XMLStreamException {
		String p = runEnd.getName().getPrefix();
		String t = runEnd.getName().getLocalPart();
		String ns = runEnd.getName().getNamespaceURI();

		out(eventFactory.createStartElement(p, ns, t));
		if (!runProperties.isEmpty()) {
			runPropertiesAttrubutes.add(eventFactory.createAttribute(XMLNames.SPC_ATTRIBUTE, String.valueOf(0)));
			out(eventFactory.createStartElement(p, ns, RUN_PROPERTIES_TAG, runPropertiesAttrubutes.iterator(), null));
			//скипаем первый потому что добавили его выше
			runProperties.stream().skip(1).forEach(event -> {
				try {
					out(event);
				} catch (XMLStreamException e) {
					log.error("Can't add new elems to writer", e);
				}
			});

			out(eventFactory.createEndElement(p, ns, RUN_PROPERTIES_TAG));
		}
		out(eventFactory.createStartElement(p, ns, XMLNames.TEXT_RANGE_TAG));
		out(eventFactory.createAttribute(XMLNames.XML_NS_PREFIX, ns, XMLNames.SPACE_ATTRIBUTE, XMLNames.SPACE_ATTRIBUTE_VALUE_PRESERVE));
		out(eventFactory.createCharacters(SPACE));
		out(eventFactory.createEndElement(p, ns, XMLNames.TEXT_RANGE_TAG));
		out(runEnd);
	}

	/**
	 * Маркировка документа. При которой ищутся rPr теги с атрибутом spc=0 и изменяется на значение из сиквенса.
	 * Также добавляется в тег offset прибавляется смещение всего текстого блока из сиквенса.
	 */
	@Override
	public void mark() throws XMLStreamException {
		while (reader.hasNext()) {
			XMLEvent event = reader.nextEvent();

			if (event.isStartElement()) {
				//Запоминаем текущий открытый элемент.
				StartElement start = event.asStartElement();

				if (RUN_PROPERTIES_TAG.equals(start.getName().getLocalPart())) {
					log.debug("Start Run");
					event = markRunPropertyTag(start);
				} else if (SHAPE_PROPERTY_TAG.equals(start.getName().getLocalPart())) {
					isShapePropertyStart = true;
				} else if (OFFSET_TAG.equals(start.getName().getLocalPart()) && A_NS_PREFIX.equals(start.getName().getPrefix())) {
					event = startOffset(start);
				}
			} else if (event.isEndElement()) {
				EndElement end = event.asEndElement();
				if (SHAPE_PROPERTY_TAG.equals(end.getName().getLocalPart())) {
					isShapePropertyStart = false;
				}
			}

			if (event != null) { //Будем обнулять если не хотим выводить!
				log.debug(" >> {}", event);
				writer.add(event);
			}
		}
		writer.close();
	}

	/**
	 * Метод для маркировки уже подготовленных slides. В найденном rPr ищется атрибут с spc=0 и он изменяется на новый из sequence.
	 */
	private XMLEvent markRunPropertyTag(StartElement start) {
		Iterator<Attribute> iterator = start.getAttributes();
		while (iterator.hasNext()) {
			Attribute attribute = iterator.next();

			if (attribute.getName().getLocalPart().equals(SPC_ATTRIBUTE) && attribute.getValue().equals(String.valueOf(0))) {
				runPropertiesAttrubutes.add(eventFactory.createAttribute(XMLNames.SPC_ATTRIBUTE, String.valueOf(sequence.nextSpacing())));
			} else {
				runPropertiesAttrubutes.add(attribute);
			}

		}
		StartElement elem = eventFactory.createStartElement(start.getName().getPrefix(), start.getName().getNamespaceURI(),
				RUN_PROPERTIES_TAG, runPropertiesAttrubutes.iterator(), null);

		runPropertiesAttrubutes.clear();
		return elem;
	}

	/**
	 * Метод для находения атрибутов rPr и вложенных "стилей" внутри него из множества beforeTextEvents.
	 */
	private void findRunProperties(List<XMLEvent> events) {
		for (XMLEvent event : events) {
			if (event.isStartElement()) {
				StartElement start = event.asStartElement();
				if (RUN_PROPERTIES_TAG.equals(start.getName().getLocalPart())) {
					start.getAttributes().forEachRemaining(runPropertiesAttrubutes::add);
					isRunStarted = true;
				}
			} else if (event.isEndElement()) {
				EndElement end = event.asEndElement();
				if (RUN_PROPERTIES_TAG.equals(end.getName().getLocalPart())) {
					isRunStarted = false;
				}
			}
			if (isRunStarted) {
				runProperties.add(event);
			}
		}
	}

	/**
	 * Подготовка документа к маркировке.
	 * Ищем run блоки с text тегом внутри и разбиваем текст на пробелы, сохраняя предыдущие стили.
	 * После в аттрибуты rPr tag-a(runProperties) добавляем spc=0(смещение).
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
				log.debug(" >> {}", event);
				writer.add(event);
			}
		}
		writer.close();
	}
}
