// Copyright (C) 2021 The Android Open Source Project
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.googlesource.gerrit.plugins.validation.dfsrefdb.dynamodb;

import static com.googlesource.gerrit.plugins.validation.dfsrefdb.dynamodb.ProjectVersionCacheModule.PROJECT_VERSION_CACHE;

import com.amazonaws.services.dynamodbv2.AcquireLockOptions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBLockClient;
import com.amazonaws.services.dynamodbv2.LockItem;
import com.amazonaws.services.dynamodbv2.model.LockNotGrantedException;
import com.gerritforge.gerrit.globalrefdb.ExtendedGlobalRefDatabase;
import com.gerritforge.gerrit.globalrefdb.GlobalRefDbLockException;
import com.gerritforge.gerrit.globalrefdb.GlobalRefDbSystemError;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.flogger.FluentLogger;
import com.google.gerrit.common.Nullable;
import com.google.gerrit.entities.Project;
import com.google.gerrit.entities.Project.NameKey;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import javax.inject.Singleton;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;

@Singleton
public class DynamoDBRefDatabase implements ExtendedGlobalRefDatabase {

  public static final String REF_DB_PRIMARY_KEY = "refPath";
  public static final String REF_DB_VALUE_KEY = "refValue";

  public static final String LOCK_DB_PRIMARY_KEY = "lockKey";
  public static final String LOCK_DB_SORT_KEY = "lockValue";

  private static final FluentLogger logger = FluentLogger.forEnclosingClass();
  private final AmazonDynamoDBLockClient lockClient;
  private final DynamoDbClient dynamoDBClient;
  private final Configuration configuration;
  private final LoadingCache<String, Optional<Integer>> projectVersionCache;

  @Inject
  DynamoDBRefDatabase(
      AmazonDynamoDBLockClient lockClient,
      DynamoDbClient dynamoDBClient,
      Configuration configuration,
      @Named(PROJECT_VERSION_CACHE) LoadingCache<String, Optional<Integer>> projectVersionCache) {
    this.lockClient = lockClient;
    this.dynamoDBClient = dynamoDBClient;
    this.configuration = configuration;
    this.projectVersionCache = projectVersionCache;
  }

  String pathFor(Project.NameKey projectName, String refName) {
    return versionPrefix(getCurrentVersion(projectName)) + "/" + projectName + "/" + refName;
  }

  static String currentVersionKey(Project.NameKey projectName) {
    return "|" + projectName;
  }

  static String versionPrefix(Integer version) {
    return version != null ? "|" + version : "";
  }

  @Override
  public boolean isUpToDate(Project.NameKey project, Ref ref) throws GlobalRefDbLockException {
    try {
      GetItemResponse response = getItemFromDynamoDB(pathFor(project, ref.getName()));
      if (!response.hasItem()) {
        return true;
      }

      String valueInDynamoDB = response.item().get(REF_DB_VALUE_KEY).s();
      ObjectId objectIdInSharedRefDb = ObjectId.fromString(valueInDynamoDB);
      boolean isUpToDate = objectIdInSharedRefDb.equals(ref.getObjectId());

      if (!isUpToDate) {
        logger.atWarning().log(
            "%s:%s is out of sync: local=%s dynamodb=%s",
            project, ref.getName(), ref.getObjectId(), objectIdInSharedRefDb);
      }
      return isUpToDate;
    } catch (Exception e) {
      throw new GlobalRefDbLockException(project.get(), ref.getName(), e);
    }
  }

  @Override
  public boolean compareAndPut(Project.NameKey project, Ref currRef, ObjectId newRefValue)
      throws GlobalRefDbSystemError {
    ObjectId newValue = Optional.ofNullable(newRefValue).orElse(ObjectId.zeroId());
    ObjectId currValue = Optional.ofNullable(currRef.getObjectId()).orElse(ObjectId.zeroId());

    return doCompareAndPut(
        project, pathFor(project, currRef.getName()), currValue.getName(), newValue.getName());
  }

