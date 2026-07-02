import { describe, it, expect, vi, afterEach } from "vitest";
import { render, screen, waitFor, cleanup } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { UploadForm } from "./UploadForm";
import * as api from "@/lib/api";

afterEach(() => {
  cleanup();
  vi.restoreAllMocks();
});

describe("UploadForm", () => {
  it("envia o arquivo e emite o jobId", async () => {
    vi.spyOn(api, "createJob").mockResolvedValue({
      jobId: "job-9",
      status: "PENDING",
      createdAt: "2026-07-01T00:00:00Z",
    });
    const onCreated = vi.fn();
    const user = userEvent.setup();

    render(<UploadForm onCreated={onCreated} />);

    const file = new File(["x"], "in.png", { type: "image/png" });
    await user.upload(screen.getByLabelText(/imagem/i), file);
    await user.click(screen.getByRole("button", { name: /enviar/i }));

    await waitFor(() =>
      expect(onCreated).toHaveBeenCalledWith("job-9", expect.any(String)),
    );
    expect(api.createJob).toHaveBeenCalledTimes(1);
  });
});
