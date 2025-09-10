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
 * Copyright 2024 Peter C. Tribble
 */

package uk.co.petertribble.pctgemini.network;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.IOException;

/**
 * Contains a response from a gemini server.
 */
public class GeminiResponse {

    /**
     * Result code 1 - need more input.
     */
    public static final int RES_NEEDMORE = 1;
    /**
     * Result code 2 - success.
     */
    public static final int RES_SUCCESS = 2;
    /**
     * Result code 3 - redirect.
     */
    public static final int RES_REDIRECT = 3;
    /**
     * Result code 4 - temporary failure.
     */
    public static final int RES_TEMPFAIL = 4;
    /**
     * Result code 5 - permanent failure.
     */
    public static final int RES_PERMFAIL = 5;
    /**
     * Result code 6 - client certificate required.
     */
    public static final int RES_NEEDCERT = 6;
    /**
     * Denotes if the response we have is valid (specifically, set to true
     * if we have a valid response code).
     */
    private boolean valid;
    /**
     * Holds the short single-digit response code.
     */
    private int rescode1;
    /**
     * Holds the full two-digit response code.
     */
    private int rescode2;
    /**
     * Holds the remainder of the header line that follows the response code.
     */
    private String metastring;
    /**
     * Holds body of the response, if there is one.
     */
    private byte[] body;

    /**
     * Create a populated GeminiResponse object, consisting of the header
     * line and optional body.
     *
     * @param header a 1-line String containing the response header
     */
    public GeminiResponse(final String header) {
	parseheader(header);
    }

    /**
     * Add the body content, if appropriate. This should only be called
     * from GeminiRequest.
     *
     * @param inbody the body of the response
     */
    protected void addBody(final byte[] inbody) {
	body = inbody; //NOPMD
    }

    /*
     * Parse the header line. It's expected to be of the form
     *
     * <STATUS><SPACE><META><CR><LF>
     *
     * so we just break it in 2.
     */
    private void parseheader(final String header) {
	String[] headers = header.split("\\s+", 2);
	// status is the first word, parse that
	try {
	    rescode2 = Integer.parseInt(headers[0]);
	} catch (NumberFormatException e) {
	    rescode2 = 0;
	}
	// check for validity
	if (rescode2 > 10 && rescode2 < 70) {
	    valid = true;
	    if (rescode2 >= 60) {
		rescode1 = RES_NEEDCERT;
	    } else if (rescode2 >= 50) {
		rescode1 = RES_PERMFAIL;
	    } else if (rescode2 >= 40) {
		rescode1 = RES_TEMPFAIL;
	    } else if (rescode2 >= 30) {
		rescode1 = RES_REDIRECT;
	    } else if (rescode2 >= 20) {
		rescode1 = RES_SUCCESS;
	    } else if (rescode2 >= 10) {
		rescode1 = RES_NEEDMORE;
	    }
	}
	metastring = headers.length > 1 ? headers[1] : "";
    }

    /**
     * Get whether this is a valid response. A valid response will start
     * with a 2 digit number in the range 1x to 6x.
     *
     * @return true if the response is valid
     */
    public boolean isValid() {
	return valid;
    }

    /**
     * Get whether this response should contain a body.
     *
     * @return true if the response is expected to have a body
     */
    public boolean hasBody() {
	return rescode1 == RES_SUCCESS;
    }

    /**
     * Get the response body as a String.
     *
     * @return the response body as a String.
     */
    public String bodyAsString() {
	if (body == null) {
	    return "";
	}
	String s = "";
	InputStreamReader isr = new InputStreamReader(
					     new ByteArrayInputStream(body));
	try {
	    int l = body.length;
	    char[] cc = new char[l];
	    isr.read(cc, 0, l);
	    s = new String(cc);
	} catch (IOException e) { }
	return s;
    }

    /**
     * Get the major 1-digit response code. The valid codes are:
     * 1 - need more input
     * 2 - success
     * 3 - redirect
     * 4 - temporary failure
     * 5 - permanent failure
     * 6 - client certificate required
     *
     * @return the single-digit response code
     */
    public int majorCode() {
	return rescode1;
    }

    /**
     * Get the full 2-digit response code.
     *
     * @return the full two-digit response code
     */
    public int minorCode() {
	return rescode2;
    }

    /**
     * Get the meta text. This is anything on the response line after the
     * initial two-digit response code.
     *
     * @return a String containing the meta text from the header
     */
    public String metaText() {
	return metastring.strip();
    }
}
