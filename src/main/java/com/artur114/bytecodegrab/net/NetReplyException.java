package com.artur114.bytecodegrab.net;

import org.apache.logging.log4j.Logger;

public class NetReplyException extends RuntimeException {
    private final String expected;
    private final String reply;

    public NetReplyException(String expected, String reply) {
        super(String.format("Reply is invalid, expected %s, got: %s", expected, reply));

        this.expected = expected;
        this.reply = reply;
    }

    public void log(Logger logger) {
        logger.error("Reply is invalid, expected {}, got: {}", this.expected, this.reply);
    }
}
