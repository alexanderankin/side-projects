# Overloads and how to simplify them

This inventories application-owned overloads in `src/main/java`, with callers in main, test, and itest sources.
Signatures below omit return types and checked exceptions where they do not distinguish overloads. Source paths are
relative to this project. This is a refactoring guide, not a record of an implemented refactor.

Different implementations of the same interface method are **overrides**, not additional overloads. Calls to overloaded
JDK, Swing, Jackson, AWS, and JSch APIs are outside this inventory. Lombok-generated interface methods are covered
separately at the end.

## 1. BackupService.restore — four signatures

Source: [BackupService.java](src/main/java/info/ankin/simplebackups/backup/BackupService.java).

All signatures start with `(BackupConfiguration backup, AppSettings settings, Snapshot snapshot)`:

| Remaining parameters                                              | Behavior                                                                                                                     | Direct callers                                                                                                                                                                                                                                                   |
|-------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `Path destination`                                                | Converts a non-null path to a URI, preserves null, supplies silent progress; indirectly defaults deletion to false.          | `BackupServiceTest.tarRoundTripAndContentDiff`, `refusesToOverwriteExistingRestoreFiles`, and `resticRestoreAndDiffReportUnsupportedWithoutAccessingSourceOrTarget`; `BackupFrame.restore` uses null to surface unsupported restic restore.                      |
| `URI destination, Consumer<String> progress`                      | Defaults `allowDelete` to false.                                                                                             | The Path-only overload; `BackupServiceITest.remoteRestoreRefusesToOverwriteExistingFiles`, `remoteRestoreRejectsSymlinksInDestinationAndItsParents`, and `invalidArchiveDoesNotCreateRemoteDestination`.                                                         |
| `Path destination, boolean allowDelete`                           | Converts the path to a URI, preserves null, supplies silent progress.                                                        | `BackupServiceITest.multipartS3BackupRestoresEveryByte`.                                                                                                                                                                                                         |
| `URI destination, boolean allowDelete, Consumer<String> progress` | Implements one-shot restore: validates ownership, handles restic's unsupported operation, prepares a plan, then executes it. | The two convenience overloads above that delegate directly; `BackupServiceTest.localRestoreHonorsDeletionFlagOnSuccessAndFailure`; integration tests for the lifecycle matrix, new remote folders/literal filenames, interrupted uploads, and deletion previews. |

**Recommendation:** retain only the complete URI signature. At local callers use `destination.toUri()`; pass `false`
explicitly where deletion was omitted, and `ignored -> {}` where progress was omitted. The UI's restic error path
becomes `restore(backup, settings, snapshot, null, false, ignored -> {})`. Preserve the unsupported-restic branch before
destination validation so null still reaches the intended exception.

The normal tar UI deliberately uses `prepareRestore` plus `executeRestore`, rather than this one-shot method: it must
prompt between those operations. Keep that distinction.

## 2. BackupService.prepareRestore — two signatures

Source: [BackupService.java](src/main/java/info/ankin/simplebackups/backup/BackupService.java).

| Signature suffix after backup/settings/snapshot                   | Behavior                                                                                                                                                                             | Direct callers                                                                                                         |
|-------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------|
| `URI destination, Consumer<String> progress`                      | Delegates with deletion permission false.                                                                                                                                            | `BackupServiceITest.restorePreviewsDeletionsAndRequiresPermission` and `restoreRejectsDestinationChangesAfterPreview`. |
| `URI destination, boolean allowDelete, Consumer<String> progress` | Extracts the snapshot and inventories the destination without modifying destination files. The flag controls cleanup if preparation fails; it does not perform destination deletion. | The shorter overload, the complete `BackupService.restore`, and `BackupFrame.restore`'s comparison worker.             |

**Recommendation:** keep the complete signature and add explicit `false` in the two integration-test callers. Do not
replace the UI's initial checkbox value with unconditional true: preparation failure cleanup also depends on that
permission.

## 3. BackupEngine.restore — two signatures

Source: [BackupEngine.java](src/main/java/info/ankin/simplebackups/backup/engine/BackupEngine.java).

| Signature                                                           | Behavior                                                                                                                                     | Direct callers                                                                  |
|---------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------|
| `restore(SnapshotStore, Snapshot, Path)`                            | Interface default delegates with `false` and silent progress.                                                                                | `BackupServiceTest.rejectsArchiveTraversalBeforeWritingIntoRestoreDestination`. |
| `restore(SnapshotStore, Snapshot, Path, boolean, Consumer<String>)` | Implemented by `TarBackupEngine` and `ResticBackupEngine`. Tar validates/extracts and restores locally; restic throws unsupported-operation. | The default overload and `BackupService.restore`'s unsupported-restic branch.   |

**Recommendation:** remove the three-argument default and adapt the traversal test to pass `false, ignored -> {}`.
Retain the complete signature and both implementation overrides. Normal service tar restores now use `extractRestore`
and the prepared-plan execution path; do not accidentally route the UI around its preview checks.

