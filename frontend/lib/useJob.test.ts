import { describe, it, expect, vi, afterEach } from "vitest";
import { renderHook, waitFor, cleanup } from "@testing-library/react";
import { useJob } from "./useJob";
import * as api from "./api";
import type { JobDetails, JobStatus } from "./api";

afterEach(() => {
  cleanup();
  vi.restoreAllMocks();
});

function job(status: JobStatus): JobDetails {
  return {
    jobId: "job-1",
    status,
    scale: 2,
    createdAt: "2026-07-01T00:00:00Z",
    startedAt: null,
    finishedAt: null,
    error: null,
  };
}

describe("useJob (polling)", () => {
  it("consulta o job e expõe o status", async () => {
    vi.spyOn(api, "getJob").mockResolvedValue(job("PROCESSING"));

    const { result } = renderHook(() => useJob("job-1", 10));

    await waitFor(() => expect(result.current.job?.status).toBe("PROCESSING"));
  });

  it("atualiza até DONE e para de consultar", async () => {
    const getJob = vi
      .spyOn(api, "getJob")
      .mockResolvedValueOnce(job("PROCESSING"))
      .mockResolvedValueOnce(job("DONE"));

    const { result } = renderHook(() => useJob("job-1", 10));

    await waitFor(() => expect(result.current.job?.status).toBe("DONE"));
    expect(getJob).toHaveBeenCalledTimes(2);

    // confirma que PAROU de consultar após o estado terminal
    const chamadas = getJob.mock.calls.length;
    await new Promise((r) => setTimeout(r, 40));
    expect(getJob.mock.calls.length).toBe(chamadas);
  });

  it("expõe erro se a consulta falhar", async () => {
    vi.spyOn(api, "getJob").mockRejectedValue(new Error("rede caiu"));

    const { result } = renderHook(() => useJob("job-1", 10));

    await waitFor(() => expect(result.current.error).toBeTruthy());
  });
});
