# 👥 Oh My Marx counter API · stdlib only ➡ nothing 2 pip-install, nothing 2 patch but th base image
FROM python:3.12-alpine
# APT_BUST = $(date +%y%m%d) fr evy build (am0/ci · deploy · patch run) ➡ layer rebuilds daily ➡ apk upgrade
#   picks up fixes th base tag hasn't shipped yet (CI run 909 @1112: krb5-libs 16× HIGH, fixed upstream)
ARG APT_BUST
RUN adduser -D -u 1000 omm && apk upgrade --no-cache && apk add --no-cache tzdata
COPY omm_api.py /app/omm_api.py
USER omm
EXPOSE 8000
CMD ["python3", "/app/omm_api.py", "--db", "/data/omm.db", "--port", "8000"]
