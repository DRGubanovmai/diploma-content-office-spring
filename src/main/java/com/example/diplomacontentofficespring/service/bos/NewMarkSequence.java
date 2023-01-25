package com.example.diplomacontentofficespring.service.bos;

import com.example.diplomacontentofficespring.service.service.transform.XMLNames;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * Реализация новой последовательности для маркировки.
 *
 * @author Alexander Korznikov
 * @since 0.2.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class NewMarkSequence extends ExistedMarkSequence {

	/**
	 * Псевдослучайный генератор.
	 */
	@JsonIgnore
	private Random rnd = new Random();

	/**
	 * Конструктор НОВОЙ последовательности.
	 * Нужен набор стилей.
	 * Можно использовать DEFAULT_STYLES.
	 */
	@Builder
	public NewMarkSequence(int[] styleSpacings, int[] inlineSpacings, List<int[]> shifts) {
		if (styleSpacings == null || styleSpacings.length == 0) {
			throw new IllegalArgumentException("Sequence styles array must have one or more elements.");
		}

		this.styles = new Style[styleSpacings.length + 1];
		// TODO: хардкод стиля с 0 спейсингом, для добавления его тоже
		//  в последовательность для маркировки подготволенного документа как сделать лучше идей нет:(
		styles[0] = new Style(XMLNames.EMPTY_STYLE, 0);
		for (int i = 1; i < styleSpacings.length + 1; i++) {
			styles[i] = new Style("et_" + i, styleSpacings[i - 1]);
		}

		this.shifts = new Shift[shifts.size()];
		for (int i = 0; i < shifts.size(); i++) {
			this.shifts[i] = new Shift(shifts.get(i));
		}

		this.spacings = inlineSpacings;
		this.sequence = new ArrayList<>();
		this.shiftsSequence = new ArrayList<>();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String nextStyle() {
		int styleIndex = rnd.nextInt(styles.length);

		this.sequence.add(styleIndex);

		return styles[styleIndex].getStyleId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int nextSpacing() {
		int spacing = spacings[rnd.nextInt(spacings.length)];

		this.sequence.add(spacing);

		return spacing;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Shift nextShift() {
		int shiftIndex = rnd.nextInt(shifts.length);

		this.shiftsSequence.add(shifts[shiftIndex].toArray());

		return shifts[shiftIndex];
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@JsonIgnore
	public boolean isNew() {
		return true;
	}

}
