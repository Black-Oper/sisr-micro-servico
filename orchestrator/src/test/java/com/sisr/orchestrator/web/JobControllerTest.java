package com.sisr.orchestrator.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sisr.orchestrator.domain.JobStatus;
import com.sisr.orchestrator.domain.ScaleFactor;
import com.sisr.orchestrator.service.CreateJobResult;
import com.sisr.orchestrator.service.JobDetails;
import com.sisr.orchestrator.service.JobNotFoundException;
import com.sisr.orchestrator.service.JobNotReadyException;
import com.sisr.orchestrator.service.JobResult;
import com.sisr.orchestrator.service.JobService;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Testes da camada web do JobController (Seção 6.1 do CLAUDE.md).
 * O JobService é mockado (@MockitoBean) para isolar o controller.
 */
@WebMvcTest(JobController.class)
class JobControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    JobService jobService;

    // ---------- POST /api/v1/jobs ----------

    @Test
    void rejeitaRequisicaoSemArquivo() throws Exception {
        mockMvc.perform(multipart("/api/v1/jobs"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejeitaRequisicaoNaoMultipart() throws Exception {
        mockMvc.perform(post("/api/v1/jobs"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejeitaScaleInvalido() throws Exception {
        var arquivo = new MockMultipartFile(
                "file", "in.png", MediaType.IMAGE_PNG_VALUE, "fake".getBytes());

        mockMvc.perform(multipart("/api/v1/jobs").file(arquivo).param("scale", "5"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void aceitaUploadValido() throws Exception {
        var arquivo = new MockMultipartFile(
                "file", "in.png", MediaType.IMAGE_PNG_VALUE, "fake".getBytes());

        when(jobService.createJob(any(), any()))
                .thenReturn(new CreateJobResult("job-123", JobStatus.PENDING, Instant.now()));

        mockMvc.perform(multipart("/api/v1/jobs").file(arquivo).param("scale", "4"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.jobId").value("job-123"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void usaScaleDoisComoPadraoQuandoOmitido() throws Exception {
        var arquivo = new MockMultipartFile(
                "file", "in.png", MediaType.IMAGE_PNG_VALUE, "fake".getBytes());
        when(jobService.createJob(any(), any()))
                .thenReturn(new CreateJobResult("job-1", JobStatus.PENDING, Instant.now()));

        mockMvc.perform(multipart("/api/v1/jobs").file(arquivo))  // sem o parâmetro scale
                .andExpect(status().isAccepted());

        var captor = ArgumentCaptor.forClass(ScaleFactor.class);
        verify(jobService).createJob(any(), captor.capture());
        assertThat(captor.getValue().value()).isEqualTo(2);
    }

    // ---------- GET /api/v1/jobs/{id} ----------

    @Test
    void retorna404QuandoJobNaoExiste() throws Exception {
        when(jobService.findJob("nao-existe")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/jobs/nao-existe"))
                .andExpect(status().isNotFound());
    }

    @Test
    void retornaStatusDoJobQuandoExiste() throws Exception {
        var detalhes = new JobDetails(
                "job-1", JobStatus.DONE, 4,
                Instant.now(), Instant.now(), Instant.now(), null);
        when(jobService.findJob("job-1")).thenReturn(Optional.of(detalhes));

        mockMvc.perform(get("/api/v1/jobs/job-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobId").value("job-1"))
                .andExpect(jsonPath("$.status").value("DONE"))
                .andExpect(jsonPath("$.scale").value(4));
    }

    // ---------- GET /api/v1/jobs/{id}/result ----------

    @Test
    void resultRetorna404QuandoJobNaoExiste() throws Exception {
        when(jobService.getResult("nao-existe")).thenThrow(new JobNotFoundException("nao-existe"));

        mockMvc.perform(get("/api/v1/jobs/nao-existe/result"))
                .andExpect(status().isNotFound());
    }

    @Test
    void resultRetorna409QuandoJobNaoEstaDone() throws Exception {
        when(jobService.getResult("job-1")).thenThrow(new JobNotReadyException("job-1"));

        mockMvc.perform(get("/api/v1/jobs/job-1/result"))
                .andExpect(status().isConflict());
    }

    @Test
    void resultRetornaImagemQuandoDone() throws Exception {
        when(jobService.getResult("job-1"))
                .thenReturn(new JobResult("img".getBytes(), "image/png"));

        mockMvc.perform(get("/api/v1/jobs/job-1/result"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.IMAGE_PNG))
                .andExpect(content().bytes("img".getBytes()));
    }
}
