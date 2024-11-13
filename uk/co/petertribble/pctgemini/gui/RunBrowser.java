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

package uk.co.petertribble.pctgemini.gui;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * Open a URL in a browser.
 */
public class RunBrowser {

    /**
     * The name of the first browser we find.
     */
    private static String browserbin;
    /*
     * We search all the following locations for a browser.
     */
    static {
	String[] xsearch = {"/usr/bin/palemoon",
			    "/usr/bin/firefox",
			    "/usr/bin/dillo",
			    "/usr/bin/netsurf",
			    "/usr/bin/xdg-open"};

	for (String s : xsearch) {
	    if (new File(s).exists()) {
		browserbin = s;
		break;
	    }
	}
    }

    /**
     * Open a URL in a browser.
     *
     * @param url The URL to open
     */
    public RunBrowser(URL url) {
	try {
	    Desktop.getDesktop().browse(url.toURI());
	} catch (Exception e) { //NOPMD
	    try {
		if (browserbin != null) {
		    String[] fullcmd = {browserbin, url.toString()};
		    Runtime.getRuntime().exec(fullcmd);
		}
	    } catch (IOException ioe) { }
	}
    }
}
