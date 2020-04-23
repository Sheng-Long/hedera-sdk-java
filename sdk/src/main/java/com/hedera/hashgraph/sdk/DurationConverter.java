package com.hedera.hashgraph.sdk;

import java.time.Duration;

final class DurationConverter {
    private DurationConverter() {
    }

    static Duration fromProtobuf(com.hedera.hashgraph.sdk.proto.Duration duration) {
        return Duration.ofSeconds(duration.getSeconds());
    }

    static com.hedera.hashgraph.sdk.proto.Duration toProtobuf(Duration duration) {
        return com.hedera.hashgraph.sdk.proto.Duration.newBuilder()
            .setSeconds(duration.getSeconds())
            .build();
    }
}
