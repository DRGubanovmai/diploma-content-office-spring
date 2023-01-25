package com.example.diplomacontentofficespring.service.bos;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

/**
 * Опции для конвертации.
 *
 * @author Daniil Gubanov
 * @since 0.2.0
 */
@Data
@Builder
@Jacksonized
public class ConvertOptions {

	/**
	 * Тип в который нужно сконвертировать файл, пока что docx или pdf.
	 */
	private ConvertType convertTo;

	public enum ConvertType {

		/**
		 * pdf from "words".
		 */
		wordsToPdf,

		/**
		 * pdf from "slides".
		 */
		slidesToPdf,

		/**
		 * docx.
		 */
		docx,

		/**
		 * pptx.
		 */
		pptx,

		/**
		 * xlsx.
		 */
		xlsx,

		/**
		 * pdf from "cells".
		 */
		cellsToPdf
	}
}
