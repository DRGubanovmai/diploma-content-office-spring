package com.example.diplomacontentofficespring.service.service.transform.processors;

import com.example.diplomacontentofficespring.service.bos.MarkingSequence;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.*;
import java.nio.file.Files;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.xml.stream.XMLStreamException;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.IOUtils;

/**
 * Процессор для обработки zip архива.
 *
 * @author Alexander Korznikov
 * @since 0.1.0
 */
@Slf4j
public class ZipProcessor {

	/**
	 * path to document/style xml files.
	 */
	private static final String WORD_PATH = "word/";

	/**
	 * style.xml file
	 */
	public static final String STYLES_XML = "styles.xml";

	/**
	 * document.xml file
	 */
	public static final String DOCUMENT_XML = "document.xml";

	/**
	 * style.xml Zip entry.
	 */
	public static final String STYLES_XML_ZIP_ENTRY = WORD_PATH + STYLES_XML;

	/**
	 * document.xml Zip entry.
	 */
	public static final String DOCUMENT_XML_ZIP_ENTRY = WORD_PATH + DOCUMENT_XML;

	/**
	 * document.xml Zip entry.
	 */
	public static final String SLIDE_XML = "ppt/slides/slide\\d+.xml";

	/**
	 * Последовательность внесенных или вносихмы маркировок.
	 */
	private final MarkingSequence sequence;

	/**
	 * Процессером отвечающий за вид последующей обработки.
	 * Подготовка {@link PrepareDocumentProcessor},
	 * маркировка {@link MarkingProcessor}
	 */
	private final OfficeProcessor processor;

	public ZipProcessor(MarkingSequence sequence, OfficeProcessor processor) {
		this.sequence = sequence;
		this.processor = processor;
	}

	/**
	 * Основной метод маркировки MS Word файла. Без ошибок отработает на любом архиве, но смыла в этом нет.
	 * Воспринимает DOCX/PPTX файл как ZIP архив. Распаковывает из input, и каждую zipEntry перекладывает в выходной поток,
	 * кроме файлов, которые нас интерисуют word/document.xml и word/styles.xml, или, ppt/slides/slide[number].xml в которые и вносятся маркировки.
	 *
	 * @param input - входной DOC/DOCX файл.
	 * @param out   - выходной (маркированный) DOC/DOCX файл.
	 * @throws IOException        - что-то не так с чтением/записью.
	 * @throws XMLStreamException - что-то не так с XML.
	 */
	public void process(InputStream input, OutputStream out) throws IOException, XMLStreamException {
		ZipInputStream zis = new ZipInputStream(input);
		ZipOutputStream zos = new ZipOutputStream(out);
		try (zis; zos) {
			Pattern pattern = Pattern.compile(SLIDE_XML);

			File stylesXml = null;
			File documentXml = null;

			ZipEntry entry;
			while ((entry = zis.getNextEntry()) != null) {
				String name = entry.getName();
				log.info("Processing next entry {} with comment {}", name, entry.getComment());

				if (STYLES_XML_ZIP_ENTRY.equalsIgnoreCase(name)) {
					log.info("🅰 Styles process {}", name);
					stylesXml = Files.createTempFile("styles", ".xml").toFile();
					try (FileOutputStream fos = new FileOutputStream(stylesXml)) {
						IOUtils.copy(zis, fos);
					}

				} else if (DOCUMENT_XML_ZIP_ENTRY.equalsIgnoreCase(name)) {
					log.info("🔠 Document process {}", name);
					documentXml = Files.createTempFile("document", ".xml").toFile();
					try (FileOutputStream fos = new FileOutputStream(documentXml)) {
						IOUtils.copy(zis, fos);
					}

				} else if (pattern.matcher(name).matches()) {
					log.info("🅰 Slides process {}", name);
					log.info("New result entry was created.");
					ZipEntry newEntry = new ZipEntry(name);
					zos.putNextEntry(newEntry);
					processor.processSlides(zis, zos);

				} else {
					ZipEntry newEntry = new ZipEntry(name);
					zos.putNextEntry(newEntry);
					log.debug("🚫 Skip {}", name);
					IOUtils.copy(zis, zos);
				}

				zos.closeEntry();
				zis.closeEntry();
				out.flush();
			}

			if (stylesXml != null && documentXml != null) {
				processStyles(stylesXml, zos);
				processDocument(documentXml, zos);
			}

			log.info("seq - \n{}", new ObjectMapper().writeValueAsString(sequence));
			log.info("All done!");
		}
	}

	/**
	 * Метод обработки style.xml файла. Берет ранее сохраненный temp файл,
	 * Создает зип энтри и вызывает трансформацию
	 */
	private void processStyles(File stylesXml, ZipOutputStream zos) throws IOException, XMLStreamException {
		try (FileInputStream stylesXmlInputStream = new FileInputStream(stylesXml)) {
			ZipEntry stylesXmlEntry = new ZipEntry(STYLES_XML_ZIP_ENTRY);
			zos.putNextEntry(stylesXmlEntry);
			processor.processStyles(stylesXmlInputStream, zos);
		} finally {
			stylesXml.delete();
		}
	}

	/**
	 * Метод обработки document.xml файла. Берет ранее сохраненный temp файл,
	 * Создает зип энтри и вызывает трансформацию
	 */
	private void processDocument(File documentXml, ZipOutputStream zos) throws IOException, XMLStreamException {
		try (FileInputStream documentXmlInputStream = new FileInputStream(documentXml)) {
			ZipEntry documentXmlEntry = new ZipEntry(DOCUMENT_XML_ZIP_ENTRY);
			zos.putNextEntry(documentXmlEntry);
			processor.processDocument(documentXmlInputStream, zos);
		} finally {
			documentXml.delete();
		}
	}
}
