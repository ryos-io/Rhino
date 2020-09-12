FROM bagdemir/rhino-grafana:1.0.0
USER root
COPY init.sh /
RUN chmod u+x /init.sh

ENTRYPOINT [ "sh", "-c", "/init.sh"]