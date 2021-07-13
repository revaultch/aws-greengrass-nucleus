/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.awssdk.aws.greengrass;

import java.lang.Override;
import software.amazon.awssdk.aws.greengrass.model.CloseAuthSessionRequest;
import software.amazon.awssdk.aws.greengrass.model.CloseAuthSessionResponse;
import software.amazon.awssdk.eventstreamrpc.OperationContinuationHandler;
import software.amazon.awssdk.eventstreamrpc.OperationContinuationHandlerContext;
import software.amazon.awssdk.eventstreamrpc.OperationModelContext;
import software.amazon.awssdk.eventstreamrpc.model.EventStreamJsonMessage;

public abstract class GeneratedAbstractCloseAuthSessionOperationHandler extends OperationContinuationHandler<CloseAuthSessionRequest, CloseAuthSessionResponse, EventStreamJsonMessage, EventStreamJsonMessage> {
  protected GeneratedAbstractCloseAuthSessionOperationHandler(
      OperationContinuationHandlerContext context) {
    super(context);
  }

  @Override
  public OperationModelContext<CloseAuthSessionRequest, CloseAuthSessionResponse, EventStreamJsonMessage, EventStreamJsonMessage> getOperationModelContext(
      ) {
    return GreengrassCoreIPCServiceModel.getCloseAuthSessionModelContext();
  }
}
