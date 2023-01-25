package com.example.diplomacontentofficespring.service.bos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import lombok.Data;

/**
 * Реализация существуйющей последовательности для маркировки.
 *
 * @author Alexander Korznikov
 * @since 0.0.2
 */
@Data
public class ExistedMarkSequence implements WordSequence, PowerPointSequence {
	/**
	 * Стили, которые применялись при маркировке.
	 */
	protected Style[] styles;

	/**
	 * Возможные смещения боксов по координатам X,Y.
	 */
	protected Shift[] shifts;

	/**
	 * Возможные inline spacings.
	 */
	protected int[] spacings;

	/**
	 * Последовательность примененных стилей или расстояний после пробелов (spacing).
	 */
	protected ArrayList<Integer> sequence;

	/**
	 * Последовательность примененных смещений для блоков в рамках PowerPoint.
	 */
	protected ArrayList<int[]> shiftsSequence;

	/**
	 * Связка с файлом и субъектом.
	 */
	protected CorrelationId correlationId;

	/**
	 * Текущая позиция в последовательности стилей.
	 * Переменная состояния, не сохраняется и не вычитывается, стартует всегда с нуля.
	 */
	@JsonIgnore
	private int currentPosition = 0;

	/**
	 * Текущая позиция в последовательности смещений для блоков в рамках PowerPoint.
	 * Переменная состояния, не сохраняется и не вычитывается, стартует всегда с нуля.
	 */
	@JsonIgnore
	private int currentShiftPosition = 0;

	/**
	 * Версия сервиса для сохранения в сиквенс.
	 */
	protected String serviceVersion;

	/**
	 * Название данного сервиса для сохранения в сиквенс.
	 */
	protected String serviceName;

	/**
	 * Версия процессора.
	 */
	protected int version = 1;

	/**
	 * Массив примененных стилей.
	 *
	 * @return - массив стилей.
	 */
	@Override
	public Style[] getStyles() {
		return styles;
	}

	/**
	 * Следующий стиль.
	 *
	 * @return - идентификатор стиля.
	 */
	@Override
	public String nextStyle() {
		return styles[sequence.get(currentPosition++)].getStyleId();
	}

	/**
	 * Всегда ложь.
	 *
	 * @return - false.
	 */
	@Override
	public boolean isNew() {
		return false;
	}

	/**
	 * Версия процессора, зарезервировано.
	 *
	 * @return - версия.
	 */
	@Override
	public int getVersion() {
		return this.version;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Shift nextShift() {
		return new Shift(shiftsSequence.get(currentShiftPosition++));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int nextSpacing() {
		return sequence.get(currentPosition++);
	}
}
