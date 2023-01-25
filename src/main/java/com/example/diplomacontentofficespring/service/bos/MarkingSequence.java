package com.example.diplomacontentofficespring.service.bos;

/**
 * Интерфейс последовательности маркировки.
 *
 * @author Alexander Korznikov
 * @since 0.0.2
 */
public interface MarkingSequence {

	/**
	 * Признак новой последовательности, в противном случае считается что последовательность существующая, и
	 * воспроизводит документ.
	 *
	 * @return - true - если последовательность новая, false - если восстановленная.
	 */
	boolean isNew();

	/**
	 * Зарезервированный на будущее метод.
	 *
	 * @return - 1;
	 */
	default int getVersion() {
		return 1;
	}
}
