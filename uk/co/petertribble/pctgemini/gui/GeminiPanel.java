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

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import uk.co.petertribble.pctgemini.network.GeminiRequest;
import uk.co.petertribble.pctgemini.network.GeminiResponse;

/**
 * A Simplistic panel to access a Gemini server.
 */

public final class GeminiPanel extends JEditorPane
    implements ActionListener, HyperlinkListener {

    private static final long serialVersionUID = 1L;

    /**
     * A JLabel with the name of the page being displayed.
     */
    private JLabel curLabel;
    /**
     * A JButton to go back to the previous page.
     */
    private JButton backButton;
    /**
     * The JEditorPane with the page content.
     */
    private JEditorPane jep;
    /**
     * Save the URL to resolve relative links.
     */
    private String surl;
    /**
     * List of opened pages for history.
     */
    private List<String> historyList = new ArrayList<>();
    /**
     * Cache of pages we might go back to.
     */
    private Map<String, GeminiResponse> pageCache = new HashMap<>();

    /**
     * Create a basic panel to display Gemini content.
     */
    public GeminiPanel() {
	setLayout(new BorderLayout());

	JToolBar jtb = new JToolBar();
	jtb.setFloatable(false);
	jtb.setRollover(true);
	backButton = new JButton("<");
	backButton.addActionListener(this);
	curLabel = new JLabel();
	jtb.add(backButton);
	jtb.add(curLabel);

	jep = new JEditorPane();
	jep.setContentType("text/html");
	jep.addHyperlinkListener(this);

	add(jtb, BorderLayout.PAGE_START);
	add(new JScrollPane(jep), BorderLayout.CENTER);
    }

    /**
     * Load a new url into the panel.
     *
     * @param url the new url to show
     */
    public void loadPage(String url) {
	surl = url;
	/*
	 * If the page is in the cache, load from there. This is only
	 * used in the case we go back, as that clears the cache.
	 */
	GeminiResponse gresp = pageCache.get(url);
	if (gresp != null) {
	    loadPage(url, gresp);
	} else {
	    GeminiRequest greq = new GeminiRequest(url);
	    greq.doConnect();
	    if (greq.getStatus() == GeminiRequest.STAT_SUCCESS) {
		gresp = greq.getResponse();
		loadPage(url, gresp);
	    } else {
		curLabel.setText("Connection failed");
	    }
	}
    }

    /*
     * Display a response in the panel.
     */
    private void loadPage(String url, GeminiResponse gresp) {
	if ("text/gemini".equals(gresp.metaText())) {
	    jep.setContentType("text/html");
	    jep.setText(GeminiUtils.geminiToHtml(gresp.bodyAsString()));
	} else {
	    jep.setText(gresp.bodyAsString());
	}
	jep.setMargin(new Insets(5, 5, 5, 5));
	jep.setCaretPosition(0);
	jep.setEditable(false);
	curLabel.setText(url);
	historyList.add(url);
	pageCache.put(url, gresp);
	backButton.setEnabled(historyList.size() > 1);
    }

    /*
     * Go back to the previous page in the history, if any.
     * Remove the current page from the history and the cache,
     * remove the previous page from the history but not the cache,
     * then call loadPage() which will add it back to the history.
     */
    private void goBack() {
	int s = historyList.size();
	if (s > 1) {
	    historyList.remove(s - 1);
	    pageCache.remove(surl);
	    String sold = historyList.get(s - 2);
	    historyList.remove(s - 2);
	    loadPage(sold);
	}
    }

    @Override
    public void actionPerformed(ActionEvent e) {
	goBack();
    }

    /*
     * Take the link text and normalize it into fully qualified form.
     */
    private String normalizeLink(String ilink) {
	/*
	 * Already fully qualified.
	 */
	if (ilink.indexOf("://") != -1) {
	    return ilink;
	}
	/*
	 * Full, but missing the protocol, assume for now it's a gemini
	 * request as those are the only ones handled internally.
	 */
	if (ilink.startsWith("//")) {
	    return "gemini:" + ilink;
	}
	/*
	 * no protocol in the url, must be relative
	 */
	if (surl.endsWith("/")) {
	    return surl + ilink;
	} else {
	    return surl.substring(0, surl.lastIndexOf('/') + 1) + ilink;
	}
    }

    /*
     * Try and visit the requested link. If it's a gemini link, we open
     * it here. If not we fail and let something else handle it.
     */
    private boolean gotoLink(String s) {
	if (s.startsWith("gemini://")) {
	    loadPage(s);
	    return true;
	} else {
	    return false;
	}
    }

    @Override
    public void hyperlinkUpdate(HyperlinkEvent ev) {
	if (ev.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
	    if (!gotoLink(normalizeLink(ev.getDescription()))) {
		// not gemini, punt to external
		if (ev.getURL() != null && Desktop.isDesktopSupported()) {
		    new RunBrowser(ev.getURL());
		}
	    }
	}
    }
}
