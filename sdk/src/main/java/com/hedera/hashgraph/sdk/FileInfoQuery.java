package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.FileGetInfoQuery;
import com.hedera.hashgraph.sdk.proto.FileServiceGrpc;
import com.hedera.hashgraph.sdk.proto.Query;
import com.hedera.hashgraph.sdk.proto.QueryHeader;
import com.hedera.hashgraph.sdk.proto.Response;
import com.hedera.hashgraph.sdk.proto.ResponseHeader;
import io.grpc.MethodDescriptor;
import java.util.concurrent.CompletableFuture;

/**
 * Get all of the information about a file, except for its contents.
 * <p>
 * When a file expires, it no longer exists, and there will be no info about it, and the fileInfo field
 * will be blank.
 * <p>
 * If a transaction or smart contract deletes the file, but it has not yet expired, then the
 * fileInfo field will be non-empty, the deleted field will be true, its size will be 0,
 * and its contents will be empty. Note that each file has a FileID, but does not have a filename.
 */
public final class FileInfoQuery extends QueryBuilder<FileInfo, FileInfoQuery> {
    private final FileGetInfoQuery.Builder builder;

    public FileInfoQuery() {
        builder = FileGetInfoQuery.newBuilder();
    }

    /**
     * Sets the file ID for which information is requested.
     *
     * @return {@code this}
     */
    public FileInfoQuery setFileId(FileId fileId) {
        builder.setFileID(fileId.toProtobuf());

        return this;
    }

    @Override
    void onMakeRequest(Query.Builder queryBuilder, QueryHeader header) {
        queryBuilder.setFileGetInfo(builder.setHeader(header));
    }

    @Override
    ResponseHeader mapResponseHeader(Response response) {
        return response.getFileGetInfo().getHeader();
    }

    @Override
    QueryHeader mapRequestHeader(Query request) {
        return request.getFileGetInfo().getHeader();
    }

    @Override
    FileInfo mapResponse(Response response) {
        return FileInfo.fromProtobuf(response.getFileGetInfo().getFileInfo());
    }

    @Override
    MethodDescriptor<Query, Response> getMethodDescriptor() {
        return FileServiceGrpc.getGetFileInfoMethod();
    }

    @Override
    public CompletableFuture<Hbar> getCostAsync(Client client) {
        // deleted accounts return a COST_ANSWER of zero which triggers `INSUFFICIENT_TX_FEE`
        // if you set that as the query payment; 25 tinybar seems to be enough to get
        // `FILE_DELETED` back instead.
        return super.getCostAsync(client).thenApply((cost) -> Hbar.fromTinybar(Math.min(cost.asTinybar(), 25)));
    }
}
