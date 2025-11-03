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

package uk.co.petertribble.pctgemini.gui;

/**
 * Some static utility helper methods.
 */
public final class GeminiUtils {

    /*
     * This class cannot be instantiated.
     */
    private GeminiUtils() {
    }

    /**
     * Convert a String of type text/gemini to html.
     *
     * @param instring the text to be converted
     *
     * @return the text converted to html form
     */
    public static String geminiToHtml(final String instring) {
	StringBuilder sb = new StringBuilder(54);
	// toggle preformatted
	boolean toggle = false;
	sb.append("<html><body>\n");
	for (String s : instring.split("\n")) {
	    if (toggle) {
		// preformatted, as-is unless the preformatted block terminates
		if (s.startsWith("```")) {
		    toggle = false;
		} else {
		    sb.append(s).append('\n');
		}
	    } else {
		// not in a preformatted block
		if (s.startsWith("=>")) {
		    String s2 = s.substring(2).strip();
		    String[] ds = s2.split("\\s+", 2);
		    if (ds.length == 1) {
			sb.append("<p><a href=\"").append(ds[0]).append("\">")
			    .append(ds[0]).append("</a></p>\n");
		    } else if (ds.length == 2) {
			sb.append("<p><a href=\"").append(ds[0]).append("\">")
			    .append(ds[1]).append("</a></p>\n");
		    }
		} else if (s.startsWith("```")) {
		    sb.append("<pre>\n");
		    toggle = true;
		} else if (s.startsWith("###")) {
		    String s2 = s.substring(3).strip();
		    sb.append("<h3>").append(s2).append("</h3>");
		} else if (s.startsWith("##")) {
		    String s2 = s.substring(2).strip();
		    sb.append("<h2>").append(s2).append("</h2>");
		} else if (s.startsWith("#")) {
		    String s2 = s.substring(1).strip();
		    sb.append("<h1>").append(s2).append("</h1>");
		} else {
		    // elide blank lines as they would be a paragraph
		    if (!s.isBlank()) {
			sb.append("<p>")
			    .append(s.replace("<", "&lt;").replace(">", "&gt;"))
			    .append("</p>\n");
		    }
		}
	    }
	}
	sb.append("</body></html>\n");
	return sb.toString();
    }
}
