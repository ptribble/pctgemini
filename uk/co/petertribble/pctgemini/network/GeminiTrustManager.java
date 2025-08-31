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

import java.security.cert.X509Certificate;
import javax.net.ssl.X509TrustManager;

/**
 * A basic TrustManager that accepts all certificates.
 *
 * Many Gemini servers don't use certificates issued by a public CA,
 * so don't validate them.
 *
 * In the future, implement TOFU and store the certificates we see
 * so they can be checked for changes.
 */
public final class GeminiTrustManager implements X509TrustManager {

    @Override
    public X509Certificate[] getAcceptedIssuers() {
	return null;
    }

    @Override
    public void checkClientTrusted(final X509Certificate[] chain,
				   final String authType) {
	// Nothing to do
    }

    @Override
    public void checkServerTrusted(final X509Certificate[] chain,
				   final String authType) {
	// Nothing to do
    }
}
