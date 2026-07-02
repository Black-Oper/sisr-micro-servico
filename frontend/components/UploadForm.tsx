"use client";

import { useState } from "react";
import { createJob } from "@/lib/api";

/**
 * Escolhe uma imagem e envia (POST /jobs), avisando a página o jobId criado e
 * uma URL de pré-visualização da imagem enviada (para mostrar o "antes").
 */
export function UploadForm({
  onCreated,
}: {
  onCreated: (jobId: string, previewUrl: string) => void;
}) {
  const [file, setFile] = useState<File | null>(null);
  const [enviando, setEnviando] = useState(false);
  const [erro, setErro] = useState<string | null>(null);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!file) return;
    setEnviando(true);
    setErro(null);
    try {
      const res = await createJob(file);
      onCreated(res.jobId, URL.createObjectURL(file));
    } catch (err) {
      setErro(err instanceof Error ? err.message : "falha ao enviar");
    } finally {
      setEnviando(false);
    }
  }

  return (
    <form onSubmit={handleSubmit} className="upload">
      <input
        type="file"
        accept="image/png,image/jpeg"
        aria-label="imagem"
        onChange={(e) => setFile(e.target.files?.[0] ?? null)}
      />
      <button type="submit" className="btn" disabled={!file || enviando}>
        {enviando ? "Enviando…" : "Enviar"}
      </button>
      {erro && (
        <p className="error" role="alert">
          {erro}
        </p>
      )}
    </form>
  );
}
