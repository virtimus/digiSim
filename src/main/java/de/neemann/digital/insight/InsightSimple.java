/*
 * Copyright (c) 2021 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.insight;

import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.draw.library.ElementLibrary;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * A insight factory which uses a simple file to create an insight.
 */
public class InsightSimple implements InsightFactory {

    /**
     * True if there is one data bit
     */
    public static final Condition ONEBIT = ve -> ve.getElementAttributes().getBits() == 1;

    /**
     * A AND condition
     *
     * @param conditions the conditions
     * @return true if all given conditions are met
     */
    public static Condition and(Condition... conditions) {
        return ve -> {
            for (Condition c : conditions)
                if (!c.isMet(ve))
                    return false;
            return true;
        };
    }

    private final ArrayList<Entry> entries;

    /**
     * Creates a new instance
     */
    public InsightSimple() {
        entries = new ArrayList<>();
    }

    /**
     * Adds a circuit
     *
     * @param name      the name of the circuit
     * @param condition the condition to use this circuit
     * @return this for chained calls
     */
    public InsightSimple add(String name, Condition condition) {
        entries.add(new Entry(name, condition));
        return this;
    }

    @Override
    public Circuit createInsight(VisualElement ve, ElementLibrary library) {
        for (Entry entry : entries)
            if (entry.isMet(ve)) {
                InputStream in = ClassLoader.getSystemResourceAsStream("insight/simple/" + entry.name);
                try {
                    return Circuit.loadCircuit(in, library.getShapeFactory());
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        return null;
    }

    /**
     * Used to check if a visual element meets a certain condition
     */
    public interface Condition {
        /**
         * Condition for a visual element
         *
         * @param ve the visual element
         * @return true if the visual element mets the condition
         */
        boolean isMet(VisualElement ve);
    }

    private static final class Entry {
        private final String name;
        private final Condition condition;

        private Entry(String name, Condition condition) {
            this.name = name;
            this.condition = condition;
        }

        public boolean isMet(VisualElement ve) {
            return condition.isMet(ve);
        }
    }
}
