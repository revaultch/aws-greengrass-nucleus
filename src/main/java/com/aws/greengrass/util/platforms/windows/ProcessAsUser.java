package com.aws.greengrass.util.platforms.windows;

import com.sun.jna.platform.win32.Advapi32;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinNT;

import java.nio.charset.StandardCharsets;

public class ProcessAsUser {


    public ProcessAsUser() {

    }

    protected static void setPrivileges() throws WindowsProcessJNAException {

        WinNT.HANDLEByReference processToken = new WinNT.HANDLEByReference();
        if (!Advapi32.INSTANCE.OpenProcessToken(Kernel32.INSTANCE.GetCurrentProcess(),
                Kernel32.TOKEN_ALL_ACCESS, processToken)) {
            throw new WindowsProcessJNAException("OpenProcessToken");
        };
        setPrivileges(processToken.getValue(), new String[]{ Kernel32.SE_TCB_NAME, Kernel32.SE_ASSIGNPRIMARYTOKEN_NAME }, true);
    }

    protected static void setPrivileges(WinNT.HANDLE processToken, String[] privileges, boolean enable)
            throws WindowsProcessJNAException {
        WinNT.TOKEN_PRIVILEGES tokenPrivileges = new WinNT.TOKEN_PRIVILEGES(privileges.length);
        for (String p : privileges) {
            WinNT.LUID luid = new WinNT.LUID();
            if (!Advapi32.INSTANCE.LookupPrivilegeValue(null, p, luid)) {
                throw new WindowsProcessJNAException("LookupPrivilegeValue");
            }
            tokenPrivileges.Privileges[0].Luid = luid;
            tokenPrivileges.Privileges[0].Attributes.setValue((enable) ? Kernel32.SE_PRIVILEGE_ENABLED : 0);
        }

        if (!Advapi32.INSTANCE.AdjustTokenPrivileges(processToken, false, tokenPrivileges,
                tokenPrivileges.size(), null /* prevState */, null /* return length */)) {
            throw new WindowsProcessJNAException("AdjustTokenPrivileges");
        }
    }

    public void run(String username, String domain, byte[] password) throws WindowsProcessJNAException {
        // set privileges for process
        setPrivileges();
        // logon user

        boolean finished = false;
        WinNT.HANDLEByReference userToken = new WinNT.HANDLEByReference();
        for (int logonType : new int[] { Kernel32.LOGON32_LOGON_INTERACTIVE, Kernel32.LOGON32_LOGON_NETWORK,
                Kernel32.LOGON32_LOGON_BATCH, Kernel32.LOGON32_LOGON_SERVICE }) {
            if (Advapi32.INSTANCE.LogonUser(username, domain, new String(password, StandardCharsets.UTF_8), logonType,
                    Kernel32.LOGON32_PROVIDER_DEFAULT, userToken)) {
                finished = true;
                break;
            }
        }
        if (!finished) {
            throw new WindowsProcessJNAException("LogonUser");
        }

        // console redirection
        WinNT.SECURITY_DESCRIPTOR securityDescriptor = new WinNT.SECURITY_DESCRIPTOR();
        if (!Advapi32.INSTANCE.InitializeSecurityDescriptor(securityDescriptor,
                Kernel32.SECURITY_DESCRIPTOR_REVISION)) {
            throw new WindowsProcessJNAException("InitializeSecurityDescriptor");
        }

        // initialize security descriptor to null dacl (grants access to everyone)?
        if (!Advapi32.INSTANCE.SetSecurityDescriptorDacl(securityDescriptor, true, null, false)) {
            throw new WindowsProcessJNAException("SetSecurityDescriptorDacl");
        }

        WinBase.SECURITY_ATTRIBUTES processSecAttributes = new WinBase.SECURITY_ATTRIBUTES();
        processSecAttributes.lpSecurityDescriptor = securityDescriptor.getPointer();
        processSecAttributes.dwLength.setValue(securityDescriptor.size());
        processSecAttributes.bInheritHandle = true;
        WinNT.HANDLEByReference duplicateToken = new WinNT.HANDLEByReference();
        if (!Advapi32.INSTANCE.DuplicateTokenEx(userToken.getValue(), 0,
                processSecAttributes,
                WinNT.SECURITY_IMPERSONATION_LEVEL.SecurityImpersonation,
                WinNT.TOKEN_TYPE.TokenPrimary,
                duplicateToken)) {
            throw new WindowsProcessJNAException("DuplicateToken");
        }

        WinBase.SECURITY_ATTRIBUTES threadSecAttributes = new WinBase.SECURITY_ATTRIBUTES();
        threadSecAttributes.lpSecurityDescriptor = null;
        threadSecAttributes.dwLength.setValue(0);
        threadSecAttributes.bInheritHandle = false;

        WinBase.STARTUPINFO startupinfo = new WinBase.STARTUPINFO();

        // console redirection

        // load profile
        UserEnv.PROFILEINFO profileInfo = new UserEnv.PROFILEINFO();
        profileInfo.userName = username;
        if (!UserEnv.INSTANCE.LoadUserProfile(duplicateToken.getValue(), profileInfo)) {
            throw new WindowsProcessJNAException("LoadUserProfile");
        }

        // load user
    }
}
