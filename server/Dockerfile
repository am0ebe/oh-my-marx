# 👥 Oh My Marx counter API · stdlib only ➡ nothing 2 pip-install, nothing 2 patch but th base image
FROM python:3.12-alpine
# no pip i th image (D-037): th app nvr installs anything ➡ th base's bundled pip = dead weight th only collects
#   advisories (nightly 935 @1114: pip 25.0.1 = 6 PYSEC, fix 26.2) · ensurepip = th same pip as a wheel ➡ out too
#   last line = guard: pip back i th image ➡ th build fails here, not th 03:15 sweep
RUN python3 -m pip uninstall -y -q --root-user-action=ignore pip \
 && rm -rf /usr/local/lib/python3*/ensurepip /usr/local/bin/pip* \
 && ! python3 -c 'import pip' 2>/dev/null
# APT_BUST = $(date +%y%m%d) fr evy build (am0/ci · deploy · patch run) ➡ layer rebuilds daily ➡ apk upgrade
#   picks up fixes th base tag hasn't shipped yet (CI run 909 @1112: krb5-libs 16× HIGH, fixed upstream)
ARG APT_BUST
RUN adduser -D -u 1000 omm && apk upgrade --no-cache && apk add --no-cache tzdata
COPY omm_api.py /app/omm_api.py
USER omm
EXPOSE 8000
CMD ["python3", "/app/omm_api.py", "--db", "/data/omm.db", "--port", "8000"]
