package side.cloud.util.acme.lib.model.challenge.presentation;

import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import side.cloud.util.acme.lib.model.challenge.ChallengeSolution;
import side.cloud.util.acme.lib.model.challenge.SupportedChallengeType;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public interface ChallengePresenter {
    static ChallengePresenter composite(List<ChallengePresenter> presenters) {
        return new CompositeChallengePresenter(presenters);
    }

    boolean supports(ChallengeSolution solution);

    void present(ChallengeSolution solution);

    void remove(ChallengeSolution solution);

    boolean verify(ChallengeSolution solution);

    @Slf4j
    @Data
    class CompositeChallengePresenter implements ChallengePresenter {
        private final List<ChallengePresenter> presenters;

        @Override
        public boolean supports(ChallengeSolution solution) {
            return presenterFor(solution) != null;
        }

        private ChallengePresenter presenterFor(ChallengeSolution solution) {
            var presenterList = presenters.stream().filter(presenters -> presenters.supports(solution)).toList();
            var count = presenterList.size();
            log.debug("challenge solution {} supported by {} presenters", solution, count);
            return count == 1 ? presenterList.getFirst() : null;
        }

        private ChallengePresenter mandatoryPresenterFor(ChallengeSolution solution) {
            var presenter = presenterFor(solution);
            if (presenter == null) {
                throw new IllegalArgumentException("no presenter found");
            }
            return presenter;
        }

        @Override
        public void present(ChallengeSolution solution) {
            mandatoryPresenterFor(solution).present(solution);
        }

        @Override
        public void remove(ChallengeSolution solution) {
            mandatoryPresenterFor(solution).remove(solution);
        }

        @Override
        public boolean verify(ChallengeSolution solution) {
            return mandatoryPresenterFor(solution).verify(solution);
        }
    }

    @Data
    class SingleHostCrudPresenter implements ChallengePresenter {
        final String host;
        final Crud crud;
        final Type type;

        @Override
        public boolean supports(ChallengeSolution solution) {
            var supportedType = switch (type) {
                case http -> SupportedChallengeType.ChallengeHTTP01 == solution.getType();
                case dns -> SupportedChallengeType.DNS_TYPES.contains(solution.getType());
            };
            if (!supportedType)
                return false;
            var idValue = solution.getIdentifier().getValue();
            var hostPart = idValue.startsWith("*.") ? idValue.substring(2) : idValue;
            return host.equals(hostPart);
        }

        @Override
        public void present(ChallengeSolution solution) {
            crud.create(solution.getKey(), solution.getValue());
        }

        @Override
        public void remove(ChallengeSolution solution) {
            crud.delete(solution.getKey());
        }

        @Override
        public boolean verify(ChallengeSolution solution) {
            try {
                return Objects.equals(crud.read(solution.getKey()), solution.getValue());
            } catch (UnsupportedOperationException e) {
                return true;
            }
        }

        public enum Type { http, dns }

        public interface Crud {
            static Crud of(Path folder) {
                return new LocalFileSystemCrud(folder);
            }

            void create(String key, String value);

            default String read(String key) {
                throw new UnsupportedOperationException();
            }

            void delete(String key);

            @Data
            class LocalFileSystemCrud implements Crud {
                final Path folder;

                @SneakyThrows
                @Override
                public void create(String key, String value) {
                    Files.writeString(folder.resolve(key), value);
                }

                @SneakyThrows
                @Override
                public String read(String key) {
                    return Files.readString(folder.resolve(key));
                }

                @SneakyThrows
                @Override
                public void delete(String key) {
                    Files.deleteIfExists(folder.resolve(key));
                }
            }
        }
    }

    class LocalStaticFileServerChallengePresenter extends SingleHostCrudPresenter {
        public LocalStaticFileServerChallengePresenter(String host, Path rootFolder) {
            super(host, Crud.of(rootFolder.resolve(Path.of(".well-known", "acme-challenge"))), Type.http);
        }
    }
}
