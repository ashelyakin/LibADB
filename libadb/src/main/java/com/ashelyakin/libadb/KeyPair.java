package com.ashelyakin.libadb;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;

import javax.security.auth.DestroyFailedException;

final class KeyPair {
    private final PrivateKey mPrivateKey;
    private final Certificate mCertificate;

    public KeyPair(PrivateKey privateKey, Certificate certificate) {
        mPrivateKey = privateKey;
        mCertificate = certificate;
    }

    public PrivateKey getPrivateKey() {
        return mPrivateKey;
    }

    public PublicKey getPublicKey() {
        return mCertificate.getPublicKey();
    }

    public Certificate getCertificate() {
        return mCertificate;
    }

    public void destroy() throws DestroyFailedException {
        try {
            mPrivateKey.destroy();
        } catch (NoSuchMethodError ignore) {
        }
    }
}
