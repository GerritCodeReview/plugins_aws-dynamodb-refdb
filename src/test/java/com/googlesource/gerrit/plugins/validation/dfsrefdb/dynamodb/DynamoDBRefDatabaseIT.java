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

import static com.google.common.truth.Truth.assertThat;
import static com.googlesource.gerrit.plugins.validation.dfsrefdb.dynamodb.Configuration.DEFAULT_LOCKS_TABLE_NAME;
import static com.googlesource.gerrit.plugins.validation.dfsrefdb.dynamodb.Configuration.DEFAULT_REFS_DB_TABLE_NAME;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.DYNAMODB;

import com.gerritforge.gerrit.globalrefdb.GlobalRefDbLockException;
import com.google.common.cache.LoadingCache;
import com.google.common.flogger.FluentLogger;
import com.google.gerrit.acceptance.LightweightPluginDaemonTest;
import com.google.gerrit.acceptance.TestPlugin;
import com.google.gerrit.acceptance.WaitUtil;
import com.google.gerrit.common.Nullable;
import com.google.gerrit.entities.Project;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import java.time.Duration;
import java.util.Optional;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectIdRef;
import org.eclipse.jgit.lib.Ref;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@TestPlugin(
    name = "aws-dynamodb-refdb",
    sysModule = "com.googlesource.gerrit.plugins.validation.dfsrefdb.dynamodb.Module")
public class DynamoDBRefDatabaseIT extends LightweightPluginDaemonTest {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();
  private static final int LOCALSTACK_PORT = 4566;
  private static final Duration DYNAMODB_TABLE_CREATION_TIMEOUT = Duration.ofSeconds(10);
  private static LocalStackContainer localstack;

  @BeforeClass
  public static void setupLocalStack() {
    logger.atFine().log("--- Testcontainers Debug Info ---");
    logger.atFine().log("DOCKER_HOST (env): %s", System.getenv("DOCKER_HOST"));
    logger.atFine().log("docker.host (prop): %s", System.getProperty("docker.host"));
    logger.atFine().log("---------------------------------");

    localstack =
        new LocalStackContainer(DockerImageName.parse("localstack/localstack").withTag("4.9.2"))
            .withServices(DYNAMODB)
            .withExposedPorts(LOCALSTACK_PORT);
    localstack.start();
  }

  @AfterClass
  public static void tearDown() {
    localstack.close();
  }

  @Before
  @Override
  public void setUpTestPlugin() throws Exception {
    System.setProperty("endpoint", localstack.getEndpointOverride(DYNAMODB).toASCIIString());
    System.setProperty("region", localstack.getRegion());

    // localstack recommends to set both access key id and secret access key to the same value
    // it ignores the secret access key
    // see https://docs.localstack.cloud/aws/capabilities/config/credentials/
    System.setProperty("aws.accessKeyId", localstack.getSecretKey());
    System.setProperty("aws.secretAccessKey", localstack.getSecretKey());

    super.setUpTestPlugin();
  }

  @Test
  public void shouldEnsureLockTableExists() throws Exception {
    WaitUtil.waitUntil(
        () -> DynamoDBLifeCycleManager.tableExists(dynamoDBClient(), DEFAULT_LOCKS_TABLE_NAME),
        DYNAMODB_TABLE_CREATION_TIMEOUT);
  }

  @Test
  public void shouldEnsureRefsDbTableExists() throws Exception {
    WaitUtil.waitUntil(
        () -> DynamoDBLifeCycleManager.tableExists(dynamoDBClient(), DEFAULT_REFS_DB_TABLE_NAME),
        DYNAMODB_TABLE_CREATION_TIMEOUT);
  }

  @Test
  public void getShouldBeEmptyWhenRefDoesntExists() throws Exception {
    Optional<String> maybeRef = dynamoDBRefDatabase().get(project, "refs/not/in/db", String.class);

    assertThat(maybeRef).isEmpty();
  }

  @Test
  public void getShouldReturnRefValueWhenItExists() throws Exception {
    String refName = "refs/changes/01/01/meta";
    String refValue = "533d3ccf8a650fb26380faa732921a2c74924d5c";
    createRefInDynamoDB(project, refName, refValue);

    Optional<String> maybeRef = dynamoDBRefDatabase().get(project, refName, String.class);

    assertThat(maybeRef).hasValue(refValue);
  }

