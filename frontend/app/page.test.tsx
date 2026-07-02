import { describe, it, expect, vi, afterEach } from "vitest";
import { render, screen, cleanup } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import Home from "@/app/page";
import * as api from "@/lib/api";
import type { JobDetails, JobStatus } from "@/lib/api";

afterEach(() => {
  cleanup();
  vi.restoreAllMocks();
});

function job(status: JobStatus): JobDetails {
  return {
    jobId: "job-x",
    status,
    scale: 2,
    createdAt: "2026-07-01T00:00:00Z",
    startedAt: null,
    finishedAt: null,
    error: null,
  };
}

describe("página (fluxo completo)", () => {
  it("envia a imagem e mostra o resultado quando DONE", async () => {
    vi.spyOn(api, "createJob").mockResolvedValue({
      jobId: "job-x",
      status: "PENDING",
      createdAt: "2026-07-01T00:00:00Z",
    });
    vi.spyOn(api, "getJob").mockResolvedValue(job("DONE"));
    const user = userEvent.setup();

    render(<Home />);

    const file = new File(["x"], "in.png", { type: "image/png" });
    await user.upload(screen.getByLabelText(/imagem/i), file);
    await user.click(screen.getByRole("button", { name: /enviar/i }));

    const img = await screen.findByAltText(/resultado/i);
    expect(img).toHaveAttribute("src", expect.stringContaining("/jobs/job-x/result"));
    expect(screen.getByText(/baixar/i)).toBeInTheDocument();
  });
});
