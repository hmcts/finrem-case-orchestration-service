ARG APP_INSIGHTS_AGENT_VERSION=3.2.4
ARG PLATFORM=""
FROM hmctspublic.azurecr.io/base/java${PLATFORM}:17-distroless

ENV APP finrem-case-orchestration.jar

COPY build/libs/$APP /opt/app/
COPY lib/AI-Agent.xml /opt/app/

EXPOSE 9000

CMD ["finrem-case-orchestration.jar"]
