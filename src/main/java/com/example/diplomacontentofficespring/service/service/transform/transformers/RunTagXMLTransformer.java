package com.example.diplomacontentofficespring.service.service.transform.transformers;

import com.example.diplomacontentofficespring.service.bos.MarkingSequence;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.*;
import lombok.extern.slf4j.Slf4j;

/**
 * Контекст и обработчик RUN-ов. Весь текст в MS Word/PowerPoint расположен в рамках RUN внутри TEXT
 * <p>
 * Текст в рамках RUN обрабатывается разбиваясь по пробелам.
 * <p>
 * <br/>
 * WARNING: Реализация сделана из расчета на том, что в одном RUN(w:r) не более одного TEXT(w:t).
 * Иначе, все это будет работать не корректно!
 *
 * @author Alexander Korznikov
 * @since 0.1.0
 */
@Slf4j
public abstract class RunTagXMLTransformer<T extends MarkingSequence> extends AbstractXMLTransformer<T> {

	/**
	 * Событие начала текущего RUN.
	 */
	StartElement run;

	/**
	 * Событие начала TEXT.
	 */
	StartElement text;

	/**
	 * Событие строки символов внутри TEXT.
	 */
	Characters characters;

	/**
	 * События ДО TEXT.
	 */
	List<XMLEvent> beforeTextEvents;

	/**
	 * События ПОСЛЕ TEXT.
	 */
	List<XMLEvent> afterTextEvents;

	/**
	 * События ВНУТРИ TEXT. В основном это Attributes.
	 */
	List<XMLEvent> insideTextEvents;

	/**
	 * Признак выхода из TEXT.
	 */
	private boolean afterText;

	/**
	 * Стили, уже примененные к текстового блоку оригинального документа.
	 */
	List<Attribute> currentStyles;

	/**
	 * Фонты, уже примененные к текстового блоку оригинального документа.
	 */
	List<Attribute> currentFonts;

	/**
	 * Аттрибуты run tag-a для дальнейшей их модификации.
	 */
	List<Attribute> runPropertiesAttrubutes;

	/**
	 * Run properties events.
	 */
	List<XMLEvent> runProperties;

	/**
	 * Перекрываем build метод.
	 */
	public RunTagXMLTransformer(InputStream input, OutputStream output, T sequence) throws XMLStreamException {
		super(input, output, sequence);

		beforeTextEvents = new ArrayList<>();
		afterTextEvents = new ArrayList<>();
		insideTextEvents = new ArrayList<>();
		currentStyles = new ArrayList<>();
		currentFonts = new ArrayList<>();

		runPropertiesAttrubutes = new ArrayList<>();
		runProperties = new ArrayList<>();
	}

	/**
	 * Обработка промежуточных событий.
	 *
	 * @param event - событие.
	 */
	protected void addEvent(XMLEvent event) {
		if (text != null) {
			if (afterText) {
				afterTextEvents.add(event);
			} else {
				insideTextEvents.add(event);
			}
		} else {
			beforeTextEvents.add(event);
		}
	}

	/**
	 * Начало RUN.
	 * @param sr - событие.
	 *
	 * @return null если событие обработано.
	 */
	public XMLEvent startRun(StartElement sr) throws XMLStreamException {
		if (run != null) {
			//Значит RUN уже был открыт, и тут открывается ещё один вложенный.
			// Значит надо вывести все что накопили и сбросить.

			out(run);
			out(beforeTextEvents);

			if (text != null) {
				//Значит текст уже был, и либо не был закрыт - но это нарушение формата
				// либо в нем были обрабатываемые символы. Сейчас мы не умеем их обрабатывать, надо бы научиться.
				//TODO: надо подумать как быть, если на верхнем
				// уровне вложенных RUN был Text & hasMarkingChars.
				// Пока идей нет.
				out(text);
				out(insideTextEvents);
			}
			out(afterTextEvents);

			clearRun();
		}

		run = sr;

		return null;
	}

	/**
	 * Событие начала тега text.
	 * @param st - событие.
	 * @return null если событие обработано.
	 */
	public XMLEvent startText(StartElement st) {
		if (run != null) {
			text = st;
			return null;
		}

		return st;
	}

	protected void outRun(String textValue, EndElement end) throws XMLStreamException {
		out(run); //RUN
		out(beforeTextEvents); // all
		out(text);
		out(eventFactory.createCharacters(textValue));
		out(afterTextEvents);
		out(end);
	}

	protected void out(Collection<XMLEvent> events) throws XMLStreamException {
		for (XMLEvent event : events) {
			out(event);
		}
	}

	protected void out(XMLEvent event) throws XMLStreamException {
		log.debug(">> \t{}", event);
		writer.add(event);
	}

	protected void clearRun() {
		clearText();

		run = null;
		beforeTextEvents.clear();
		afterTextEvents.clear();
		currentFonts.clear();
		currentStyles.clear();
		runProperties.clear();
		runPropertiesAttrubutes.clear();
	}

	/**
	 * Конец тега.
	 *
	 * @param end событие.
	 * @return null если событие обработано.
	 */
	public XMLEvent endText(EndElement end) {
		if (!hasMarkingChars(characters)) {
			log.debug("\t\tSkip text processing!");
			skipProcess(characters, end);
			clearText();
			if (run == null) {
				beforeTextEvents.clear();
			}

		} else {
			log.debug("Keep end tag for future");
			afterText = true;
			afterTextEvents.add(end); // Суррогат, чтоб не заводить поле.
		}
		return null;
	}

	protected void skipProcess(Characters characters, EndElement end) {
		//Обрабатывать нечего, не будем и пытаться.
		//Просто добиваем в Run Events
		if (text != null) {
			beforeTextEvents.add(text);
		}

		beforeTextEvents.addAll(insideTextEvents);

		if (characters != null) {
			beforeTextEvents.add(characters);
		}

		beforeTextEvents.add(end);
	}

	protected void clearText() {
		log.debug("Clear Text state.");
		afterText = false;

		text = null;
		characters = null;

		insideTextEvents.clear();
	}

	/**
	 * Старт тега.
	 *
	 * @param start событие
	 * @return null если событие обработано.
	 */
	public XMLEvent startEvent(StartElement start) {
		if (run != null) {
			addEvent(start);

			return null;
		}
		return start;
	}

	/**
	 * Конец тега.
	 *
	 * @param end -событие.
	 * @return null если событие обработано.
	 */
	public XMLEvent endEvent(EndElement end) {
		if (run != null) {
			addEvent(end);

			return null;
		}

		return end;
	}

	/**
	 * Событие символов внутри Текста.
	 *
	 * @param chars - событие.
	 * @return - null если событие обработано.
	 */
	public XMLEvent characterEvent(Characters chars) {
		if (run != null) {
			if (text != null) {
				if (characters != null) {
					characters = eventFactory.createCharacters(characters.getData() + chars.getData());
				} else {
					characters = chars;
				}
			} else {
				addEvent(chars);
			}
			return null;
		}

		return chars;
	}

	protected abstract void outMark(EndElement end) throws XMLStreamException;


	protected boolean hasMarkingChars(Characters characters) {
		return characters != null && (!characters.isCData());
	}

}
