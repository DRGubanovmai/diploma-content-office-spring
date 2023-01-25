package com.example.diplomacontentofficespring.service.bos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Сдвиг TextBox-a.
 *
 * @author Alexander Korznikov
 * @author Daniil Gubanov
 * @since 0.1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Shift {

	/**
	 * Индекс сдвига по X в массиве values.
	 */
	private static final int X = 0;

	/**
	 * Индекс сдвига по Y в массиве values.
	 */
	private static final int Y = 1;

	/**
	 * Значение свдига - пара dx-dy.
	 * int[0] - dx.
	 * int[1] - dy.
	 */
	int[] values;

	@JsonIgnore
	public int[] toArray() {
		return values;
	}

	@JsonIgnore
	public int getDx() {
		return this.values[X];
	}

	@JsonIgnore
	public int getDy() {
		return this.values[Y];
	}

}
