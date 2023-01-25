package com.example.diplomacontentofficespring.service.resource;

import com.example.diplomacontentofficespring.service.bos.CorrelationId;
import com.example.diplomacontentofficespring.service.bos.MarkingRequest;
import com.example.diplomacontentofficespring.service.exceptions.MicroException;
import com.example.diplomacontentofficespring.service.service.MarkingService;
import java.io.IOException;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Path("/api/v1/word")
public class MarkingResource {

    /**
     * Сервис маркировки.
     */
    @Inject
    MarkingService service;

    /** Получение существующего документа в маркированном виде.
     *
     *
     * @param contentId - идентификатор файла на storage-service.
     * @param subjectId - субъект, вставляемый в MarkSequence генерируемый при маркировке.
     *
     * @return - маркированный файл в формате DOC/DOCX.
     * @throws IOException - что-то не читается или не пишется.
     * @throws MicroException - логическая ошибка.
     */
    @GET
    @Path("/{contentId}/{subjectId}")
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response processCorrIdNoBody(
            @PathParam("contentId") @NotBlank String contentId,
            @PathParam("subjectId") @NotBlank String subjectId) throws IOException, MicroException {

        return callService(contentId, subjectId, MarkingRequest.builder().build());
    }

    /**
     * Cм. выше.
     */
    @POST
    @Path("/{contentId}/{subjectId}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    //@MultipartForm
    public Response processCorrIdAndBody(
            @PathParam("contentId") String contentId,
            @PathParam("subjectId") String subjectId,
            @Valid MarkingRequest request) throws IOException, MicroException {

        return callService(contentId, subjectId, request);
    }

    /**
     * Смотри выше.
     */
    private Response callService(String contentId, String subjectId, MarkingRequest request)
            throws IOException, MicroException {

        StreamingOutput streamingOutput = service.process(request, CorrelationId.builder().fileId(contentId).sessionId(subjectId).build());
        return Response.accepted(streamingOutput).build();
    }

}
