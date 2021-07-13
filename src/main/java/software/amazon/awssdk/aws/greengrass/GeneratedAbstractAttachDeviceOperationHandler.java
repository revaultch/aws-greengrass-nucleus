/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.awssdk.aws.greengrass;

import java.lang.Override;
import software.amazon.awssdk.aws.greengrass.model.AttachDeviceRequest;
import software.amazon.awssdk.aws.greengrass.model.AttachDeviceResponse;
import software.amazon.awssdk.eventstreamrpc.OperationContinuationHandler;
import software.amazon.awssdk.eventstreamrpc.OperationContinuationHandlerContext;
import software.amazon.awssdk.eventstreamrpc.OperationModelContext;
import software.amazon.awssdk.eventstreamrpc.model.EventStreamJsonMessage;

public abstract class GeneratedAbstractAttachDeviceOperationHandler extends OperationContinuationHandler<AttachDeviceRequest, AttachDeviceResponse, EventStreamJsonMessage, EventStreamJsonMessage> {
  protected GeneratedAbstractAttachDeviceOperationHandler(
      OperationContinuationHandlerContext context) {
    super(context);
  }

  @Override
  public OperationModelContext<AttachDeviceRequest, AttachDeviceResponse, EventStreamJsonMessage, EventStreamJsonMessage> getOperationModelContext(
      ) {
    return GreengrassCoreIPCServiceModel.getAttachDeviceModelContext();
  }
}
