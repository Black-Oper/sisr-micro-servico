package com.sisr.orchestrator.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sisr.orchestrator.service.JobNotFoundException;
import com.sisr.orchestrator.service.JobService;
import com.sisr.orchestrator.web.JobController;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Testa o filtro de API key (aplicado antes dos controllers). A chave válida
 * de teste é "test-key" (definida via @TestPropertySource).
 */
@WebMvcTest(JobController.class)
@TestPropertySource(properties = "security.api-key=test-key")
class ApiKeyAuthFilterTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    JobService jobService;

    @Test
    void semChaveRetorna401() throws Exception {
        mockMvc.perform(get("/api/v1/jobs/abc"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void chaveErradaRetorna401() throws Exception {
        mockMvc.perform(get("/api/v1/jobs/abc").header("X-API-Key", "errada"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void chaveCorretaPassa() throws Exception {
        when(jobService.findJob("abc")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/jobs/abc").header("X-API-Key", "test-key"))
                .andExpect(status().isNotFound()); // passou o filtro e chegou ao controller
    }

    @Test
    void downloadDoResultadoEhPublico() throws Exception {
        // /result não exige chave (URL-capability protegida pelo UUID).
        when(jobService.getResult("abc")).thenThrow(new JobNotFoundException("abc"));

        mockMvc.perform(get("/api/v1/jobs/abc/result"))
                .andExpect(status().isNotFound()); // 404, e NÃO 401 -> passou o filtro
    }

    @Test
    void preflightOptionsNaoExigeChave() throws Exception {
        // O navegador manda OPTIONS (preflight CORS) sem X-API-Key antes do
        // POST/GET real. O filtro não pode bloquear isso com 401, senão o
        // navegador nunca chega a ver os cabeçalhos de CORS da resposta real.
        var result = mockMvc.perform(options("/api/v1/jobs/abc")
                        .header("Origin", "https://sisr-micro-servico.vercel.app")
                        .header("Access-Control-Request-Method", "GET"))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isNotEqualTo(401);
    }
}
