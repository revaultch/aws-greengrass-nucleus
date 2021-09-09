package com.aws.greengrass.util.platforms.windows;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.WString;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinCrypt;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.ByReference;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

import java.util.List;

import static java.util.Arrays.asList;

public interface UserEnv extends StdCallLibrary {
    UserEnv INSTANCE = Native.load("userenv", UserEnv.class, W32APIOptions.ASCII_OPTIONS);

    /**
     * Load user's profile.
     * @param hToken Token for the user, which is returned by the LogonUser, CreateRestrictedToken, DuplicateToken,
     *               OpenProcessToken, or OpenThreadToken function. The token must have TOKEN_QUERY,
     *               TOKEN_IMPERSONATE, and TOKEN_DUPLICATE access. For more information, see Access Rights for
     *               Access-Token Objects.
     * @param profile Pointer to a PROFILEINFO structure. LoadUserProfile fails and returns ERROR_INVALID_PARAMETER
     *                if the dwSize member of the structure is not set to sizeof(PROFILEINFO) or if the lpUserName
     *                member is NULL. For more information
     * @return <code>true</code> if successful; otherwise <code>false</code>
     * @see <a href="https://docs.microsoft.com/en-us/windows/win32/api/userenv/nf-userenv-loaduserprofilea">Docs</a>
     */
    boolean LoadUserProfile(WinNT.HANDLE hToken, PROFILEINFO profile);

    /**
     * typedef struct _PROFILEINFOA {
     *   DWORD             dwSize;
     *   DWORD             dwFlags;
     *   MIDL_STRING LPSTR lpUserName;
     *   MIDL_STRING LPSTR lpProfilePath;
     *   MIDL_STRING LPSTR lpDefaultPath;
     *   MIDL_STRING LPSTR lpServerName;
     *   MIDL_STRING LPSTR lpPolicyPath;
     * #if ...
     *   ULONG_PTR         hProfile;
     * #else
     *   HANDLE            hProfile;
     * #endif
     * } PROFILEINFOA, *LPPROFILEINFOA;
     */

    public static class PROFILEINFO extends Structure {
        public static class ByReference extends PROFILEINFO implements Structure.ByReference {

        }

        @SuppressWarnings("unused") public int dwSize;
        @SuppressWarnings("unused") public int flags;
        @SuppressWarnings("unused") public String userName;
        @SuppressWarnings("unused") public String profilePath;
        @SuppressWarnings("unused") public String defaultPath;
        @SuppressWarnings("unused") public String serverName;
        @SuppressWarnings("unused") public String policyPath;
        @SuppressWarnings("unused") public WinNT.HANDLE hProfile;

        protected List<String> getFieldOrder() {
            return asList("dwSize", "flags", "userName", "profilePath", "defaultPath", "serverName", "policyPath",
                    "hProfile");
        }
    }
}