## 4. RestoreComparison.restoreLocal — two signatures

Source: [RestoreComparison.java](src/main/java/info/ankin/simplebackups/backup/RestoreComparison.java).

| Signature                                                                                       | Behavior                                                                                                          | Direct callers                                                                                |
|-------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------|
| `restoreLocal(Path contents, Path destination, boolean allowDelete, Consumer<String> progress)` | Delegates with a null approved inventory, omitting the comparison with a previously approved destination state.   | `TarBackupEngine.restore`.                                                                    |
| Same parameters plus `Map<String, String> approvedInventory`                                    | Rechecks a supplied inventory, computes deletions/replacements, requires permission, and copies missing contents. | The shorter overload and `BackupService.executeRestore`, which supplies the plan's inventory. |

**Recommendation:** keep the complete signature. Adapt the tar engine caller to pass
`RestoreComparison.inventory(destination)` explicitly. The service must continue passing `plan.destinationInventory()`;
passing null there would remove the check that prevents deleting changes made after preview. Once all callers supply an
inventory, make it required rather than retaining an undocumented null mode.

## 5. BackupFrame.restore — two signatures

Source: [BackupFrame.java](src/main/java/info/ankin/simplebackups/BackupFrame.java).

| Signature                                                                    | Behavior                                                                                    | Direct callers                                                                                                         |
|------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------|
| `restore(Snapshot)`                                                          | Seeds the destination from the selected backup's source and defaults the checkbox to false. | The snapshot table's Restore action.                                                                                   |
| `restore(Snapshot, String initialDestination, boolean initiallyAllowDelete)` | Displays the form and performs the prepare/prompt/execute flow.                             | The short overload; its own cancellation callback reopens the form with the user's destination and deletion unchecked. |

**Recommendation:** rename the longer method `showRestoreDialog`. Keep the short entrypoint named `restore` if useful.
Update its delegation and the cancellation callback to the new name. Alternatively remove the short wrapper and put its
three arguments directly in the table action. Preserve the destination entered by the user when reopening after
cancellation.

## 6. BackupFrame.run — two signatures

Source: [BackupFrame.java](src/main/java/info/ankin/simplebackups/BackupFrame.java).

| Signature                                   | Behavior                                                                                       | Direct callers                                                                                                                                                                                                                                                                                                                             |
|---------------------------------------------|------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `<T> run(String, Callable<T>, Consumer<T>)` | Supplies a no-op failure callback.                                                             | Ordinary workers: backup execution, configuration listing/save/import/export, snapshot listing, diff, restic restore, restore preparation/execution, and settings save. These occur in `backupsPanel`, `reloadBackups`, `showSnapshots`, `snapshotsPanel`, `editBackup`, `importBackup`, `exportBackup`, `restore`, and `rebuildSettings`. |
| Same parameters plus `Runnable failure`     | Owns SwingWorker execution, busy-state handling, error display, and optional failure recovery. | The short wrapper and constructor startup settings loading. Startup passes `this::reloadBackups` so configurations still load if settings loading fails.                                                                                                                                                                                   |

**Recommendation:** rename the full helper `runWithFailureHandler`, retaining `run` as the ordinary convenience name.
Update the wrapper delegation and constructor startup call. This removes the overload without adding no-op callbacks at
every UI worker. To eliminate the wrapper entirely instead, add an explicit no-op fourth argument to every ordinary
worker; that is more repetitive and is not the preferred option.

## 7. ConfigurationStore.read — two signatures

Source: [ConfigurationStore.java](src/main/java/info/ankin/simplebackups/config/ConfigurationStore.java).

| Signature                    | Behavior                                                                      | Direct callers                                                                                                                                                            |
|------------------------------|-------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `<T> read(Path, Class<T>)`   | Opens/closes a buffered reader and delegates JSON decoding.                   | `ConfigurationStore.list` and `settings`; `BackupFrame.importBackup`; `ConfigurationStoreTest`'s round-trip, invalid-document, compatibility, and invalid-metadata tests. |
| `<T> read(Reader, Class<T>)` | Decodes JSON through validating constructors; accepts an already-open reader. | The Path overload and `BackupService.snapshots`, which reads snapshot metadata from either local storage or S3.                                                           |

**Recommendation:** rename these `readFile` and `readJson`, respectively. Update the Path method's delegation and every
caller by argument type. Do not force the snapshot-stream caller to create a temporary file. Preserve existing
stream/reader closure behavior: file reads have a try-with-resources wrapper, and snapshot listing also owns an
enclosing reader scope.

## 8. RemoteRestore.inventory — two signatures

Source: [RemoteRestore.java](src/main/java/info/ankin/simplebackups/backup/RemoteRestore.java).

