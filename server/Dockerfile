# 👥 Oh My Marx counter API · stdlib only ➡ nothing 2 pip-install, nothing 2 patch but th base image
FROM python:3.12-alpine
RUN adduser -D -u 1000 omm && apk add --no-cache tzdata
COPY omm_api.py /app/omm_api.py
USER omm
EXPOSE 8000
CMD ["python3", "/app/omm_api.py", "--db", "/data/omm.db", "--port", "8000"]
