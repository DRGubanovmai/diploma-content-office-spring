package com.example.diplomacontentofficespring.service.service;

import java.util.List;

/**
 * Конфигурация для маркировки MS Office документа.
 *
 * @author Alexander Korznikov
 * @since 0.1.0
 */
//@ConfigMapping(prefix = "micro.word")
public interface OfficeServiceConfig {

	/**
	 * Aspose license path.
	 */
	String asposeLicensePath();

	/**
	 * Смещение пробела для вордовго документа.
	 */
	int[] docxSpacings();

	/**
	 * Смещение пробела для PowerPoint-а.
	 */
	int[] pptxSpacings();

	/**
	 * Смещение TextBox-a для  PowerPoint-а.
	 */
	List<int[]> pptxShifts();

}
