package com.example.diplomacontentofficespring.service.service.transform.processors;

import com.example.diplomacontentofficespring.service.bos.MarkingSequence;
import com.example.diplomacontentofficespring.service.bos.PowerPointSequence;
import com.example.diplomacontentofficespring.service.bos.WordSequence;
import com.example.diplomacontentofficespring.service.service.transform.transformers.DocumentXMLTransformer;
import com.example.diplomacontentofficespring.service.service.transform.transformers.SlidesXMLTransformer;
import com.example.diplomacontentofficespring.service.service.transform.transformers.StyleXMLTransformer;
import java.io.InputStream;
import java.io.OutputStream;
import javax.xml.stream.XMLStreamException;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Процессор для маркировки документа.
 * На вход для маркировку уже требуется подготовленный к маркировке документ {@link PrepareDocumentProcessor}.
 * При маркировке в styles.xml добавляются кастомные стили со смещением задаваемым из конфига и id = et_0..n.
 * Далее происходит процессинг document.xml в котором ищутся стили с id=et_0 и заменяются на id из последовательности.
 *
 * @author Daniil Gubanov
 * @since 0.2.0
 */
@Data
@NoArgsConstructor
public class MarkingProcessor implements OfficeProcessor {

	/**
	 * Последовательность маркировки.
	 */
	MarkingSequence sequence;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void processDocument(InputStream inputStream, OutputStream outputStream) throws XMLStreamException {
		DocumentXMLTransformer processor = DocumentXMLTransformer.builder()
				.input(inputStream)
				.output(outputStream)
				.sequence((WordSequence) sequence)
				.build();

		processor.mark();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void processStyles(InputStream inputStream, OutputStream outputStream) throws XMLStreamException {
		StyleXMLTransformer processor = StyleXMLTransformer.builder()
				.input(inputStream)
				.output(outputStream)
				.sequence((WordSequence) sequence)
				.build();

		processor.mark();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void processSlides(InputStream inputStream, OutputStream outputStream) throws XMLStreamException {
		SlidesXMLTransformer processor = SlidesXMLTransformer.builder()
				.input(inputStream)
				.output(outputStream)
				.sequence((PowerPointSequence) sequence)
				.build();
		processor.mark();
	}
}
