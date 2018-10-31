FROM hmcts/cnp-java-base:openjdk-jre-8-alpine-1.4

COPY build/libs/finrem-case-progression.jar /opt/app/

WORKDIR /opt/app

HEALTHCHECK --interval=10s --timeout=10s --retries=10 CMD http_proxy="" curl --silent --fail http://localhost:9000/health || exit 1

EXPOSE 9000

ENTRYPOINT ["/opt/app/bin/finrem-case-progression"]