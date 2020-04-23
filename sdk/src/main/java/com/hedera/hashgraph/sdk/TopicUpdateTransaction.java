package com.hedera.hashgraph.sdk;

import com.google.protobuf.StringValue;
import com.hedera.hashgraph.sdk.proto.AccountID;
import com.hedera.hashgraph.sdk.proto.ConsensusUpdateTopicTransactionBody;
import com.hedera.hashgraph.sdk.proto.KeyList;
import com.hedera.hashgraph.sdk.proto.TransactionBody;

import java.time.Duration;

public final class TopicUpdateTransaction extends TransactionBuilder<TopicUpdateTransaction> {
    private final ConsensusUpdateTopicTransactionBody.Builder builder;

    public TopicUpdateTransaction() {
        builder = ConsensusUpdateTopicTransactionBody.newBuilder();
    }

    /**
     * Set the topic ID to update.
     *
     * @return {@code this}.
     */
    public TopicUpdateTransaction setTopicId(TopicId topicId) {
        builder.setTopicID(topicId.toProtobuf());
        return this;
    }

    /**
     * Set a new memo for this topic.
     *
     * @return {@code this}.
     */
    public TopicUpdateTransaction setTopicMemo(String memo) {
        builder.setMemo(StringValue.of(memo));
        return this;
    }

    /**
     * Clear the memo for this topic.
     *
     * @return {@code this}.
     */
    public TopicUpdateTransaction clearTopicMemo() {
        builder.setMemo(StringValue.of(""));
        return this;
    }

    /**
     * Set a new admin key for this topic.
     *
     * @return {@code this}.
     */
    public TopicUpdateTransaction setAdminKey(Key adminKey) {
        builder.setAdminKey(adminKey.toKeyProtobuf());
        return this;
    }

    /**
     * Clear the admin key for this topic.
     *
     * @return {@code this}.
     */
    public TopicUpdateTransaction clearAdminKey() {
        builder.setAdminKey(com.hedera.hashgraph.sdk.proto.Key.newBuilder()
            .setKeyList(KeyList.getDefaultInstance())
            .build());

        return this;
    }

    /**
     * Set a new submit key for this topic.
     *
     * @return {@code this}.
     */
    public TopicUpdateTransaction setSubmitKey(Key submitKey) {
        builder.setSubmitKey(submitKey.toKeyProtobuf());
        return this;
    }

    /**
     * Clear the submit key for this topic.
     *
     * @return {@code this}.
     */
    public TopicUpdateTransaction clearSubmitKey() {
        builder.setSubmitKey(com.hedera.hashgraph.sdk.proto.Key.newBuilder()
            .setKeyList(KeyList.getDefaultInstance())
            .build());

        return this;
    }

    /**
     * Set a new auto renew period for this topic.
     *
     * @return {@code this}.
     */
    public TopicUpdateTransaction setAutoRenewPeriod(Duration autoRenewPeriod) {
        builder.setAutoRenewPeriod(DurationConverter.toProtobuf(autoRenewPeriod));
        return this;
    }

    /**
     * Set a new auto renew account ID for this topic.
     *
     * @return {@code this}.
     */
    public TopicUpdateTransaction setAutoRenewAccountId(AccountId autoRenewAccountId) {
        builder.setAutoRenewAccount(autoRenewAccountId.toProtobuf());
        return this;
    }

    /**
     * Clear the auto renew account ID for this topic.
     *
     * @return {@code this}.
     */
    public TopicUpdateTransaction clearAutoRenewAccountId(AccountId autoRenewAccountId) {
        builder.setAutoRenewAccount(AccountID.getDefaultInstance());
        return this;
    }

    @Override
    void onBuild(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setConsensusUpdateTopic(builder);
    }
}
