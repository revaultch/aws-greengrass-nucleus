/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.aws.greengrass.componentmanager;

import com.aws.greengrass.deployment.DeviceConfiguration;
import com.aws.greengrass.logging.api.Logger;
import com.aws.greengrass.logging.impl.LogManager;
import com.aws.greengrass.util.Coerce;
import com.aws.greengrass.util.EncryptionUtils;
import com.aws.greengrass.util.IotSdkClientFactory;
import com.aws.greengrass.util.ProxyUtils;
import com.aws.greengrass.util.RegionUtils;
import com.aws.greengrass.util.Utils;
import com.aws.greengrass.util.exceptions.InvalidEnvironmentStageException;
import com.aws.greengrass.util.exceptions.TLSAuthException;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import sun.security.pkcs11.SunPKCS11;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.List;
import javax.crypto.Cipher;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.security.auth.x500.X500Principal;

public final class ClientConfigurationUtils {

    private static final Logger logger = LogManager.getLogger(ClientConfigurationUtils.class);

    private static final Provider sunPkcsProvider;
    private static final char[] userPin = "7526".toCharArray();

    static {
        Path configPath = Paths.get(System.getProperty("user.home"), "Workspace", "hsm", "pkcs11.cfg");
        logger.atInfo().kv("configPath", configPath).log("pkcs11 config path");
        sunPkcsProvider = new SunPKCS11(configPath.toString());
        if (Security.addProvider(sunPkcsProvider) == -1) {
            throw new RuntimeException("Could not configure SunPKCS11 provider");
        }
    }

    private ClientConfigurationUtils() {
    }

    /**
     * Get the greengrass service endpoint.
     *
     * @param deviceConfiguration    {@link DeviceConfiguration}
     * @return service end point
     */
    public static String getGreengrassServiceEndpoint(DeviceConfiguration deviceConfiguration) {
        IotSdkClientFactory.EnvironmentStage stage;
        try {
            stage = IotSdkClientFactory.EnvironmentStage
                    .fromString(Coerce.toString(deviceConfiguration.getEnvironmentStage()));
        } catch (InvalidEnvironmentStageException e) {
            logger.atError().setCause(e).log("Caught exception while parsing Nucleus args");
            throw new RuntimeException(e);
        }
        return RegionUtils.getGreengrassDataPlaneEndpoint(Coerce.toString(deviceConfiguration.getAWSRegion()), stage,
                Coerce.toInt(deviceConfiguration.getGreengrassDataPlanePort()));
    }

    /**
     * Configure the http client builder with the required certificates for the mutual auth connection.
     *
     * @param deviceConfiguration    {@link DeviceConfiguration}
     * @return configured http client
     */
    public static ApacheHttpClient.Builder getConfiguredClientBuilder(DeviceConfiguration deviceConfiguration) {
        ApacheHttpClient.Builder httpClient = ProxyUtils.getSdkHttpClientBuilder();

        try {
            configureClientMutualTLS(httpClient, deviceConfiguration);
        } catch (TLSAuthException e) {
            logger.atWarn("configure-greengrass-mutual-auth")
                    .log("Error during configure greengrass client mutual auth", e);
        }
        return httpClient;
    }

    private static void configureClientMutualTLS(ApacheHttpClient.Builder httpBuilder,
                                          DeviceConfiguration deviceConfiguration) throws TLSAuthException {
        String certificatePath = Coerce.toString(deviceConfiguration.getCertificateFilePath());
        String privateKeyPath = Coerce.toString(deviceConfiguration.getPrivateKeyFilePath());
        String rootCAPath = Coerce.toString(deviceConfiguration.getRootCAFilePath());
        if (Utils.isEmpty(certificatePath) || Utils.isEmpty(privateKeyPath) || Utils.isEmpty(rootCAPath)) {
            return;
        }

        TrustManager[] trustManagers = createTrustManagers(rootCAPath);
        KeyManager[] keyManagers = createKeyManagers(privateKeyPath, certificatePath);

        httpBuilder.tlsKeyManagersProvider(() -> keyManagers).tlsTrustManagersProvider(() -> trustManagers);
    }

    private static TrustManager[] createTrustManagers(String rootCAPath) throws TLSAuthException {
        try {
            List<X509Certificate> trustCertificates = EncryptionUtils.loadX509Certificates(rootCAPath);

            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(null, userPin);
            for (X509Certificate certificate : trustCertificates) {
                X500Principal principal = certificate.getSubjectX500Principal();
                String name = principal.getName("RFC2253");
                keyStore.setCertificateEntry(name, certificate);
            }
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("X509");
            trustManagerFactory.init(keyStore);
            return trustManagerFactory.getTrustManagers();
        } catch (GeneralSecurityException | IOException e) {
            throw new TLSAuthException("Failed to get trust manager", e);
        }
    }

    private static KeyManager[] createKeyManagers(String privateKeyPath, String certificatePath)
            throws TLSAuthException {
        try {
            //List<X509Certificate> certificateChain = EncryptionUtils.loadX509Certificates(certificatePath);

            //PrivateKey privateKey = EncryptionUtils.loadPrivateKey(privateKeyPath);

            KeyStore keyStore = KeyStore.getInstance("PKCS11", sunPkcsProvider);
            keyStore.load(null, userPin);
//            keyStore.setKeyEntry("private-key", privateKey, null, certificateChain.toArray(new Certificate[0]));
//            keyStore.store(null);
//            PrivateKey privateKey = (PrivateKey) keyStore.getKey("iotkey", userPin);
//            logger.atInfo().kv("privateKey", privateKey).log("private key in HSM");
//            PublicKey publicKey = keyStore.getCertificate("iotkey").getPublicKey();
//            logger.atInfo().kv("publicKey", publicKey).log("public key in HSM");
//            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding", sunPkcsProvider);
//            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
//            String text = "this is a plain text";
//            byte[] encrypted = cipher.doFinal(text.getBytes());
//            logger.atInfo().kv("encrypted", Base64.getEncoder().encodeToString(encrypted)).log("Encrypted text");
//            cipher.init(Cipher.DECRYPT_MODE, privateKey);
//            byte[] decrypted = cipher.doFinal(encrypted);
//            logger.atInfo().kv("decrypted", new String(decrypted)).log("Decrypted text");

            KeyManagerFactory keyManagerFactory =
                    KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, null);
            return keyManagerFactory.getKeyManagers();
        } catch (GeneralSecurityException | IOException e) {
            throw new TLSAuthException("Failed to get key manager", e);
        }
    }
}
