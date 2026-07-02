import { describe, it, expect, afterEach } from "vitest";
import { render, screen, cleanup } from "@testing-library/react";
import { JobView } from "./JobView";
import type { JobDetails, JobStatus } from "@/lib/api";

afterEach(() => cleanup());

function job(status: JobStatus, error: string | null = null): JobDetails {
  return {
    jobId: "job-1",
    status,
    scale: 2,
    createdAt: "2026-07-01T00:00:00Z",
    startedAt: null,
    finishedAt: null,
    error,
  };
}

describe("JobView", () => {
  it("mostra as imagens (original + resultado) e o download quando DONE", () => {
    render(
      <JobView jobId="job-1" job={job("DONE")} error={null} inputUrl="blob:in" />,
    );

    expect(screen.getByAltText(/resultado/i)).toHaveAttribute(
      "src",
      expect.stringContaining("/jobs/job-1/result"),
    );
    expect(screen.getByAltText(/original/i)).toHaveAttribute("src", "blob:in");
    expect(screen.getByText(/baixar/i)).toBeInTheDocument();
  });

  it("mostra o status enquanto processa", () => {
    render(<JobView jobId="job-1" job={job("PROCESSING")} error={null} />);
    expect(screen.getByText(/PROCESSING/)).toBeInTheDocument();
  });

  it("mostra o erro quando FAILED", () => {
    render(<JobView jobId="job-1" job={job("FAILED", "modelo explodiu")} error={null} />);
    expect(screen.getByText(/modelo explodiu/)).toBeInTheDocument();
  });
});
