@echo off
setlocal
cd /d "%~dp0"
if exist "C:\Program Files\NVIDIA GPU Computing Toolkit\CUDA\v13.3\include\cuda.h" (
  set "CUDA_PATH=C:\Program Files\NVIDIA GPU Computing Toolkit\CUDA\v13.3"
) else if exist "C:\Program Files\NVIDIA GPU Computing Toolkit\CUDA\v13.2\include\cuda.h" (
  set "CUDA_PATH=C:\Program Files\NVIDIA GPU Computing Toolkit\CUDA\v13.2"
)
if defined CUDA_PATH (
  set "CUDA_HOME=%CUDA_PATH%"
  set "PATH=%CUDA_PATH%\bin;%CUDA_PATH%\bin\x64;%PATH%"
)
python quantum_service.py
