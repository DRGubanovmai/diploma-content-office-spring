package com.example.diplomacontentofficespring.service.service;

import com.example.diplomacontentofficespring.service.exceptions.MicroException;

/**
 * // TODO : describe javaDocs.
 *
 * @author Aksenov Ivan
 * @since 0.0.1
 */
public interface Transformer<T, R> {

	@SuppressWarnings("UnusedReturnValue")
	R make(T input, Object... params) throws MicroException;

	enum OutputResult {

		/**
		 * Офисный формат. docx | pptx | xlsx
		 */
		office,

		/**
		 * Pdf.
		 */
		pdf
	}
}
