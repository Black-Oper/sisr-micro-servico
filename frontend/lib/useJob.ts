import { useEffect, useState } from "react";
import * as api from "./api";
import type { JobDetails } from "./api";

export interface UseJobState {
  job: JobDetails | null;
  error: string | null;
  loading: boolean;
}

const DEFAULT_INTERVAL_MS = 1500;

function isTerminal(status: string): boolean {
  return status === "DONE" || status === "FAILED";
}

/**
 * Consulta o job periodicamente até ele chegar a um estado terminal
 * (DONE/FAILED), então para. Encapsula todo o polling para os componentes.
 */
export function useJob(
  jobId: string | null,
  intervalMs = DEFAULT_INTERVAL_MS,
): UseJobState {
  const [job, setJob] = useState<JobDetails | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!jobId) return;

    let active = true;
    let timer: ReturnType<typeof setTimeout>;

    async function poll() {
      try {
        const details = await api.getJob(jobId as string);
        if (!active) return;
        setJob(details);
        if (!isTerminal(details.status)) {
          timer = setTimeout(poll, intervalMs);
        }
      } catch (e) {
        if (!active) return;
        setError(e instanceof Error ? e.message : "erro ao consultar o job");
      }
    }

    poll();

    return () => {
      active = false;
      clearTimeout(timer);
    };
  }, [jobId, intervalMs]);

  const loading = jobId !== null && job === null && error === null;
  return { job, error, loading };
}
