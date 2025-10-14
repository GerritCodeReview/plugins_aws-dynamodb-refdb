load("//tools/bzl:maven_jar.bzl", "maven_jar")

AWS_SDK_VER = "2.35.5"
JACKSON_VER = "2.20.0"
DOCKER_JAVA_VERS = "3.6.0"
LOCALSTACK_VERS = "1.21.3"

def external_plugin_deps():
    """Dependencies of the aws-dynamodb-refdb plugin"""
    maven_jar(
        name = "amazon-dynamodb",
        artifact = "software.amazon.awssdk:dynamodb:" + AWS_SDK_VER,
        sha1 = "d4ee5bdf62c768aca766a5730fdf228ae081fdea",
    )

    maven_jar(
        name = "dynamodb-lock-client",
        artifact = "com.amazonaws:dynamodb-lock-client:1.4.0",
        sha1 = "6aae4bc1b4eeab582e370333b2174623c62f82c7",
    )

    maven_jar(
        name = "amazon-regions",
        artifact = "software.amazon.awssdk:regions:" + AWS_SDK_VER,
        sha1 = "e0b90db310a950960edd8d087ded45fc65ca3be3",
    )

    maven_jar(
        name = "amazon-sdk-auth",
        artifact = "software.amazon.awssdk:auth:" + AWS_SDK_VER,
        sha1 = "372b6ac4d431a00a4a6d50892eeb3ac0de1f42c2",
    )

    maven_jar(
        name = "amazon-sdk-identity-spi",
        artifact = "software.amazon.awssdk:identity-spi:" + AWS_SDK_VER,
        sha1 = "59dcd09850a6483e04301f2802c32266a38ec8eb",
    )

    maven_jar(
        name = "amazon-sdk-core",
        artifact = "software.amazon.awssdk:sdk-core:" + AWS_SDK_VER,
        sha1 = "7f236b027d7de63ec9c8c1ed7415d2f7773bc17f",
    )

    maven_jar(
        name = "amazon-aws-core",
        artifact = "software.amazon.awssdk:aws-core:" + AWS_SDK_VER,
        sha1 = "cd9b475ad185f6d65b139dc4dabfcfe88e2832f2",
    )

    maven_jar(
        name = "amazon-utils",
        artifact = "software.amazon.awssdk:utils:" + AWS_SDK_VER,
        sha1 = "98858f72d540a4d5f679c2a2c1fb795718575761",
    )

    maven_jar(
        name = "jackson-databind",
        artifact = "com.fasterxml.jackson.core:jackson-databind:" + JACKSON_VER,
        sha1 = "f0a5e62fbd21285e9a5498a60dccb097e1ef793b",
    )

    maven_jar(
        name = "jackson-dataformat-cbor",
        artifact = "com.fasterxml.jackson.dataformat:jackson-dataformat-cbor:" + JACKSON_VER,
        sha1 = "c10e9032bec62df3089ca1cbdef43a1453aca261",
    )

    maven_jar(
        name = "jackson-annotations",
        artifact = "com.fasterxml.jackson.core:jackson-annotations:" + JACKSON_VER,
        sha1 = "6a5e7291ea3f2b590a7ce400adb7b3aea4d7e12c",
    )

    maven_jar(
        name = "jackson-core",
        artifact = "com.fasterxml.jackson.core:jackson-core:" + JACKSON_VER,
        sha1 = "3c97f7fad069f7cfae639d790bd93d6a0b2dff31",
    )

    maven_jar(
        name = "joda-time",
        artifact = "joda-time:joda-time:2.14.0",
        sha1 = "1fa665c1ce64a2c8c94f63fc5c1ee7bd742d2022",
    )

    maven_jar(
        name = "testcontainer-localstack",
        artifact = "org.testcontainers:localstack:" + LOCALSTACK_VERS,
        sha1 = "86cd23aaba16741005c794d26419a16c8470a8e1",
    )

    maven_jar(
        name = "testcontainers",
        artifact = "org.testcontainers:testcontainers:" + LOCALSTACK_VERS,
        sha1 = "aa3e792d2cf4598019933c42f1cfa55bd608ce8b",
    )

    maven_jar(
        name = "visible-assertions",
        artifact = "org.rnorth.visible-assertions:visible-assertions:2.1.2",
        sha1 = "20d31a578030ec8e941888537267d3123c2ad1c1",
    )

    maven_jar(
        name = "docker-java-api",
        artifact = "com.github.docker-java:docker-java-api:" + DOCKER_JAVA_VERS,
        sha1 = "caeb5bee6a9c07bff31f73ace576436168e2aa47",
    )

    maven_jar(
        name = "docker-java-transport",
        artifact = "com.github.docker-java:docker-java-transport:" + DOCKER_JAVA_VERS,
        sha1 = "d522c467aad17fd927e0db0130d2849a321a36aa",
    )

    maven_jar(
        name = "duct-tape",
        artifact = "org.rnorth.duct-tape:duct-tape:1.0.8",
        sha1 = "92edc22a9ab2f3e17c9bf700aaee377d50e8b530",
    )

    maven_jar(
        name = "jna",
        artifact = "net.java.dev.jna:jna:5.18.1",
        sha1 = "b27ba04287cc4abe769642fe8318d39fc89bf937",
    )
