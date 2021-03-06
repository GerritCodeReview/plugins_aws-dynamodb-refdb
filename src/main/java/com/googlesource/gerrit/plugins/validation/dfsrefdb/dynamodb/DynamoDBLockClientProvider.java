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

import static com.googlesource.gerrit.plugins.validation.dfsrefdb.dynamodb.DynamoDBRefDatabase.LOCK_DB_PRIMARY_KEY;
import static com.googlesource.gerrit.plugins.validation.dfsrefdb.dynamodb.DynamoDBRefDatabase.LOCK_DB_SORT_KEY;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBLockClient;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBLockClientOptions;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import java.util.concurrent.TimeUnit;

@Singleton
class DynamoDBLockClientProvider implements Provider<AmazonDynamoDBLockClient> {
  private final Configuration configuration;
  private final AmazonDynamoDB dynamoDB;

  @Inject
  DynamoDBLockClientProvider(Configuration configuration, AmazonDynamoDB dynamoDB) {
    this.configuration = configuration;
    this.dynamoDB = dynamoDB;
  }

  @Override
  public AmazonDynamoDBLockClient get() {
    final boolean createHeartbeatBackgroundThread = true;
    return new AmazonDynamoDBLockClient(
        AmazonDynamoDBLockClientOptions.builder(dynamoDB, configuration.getLocksTableName())
            .withPartitionKeyName(LOCK_DB_PRIMARY_KEY)
            .withSortKeyName(LOCK_DB_SORT_KEY)
            .withTimeUnit(TimeUnit.SECONDS)
            .withLeaseDuration(10L)
            .withHeartbeatPeriod(3L)
            .withCreateHeartbeatBackgroundThread(createHeartbeatBackgroundThread)
            .build());
  }
}
