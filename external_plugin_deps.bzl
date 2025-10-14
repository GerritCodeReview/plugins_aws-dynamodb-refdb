load("//tools/bzl:maven_jar.bzl", "maven_jar")

def external_plugin_deps():
    """Dependencies of the aws-dynamodb-refdb plugin"""

    AWS_SDK_VER = "2.35.7"

    maven_jar(
        name = "amazon-dynamodb-lock-client",
        artifact = "com.amazonaws:dynamodb-lock-client:1.4.0",
        sha1 = "6aae4bc1b4eeab582e370333b2174623c62f82c7",
    )

    maven_jar(
        name = "amazon-sdk-apache-client",
        artifact = "software.amazon.awssdk:apache-client:" + AWS_SDK_VER,
        sha1 = "e3ba5ade5c38238a8735bd07d030afb1286d6a0d",
    )

    maven_jar(
        name = "amazon-sdk-auth",
        artifact = "software.amazon.awssdk:auth:" + AWS_SDK_VER,
        sha1 = "371377f6eeb54cc0d847536208bdf4a4685ea48c",
    )

    maven_jar(
        name = "amazon-sdk-aws-core",
        artifact = "software.amazon.awssdk:aws-core:" + AWS_SDK_VER,
        sha1 = "a139bfdf6125379b3153d12b66b33597a60f1f8e",
    )

    maven_jar(
        name = "amazon-sdk-aws-json-protocol",
        artifact = "software.amazon.awssdk:aws-json-protocol:" + AWS_SDK_VER,
        sha1 = "fe3580b0839932b5922fe16b68b6a0937131aba7",
    )

    maven_jar(
        name = "amazon-sdk-checksums",
        artifact = "software.amazon.awssdk:checksums:" + AWS_SDK_VER,
        sha1 = "47ad6340a76b91706dff61a65e4318a791754733",
    )

    maven_jar(
        name = "amazon-sdk-checksums-spi",
        artifact = "software.amazon.awssdk:checksums-spi:" + AWS_SDK_VER,
        sha1 = "a924e1f21f41be1fd37563938c4d5ff3287c54ba",
    )

    maven_jar(
        name = "amazon-sdk-core",
        artifact = "software.amazon.awssdk:sdk-core:" + AWS_SDK_VER,
        sha1 = "8e4fd7f3020a00ff00e8ca2dacbd2256486dbf93",
    )

    maven_jar(
        name = "amazon-sdk-dynamodb",
        artifact = "software.amazon.awssdk:dynamodb:" + AWS_SDK_VER,
        sha1 = "40ef2f0e7256a03a97f5fa65b140f947538e8b7e",
    )

    maven_jar(
        name = "amazon-sdk-endpoints-spi",
        artifact = "software.amazon.awssdk:endpoints-spi:" + AWS_SDK_VER,
        sha1 = "99f906164192dc3242aad2e17313c48231b7fb0b",
    )

    maven_jar(
        name = "amazon-sdk-http-auth",
        artifact = "software.amazon.awssdk:http-auth:" + AWS_SDK_VER,
        sha1 = "0922970e363017e3eba324e7cd2d3a92b87fc97f",
    )

    maven_jar(
        name = "amazon-sdk-http-auth-aws",
        artifact = "software.amazon.awssdk:http-auth-aws:" + AWS_SDK_VER,
        sha1 = "cef979ee5f39359b75189363867800cee6ff8fc1",
    )

    maven_jar(
        name = "amazon-sdk-http-auth-spi",
        artifact = "software.amazon.awssdk:http-auth-spi:" + AWS_SDK_VER,
        sha1 = "f547073978d6f5968b08bc5a1eae1c689dd50006",
    )

    maven_jar(
        name = "amazon-sdk-http-client-spi",
        artifact = "software.amazon.awssdk:http-client-spi:" + AWS_SDK_VER,
        sha1 = "55aaa5e6b03302b3d801f16e5cfb1b71de983bff",
    )

    maven_jar(
        name = "amazon-sdk-identity-spi",
        artifact = "software.amazon.awssdk:identity-spi:" + AWS_SDK_VER,
        sha1 = "d3cc80a7a6a99492b242482c25020c737cbf3959",
    )

    maven_jar(
        name = "amazon-sdk-json-utils",
        artifact = "software.amazon.awssdk:json-utils:" + AWS_SDK_VER,
        sha1 = "013636801d30c51e0e18f12636a75db8f45f501b",
    )

    maven_jar(
        name = "amazon-sdk-metrics-spi",
        artifact = "software.amazon.awssdk:metrics-spi:" + AWS_SDK_VER,
        sha1 = "320c8205baad6a6e15d868c7f45b74f497b59d3e",
    )

    maven_jar(
        name = "amazon-sdk-netty-nio-client",
        artifact = "software.amazon.awssdk:netty-nio-client:" + AWS_SDK_VER,
        sha1 = "98e60e5af0f421b42b9afc582d3f08ce7fc9c58f",
    )

    maven_jar(
        name = "amazon-sdk-profiles",
        artifact = "software.amazon.awssdk:profiles:" + AWS_SDK_VER,
        sha1 = "199c71ed266e97011dd999e2a41d2d305dd8baa9",
    )

    maven_jar(
        name = "amazon-sdk-protocol-core",
        artifact = "software.amazon.awssdk:protocol-core:" + AWS_SDK_VER,
        sha1 = "e02285068d1b7c78e09d0a7233f94c07c9d0c5bb",
    )

    maven_jar(
        name = "amazon-sdk-regions",
        artifact = "software.amazon.awssdk:regions:" + AWS_SDK_VER,
        sha1 = "43a383d970fbaabb61ba261192c9a592f94b491d",
    )

    maven_jar(
        name = "amazon-sdk-retries",
        artifact = "software.amazon.awssdk:retries:" + AWS_SDK_VER,
        sha1 = "81e625923a487d4a24e3e546ec74620b8ccc9e92",
    )

    maven_jar(
        name = "amazon-sdk-retries-spi",
        artifact = "software.amazon.awssdk:retries-spi:" + AWS_SDK_VER,
        sha1 = "a902e42e49b32799af4b76f176cae715298dcdd8",
    )

    maven_jar(
        name = "amazon-sdk-third-party-jackson-core",
        artifact = "software.amazon.awssdk:third-party-jackson-core:" + AWS_SDK_VER,
        sha1 = "039256c93502cb9a0d859600ce4d0623c45bdc55",
    )

    maven_jar(
        name = "amazon-sdk-utils",
        artifact = "software.amazon.awssdk:utils:" + AWS_SDK_VER,
        sha1 = "1240513bdae17b33857d34af95c7f8bd3090e2d0",
    )

    maven_jar(
        name = "reactive-streams",
        artifact = "org.reactivestreams:reactive-streams:1.0.4",
        sha1 = "3864a1320d97d7b045f729a326e1e077661f31b7",
    )

    JACKSON_VER = "2.20.0"

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
        # the third part of the version number was missed in this release
        artifact = "com.fasterxml.jackson.core:jackson-annotations:2.20",
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

    TESTCONTAINERS_VERS = "1.21.3"

    maven_jar(
        name = "testcontainer-localstack",
        artifact = "org.testcontainers:localstack:" + TESTCONTAINERS_VERS,
        sha1 = "86cd23aaba16741005c794d26419a16c8470a8e1",
    )

    maven_jar(
        name = "testcontainers",
        artifact = "org.testcontainers:testcontainers:" + TESTCONTAINERS_VERS,
        sha1 = "aa3e792d2cf4598019933c42f1cfa55bd608ce8b",
    )

    maven_jar(
        name = "visible-assertions",
        artifact = "org.rnorth.visible-assertions:visible-assertions:2.1.2",
        sha1 = "20d31a578030ec8e941888537267d3123c2ad1c1",
    )

    DOCKER_JAVA_VERS = "3.6.0"

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
        name = "docker-java-transport-zerodep",
        artifact = "com.github.docker-java:docker-java-transport-zerodep:" + DOCKER_JAVA_VERS,
        sha1 = "549f4985f9c7714deff47d1041603e85e132d184",
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
