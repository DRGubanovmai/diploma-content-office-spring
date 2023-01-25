package com.example.diplomacontentofficespring.service.service.transform.processors;

import java.io.InputStream;
import java.io.OutputStream;
import javax.xml.stream.XMLStreamException;

/**
 * Интерфейс для процессинга PowerPoint.
 * Пока что только в режиме подготовка, маркировки.
 * При подготовке во всех slides.xml ищутся run блоки с <a:text>{someText}</a:text>
 * Этот текст разбивается по пробелам на отдельные однотипные блоки с теме же с стилями.
 * К каждому пробелу в <a:rPr></a:rPr> добавляется атрибут spc=0.
 *
 * При маркеровки ищутся <a:rPr></a:rPr> с атрибутом spc=0 и заменяется на значение из сиквенса.
 * Также при маркеровке в добавляется смещение всего textBox-a -> в аттрибуты x,y тэга <a:offset></a:offset> прибавляется значение из сиквенса
 *
 * @author Daniil Gubanov
 * @since 0.2.0
 */
public interface SlidesProcessor {
	default void processSlides(InputStream inputStream, OutputStream outputStream) throws XMLStreamException {
		throw new UnsupportedOperationException("Can't process slides for this request");
	}
}
