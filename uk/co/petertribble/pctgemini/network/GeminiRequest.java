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
 * Copyright 2025  Peter C. Tribble
 */

package uk.co.petertribble.pctgemini.network;

import java.net.URI;
import java.net.URISyntaxException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import javax.net.ssl.SSLSocket;

/**
 * Make a request to a gemini server.
 */
public class GeminiRequest {

    /**
     * Status: created but not running.
     */
    public static final int STAT_INIT = 1;
    /**
     * Status: request is active.
     */
    public static final int STAT_ACTIVE = 2;
    /**
     * Status: request succeeded. Note that this refers to the request layer,
     * if a valid response was received it may contain a failure code.
     */
    public static final int STAT_SUCCESS = 3;
    /**
     * Status: request failed. Note that this refers to the request layer,
     * if a valid response was received it may contain a failure code.
     */
    public static final int STAT_FAIL = 4;

    /**
     * The default Gemini port is 1965.
     */
    private static final int GEMINI_PORT = 1965;

    /**
     * The size of the incoming byte buffer.
     */
    private static final int INBUFSIZE = 32_768;
    /**
     * Request status, should be one of the STAT codes above.
     */
    private int status;
    /**
     * Request error, if any.
     */
    private String statusMsg;
    /**
     * The response to this request.
     */
    private GeminiResponse response;
    /**
     * The requested url.
     */
    private String gurl;
    /**
     * A backing url used to parse the requested url.
     */
    private URI backurl;

    /**
     * Set up a request to the given URL.
     *
     * @param url the URL to be retrieved.
     */
    public GeminiRequest(String url) {
	gurl = url;
	status = STAT_INIT;
    }

    /**
     * Initiate the connection.
     */
    public void doConnect() {
	// setup
	String host = getHost();
	int port = getPort();
	status = STAT_ACTIVE;
	/*
	 * The convenience method GeminiSocketFactory.getSocket() is
	 * called here, which returns a configured socket that's had
	 * setSSLParameters() invoked on it already, as we can't do that
	 * manipulation inside the try-with-resources block.
	 */
	try (SSLSocket sslsock = GeminiSocketFactory.getSocket(host, port);
	     InputStream instream = sslsock.getInputStream();
	     OutputStream outstream = sslsock.getOutputStream();
	     PrintStream reqstream = new PrintStream(outstream); ) {
	    // the spec says terminate with <CR><LF> so be explicit
	    reqstream.print(gurl + "\r\n");
	    reqstream.flush();
	    /*
	     * Now we read what we get back, but we have to do it in 2
	     * parts. We first read the header, which is a single line of
	     * text terminated with <CR><LF> (although we actually terminate
	     * at the \n and skip any \r). Then we read the body as a byte
	     * array because it could be any mime type.
	     */
	    int i = instream.read();
	    StringBuilder hbuf = new StringBuilder();
	    while (i != -1) {
		char c = (char) i;
		if (c == '\n') {
		    break;
		}
		if (c != '\r') {
		    hbuf.append(c);
		}
		i = instream.read();
	    }
	    // once we have a header, create the response object
	    response = new GeminiResponse(hbuf.toString());
	    // and only read the body if we have a 2x code
	    if (response.hasBody()) {
		byte[] b = readBody(instream);
		response.addBody(b);
	    }
	    status = STAT_SUCCESS;
	} catch (IOException ioe) {
	    // indicate a failure at the connection level
	    status = STAT_FAIL;
	    statusMsg = ioe.getMessage();
	}
    }

    /*
     * Break the URL string into parts.
     *
     * protocol://host[:port]/path...
     *
     * We cheat by rewriting the protocol from gemini which isn't
     * supported to http which is. This works fine as we're only interested
     * in parsing it.
     */
    private void setBackURL() {
	if (backurl == null) {
	    try {
		backurl = new URI(gurl.replace("gemini://", "http://"));
	    } catch (URISyntaxException mue) {
	    }
	}
    }

    private String getHost() {
	setBackURL();
	return backurl.getHost();
    }

    private int getPort() {
	setBackURL();
	int nport = backurl.getPort();
	return nport == -1 ? GEMINI_PORT : nport;
    }

    /*
     * Read the rest of the data into a byte array.
     */
    private byte[] readBody(InputStream instream) {
	byte[] inbuf = new byte[INBUFSIZE];
	int nread;
	ByteArrayOutputStream outbuf = new ByteArrayOutputStream();
	try {
	    while ((nread = instream.read(inbuf)) != -1) {
		outbuf.write(inbuf, 0, nread);
	    }
	} catch (IOException ioe) { }
	return outbuf.toByteArray();
    }

    /**
     * Get the current status of this request.
     *
     * @return an int describing the current status of this GeminiRequest
     */
    public int getStatus() {
	return status;
    }

    /**
     * Describe the current status of this request.
     *
     * @return a String describing the current status of this GeminiRequest
     */
    public String getStatusMsg() {
	return statusMsg;
    }

    /**
     * Get the response to this request. It will only be correctly filled in
     * once the request has succeeded.
     *
     * @return the response to this request
     */
    public GeminiResponse getResponse() {
	return response;
    }
}
