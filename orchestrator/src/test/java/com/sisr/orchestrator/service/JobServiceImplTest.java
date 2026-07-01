package com.sisr.orchestrator.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sisr.orchestrator.domain.Job;
import com.sisr.orchestrator.domain.JobStatus;
import com.sisr.orchestrator.domain.ScaleFactor;
import com.sisr.orchestrator.messaging.JobMessage;
import com.sisr.orchestrator.messaging.JobPublisher;
import com.sisr.orchestrator.repository.JobRepository;
import com.sisr.orchestrator.storage.ArtifactStorage;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;

/**
 * Teste UNITÁRIO do JobServiceImpl: os 3 colaboradores (repo/storage/publisher)
 * são mockados. Verifica a orquestração de createJob e a lógica de getResult.
 */
class JobServiceImplTest {

    private final JobRepository repository = mock(JobRepository.class);
    private final ArtifactStorage storage = mock(ArtifactStorage.class);
    private final JobPublisher publisher = mock(JobPublisher.class);
    private final JobServiceImpl service =
            new JobServiceImpl(repository, storage, publisher, "sisr-inputs", "sisr-outputs");

    @Test
    void createJobOrquestraOsTresPassos() {
        var file = new MockMultipartFile("file", "input.png", "image/png", "bytes".getBytes());

        CreateJobResult result = service.createJob(file, ScaleFactor.of(4));

        assertThat(result.status()).isEqualTo(JobStatus.PENDING);
        assertThat(result.jobId()).isNotBlank();

        var keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(storage).upload(eq("sisr-inputs"), keyCaptor.capture(), any(), eq("image/png"));
        assertThat(keyCaptor.getValue()).isEqualTo(result.jobId() + "/input.png");

        var jobCaptor = ArgumentCaptor.forClass(Job.class);
        verify(repository).save(jobCaptor.capture());
        Job salvo = jobCaptor.getValue();
        assertThat(salvo.id()).isEqualTo(result.jobId());
        assertThat(salvo.status()).isEqualTo(JobStatus.PENDING);
        assertThat(salvo.scale()).isEqualTo(4);

        var msgCaptor = ArgumentCaptor.forClass(JobMessage.class);
        verify(publisher).publish(msgCaptor.capture());
        JobMessage msg = msgCaptor.getValue();
        assertThat(msg.jobId()).isEqualTo(result.jobId());
        assertThat(msg.inputBucket()).isEqualTo("sisr-inputs");
        assertThat(msg.scale()).isEqualTo(4);
    }

    @Test
    void findJobLeDoRepositorio() {
        var agora = Instant.parse("2026-06-30T12:00:00Z");
        var job = new Job("job-1", JobStatus.DONE, 4,
                "job-1/input.png", "job-1/output.png", agora, agora, agora, null);
        when(repository.findById("job-1")).thenReturn(Optional.of(job));

        Optional<JobDetails> details = service.findJob("job-1");

        assertThat(details).isPresent();
        assertThat(details.get().jobId()).isEqualTo("job-1");
        assertThat(details.get().status()).isEqualTo(JobStatus.DONE);
        assertThat(details.get().scale()).isEqualTo(4);
    }

    @Test
    void getResultLancaQuandoJobNaoExiste() {
        when(repository.findById("x")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getResult("x"))
                .isInstanceOf(JobNotFoundException.class);
    }

    @Test
    void getResultLancaQuandoJobNaoEstaDone() {
        var agora = Instant.parse("2026-06-30T12:00:00Z");
        var job = Job.pending("job-1", 4, "job-1/input.png", agora); // PENDING
        when(repository.findById("job-1")).thenReturn(Optional.of(job));

        assertThatThrownBy(() -> service.getResult("job-1"))
                .isInstanceOf(JobNotReadyException.class);
    }

    @Test
    void getResultBaixaOutputQuandoDone() {
        var agora = Instant.parse("2026-06-30T12:00:00Z");
        var job = new Job("job-1", JobStatus.DONE, 4,
                "job-1/input.png", "job-1/output.png", agora, agora, agora, null);
        when(repository.findById("job-1")).thenReturn(Optional.of(job));
        when(storage.download("sisr-outputs", "job-1/output.png")).thenReturn("img".getBytes());

        JobResult result = service.getResult("job-1");

        assertThat(result.content()).isEqualTo("img".getBytes());
    }
}
