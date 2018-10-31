FROM hmcts/cnp-java-base:openjdk-jre-8-alpine-1.4

ENV APP finrem-case-progression.jar
ENV APPLICATION_TOTAL_MEMORY 1024M
ENV APPLICATION_SIZE_ON_DISK_IN_MB 59

COPY build/libs/$APP /opt/app/

WORKDIR /opt/app

HEALTHCHECK --interval=100s --timeout=100s --retries=10 CMD http_proxy="" wget -q http://localhost:9000/health || exit 1

EXPOSE 9000