  @Override
  public <T> boolean compareAndPut(Project.NameKey project, String refName, T currValue, T newValue)
      throws GlobalRefDbSystemError {
    String refPath = pathFor(project, refName);

    String newRefValue =
        Optional.ofNullable(newValue).map(Object::toString).orElse(ObjectId.zeroId().getName());
    String curRefValue =
        Optional.ofNullable(currValue).map(Object::toString).orElse(ObjectId.zeroId().getName());

    return doCompareAndPut(project, refPath, curRefValue, newRefValue);
  }

  private boolean doCompareAndPut(
      Project.NameKey project, String refPath, String currValueForPath, String newValueForPath)
      throws GlobalRefDbSystemError {
    Map<String, AttributeValue> key = getKey(refPath);
    Map<String, AttributeValue> expressionAttributeValues =
        Map.of(
            ":old_value", AttributeValue.builder().s(currValueForPath).build(),
            ":new_value", AttributeValue.builder().s(newValueForPath).build());
    UpdateItemRequest updateItemRequest =
        UpdateItemRequest.builder()
            .tableName(configuration.getRefsDbTableName())
            .key(key)
            .expressionAttributeValues(expressionAttributeValues)
            .updateExpression(String.format("SET %s = :new_value", REF_DB_VALUE_KEY))
            .conditionExpression(
                String.format(
                    "attribute_not_exists(%s) OR %s = :old_value",
                    REF_DB_PRIMARY_KEY, REF_DB_VALUE_KEY))
            .build();
    try {
      dynamoDBClient.updateItem(updateItemRequest);
      logger.atFine().log(
          "Updated path for project %s. Current: %s New: %s",
          project.get(), currValueForPath, newValueForPath);
      return true;
    } catch (ConditionalCheckFailedException e) {
      logger.atWarning().withCause(e).log(
          "Conditional Check Failure when updating refPath %s. expected: %s New: %s",
          refPath, currValueForPath, newValueForPath);
      return false;
    } catch (SdkException e) {
      throw new GlobalRefDbSystemError(
          String.format(
              "Error updating refPath %s. expected: %s new: %s",
              project.get(), currValueForPath, newValueForPath),
          e);
    }
  }

  @Override
  public <T> void put(NameKey project, String refName, T value) throws GlobalRefDbSystemError {
    doPut(project, pathFor(project, refName), value);
  }

  public <T> void doPut(NameKey project, String refPath, T value) throws GlobalRefDbSystemError {
    String refValue =
        Optional.ofNullable(value).map(Object::toString).orElse(ObjectId.zeroId().getName());
    Map<String, AttributeValue> key = getKey(refPath);
    Map<String, AttributeValue> expressionAttributeValues =
        Map.of(":val", AttributeValue.builder().s(refValue).build());
    UpdateItemRequest request =
        UpdateItemRequest.builder()
            .tableName(configuration.getRefsDbTableName())
            .key(key)
            .updateExpression(String.format("SET %s = :val", REF_DB_VALUE_KEY))
            .expressionAttributeValues(expressionAttributeValues)
            .build();
    try {
      dynamoDBClient.updateItem(request);
      logger.atFine().log(
          "Updated path for project %s, path %s, value: %s", project.get(), refPath, refValue);
    } catch (SdkException e) {
      throw new GlobalRefDbSystemError(
          String.format(
              "Error updating path for project %s, path %s. value: %s",
              project.get(), refPath, refValue),
          e);
    }
  }

  @Override
  public AutoCloseable lockRef(Project.NameKey project, String refName)
      throws GlobalRefDbLockException {
    String refPath = pathFor(project, refName);
    try {
      // Attempts to acquire a lock until it either acquires the lock, or a specified
      // additionalTimeToWaitForLock is reached.
      // TODO: 'additionalTimeToWaitForLock' should be configurable
      // Hydrate with instanceId and ServerId and CurrentUser and Thread
      LockItem lockItem =
          lockClient.acquireLock(AcquireLockOptions.builder(refPath).withSortKey(refPath).build());
      logger.atFine().log("Acquired lock for %s", refPath);
      return lockItem;
    } catch (InterruptedException e) {
      logger.atSevere().withCause(e).log(
          "Received interrupted signal when trying to acquire lock for %s", refPath);
      throw new GlobalRefDbLockException(project.get(), refName, e);
    } catch (LockNotGrantedException e) {
      logger.atSevere().withCause(e).log("Failed to acquire lock for %s", refPath);
      throw new GlobalRefDbLockException(project.get(), refName, e);
    }
  }

