/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.awssdk.aws.greengrass;

import java.lang.Override;
import software.amazon.awssdk.aws.greengrass.model.CertificateUpdateEvent;
import software.amazon.awssdk.aws.greengrass.model.SubscribeToServerCertificateUpdateRequest;
import software.amazon.awssdk.aws.greengrass.model.SubscribeToServerCertificateUpdateResponse;
import software.amazon.awssdk.eventstreamrpc.OperationContinuationHandler;
import software.amazon.awssdk.eventstreamrpc.OperationContinuationHandlerContext;
import software.amazon.awssdk.eventstreamrpc.OperationModelContext;
import software.amazon.awssdk.eventstreamrpc.model.EventStreamJsonMessage;

public abstract class GeneratedAbstractSubscribeToServerCertificateUpdateOperationHandler extends OperationContinuationHandler<SubscribeToServerCertificateUpdateRequest, SubscribeToServerCertificateUpdateResponse, EventStreamJsonMessage, CertificateUpdateEvent> {
  protected GeneratedAbstractSubscribeToServerCertificateUpdateOperationHandler(
      OperationContinuationHandlerContext context) {
    super(context);
  }

  @Override
  public OperationModelContext<SubscribeToServerCertificateUpdateRequest, SubscribeToServerCertificateUpdateResponse, EventStreamJsonMessage, CertificateUpdateEvent> getOperationModelContext(
      ) {
    return GreengrassCoreIPCServiceModel.getSubscribeToServerCertificateUpdateModelContext();
  }
}
