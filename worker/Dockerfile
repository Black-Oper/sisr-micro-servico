# =========================================================
#  worker/Dockerfile — servico Python (consome a fila, roda o modelo RTDVSR)
#
#  PyTorch e instalado do indice CPU dedicado (sem libs CUDA). Os pesos do
#  modelo NAO vao na imagem: sao montados via volume (./worker/models) em runtime.
# =========================================================
FROM python:3.9-slim

WORKDIR /app

# pip atualizado: o pip da imagem slim tem um bug de normalizacao de nome que
# rejeita wheels do indice do PyTorch (typing-extensions vs typing_extensions).
RUN pip install --no-cache-dir --upgrade pip

# PyTorch (CPU) — indice dedicado para evitar o download das libs CUDA
RUN pip install --no-cache-dir \
        --index-url https://download.pytorch.org/whl/cpu \
        torch==2.8.0 torchvision==0.23.0

# Demais dependencias (PyPI)
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY src ./src
ENV PYTHONPATH=/app/src

CMD ["python", "-m", "worker"]
