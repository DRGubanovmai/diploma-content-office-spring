package com.example.diplomacontentofficespring.service.bos;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Стиль маркера. Стиль - сущность из документа, применяется к тому или иному встреченному пробелу в документе.
 * <p>
 * Специальные стили в файле doc.docx/word/styles.xml выглядят следующим образом:
 * увеличенный пробел -
 * <w:style w:type="character" w:customStyle="1" w:styleId="et-b">
 * <w:name w:val="et-b"/>
 * <w:uiPriority w:val="1"/>
 * <w:rPr>
 * <w:spacing w:val="40"/>
 * </w:rPr>
 * </w:style>
 * <p>
 * уменьшенный пробел -
 * <w:style w:type="character" w:customStyle="1" w:styleId="et-s">
 * <w:name w:val="et-s"/>
 * <w:uiPriority w:val="1"/>
 * <w:rPr>
 * <w:spacing w:val="-20"/>
 * </w:rPr>
 * </w:style>
 *
 * @author Alexander Korznikov
 * @since 0.1.0
 */
@Data
@NoArgsConstructor
public class Style {
	/**
	 * Шаблон для вывода XML.
	 */
	private static final String STYLE_START = "<w:style w:type=\"character\" w:customStyle=\"1\" w:styleId=\"";
	/**
	 * Шаблон для вывода XML.
	 */
	private static final String STYLE_NAME  = "\"><w:name w:val=\"";

	/**
	 * Шаблон для вывода XML.
	 */
	private static final String SPACING = "\"/><w:rPr><w:spacing w:val=\"";

	/**
	 * Шаблон для вывода XML.
	 */
	private static final String STYLE_END = "\"/><w:hidden/></w:rPr></w:style>";

	/**
	 * Идентификатор стиля.
	 */
	private String styleId;

	/**
	 * Отступ. Если положительный то вправо, если отрицательный - влево. Изменяется в типографских пойнтах.
	 */
	private int spacing;

	/**
	 * Конструктор стиля.
	 *
	 * @param styleId - идентификатор (должен быть уникален в рамках одногой Sequence.
	 * @param spacing - отступ. +10 перед пробелом добавится 1.0 pt (пункт), если -10 - отнимется, т.е. пробел съедет.
	 */
	@Builder
	public Style(String styleId, int spacing) {
		this.styleId = styleId;
		this.spacing = spacing;
	}


	/**
	 * Для вывода текущего стиля в нужном XML формате.
	 *
	 * @param os - выходной поток.
	 * @throws IOException - не может записать.
	 */
	public void printOut(OutputStream os) throws IOException {
		os.write((STYLE_START + styleId + STYLE_NAME + styleId
				+ SPACING + spacing + STYLE_END).getBytes(StandardCharsets.UTF_8));
	}
}
