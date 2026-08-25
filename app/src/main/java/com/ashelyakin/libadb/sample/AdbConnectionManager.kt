package com.ashelyakin.libadb.sample

import android.os.Build
import android.sun.security.x509.AlgorithmId
import android.sun.security.x509.CertificateAlgorithmId
import android.sun.security.x509.CertificateExtensions
import android.sun.security.x509.CertificateIssuerName
import android.sun.security.x509.CertificateSerialNumber
import android.sun.security.x509.CertificateSubjectName
import android.sun.security.x509.CertificateValidity
import android.sun.security.x509.CertificateVersion
import android.sun.security.x509.CertificateX509Key
import android.sun.security.x509.KeyIdentifier
import android.sun.security.x509.PrivateKeyUsageExtension
import android.sun.security.x509.SubjectKeyIdentifierExtension
import android.sun.security.x509.X500Name
import android.sun.security.x509.X509CertImpl
import android.sun.security.x509.X509CertInfo
import com.ashelyakin.libadb.AbsAdbConnectionManager
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.SecureRandom
import java.security.cert.Certificate
import java.util.Date
import java.util.Random

class AdbConnectionManager(
    private val certSubjectString: String
): AbsAdbConnectionManager() {

    private var mPrivateKey: PrivateKey? = null
    private var mCertificate: Certificate? = null

    init {
        api = Build.VERSION.SDK_INT
        if (mPrivateKey == null) {
            // Generate a new key pair
            val keySize = 2048
            val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
            keyPairGenerator.initialize(keySize, SecureRandom.getInstance("SHA1PRNG"))
            val generateKeyPair = keyPairGenerator.generateKeyPair()
            val publicKey = generateKeyPair.public
            mPrivateKey = generateKeyPair.private
            // Generate a new certificate
            val subject = "CN=$certSubjectString"
            val algorithmName = "SHA512withRSA"
            val expiryDate = System.currentTimeMillis() + 86400000
            val certificateExtensions = CertificateExtensions()
            certificateExtensions["SubjectKeyIdentifier"] = SubjectKeyIdentifierExtension(
                KeyIdentifier(publicKey).identifier
            )
            val x500Name = X500Name(subject)
            val notBefore = Date()
            val notAfter = Date(expiryDate)
            certificateExtensions["PrivateKeyUsage"] =
                PrivateKeyUsageExtension(notBefore, notAfter)
            val certificateValidity = CertificateValidity(notBefore, notAfter)
            val x509CertInfo = X509CertInfo()
            x509CertInfo["version"] = CertificateVersion(2)
            x509CertInfo["serialNumber"] =
                CertificateSerialNumber(Random().nextInt() and Int.MAX_VALUE)
            x509CertInfo["algorithmID"] = CertificateAlgorithmId(AlgorithmId.get(algorithmName))
            x509CertInfo["subject"] = CertificateSubjectName(x500Name)
            x509CertInfo["key"] = CertificateX509Key(publicKey)
            x509CertInfo["validity"] = certificateValidity
            x509CertInfo["issuer"] = CertificateIssuerName(x500Name)
            x509CertInfo["extensions"] = certificateExtensions
            val x509CertImpl = X509CertImpl(x509CertInfo)
            x509CertImpl.sign(mPrivateKey, algorithmName)
            mCertificate = x509CertImpl
        }
    }

    override fun getPrivateKey(): PrivateKey {
        return mPrivateKey!!
    }

    override fun getCertificate(): Certificate {
        return mCertificate!!
    }

    override fun getDeviceName(): String {
        return certSubjectString
    }
}