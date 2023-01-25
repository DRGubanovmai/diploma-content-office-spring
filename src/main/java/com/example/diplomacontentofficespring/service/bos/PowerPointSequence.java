package com.example.diplomacontentofficespring.service.bos;

/**
 * Базовый интерфейс последовательности для MS PowerPoint формата.
 * Вся трансформация базируется на изменении положения TextBox-a и изменении свойств пробела.
 * Доступные для маркировки смещения TextBox-a и пробелов добавляются в последовательности.
 *
 * @author Alexander Korznikov
 * @author Daniil Gubanov
 * @since 0.1.0
 */
public interface PowerPointSequence extends MarkingSequence {

	/**
	 * Ключевой метод получения сдвига для текущего обрабатываемого TextBox-a.
	 *
	 * @return - смещение.
	 */
	Shift nextShift();

	/**
	 * Ключевой метод для получения значения сдвига текущего обрабатываемого пробела.
	 *
	 * @return - inline spacing.
	 */
	int nextSpacing();

}
