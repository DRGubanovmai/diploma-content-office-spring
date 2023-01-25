package com.example.diplomacontentofficespring.service.service.transform.processors;

import com.example.diplomacontentofficespring.service.service.transform.transformers.DocumentXMLTransformer;
import com.example.diplomacontentofficespring.service.service.transform.transformers.SlidesXMLTransformer;
import com.example.diplomacontentofficespring.service.service.transform.transformers.StyleXMLTransformer;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.stream.XMLStreamException;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Процессор для подготовки документа к маркировке.
 * Для этого в styles.xml добавляется кастомный стиль с id et_0 и атрибутом spacing = 0.
 * В document.xml происходит разбиение блоков текста <w:t>some text</w:t> по пробелам, где к каждому пробелу
 * добавляется ссылка на стиль с 0 спейсингом <w:rStyle w:val="et_0"></w:rStyle>. Так же при процессинге запоминаюся
 * стили и фонты которые были применены к изначальному блоку, которые добавляются к блокам с пробелами.
 * Визуально файл не отличается от оригинала.
 * UPD: Логика снятия маркеров для r7 была объединена со снятием сервиса подготовки. Т.е. если в документе наши стили измененные r7
 * Он найдет их, сохранит в контекст, удалит из style.xml, в document.xml заменит их на et_0 и разобьет текст на пробелы если он не разбит.
 *
 * @author Daniil Gubanov
 * @since 0.2.0
 */
@Data
@NoArgsConstructor
public class PrepareDocumentProcessor implements OfficeProcessor {

	/**
	 * Контекст обработки документа.
	 */
	ResetContext resetContext;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void processDocument(InputStream inputStream, OutputStream outputStream) throws XMLStreamException {
		DocumentXMLTransformer processor = DocumentXMLTransformer.builder()
				.input(inputStream)
				.output(outputStream)
				.resetContext(resetContext)
				.sequence(null)
				.build();

		processor.prepare();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void processStyles(InputStream inputStream, OutputStream outputStream) throws XMLStreamException {
		StyleXMLTransformer processor = StyleXMLTransformer.builder()
				.input(inputStream)
				.output(outputStream)
				.sequence(null)
				.build();

		processor.prepare();

		resetContext = processor.getResetContext();
	}

	/**
	 * Вызов {@link SlidesXMLTransformer} в котором в каждой slide.xml текст внутри run блоков разбивается
	 * на пробелы и к тегу <a:rPr></a:rPr> внутри run блока с пробелом добавляется атрибут spc=0
	 */
	@Override
	public void processSlides(InputStream inputStream, OutputStream outputStream) throws XMLStreamException {
		SlidesXMLTransformer transformer = SlidesXMLTransformer.builder()
				.input(inputStream)
				.output(outputStream)
				.sequence(null)
				.build();

		transformer.prepare();
	}

	/**
	 * Контекст обработки документа при сбрасывании маркеров.
	 * Используется для сохранения изменившихся от r7office styleId
	 * у наших кастомных стилей.
	 * В style.xml ищутся данные стили, удаляются с сохранением styleIds в контекст
	 * Далее в document.xml файле эти id заменяются на et_0.
	 */
	@Data
	public static class ResetContext {

		/**
		 * список id наших кастомных стилей, который r7office назвал по своему.
		 */
		private List<String> styleIds = new ArrayList<>();
	}
}
