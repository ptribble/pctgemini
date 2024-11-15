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

import uk.co.petertribble.pctgemini.gui.GeminiUtils;
/**
 * Simple test of a gemini request.
 */
public final class GeminiExample {

    /*
     * This class cannot be instantiated.
     */
    private GeminiExample() {
    }

    /**
     * Test the network code by performing a simple request
     * and printing the response.
     *
     * @param args ignored
     */
    public static void main(String[] args) {
	String sreq = (args.length > 0) ? args[0]
	    : "gemini://geminiprotocol.net/docs/tech-overview.gmi";
	GeminiRequest greq = new GeminiRequest(sreq);
	greq.doConnect();
	GeminiResponse gresp = greq.getResponse();
	System.out.println(gresp.minorCode());
	System.out.println(gresp.metaText());
	System.out.println(gresp.bodyAsString());
	System.out.println(GeminiUtils.geminiToHtml(gresp.bodyAsString()));
    }
}
