// Cliente da API REST do orchestrator (contrato da Seção 6.1 do CLAUDE.md).

export type JobStatus = "PENDING" | "PROCESSING" | "DONE" | "FAILED";

export interface CreateJobResponse {
  jobId: string;
  status: JobStatus;
  createdAt: string;
}

export interface JobDetails {
  jobId: string;
  status: JobStatus;
  scale: number;
  createdAt: string;
  startedAt: string | null;
  finishedAt: string | null;
  resultUrl?: string | null;
  error: string | null;
}

export const API_BASE =
  process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080/api/v1";

export const API_KEY = process.env.NEXT_PUBLIC_API_KEY ?? "dev-local-key";

export async function createJob(
  file: File,
  scale = 2,
): Promise<CreateJobResponse> {
  const form = new FormData();
  form.append("file", file);
  form.append("scale", String(scale));

  const res = await fetch(`${API_BASE}/jobs`, {
    method: "POST",
    body: form,
    headers: { "X-API-Key": API_KEY },
  });
  if (!res.ok) {
    throw new Error(`falha ao criar o job (HTTP ${res.status})`);
  }
  return res.json();
}

export async function getJob(id: string): Promise<JobDetails> {
  const res = await fetch(`${API_BASE}/jobs/${id}`, {
    headers: { "X-API-Key": API_KEY },
  });
  if (!res.ok) {
    throw new Error(`falha ao consultar o job (HTTP ${res.status})`);
  }
  return res.json();
}

export function resultUrl(id: string): string {
  return `${API_BASE}/jobs/${id}/result`;
}
