package com.example.diplomacontentofficespring.service.resource;

import com.example.diplomacontentofficespring.service.bos.ConvertOptions;
import com.example.diplomacontentofficespring.service.bos.CorrelationId;
import com.example.diplomacontentofficespring.service.bos.MarkingRequest;
import com.example.diplomacontentofficespring.service.service.PreparationService;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.inject.Inject;
import com.example.diplomacontentofficespring.service.exceptions.MicroException;
import javax.ws.rs.core.StreamingOutput;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Path("/api/v1/word/prepare")
public class PrepareResource {

    /**
     * Inject MarkingService bean.
     */
    @Inject
    PreparationService service;

    /**
     * Cм. выше.
     */
    @POST
    @Path("/{contentId}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    //form  @MultipartForm
    public Response processCorrIdAndBody(
            @PathParam("contentId") String contentId,
            @Valid MarkingRequest request) throws MicroException {

        return prepareDocument(contentId, request, ConvertOptions.builder().convertTo(ConvertOptions.ConvertType.docx).build());
    }

    /**
     * Базовый метод для вызова сервиса подготовки документа к маркировке.
     *
     * @param contentId - идентификатор файла на storage-service.
     * @param request   - реквест на подготовку.
     */
    private Response prepareDocument(String contentId, MarkingRequest request, ConvertOptions options)
            throws MicroException {
        StreamingOutput streamingOutput = service.prepare(request, CorrelationId.builder().fileId(contentId).build(), options);
        return Response.accepted(streamingOutput).build();
    }
}