  @Test
  public void existsShouldReturnFalseWhenRefIsNotStored() throws Exception {
    assertThat(dynamoDBRefDatabase().exists(project, "refs/not/in/db")).isFalse();
  }

  @Test
  public void existShouldReturnTrueWhenRefIsStored() throws Exception {
    String refName = "refs/changes/01/01/meta";
    String refValue = "533d3ccf8a650fb26380faa732921a2c74924d5c";
    createRefInDynamoDB(project, refName, refValue);

    assertThat(dynamoDBRefDatabase().exists(project, refName)).isTrue();
  }

  @Test
  public void isUpToDateShouldReturnTrueWhenRefPointsToTheStoredRefValue() throws Exception {
    String refName = "refs/changes/01/01/meta";
    String currentRefValue = "533d3ccf8a650fb26380faa732921a2c74924d5c";

    createRefInDynamoDB(project, refName, currentRefValue);

    assertThat(dynamoDBRefDatabase().isUpToDate(project, refOf(refName, currentRefValue))).isTrue();
  }

  @Test
  public void isUpToDateShouldReturnFalseWhenRefDoesNotPointToTheStoredRefValue()
      throws GlobalRefDbLockException {
    String refName = "refs/changes/01/01/meta";
    String currentRefValue = "533d3ccf8a650fb26380faa732921a2c74924d5c";
    String previousRefValue = "9f6f2963cf44505428c61b935ff1ca65372cf28c";

    createRefInDynamoDB(project, refName, previousRefValue);

    assertThat(dynamoDBRefDatabase().isUpToDate(project, refOf(refName, currentRefValue)))
        .isFalse();
  }

  @Test
  public void isUpToDateShouldBeConsideredTrueWhenNoPreviousRefExists()
      throws GlobalRefDbLockException {
    String refName = "refs/changes/01/01/meta";
    String currentRefValue = "533d3ccf8a650fb26380faa732921a2c74924d5c";

    assertThat(dynamoDBRefDatabase().isUpToDate(project, refOf(refName, currentRefValue))).isTrue();
  }

  @Test
  public void compareAndPutShouldBeSuccessfulWhenNoPreviousRefExists() {
    String refName = "refs/changes/01/01/meta";
    String newRefValue = "533d3ccf8a650fb26380faa732921a2c74924d5c";

    assertThat(
            dynamoDBRefDatabase()
                .compareAndPut(project, refOf(refName, null), ObjectId.fromString(newRefValue)))
        .isTrue();
  }

  @Test
  public void putShouldBeSuccessfulWhenNoPreviousValueForRefExists() {
    String refName = "refs/changes/01/01/meta";
    String newRefValue = "533d3ccf8a650fb26380faa732921a2c74924d5c";

    dynamoDBRefDatabase().put(project, refName, newRefValue);
    Optional<String> result = dynamoDBRefDatabase().get(project, refName, String.class);
    assertThat(result).hasValue(newRefValue);
  }

  @Test
  public void putShouldSuccessfullyUpdateRemovedRef() {
    String refName = "refs/changes/01/01/meta";
    String newRefValue = null;

    dynamoDBRefDatabase().put(project, refName, newRefValue);
    Optional<String> result = dynamoDBRefDatabase().get(project, refName, String.class);
    assertThat(result).hasValue(ObjectId.zeroId().getName());
  }

  @Test
  public void putShouldBeSuccessfulWhenUpdatingRef() {
    String refName = "refs/changes/01/01/meta";
    String oldValue = "123";
    String newValue = "345";
    dynamoDBRefDatabase().put(project, refName, oldValue);

    dynamoDBRefDatabase().put(project, refName, newValue);

    Optional<String> result = dynamoDBRefDatabase().get(project, refName, String.class);
    assertThat(result).hasValue(newValue);
  }

