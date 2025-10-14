// Copyright (C) 2025 The Android Open Source Project
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

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.providers.DefaultAwsRegionProviderChain;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClientBuilder;

@Singleton
class DynamoDbClientProvider implements Provider<DynamoDbClient> {
  private final Configuration configuration;

  @Inject
  DynamoDbClientProvider(Configuration configuration) {
    this.configuration = configuration;
  }

  @Override
  public DynamoDbClient get() {
    DynamoDbClientBuilder builder =
        DynamoDbClient.builder()
            .credentialsProvider(getCredentialsProvider())
            .region(
                configuration
                    .getRegion()
                    .orElseGet(() -> new DefaultAwsRegionProviderChain().getRegion()));
    configuration.getEndpoint().ifPresent(builder::endpointOverride);

    return builder.build();
  }

  private AwsCredentialsProvider getCredentialsProvider() {
    return configuration
        .getAwsConfigurationProfileName()
        .<AwsCredentialsProvider>map(
            profileName -> ProfileCredentialsProvider.builder().profileName(profileName).build())
        .orElseGet(() -> DefaultCredentialsProvider.builder().build());
  }
}
