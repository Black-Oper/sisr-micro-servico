import { resultUrl } from "@/lib/api";
import type { JobDetails } from "@/lib/api";
import { ImageSlider } from "./ImageSlider";

/** Visualização (pura) do estado de um job: status, ou o comparador + erro. */
export function JobView({
  jobId,
  job,
  error,
  inputUrl,
}: {
  jobId: string;
  job: JobDetails | null;
  error: string | null;
  inputUrl?: string | null;
}) {
  if (error) {
    return (
      <p className="error" role="alert">
        Erro: {error}
      </p>
    );
  }
  if (!job) {
    return <p className="status">Carregando...</p>;
  }
  if (job.status === "FAILED") {
    return (
      <p className="error" role="alert">
        Falhou: {job.error ?? "erro desconhecido"}
      </p>
    );
  }
  if (job.status === "DONE") {
    const out = resultUrl(jobId);
    return (
      <div>
        <p className="status">Pronto</p>
        <ImageSlider beforeUrl={inputUrl} afterUrl={out} />
        <p style={{ marginTop: "1rem" }}>
          <a className="btn" href={out} download>
            Baixar resultado
          </a>
        </p>
      </div>
    );
  }
  return <p className="status">Status: {job.status}...</p>;
}
