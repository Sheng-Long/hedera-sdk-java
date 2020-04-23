package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.TransactionID;

import javax.annotation.Nullable;
import java.time.Clock;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.CompletableFuture.completedFuture;

/**
 * The client-generated ID for a transaction.
 *
 * <p>This is used for retrieving receipts and records for a transaction, for appending to a file
 * right after creating it, for instantiating a smart contract with bytecode in a file just created,
 * and internally by the network for detecting when duplicate transactions are submitted.
 */
public final class TransactionId implements WithGetReceipt, WithGetRecord {
    @Nullable
    private static Instant lastInstant = null;

    /**
     * The Account ID that paid for this transaction.
     */
    public final AccountId accountId;

    /**
     * The time from when this transaction is valid.
     *
     * <p>When a transaction is submitted there is additionally a validDuration (defaults to 120s)
     * and together they define a time window that a transaction may be processed in.
     */
    public final Instant validStart;

    public TransactionId(AccountId accountId, Instant validStart) {
        this.accountId = accountId;
        this.validStart = validStart;
    }

    /**
     * Generates a new transaction ID for the given account ID.
     *
     * <p>Note that transaction IDs are made of the valid start of the transaction and the account
     * that will be charged the transaction fees for the transaction.
     *
     * @param accountId the ID of the Hedera account that will be charge the transaction fees.
     */
    public static TransactionId generate(AccountId accountId) {
        return new TransactionId(accountId, getIncreasingInstant());
    }

    private static synchronized Instant getIncreasingInstant() {
        // allows the transaction to be accepted as long as the
        // server is not more than 10 seconds behind us
        Instant start = Clock.systemUTC().instant().minusSeconds(10);

        // ensures every instant is at least greater than the last
        lastInstant =
            lastInstant != null && start.compareTo(lastInstant) <= 0
                ? lastInstant.plusNanos(1)
                : start;

        return lastInstant;
    }

    static TransactionId fromProtobuf(TransactionID transactionID) {
        return new TransactionId(
            AccountId.fromProtobuf(transactionID.getAccountID()),
            InstantConverter.fromProtobuf(transactionID.getTransactionValidStart()));
    }

    public static TransactionId fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        return fromProtobuf(TransactionID.parseFrom(bytes).toBuilder().build());
    }

    @Override
    @FunctionalExecutable(type = "TransactionReceipt", exceptionTypes = {"HederaReceiptStatusException"})
    public CompletableFuture<TransactionReceipt> getReceiptAsync(Client client) {
        return new TransactionReceiptQuery()
            .setTransactionId(this)
            .executeAsync(client)
            .thenCompose(receipt -> {
                if (receipt.status != Status.Success) {
                    var future = new CompletableFuture<TransactionReceipt>();
                    future.completeExceptionally(new HederaReceiptStatusException(this, receipt));
                    return future;
                }

                return completedFuture(receipt);
            });
    }

    @Override
    @FunctionalExecutable(type = "TransactionRecord", exceptionTypes = {"HederaReceiptStatusException"})
    public CompletableFuture<TransactionRecord> getRecordAsync(Client client) {
        // note: we get the receipt first to ensure consensus has been reached
        return getReceiptAsync(client).thenCompose(receipt -> new TransactionRecordQuery()
            .setTransactionId(this)
            .executeAsync(client));
    }

    TransactionID toProtobuf() {
        return TransactionID.newBuilder()
            .setAccountID(accountId.toProtobuf())
            .setTransactionValidStart(InstantConverter.toProtobuf(validStart))
            .build();
    }

    @Override
    public String toString() {
        return "" + accountId + "@" + validStart.getEpochSecond() + "." + validStart.getNano();
    }

    public byte[] toBytes() {
        return this.toProtobuf().toByteArray();
    }
}
