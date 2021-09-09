package com.aws.greengrass.util.platforms.windows;

import com.aws.greengrass.util.exceptions.ProcessCreationException;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Kernel32Util;

class WindowsProcessJNAException extends ProcessCreationException {

    WindowsProcessJNAException(String operation) {
        super(String.format("Error while calling %s: Error code: %d: %s",
                operation,
                Kernel32.INSTANCE.GetLastError(),
                Kernel32Util.getLastErrorMessage()));
    }
}
