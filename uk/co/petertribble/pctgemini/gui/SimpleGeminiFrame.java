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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

/**
 * A Simplistic frame to access a Gemini server.
 */

public final class SimpleGeminiFrame extends JFrame implements ActionListener {

    private static final long serialVersionUID = 1L;

    /**
     * A menu item for Exit.
     */
    private JMenuItem exitItem;
    /**
     * A menu item for View Source.
     */
    private JMenuItem viewSourceItem;
    /**
     * The panel being displayed.
     */
    private GeminiPanel gpanel;

    /**
     * Create a new SimpleGeminiFrame, which is simply a wrapper around
     * a GeminiPanel that displays the content.
     *
     * @param url the initial url to be displayed
     */
    public SimpleGeminiFrame(String url) {
	super("Gemini");
	addWindowListener(new WindowExit());

	JMenu jmf = new JMenu("File");
	jmf.setMnemonic(KeyEvent.VK_F);
	viewSourceItem = new JMenuItem("View Source", KeyEvent.VK_U);
	viewSourceItem.addActionListener(this);
	jmf.add(viewSourceItem);
	jmf.addSeparator();
	exitItem = new JMenuItem("Exit", KeyEvent.VK_X);
	exitItem.addActionListener(this);
	jmf.add(exitItem);

	JMenuBar jm = new JMenuBar();
	jm.add(jmf);
	setJMenuBar(jm);

	gpanel = new GeminiPanel();
	add(gpanel);

        setSize(720, 600);
        setVisible(true);

	gpanel.loadPage(url);
    }

    class WindowExit extends WindowAdapter {
	@Override
	public void windowClosing(WindowEvent we) {
	    System.exit(0);
	}
    }

    /**
     * Pop up a window with the source of the currently diasplayed page.
     * Simply directs the panel to do the actual work.
     */
    public void showViewSource() {
	gpanel.viewSource();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
	if (exitItem.equals(e.getSource())) {
	    System.exit(0);
	}
	if (viewSourceItem.equals(e.getSource())) {
	    showViewSource();
	}
    }

    /**
     * Create a new SimpleGeminiFrame. If an argument is supplied it
     * will be used as the initial url to display.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
	if (args.length > 0) {
	    new SimpleGeminiFrame(args[0]);
	} else {
	    new SimpleGeminiFrame("gemini://geminiquickst.art/");
	}
    }
}
