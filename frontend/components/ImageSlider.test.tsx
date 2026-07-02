import { describe, it, expect, afterEach } from "vitest";
import { render, screen, cleanup, fireEvent } from "@testing-library/react";
import { ImageSlider } from "./ImageSlider";

afterEach(() => cleanup());

describe("ImageSlider (antes/depois)", () => {
  it("mostra as duas imagens e o controle de comparação", () => {
    render(<ImageSlider beforeUrl="blob:antes" afterUrl="http://x/depois" />);

    expect(screen.getByAltText(/original/i)).toHaveAttribute("src", "blob:antes");
    expect(screen.getByAltText(/resultado/i)).toHaveAttribute("src", "http://x/depois");
    expect(screen.getByLabelText(/comparar/i)).toBeInTheDocument();
  });

  it("revela mais do resultado ao mover o controle", () => {
    render(<ImageSlider beforeUrl="blob:antes" afterUrl="http://x/depois" />);

    const range = screen.getByLabelText(/comparar/i);
    fireEvent.change(range, { target: { value: "20" } });

    // 20% revelado => o recorte esconde 80% da direita da imagem de cima
    expect(screen.getByAltText(/resultado/i).style.clipPath).toContain("80%");
  });
});
