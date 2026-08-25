package com.ashelyakin.libadb;

/**
 * Thrown when the ADB daemon rejects our initial authentication attempt, which typically means that the peer has not
 * previously saved our public key.
 */
public class AdbAuthenticationFailedException extends RuntimeException {
    public AdbAuthenticationFailedException() {
        super("Initial authentication attempt rejected by peer.");
    }
}
