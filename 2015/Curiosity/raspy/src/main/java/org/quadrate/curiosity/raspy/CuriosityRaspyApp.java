package org.quadrate.curiosity.raspy;

import org.apache.commons.cli.*;
import org.quadrate.curiosity.raspy.serviceFactory.DefaultServiceFactory;
import org.quadrate.curiosity.raspy.serviceFactory.ServiceFactory;

import java.io.PrintWriter;
import java.util.Comparator;

import static org.quadrate.curiosity.CuriosityConstants.*;

public class CuriosityRaspyApp {
    private static final Class<? extends ServiceFactory> DEFAULT_SERVICE_FACTORY_CLASS = DefaultServiceFactory.class;

    private static final Options OPTIONS = new Options();

    private static final OptionEx NAME_OPTION;
    private static final OptionEx OPERATOR_PASSWORD_OPTION;
    private static final OptionEx OBSERVER_PASSWORD_OPTION;
    private static final OptionEx HOST_OPTION;
    private static final OptionEx CONTROL_PORT_OPTION;
    private static final OptionEx VIDEO_PORT_OPTION;
    private static final OptionEx AUDIO_PORT_OPTION;
    private static final OptionEx SERVICE_FACTORY_OPTION;

    static {
        NAME_OPTION = addOption("n", "name", "Name of robot", true, false);
        OPERATOR_PASSWORD_OPTION = addOption("pw", "oprpassw", "Operator password", true, false);
        OBSERVER_PASSWORD_OPTION = addOption("bw", "obspassw", "Observer password", true, false);
        HOST_OPTION = addOption("h", "host", "Bind IP address", true, false);
        CONTROL_PORT_OPTION = addOption("cp", "ctrlport", "Control port number. Default " + DEFAULT_CONTROL_PORT_NUMBER, false, false);
        VIDEO_PORT_OPTION = addOption("vp", "videoport", "Video port number. Default " + DEFAULT_VIDEO_PORT_NUMBER, false, false);
        AUDIO_PORT_OPTION = addOption("ap", "audioport", "Audio port number. Default " + DEFAULT_AUDIO_PORT_NUMBER, false, false);
        SERVICE_FACTORY_OPTION = addOption("sf", "srvfactory", "Service factory class. Default '" + DEFAULT_SERVICE_FACTORY_CLASS.getName() + "'", false, false);
    }

    public static void main(final String[] args) {
        final RaspyConfig config = new RaspyConfig();

        final Class<? extends ServiceFactory> serviceFactoryClass;

        try {
            final CommandLine cmdLine = new PosixParser().parse(OPTIONS, args);

            config.setName(NAME_OPTION.getValue(cmdLine));
            config.setOperatorPassword(OPERATOR_PASSWORD_OPTION.getValue(cmdLine));
            config.setObserverPassword(OBSERVER_PASSWORD_OPTION.getValue(cmdLine));
            config.setHost(HOST_OPTION.getValue(cmdLine));
            config.setControlPort(CONTROL_PORT_OPTION.getValue(cmdLine, Integer.class, DEFAULT_CONTROL_PORT_NUMBER));
            config.setVideoPort(VIDEO_PORT_OPTION.getValue(cmdLine, Integer.class, DEFAULT_VIDEO_PORT_NUMBER));
            config.setAudioPort(AUDIO_PORT_OPTION.getValue(cmdLine, Integer.class, DEFAULT_AUDIO_PORT_NUMBER));

            serviceFactoryClass = SERVICE_FACTORY_OPTION.getValue(cmdLine, Class.class, DEFAULT_SERVICE_FACTORY_CLASS);
        } catch(final ParseException exception) {
            final HelpFormatter helpFormatter = new HelpFormatter();

            helpFormatter.setOptionComparator(new Comparator<OptionEx>() {
                @Override
                public int compare(OptionEx o1, OptionEx o2) {
                    return o1.compareTo(o2);
                }
            });

            System.out.println("usage: raspy [options]");
            helpFormatter.printOptions(new PrintWriter(System.out, true), 80, OPTIONS, 1, 3);

            throw new RuntimeException("Incorrect program options are specified", exception);
        }

        final ServiceFactory serviceFactory;

        try {
            serviceFactory = serviceFactoryClass.newInstance();
        } catch (final ReflectiveOperationException exception) {
            throw new RuntimeException("Failed to create service factory instance of '" + serviceFactoryClass.getName() + "'", exception);
        }

        final RaspyServer raspyServer = new RaspyServer(config, serviceFactory);

        try {
            raspyServer.start();
        } catch (final Exception exception) {
            throw new RuntimeException("RaspyServer failure", exception);
        }
    }

    private static OptionEx addOption(final String opt, final String longOpt, final String description, boolean required, final Boolean optionalArg) {
        final OptionEx option = new OptionEx(opt, longOpt, description, required, optionalArg);

        OPTIONS.addOption(option);

        return option;
    }
}
