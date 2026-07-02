"use client";

import { useState } from "react";

/**
 * Comparador antes/depois: as duas imagens ficam sobrepostas na mesma moldura
 * e o controle (range) revela mais ou menos da imagem de cima (o resultado).
 */
export function ImageSlider({
  beforeUrl,
  afterUrl,
}: {
  beforeUrl?: string | null;
  afterUrl: string;
}) {
  const [pct, setPct] = useState(50);

  return (
    <div className="slider">
      <div className="slider-frame">
        {/* eslint-disable-next-line @next/next/no-img-element */}
        <img
          className="slider-img"
          src={beforeUrl ?? undefined}
          alt="imagem original"
        />
        {/* eslint-disable-next-line @next/next/no-img-element */}
        <img
          className="slider-img slider-after-img"
          src={afterUrl}
          alt="resultado super-resolvido"
          style={{ clipPath: `inset(0 ${100 - pct}% 0 0)` }}
        />
        <div className="slider-divider" style={{ left: `${pct}%` }} />
      </div>
      <input
        type="range"
        min={0}
        max={100}
        value={pct}
        onChange={(e) => setPct(Number(e.target.value))}
        aria-label="comparar antes e depois"
      />
    </div>
  );
}
