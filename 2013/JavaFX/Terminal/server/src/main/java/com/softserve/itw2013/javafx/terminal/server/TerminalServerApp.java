package com.softserve.itw2013.javafx.terminal.server;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created with IntelliJ IDEA.
 * User: V.Stefanyuk
 * Date: 22.10.13
 * Time: 1:18
 * To change this template use File | Settings | File Templates.
 */
public class TerminalServerApp {
    public static void main(final String[] args) throws Exception {
        new ClassPathXmlApplicationContext("/application-context.xml");

        System.out.println("Terminal server application is running. Press Ctrl-C to shutdown...");

        Thread.sleep(Long.MAX_VALUE);
    }
}
