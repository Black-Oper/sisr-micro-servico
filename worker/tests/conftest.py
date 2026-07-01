"""Config compartilhada dos testes.

No Windows, o Docker Desktop (contexto desktop-linux) escuta num named pipe
diferente do padrao. Apontamos o testcontainers/docker-py para ele, a menos
que o DOCKER_HOST ja tenha sido definido no ambiente.
"""
import os
import sys

if sys.platform == "win32":
    os.environ.setdefault("DOCKER_HOST", "npipe:////./pipe/dockerDesktopLinuxEngine")
