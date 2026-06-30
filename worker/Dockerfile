# =========================================================
#  worker/Dockerfile — PLACEHOLDER (Etapa 1)
#
#  Serviço Python responsável por rodar o modelo do TCC.
#  Ainda NÃO há código-fonte: este Dockerfile só fixa a
#  imagem-base para isolar o ambiente. As etapas de build
#  (COPY do código, pip install -r requirements.txt,
#  download/cópia dos pesos do modelo) entram depois.
# =========================================================

# --- Imagem base (Python 3.9 slim) ---
FROM python:3.9-slim

WORKDIR /app

# Placeholder enquanto não há aplicação.
# Em etapas futuras:
#   COPY requirements.txt .
#   RUN pip install --no-cache-dir -r requirements.txt
#   COPY . .
#   CMD ["python", "worker.py"]
CMD ["bash", "-c", "echo 'worker placeholder — sem app ainda (Etapa 1)'; sleep infinity"]
