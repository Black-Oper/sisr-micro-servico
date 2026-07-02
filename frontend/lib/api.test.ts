import { describe, it, expect, vi, afterEach } from "vitest";
import { createJob, getJob, resultUrl } from "./api";

afterEach(() => {
  vi.restoreAllMocks();
});

describe("cliente da API", () => {
  it("createJob faz POST multipart e retorna o job", async () => {
    const fetchMock = vi.fn().mockResolvedValue({
      ok: true,
      json: async () => ({
        jobId: "job-1",
        status: "PENDING",
        createdAt: "2026-07-01T00:00:00Z",
      }),
    });
    vi.stubGlobal("fetch", fetchMock);

    const file = new File(["x"], "in.png", { type: "image/png" });
    const res = await createJob(file, 2);

    expect(res.jobId).toBe("job-1");
    expect(res.status).toBe("PENDING");

    const [url, opts] = fetchMock.mock.calls[0];
    expect(String(url)).toContain("/jobs");
    expect(opts.method).toBe("POST");
    expect(opts.body).toBeInstanceOf(FormData);
    expect((opts.body as FormData).get("scale")).toBe("2");
    expect((opts.body as FormData).get("file")).toBeInstanceOf(File);
  });

  it("getJob faz GET e retorna os detalhes", async () => {
    const fetchMock = vi.fn().mockResolvedValue({
      ok: true,
      json: async () => ({ jobId: "job-1", status: "DONE", scale: 2 }),
    });
    vi.stubGlobal("fetch", fetchMock);

    const res = await getJob("job-1");

    expect(res.status).toBe("DONE");
    const [url] = fetchMock.mock.calls[0];
    expect(String(url)).toContain("/jobs/job-1");
  });

  it("resultUrl monta a URL do resultado", () => {
    expect(resultUrl("job-1")).toContain("/jobs/job-1/result");
  });

  it("createJob lança erro em resposta não-ok", async () => {
    vi.stubGlobal("fetch", vi.fn().mockResolvedValue({ ok: false, status: 500 }));
    const file = new File(["x"], "in.png", { type: "image/png" });
    await expect(createJob(file)).rejects.toThrow();
  });
});
