package com.example.diplomacontentofficespring.service.service.transform.processors;

/**
 * Базовый интерфейс для вызова методов процессинга отдельных xml сущностей из {@link ZipProcessor}.
 * Временное решение, пока нет представления как лучше сделать.
 *
 * @author Daniil Gubanov
 * @since 0.2.0
 */
public interface OfficeProcessor extends DocumentProcessor, SlidesProcessor {
}
