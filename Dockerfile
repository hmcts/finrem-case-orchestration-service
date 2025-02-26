ARG APP_INSIGHTS_AGENT_VERSION=3.7.0
FROM hmctspublic.azurecr.io/base/java:21-distroless

COPY build/libs/finrem-case-orchestration.jar /opt/app/
COPY lib/applicationinsights.json /opt/app/

EXPOSE 9000

CMD [ \
    "--add-opens", "java.base/java.lang=ALL-UNNAMED", \
    "finrem-case-orchestration.jar" \
]
