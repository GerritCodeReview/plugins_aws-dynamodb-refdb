package com.googlesource.gerrit.plugins.validation.dfsrefdb.dynamodb;

import static com.googlesource.gerrit.plugins.validation.dfsrefdb.dynamodb.DynamoDBRefDatabase.LOCK_DB_PRIMARY_KEY;
import static com.googlesource.gerrit.plugins.validation.dfsrefdb.dynamodb.DynamoDBRefDatabase.LOCK_DB_SORT_KEY;
import static com.googlesource.gerrit.plugins.validation.dfsrefdb.dynamodb.DynamoDBRefDatabase.REF_DB_PRIMARY_KEY;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.flogger.FluentLogger;
import com.google.gerrit.extensions.events.LifecycleListener;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.ProvisionedThroughput;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;
import software.amazon.awssdk.services.dynamodb.waiters.DynamoDbWaiter;

@Singleton
class DynamoDBLifeCycleManager implements LifecycleListener {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();
  private final Configuration configuration;
  private final DynamoDbClient dynamoDbClient;

  @Inject
  DynamoDBLifeCycleManager(Configuration configuration, DynamoDbClient dynamoDbClient) {
    this.configuration = configuration;
    this.dynamoDbClient = dynamoDbClient;
  }

  // TODO: it is useful to create these at start up during development
  // however ddb tables should be created beforehand, because it might take a while.
  // Perhaps it'd be useful to move this logic to an SSH command.

  @Override
  public void start() {
    createLockTableIfDoesntExist();
    createRefsDbTableIfDoesntExist();
  }

  @Override
  public void stop() {
    // No-op. The dynamoDbClient lifecycle is managed by the provider.
  }

  private void createLockTableIfDoesntExist() {
    String tableName = configuration.getLocksTableName();
    if (tableExists(dynamoDbClient, tableName)) {
      logger.atFine().log("Lock table '%s' already exists, nothing to do.", tableName);
      return;
    }

    logger.atInfo().log("Attempting to create lock table '%s'", tableName);
    try {
      CreateTableRequest request =
          CreateTableRequest.builder()
              .tableName(tableName)
              .attributeDefinitions(
                  AttributeDefinition.builder()
                      .attributeName(LOCK_DB_PRIMARY_KEY)
                      .attributeType(ScalarAttributeType.S)
                      .build(),
                  AttributeDefinition.builder()
                      .attributeName(LOCK_DB_SORT_KEY)
                      .attributeType(ScalarAttributeType.S)
                      .build())
              .keySchema(
                  KeySchemaElement.builder()
                      .attributeName(LOCK_DB_PRIMARY_KEY)
                      .keyType(KeyType.HASH)
                      .build(),
                  KeySchemaElement.builder()
                      .attributeName(LOCK_DB_SORT_KEY)
                      .keyType(KeyType.RANGE)
                      .build())
              .provisionedThroughput(
                  ProvisionedThroughput.builder()
                      .readCapacityUnits(10L)
                      .writeCapacityUnits(10L)
                      .build())
              .build();

      dynamoDbClient.createTable(request);

      // Use a waiter to block until the table is active.
      try (DynamoDbWaiter waiter = dynamoDbClient.waiter()) {
        logger.atInfo().log("Waiting for lock table '%s' to become active...", tableName);
        waiter.waitUntilTableExists(DescribeTableRequest.builder().tableName(tableName).build());
        logger.atInfo().log("Lock table '%s' successfully created and active.", tableName);
      }
    } catch (Exception e) {
      // Catching a broad exception as waiter can throw different things.
      logger.atSevere().withCause(e).log("Failed to create or wait for lock table '%s'", tableName);
    }
  }

  private void createRefsDbTableIfDoesntExist() {
    String tableName = configuration.getRefsDbTableName();
    if (tableExists(dynamoDbClient, tableName)) {
      logger.atFine().log("RefsDb table '%s' already exists, nothing to do.", tableName);
      return;
    }

    logger.atInfo().log("Attempting to create refsDb table '%s'", tableName);
    try {
      CreateTableRequest request =
          CreateTableRequest.builder()
              .tableName(tableName)
              .attributeDefinitions(
                  AttributeDefinition.builder()
                      .attributeName(REF_DB_PRIMARY_KEY)
                      .attributeType(ScalarAttributeType.S)
                      .build())
              .keySchema(
                  KeySchemaElement.builder()
                      .attributeName(REF_DB_PRIMARY_KEY)
                      .keyType(KeyType.HASH)
                      .build())
              .provisionedThroughput(
                  ProvisionedThroughput.builder()
                      .readCapacityUnits(10L)
                      .writeCapacityUnits(10L)
                      .build())
              .build();

      dynamoDbClient.createTable(request);

      try (DynamoDbWaiter waiter = dynamoDbClient.waiter()) {
        logger.atInfo().log("Waiting for refsDb table '%s' to become active...", tableName);
        waiter.waitUntilTableExists(DescribeTableRequest.builder().tableName(tableName).build());
        logger.atInfo().log("RefsDb table '%s' successfully created and active.", tableName);
      }
    } catch (Exception e) {
      logger.atSevere().withCause(e).log(
          "Failed to create or wait for refsDb table '%s'", tableName);
    }
  }

  @VisibleForTesting
  static boolean tableExists(DynamoDbClient dynamoDbClient, String tableName) {
    try {
      dynamoDbClient.describeTable(DescribeTableRequest.builder().tableName(tableName).build());
      return true;
    } catch (ResourceNotFoundException e) {
      return false;
    }
  }
}
