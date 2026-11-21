this is a big monorepo do not assume the projects are connected. the conventions and learnings/findings are but projects typically not.

prioritize writing the code in a simple to understand and maintain way.

use lombok not records
also use Slf4j, Data, SneakyThrows
use jackson not gson
use jacksons @JsonSubType as a tagged enum for data/settings representation of immutable states
use jackson 3
prefer awspring and other spring managed versions to aws's own bom
use bean validation not if (!..) throw new ...
use restclient from spring boot not jdk httpclient
use spring managed versions over custom versions of spring managed software
use yaml not yml
use liquibase to manage db versions, include ./changes as subpath not just dump into main file. use sql format.
avoid annotations on the main spring boot class e.g.:
@Component @ConfigurationProperties on same class
@Import on @Configuration classes over the main class.
use testcontainers instead of mocking for all integration testing. do not use h2.
use wiremock testcontainer for mocking all third party apis over mockito
put mockito tests in "test" and integration tests in "itest"
format java stuff with the intellij mcp, js with prettier, python with ruff, terraform with tofu fmt, go with go fmt
