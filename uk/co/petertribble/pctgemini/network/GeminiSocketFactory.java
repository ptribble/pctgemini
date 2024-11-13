/*
 * SPDX-License-Identifier: CDDL-1.0
 *
 * This file and its contents are supplied under the terms of the
 * Common Development and Distribution License ("CDDL"), version 1.0.
 * You may only use this file in accordance with the terms of version
 * 1.0 of the CDDL.
 *
 * A full copy of the text of the CDDL should have accompanied this
 * source. A copy of the CDDL is also available via the Internet at
 * http://www.illumos.org/license/CDDL.
 *
 * Copyright 2024  Peter C. Tribble
 */

package uk.co.petertribble.pctgemini.network;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.List;
import javax.net.ssl.SNIHostName;
import javax.net.ssl.SNIServerName;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.X509TrustManager;

/**
 * A customised SocketFactory appropriate for Gemini requests.
 */
public final class GeminiSocketFactory {

    /**
     * The singleton SocketFactory that will be configured.
     */
    private static SSLSocketFactory geminifactory;

    /*
     * This class cannot be instantiated
     */
    private GeminiSocketFactory() {
    }

    /*
     * Initialize the factory.
     */
    private static void initFactory() {
	try {
	    SSLContext sslContext = SSLContext.getInstance("TLS");
	    GeminiTrustManager gtm = new GeminiTrustManager();
	    X509TrustManager[] xtm = {gtm};
	    sslContext.init(null, xtm, new SecureRandom());
	    geminifactory = sslContext.getSocketFactory();
	} catch (NoSuchAlgorithmException | KeyManagementException e) {
	}
    }

    /*
     * Get the configured SocketFactory.
     *
     * @return the singleton SSLSocketFactory provided by this class
     */
    private static SSLSocketFactory getFactory() {
	if (geminifactory == null) {
	    initFactory();
	}
	return geminifactory;
    }

    /**
     * Create an SSLSocket using the configured SocketFactory. The Socket
     * returned will have the SNI parameter appropriately set.
     *
     * @param host the name of the host to connect to
     * @param port the port to connect to
     *
     * @throws IOException if the underlying socket creation fails
     *
     * @return an SSLSocket
     */
    public static SSLSocket getSocket(String host, int port)
		throws IOException {
	SSLSocket sslsock = (SSLSocket) getFactory().createSocket(host, port);
	SSLParameters params = new SSLParameters();
	List<SNIServerName> hlist = List.of(new SNIHostName(host));
	params.setServerNames(hlist);
	sslsock.setSSLParameters(params);
	return sslsock;
    }
}
