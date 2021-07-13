/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.awssdk.aws.greengrass;

import java.lang.Override;
import software.amazon.awssdk.aws.greengrass.model.CanEntityPerformRequest;
import software.amazon.awssdk.aws.greengrass.model.CanEntityPerformResponse;
import software.amazon.awssdk.eventstreamrpc.OperationContinuationHandler;
import software.amazon.awssdk.eventstreamrpc.OperationContinuationHandlerContext;
import software.amazon.awssdk.eventstreamrpc.OperationModelContext;
import software.amazon.awssdk.eventstreamrpc.model.EventStreamJsonMessage;

public abstract class GeneratedAbstractCanEntityPerformOperationHandler extends OperationContinuationHandler<CanEntityPerformRequest, CanEntityPerformResponse, EventStreamJsonMessage, EventStreamJsonMessage> {
  protected GeneratedAbstractCanEntityPerformOperationHandler(
      OperationContinuationHandlerContext context) {
    super(context);
  }

  @Override
  public OperationModelContext<CanEntityPerformRequest, CanEntityPerformResponse, EventStreamJsonMessage, EventStreamJsonMessage> getOperationModelContext(
      ) {
    return GreengrassCoreIPCServiceModel.getCanEntityPerformModelContext();
  }
}
