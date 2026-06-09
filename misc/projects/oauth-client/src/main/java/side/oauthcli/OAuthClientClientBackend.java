package side.oauthcli;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public interface OAuthClientClientBackend {
    static OAuthClientClientBackend inMemory() {
        return new InMemoryOAuthClientClientBackend();
    }

    @SneakyThrows
    static OAuthClientClientBackend fileBacked(Path path, JsonMapper jsonMapper) {
        var _ = path.getParent().toFile().mkdirs();

        try {
            Files.writeString(path, "{}", StandardOpenOption.CREATE_NEW);
        } catch (FileAlreadyExistsException ignored) {
        }
        return new FileOAuthClientClientBackend(path, jsonMapper);
    }

    static OAuthClientClientBackend keyChainBacked(String namespace, JsonMapper jsonMapper) {
        return new KeyChainClientClientBackend(KeyChain.forCurrentOs().setNamespace(namespace), jsonMapper);
    }

    OAuthClient.OAuthClientClient create(String name, OAuthClient.OAuthClientClient client);

    OAuthClient.OAuthClientClient get(String name);

    List<String> list();

    OAuthClient.OAuthClientClient update(String name, OAuthClient.OAuthClientClient client);

    void delete(String name);

    class InMemoryOAuthClientClientBackend implements OAuthClientClientBackend {
        private final Map<String, OAuthClient.OAuthClientClient> backend = Collections.synchronizedMap(new TreeMap<>());

        @Override
        public OAuthClient.OAuthClientClient create(String name, OAuthClient.OAuthClientClient client) {
            var previousValue = backend.putIfAbsent(name, client);
            if (previousValue != null)
                throw new IllegalArgumentException("name " + name + " already exists");
            return client;
        }

        @Override
        public OAuthClient.OAuthClientClient get(String name) {
            return backend.get(name);
        }

        @Override
        public List<String> list() {
            return new ArrayList<>(backend.keySet());
        }

        @Override
        public OAuthClient.OAuthClientClient update(String name, OAuthClient.OAuthClientClient client) {
            var oldValue = backend.computeIfPresent(name, (_, _) -> client);
            Objects.requireNonNull(oldValue, () -> "name " + name + " does not exist");
            return client;
        }

        @Override
        public void delete(String name) {
            var oldValue = backend.remove(name);
            Objects.requireNonNull(oldValue, () -> "name " + name + " does not exist");
        }
    }

    @RequiredArgsConstructor
    class FileOAuthClientClientBackend implements OAuthClientClientBackend {
        static final TypeReference<Map<String, OAuthClient.OAuthClientClient>> VALUE_TYPE_REF = new TypeReference<>() {
        };
        final Path file;
        final JsonMapper jsonMapper;
        final ReadWriteLock lock = new ReentrantReadWriteLock();

        @Override
        public OAuthClient.OAuthClientClient create(String name, OAuthClient.OAuthClientClient client) {
            try (var _ = new FileOAuthClientClientBackend.LockHolder(lock.writeLock())) {
                var value = jsonMapper.readValue(file, VALUE_TYPE_REF);
                var previous = value.putIfAbsent(name, client);
                if (previous != null)
                    throw new IllegalStateException("already exists: " + name);
                jsonMapper.writeValue(file, value);
                return client;
            }
        }

        @Override
        public OAuthClient.OAuthClientClient get(String name) {
            try (var _ = new FileOAuthClientClientBackend.LockHolder(lock.readLock())) {
                return jsonMapper.readValue(file, VALUE_TYPE_REF).get(name);
            }
        }

        @Override
        public List<String> list() {
            try (var _ = new FileOAuthClientClientBackend.LockHolder(lock.readLock())) {
                return new ArrayList<>(jsonMapper.readValue(file, VALUE_TYPE_REF).keySet());
            }
        }

        @Override
        public OAuthClient.OAuthClientClient update(String name, OAuthClient.OAuthClientClient client) {
            try (var _ = new FileOAuthClientClientBackend.LockHolder(lock.writeLock())) {
                var value = jsonMapper.readValue(file, VALUE_TYPE_REF);
                var oldValue = value.computeIfPresent(name, (_, _) -> client);
                Objects.requireNonNull(oldValue, () -> "name " + name + " does not exist");
                jsonMapper.writeValue(file, value);
                return client;
            }
        }

        @Override
        public void delete(String name) {
            try (var _ = new FileOAuthClientClientBackend.LockHolder(lock.writeLock())) {
                var value = jsonMapper.readValue(file, VALUE_TYPE_REF);
                var oldValue = value.remove(name);
                Objects.requireNonNull(oldValue, () -> "name " + name + " does not exist");
                jsonMapper.writeValue(file, value);
            }
        }

        record LockHolder(Lock lock) implements AutoCloseable {
            @SneakyThrows
            LockHolder {
                if (!lock.tryLock(10, TimeUnit.SECONDS))
                    throw new IllegalStateException();
            }

            @Override
            public void close() {
                lock.unlock();
            }
        }
    }

    @Data
    @Accessors(chain = true)
    class KeyChainClientClientBackend implements OAuthClientClientBackend {
        private final KeyChain keyChain;
        private final JsonMapper jsonMapper;

        @Override
        public OAuthClient.OAuthClientClient create(String name, OAuthClient.OAuthClientClient client) {
            keyChain.set(name, jsonMapper.writeValueAsString(client));
            return client;
        }

        @Override
        public OAuthClient.OAuthClientClient get(String name) {
            return jsonMapper.readValue(keyChain.get(name), OAuthClient.OAuthClientClient.class);
        }

        @Override
        public List<String> list() {
            return keyChain.list();
        }

        @Override
        public OAuthClient.OAuthClientClient update(String name, OAuthClient.OAuthClientClient client) {
            keyChain.update(name, jsonMapper.writeValueAsString(client));
            return client;
        }

        @Override
        public void delete(String name) {
            keyChain.delete(name);
        }
    }
}
