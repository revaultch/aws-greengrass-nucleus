/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.awssdk.aws.greengrass;

import java.lang.Override;
import software.amazon.awssdk.aws.greengrass.model.CreateAuthSessionRequest;
import software.amazon.awssdk.aws.greengrass.model.CreateAuthSessionResponse;
import software.amazon.awssdk.eventstreamrpc.OperationContinuationHandler;
import software.amazon.awssdk.eventstreamrpc.OperationContinuationHandlerContext;
import software.amazon.awssdk.eventstreamrpc.OperationModelContext;
import software.amazon.awssdk.eventstreamrpc.model.EventStreamJsonMessage;

public abstract class GeneratedAbstractCreateAuthSessionOperationHandler extends OperationContinuationHandler<CreateAuthSessionRequest, CreateAuthSessionResponse, EventStreamJsonMessage, EventStreamJsonMessage> {
  protected GeneratedAbstractCreateAuthSessionOperationHandler(
      OperationContinuationHandlerContext context) {
    super(context);
  }

  @Override
  public OperationModelContext<CreateAuthSessionRequest, CreateAuthSessionResponse, EventStreamJsonMessage, EventStreamJsonMessage> getOperationModelContext(
      ) {
    return GreengrassCoreIPCServiceModel.getCreateAuthSessionModelContext();
  }
}
