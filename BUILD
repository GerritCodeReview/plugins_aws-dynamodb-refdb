load("//tools/bzl:junit.bzl", "junit_tests")
load(
    "//tools/bzl:plugin.bzl",
    "PLUGIN_DEPS",
    "PLUGIN_TEST_DEPS",
    "gerrit_plugin",
)

gerrit_plugin(
    name = "aws-dynamodb-refdb",
    srcs = glob(["src/main/java/**/*.java"]),
    manifest_entries = [
        "Gerrit-PluginName: aws-dynamodb-refdb",
        "Gerrit-Module: com.googlesource.gerrit.plugins.validation.dfsrefdb.dynamodb.Module",
        "Implementation-Title: AWS DynamoDB shared ref-database implementation",
        "Implementation-URL: https://gerrit-review.googlesource.com/#/admin/projects/plugins/aws-dynamodb-refdb",
    ],
    resources = glob(["src/main/resources/**/*"]),
    deps = [
        ":global-refdb-neverlink",
        "@amazon-dynamodb-lock-client//jar",
        "@amazon-sdk-apache-client//jar",
        "@amazon-sdk-auth//jar",
        "@amazon-sdk-aws-core//jar",
        "@amazon-sdk-aws-json-protocol//jar",
        "@amazon-sdk-checksums-spi//jar",
        "@amazon-sdk-checksums//jar",
        "@amazon-sdk-core//jar",
        "@amazon-sdk-dynamodb//jar",
        "@amazon-sdk-endpoints-spi//jar",
        "@amazon-sdk-http-auth-aws//jar",
        "@amazon-sdk-http-auth-spi//jar",
        "@amazon-sdk-http-auth//jar",
        "@amazon-sdk-http-client-spi//jar",
        "@amazon-sdk-identity-spi//jar",
        "@amazon-sdk-metrics-spi//jar",
        "@amazon-sdk-profiles//jar",
        "@amazon-sdk-regions//jar",
        "@amazon-sdk-retries-spi//jar",
        "@amazon-sdk-retries//jar",
        "@amazon-sdk-third-party-jackson-core//jar",
        "@amazon-sdk-utils//jar",
        "@jackson-annotations//jar",
        "@jackson-core//jar",
        "@jackson-databind//jar",
        "@jackson-dataformat-cbor//jar",
        "@joda-time//jar",
        "@reactive-streams//jar",
    ],
)

junit_tests(
    name = "aws-dynamodb-refdb_tests",
    timeout = "long",
    srcs = glob(["src/test/java/**/*.java"]),
    resources = glob(["src/test/resources/**/*"]),
    tags = [
        "aws-dynamodb-refdb",
        "no-sandbox",
    ],
    deps = [
        ":aws-dynamodb-refdb__plugin_test_deps",
    ],
)

java_library(
    name = "aws-dynamodb-refdb__plugin_test_deps",
    testonly = 1,
    visibility = ["//visibility:public"],
    exports = PLUGIN_DEPS + PLUGIN_TEST_DEPS + [
        ":aws-dynamodb-refdb__plugin",
        "//plugins/global-refdb",
        "@amazon-sdk-auth//jar",
        "@amazon-sdk-dynamodb//jar",
        "@amazon-sdk-http-client-spi//jar",
        "@amazon-sdk-identity-spi//jar",
        "@amazon-sdk-json-utils//jar",
        "@amazon-sdk-netty-nio-client//jar",
        "@amazon-sdk-protocol-core//jar",
        "@amazon-sdk-regions//jar",
        "@docker-java-api//jar",
        "@docker-java-transport-zerodep//jar",
        "@docker-java-transport//jar",
        "@duct-tape//jar",
        "@jna//jar",
        "@testcontainer-localstack//jar",
        "@testcontainers//jar",
        "@visible-assertions//jar",
    ],
)

java_library(
    name = "global-refdb-neverlink",
    neverlink = 1,
    exports = ["//plugins/global-refdb"],
)