| Signature                                                                         | Behavior                                                                        | Direct callers                                                       |
|-----------------------------------------------------------------------------------|---------------------------------------------------------------------------------|----------------------------------------------------------------------|
| `inventory(URI, AppSettings, Consumer<String>)`                                   | Opens/closes a connection, inspects the destination, creates and returns a map. | `BackupService.prepareRestore` and `executeRestore`.                 |
| `inventory(ChannelSftp, String root, String relative, Map<String,String> result)` | Recursively fills an existing map using an existing connection.                 | The URI entrypoint, `RemoteRestore.restore`, and itself recursively. |

**Recommendation:** rename the recursive method `collectInventory`. Update those three call sites. Keep connection
ownership at the entrypoints; do not replace recursion with calls to the URI entrypoint, which would repeatedly open
connections.

## 9. SnapshotStore.open — two signatures

Source: [SnapshotStore.java](src/main/java/info/ankin/simplebackups/backup/snapshot/SnapshotStore.java).

| Signature                       | Behavior                                                                                           | Direct callers                                                                                                            |
|---------------------------------|----------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------|
| Static `open(URI, AppSettings)` | Factory constructing a local or S3 store.                                                          | `BackupService.backup`, `snapshots`, `prepareRestore`, and `diff`; the integration lifecycle test's artifact checks.      |
| Instance `open(String key)`     | Opens an artifact stream relative to the store root. Implemented by `Local` and `S3SnapshotStore`. | `BackupService.snapshots` for metadata, and `TarBackupEngine.extract` for the archive (including restore and diff paths). |

**Recommendation:** rename only the factory to `openStore`. Update class-qualified factory calls; retain instance
`store.open(key)` and its implementation overrides. The factory's result is a store to close; the reader's result is a
stream to close. They are different operations, not interchangeable convenience forms.

## 10. S3SnapshotStore constructors — two signatures

Source: [S3SnapshotStore.java](src/main/java/info/ankin/simplebackups/backup/snapshot/S3SnapshotStore.java).

| Constructor                                             | Behavior                                                                            | Direct callers                                                                                                 |
|---------------------------------------------------------|-------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------|
| `S3SnapshotStore(URI, AppSettings)`                     | Derives bucket/prefix, creates the configured credentials provider and SDK client.  | `SnapshotStore.open`'s S3 factory branch.                                                                      |
| `S3SnapshotStore(S3Client, String bucket, String base)` | Accepts a prebuilt client; sets owned credentials to null. Intended as a test seam. | No current callers in main, test, or itest sources. Integration tests exercise the production factory instead. |

**Recommendation:** remove the unused injected-client constructor. No caller adaptation is needed. If a future
SDK-boundary test needs injection, deliberately reintroduce a named factory or dependency seam then. Note that `close()`
closes the client in either construction mode; injection currently does not mean borrowed ownership.

## 11. Lombok-generated JSch interface overloads

Source: [AcceptNewHostKeys.java](src/main/java/info/ankin/simplebackups/backup/AcceptNewHostKeys.java), via `@Delegate`
on `HostKeyRepository`.

| Generated family                                                                      | Callers and meaning                                                                                                                                                                                                                                          | Recommendation                                                                                                                                          |
|---------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------|
| `getHostKey()` and `getHostKey(String host, String type)`                             | `AcceptNewHostKeysTest.persistenceFailureDoesNotAcceptTheKey` uses the unfiltered form; `existingHashedAndRevokedEntriesRemainEffective` uses the filtered form. JSch also consumes the repository interface, including filtered lookup for host-key checks. | Keep both to satisfy the external interface. Explicit forwarding methods could replace Lombok generation, but would not remove the interface overloads. |
| `remove(String host, String type)` and `remove(String host, String type, byte[] key)` | No direct application callers. Generated to satisfy JSch's repository contract; usage from JSch depends on its host-key policy.                                                                                                                              | Keep both. Removing them requires changing the interface integration, not just adapting application call sites.                                         |

`check` and `getKnownHostsRepositoryID` are explicitly implemented overrides and are not overload families here.
Repeated `visitFile`/`preVisitDirectory` declarations in separate anonymous visitors are also overrides, not overloads
on one class.

## Suggested refactoring order

1. Remove the unused S3 constructor.
2. Rename private UI and recursive helpers, preserving their defaults and connection ownership.
3. Remove service/engine/comparison convenience overloads, making URI conversion, deletion permission, progress
   handling, and approved inventories explicit at their callers.
4. Rename JSON entrypoints and the snapshot-store factory. Compile main, test, and itest sources to catch all call-site
   adaptations.
5. Run `./gradlew -p misc/projects/apps/simple-backups test integrationTest`.

The important regression cases are deletion disabled/enabled, changed destinations after preview, cancellation returning
to the same form, retained staging on failures, literal SSH paths, archive validation before destination writes,
local/S3 snapshot access, and unsupported restic operations. Preserve these behaviors while reducing overloads. An
overload removal should not silently enable deletion or replace a preview-approved inventory with a newly captured one.
