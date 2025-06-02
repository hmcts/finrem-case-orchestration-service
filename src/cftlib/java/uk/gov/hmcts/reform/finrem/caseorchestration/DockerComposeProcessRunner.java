package uk.gov.hmcts.reform.finrem.caseorchestration;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.util.Objects;

import static java.io.File.separator;

/**
 * Utility class to start Docker Compose processes in a local development environment.
 * This class is not intended to be instantiated.
 * The Docker Compose files are expected to be specified in the CFTLIB_EXTRA_COMPOSE_FILES environment variable.
 * The files should be located in the "compose" directory in the classpath.
 * The files should be separated by commas.
 */
@Slf4j
final class DockerComposeProcessRunner {
    private DockerComposeProcessRunner() {
        // Utility class
    }

    @SneakyThrows
    static void start()  {
        String cftlibExtraComposeFiles = System.getenv("CFTLIB_EXTRA_COMPOSE_FILES");
        if (cftlibExtraComposeFiles == null) {
            log.info("CFTLIB_EXTRA_COMPOSE_FILES is not set");
            return;
        }

        String[] composeFiles = cftlibExtraComposeFiles.split(",");
        URL compose = Thread.currentThread().getContextClassLoader().getResource("compose");
        String basePath = Objects.requireNonNull(compose).getPath();

        for (String file : composeFiles) {
            String path = String.join(separator, basePath, file);

            startDockerCompose(path, file);
        }
    }

    @SneakyThrows
    private static void startDockerCompose(String path, String file) {
        Process process = new ProcessBuilder("docker", "compose", "-f", path, "up", "-d").inheritIO().start();

        int code = process.waitFor();

        if (code != 0) {
            log.error("****** Failed to start services in {} ******", file);
            log.info("Exit value: {}", code);
            return;
        }

        log.info("Successfully started services in {}", file);
    }
}
