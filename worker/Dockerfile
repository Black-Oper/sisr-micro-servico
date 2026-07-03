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

# PyTorch: em x86_64 usamos o indice CPU dedicado (evita baixar as libs CUDA);
# em aarch64 (ARM, ex.: Oracle Ampere) usamos o PyPI, onde as wheels ARM ja
# sao CPU-only. Assim a mesma imagem builda na sua maquina/CI e na VM.
RUN if [ "$(uname -m)" = "x86_64" ]; then \
        pip install --no-cache-dir --index-url https://download.pytorch.org/whl/cpu \
            torch==2.8.0 torchvision==0.23.0 ; \
    else \
        pip install --no-cache-dir torch==2.8.0 torchvision==0.23.0 ; \
    fi

# Demais dependencias (PyPI)
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY src ./src
ENV PYTHONPATH=/app/src

CMD ["python", "-m", "worker"]
