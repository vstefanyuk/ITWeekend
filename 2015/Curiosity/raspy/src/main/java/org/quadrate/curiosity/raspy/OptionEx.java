package org.quadrate.curiosity.raspy;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class OptionEx extends Option implements Comparable {
    private static final AtomicInteger NEXT_ORDINAL = new AtomicInteger();

    private final int ordinal;

    public OptionEx(final String opt, final String longOpt, final String description, final boolean required, final boolean hasArg) throws IllegalArgumentException {
        super(opt, longOpt, hasArg, description);

        setRequired(required);

        ordinal = NEXT_ORDINAL.getAndIncrement();
    }

    @Override
    public int compareTo(Object o) {
        return ordinal - ((OptionEx)o).ordinal;
    }

    public String getValue(final CommandLine cmdLine) {
        Objects.requireNonNull(cmdLine);

        return cmdLine.getOptionValue(getOpt());
    }

    public String getValue(final CommandLine cmdLine, final String defValue) {
        Objects.requireNonNull(cmdLine);
        Objects.requireNonNull(defValue);

        final String value = getValue(cmdLine);

        return value != null ? value : defValue;
    }

    public <T> T getValue(final CommandLine cmdLine, final Class<T> valueType) {
        Objects.requireNonNull(cmdLine);
        Objects.requireNonNull(valueType);

        final String strValue = cmdLine.getOptionValue(getOpt());

        return convertValue(strValue, valueType);
    }

    public <T> T getValue(final CommandLine cmdLine, final Class<T> valueType, final T defValue) {
        Objects.requireNonNull(cmdLine);
        Objects.requireNonNull(valueType);
        Objects.requireNonNull(defValue);

        final T value = getValue(cmdLine, valueType);

        return value != null ? value : defValue;
    }

    private <T> T convertValue(final String strValue, final Class<T> valueType) {
        Objects.requireNonNull(valueType);

        T value = null;

        if (strValue != null) {
            if (String.class.equals(valueType)) {
                value = (T)strValue;
            } else {
                try {
                    if (Class.class.equals(valueType)) {
                        value = (T)Class.forName(strValue);
                    } else {
                        final Method valueOfMethod = valueType.getMethod("valueOf", String.class);

                        final Class<?> returnType = valueOfMethod.getReturnType();

                        if (!valueType.equals(returnType)) {
                            throw new Exception("ValueOf method returns \"" + returnType + "\" instead of \"" + valueType + "\"");
                        }

                        return (T)valueOfMethod.invoke(null, strValue);
                    }
                } catch(final Exception exception) {
                    throw new RuntimeException("Can't convert '" + strValue + "' to \"" + valueType + "\"", exception);
                }
            }
        }

        return value;
    }
}
