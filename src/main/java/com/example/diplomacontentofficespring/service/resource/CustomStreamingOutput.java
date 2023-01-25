package com.example.diplomacontentofficespring.service.resource;

import com.example.diplomacontentofficespring.service.exceptions.MicroException;
import com.example.diplomacontentofficespring.service.service.Transformer;
import java.io.OutputStream;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
/**
 * Реализация интерфейса {@link StreamingOutput}.
 * Работать с анонимной реализацией интерфейса не всегда удобно, а в некоторых случаях вообще невозможно реализовать,
 * см. в пункт {@code IOException выбрасываемый из StreamingOutput} нюансов сервиса <i>micro-ppdf</i>
 *
 * @author Aksenov Ivan
 * @since 0.0.1
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class CustomStreamingOutput<T> implements StreamingOutput {

	/**
	 * Generic входные данные для {@link #transformer}.
	 */
	T input;

	/**
	 * Реализация {@link Transformer}, которая оперирует с {@link OutputStream},
	 * и который потом надо будет передавать через HTTP.
	 */
	Transformer<T, ?> transformer;

	/**
	 * Запись в выходной поток. Что из себя будет представлять данный поток - будет ясно только на этапе runtime.
	 * <p>
	 * Например, в случае возврата {@link StreamingOutput} в качестве ответа на REST запрос, скорее всего внутри
	 * логики обработки ответа в этот метод будет передан {@code  HttpResponse.Writer}, который запишет ответ в выходной
	 * HTTP стрим.
	 *
	 * @param output {@link OutputStream} куда должен быть записан ответ.
	 * @throws WebApplicationException если что-то не срослось с ответом по HTTP.
	 */
	@Override
	public void write(OutputStream output) throws WebApplicationException {
		try {
			transformer.make(input, output);
		} catch (MicroException e) {
			throw new WebApplicationException(e.getMessage(), e, Response.Status.BAD_REQUEST);
		}
	}
}