  @Test
  public void compareAndPutShouldSuccessfullyUpdateRemovedRef() throws Exception {
    String refName = "refs/changes/01/01/meta";
    String currentRefValue = "533d3ccf8a650fb26380faa732921a2c74924d5c";

    createRefInDynamoDB(project, refName, currentRefValue);

    assertThat(
            dynamoDBRefDatabase()
                .compareAndPut(project, refOf(refName, currentRefValue), ObjectId.zeroId()))
        .isTrue();
  }

  @Test
  public void compareAndPutShouldReturnFalseWhenStoredRefIsNotExpected() {
    String refName = "refs/changes/01/01/meta";
    String currentRefValue = "533d3ccf8a650fb26380faa732921a2c74924d5c";
    String newRefValue = "9f6f2963cf44505428c61b935ff1ca65372cf28c";
    String expectedRefValue = "875ce4b14278b64be61478f91a40cf480758bfba";
    Ref expectedRef = refOf(refName, expectedRefValue);

    createRefInDynamoDB(project, refName, currentRefValue);
    assertThat(
            dynamoDBRefDatabase()
                .compareAndPut(project, expectedRef, ObjectId.fromString(newRefValue)))
        .isFalse();
  }

  @Test
  public void compareAndPutStringsShouldBeSuccessful() throws Exception {
    String refName = "refs/changes/01/01/meta";
    String currentRefValue = "533d3ccf8a650fb26380faa732921a2c74924d5c";
    String newRefValue = "9f6f2963cf44505428c61b935ff1ca65372cf28c";

    createRefInDynamoDB(project, refName, currentRefValue);

    assertThat(dynamoDBRefDatabase().compareAndPut(project, refName, currentRefValue, newRefValue))
        .isTrue();
  }

  @Test
  public void compareAndPutStringsShouldBeSuccessfulWhenNoPreviousRefExists() {
    String refName = "refs/changes/01/01/meta";
    String newRefValue = "533d3ccf8a650fb26380faa732921a2c74924d5c";

    assertThat(dynamoDBRefDatabase().compareAndPut(project, refName, null, newRefValue)).isTrue();
  }

  @Test
  public void projectVersionShouldBeUsedAsPrefix() {
    String versionKey = DynamoDBRefDatabase.currentVersionKey(project);
    dynamoDBRefDatabase().doPut(project, versionKey, "1");

    assertThat(dynamoDBRefDatabase().pathFor(project, "refs/heads/master"))
        .isEqualTo("|1/" + project + "/refs/heads/master");
  }

  @Test
  public void removeProjectShouldIncreaseProjectVersionWhenNotCached() {
    assertThat(dynamoDBRefDatabase().getCurrentVersion(project)).isNull();

    dynamoDBRefDatabase().remove(project);
    projectVersionCache().invalidate(project.get());

    assertThat(dynamoDBRefDatabase().getCurrentVersion(project)).isEqualTo(1);
  }

  @Test
  public void removeProjectShouldKeepCurrentVersionWhenCached() {
    assertThat(dynamoDBRefDatabase().getCurrentVersion(project)).isNull();

    dynamoDBRefDatabase().remove(project);

    assertThat(dynamoDBRefDatabase().getCurrentVersion(project)).isNull();
  }

  private DynamoDbClient dynamoDBClient() {
    return plugin.getSysInjector().getInstance(DynamoDbClient.class);
  }

  private DynamoDBRefDatabase dynamoDBRefDatabase() {
    return plugin.getSysInjector().getInstance(DynamoDBRefDatabase.class);
  }

  private LoadingCache<String, Optional<Integer>> projectVersionCache() {
    return plugin
        .getSysInjector()
        .getInstance(
            Key.get(
                new TypeLiteral<>() {},
                Names.named(ProjectVersionCacheModule.PROJECT_VERSION_CACHE)));
  }

  private void createRefInDynamoDB(Project.NameKey project, String refPath, String refValue) {
    dynamoDBRefDatabase().put(project, refPath, refValue);
  }

  private Ref refOf(String refName, @Nullable String objectIdSha1) {
    return new ObjectIdRef.Unpeeled(
        Ref.Storage.NETWORK,
        refName,
        Optional.ofNullable(objectIdSha1).map(ObjectId::fromString).orElse(null));
  }
}
