this is a big monorepo do not assume the projects are connected. the conventions and learnings/findings are but projects typically not.

prioritize writing the code in a simple to understand and maintain way.

use lombok not records
also use Slf4j, Data, SneakyThrows
use jackson not gson
use jacksons @JsonSubType as a tagged enum for data/settings representation of immutable states
prefer awspring and other spring managed versions to aws's own bom