  @Override
  public boolean exists(Project.NameKey project, String refName) {
    try {
      if (!exists(getItemFromDynamoDB(pathFor(project, refName)))) {
        logger.atFine().log("ref '%s' does not exist in dynamodb", pathFor(project, refName));
        return false;
      }
      return true;

    } catch (Exception e) {
      logger.atSevere().withCause(e).log(
          "Could not check for '%s' existence", pathFor(project, refName));
    }

    return false;
  }

  @Nullable
  public Integer getCurrentVersion(Project.NameKey project) throws GlobalRefDbSystemError {
    try {
      return projectVersionCache.get(project.get()).orElse(null);
    } catch (ExecutionException e) {
      throw new GlobalRefDbSystemError("Could not check project version", e);
    }
  }

  @Override
  public void remove(Project.NameKey project) throws GlobalRefDbSystemError {
    Integer currentVersion = getCurrentVersion(project);
    int nextVersion = (currentVersion != null ? currentVersion : 0) + 1;

    doPut(project, currentVersionKey(project), Integer.toString(nextVersion));
    logger.atWarning().log(
        "Project %s removed, current version %s, next version %s",
        project, currentVersion, nextVersion);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> Optional<T> get(Project.NameKey project, String refName, Class<T> clazz)
      throws GlobalRefDbSystemError {
    try {
      GetItemResponse reponse = getItemFromDynamoDB(pathFor(project, refName));
      if (!reponse.hasItem()) {
        return Optional.empty();
      }
      String refValue = reponse.item().get(REF_DB_VALUE_KEY).s();

      // TODO: not every string might be cast to T (it should work now because the
      // only usage of this function requests string, but we should be serializing
      // deserializing objects before adding them to dynamo.
      return Optional.ofNullable((T) refValue);
    } catch (Exception e) {
      logger.atSevere().withCause(e).log("Cannot get value for %s", pathFor(project, refName));
      return Optional.empty();
    }
  }

  private GetItemResponse getItemFromDynamoDB(String refPath) {
    return getItemFromDynamoDB(refPath, true);
  }

  public GetItemResponse getItemFromDynamoDB(String refPath, Boolean consistentRead) {
    Map<String, AttributeValue> key = getKey(refPath);
    GetItemRequest request =
        GetItemRequest.builder()
            .tableName(configuration.getRefsDbTableName())
            .key(key)
            .consistentRead(consistentRead)
            .build();
    return dynamoDBClient.getItem(request);
  }

  public Map<String, AttributeValue> getKey(String refPath) {
    return Map.of(REF_DB_PRIMARY_KEY, AttributeValue.builder().s(refPath).build());
  }

  public boolean exists(GetItemResponse response) {
    return response.hasItem() && !response.item().isEmpty();
  }

  static class ProjectVersionCacheLoader extends CacheLoader<String, Optional<Integer>> {

    private final Provider<DynamoDBRefDatabase> dynamoDBRefDatabaseProvider;

    @Inject
    public ProjectVersionCacheLoader(Provider<DynamoDBRefDatabase> dynamoDBRefDatabaseProvider) {
      this.dynamoDBRefDatabaseProvider = dynamoDBRefDatabaseProvider;
    }

    @Override
    public Optional<Integer> load(String project) throws Exception {
      GetItemResponse item =
          dynamoDBRefDatabaseProvider
              .get()
              .getItemFromDynamoDB(currentVersionKey(Project.nameKey(project)), false);
      Integer currentVersion =
          dynamoDBRefDatabaseProvider.get().exists(item)
              ? Integer.parseInt(item.item().get(REF_DB_VALUE_KEY).s())
              : null;
      return Optional.ofNullable(currentVersion);
    }
  }
}
