"use client";

import { useState } from "react";
import { UploadForm } from "@/components/UploadForm";
import { JobView } from "@/components/JobView";
import { useJob } from "@/lib/useJob";

export default function Home() {
  const [jobId, setJobId] = useState<string | null>(null);
  const [inputUrl, setInputUrl] = useState<string | null>(null);
  const { job, error } = useJob(jobId);

  return (
    <main className="card">
      <h1>Super-Resolução de Imagem</h1>
      <p className="subtitle">
        Envie uma imagem e receba a versão em resolução maior (2×).
      </p>

      <UploadForm
        onCreated={(id, url) => {
          setJobId(id);
          setInputUrl(url);
        }}
      />

      {jobId && (
        <section style={{ marginTop: "1.5rem" }}>
          <p className="job-id">
            <strong>Job:</strong> {jobId}
          </p>
          <JobView jobId={jobId} job={job} error={error} inputUrl={inputUrl} />
        </section>
      )}
    </main>
  );
}
