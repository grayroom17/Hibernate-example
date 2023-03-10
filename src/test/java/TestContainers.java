import com.github.dockerjava.api.model.PortBinding;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.MountableFile;

import java.time.Duration;

import static com.github.dockerjava.api.model.Ports.Binding.bindPort;
import static java.util.Objects.requireNonNull;

public class TestContainers {
    @SuppressWarnings("resource")
    public static PostgreSQLContainer<?> postgres() {
        var innerPort = 5432;
        var outerPort = 5434;

        return new PostgreSQLContainer<>("postgres:14.5")
                .withPassword("postgres")
                .withUsername("postgres")
                .withDatabaseName("postgres")
                .withExposedPorts(innerPort)
                .withNetworkAliases("postgres_it")
                .withCreateContainerCmdModifier(cmd -> {
                    requireNonNull(cmd.getHostConfig())
                            .withPortBindings(new PortBinding(bindPort(outerPort),
                                    requireNonNull(cmd.getExposedPorts())[0]));
                    cmd.withName("postgres_it");
                })
                .withCopyFileToContainer(
                        MountableFile.forClasspathResource("db-init-script.sql"),
                        "/docker-entrypoint-initdb.d/db-init-script.sql"
                )
                .waitingFor(Wait.forListeningPort().withStartupTimeout(Duration.ofMinutes(2)));
    }
}
