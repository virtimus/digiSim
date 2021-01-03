/*
 * Copyright (c) 2021 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.insight;

import de.neemann.digital.core.basic.Not;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.io.In;
import de.neemann.digital.core.wiring.Clock;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.draw.elements.Wire;
import de.neemann.digital.draw.graphics.Vector;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.model.InverterConfig;
import de.neemann.digital.draw.shapes.ShapeFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static de.neemann.digital.draw.shapes.GenericShape.SIZE;

/**
 * A insight factory which uses a simple file to create an insight.
 */
public class InsightSimple implements InsightFactory {

    /**
     * True if there is one data bit
     */
    public static final Condition ONEBIT = attr -> attr.getBits() == 1;

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
    private boolean unconditional;

    /**
     * Creates a new instance
     */
    public InsightSimple() {
        entries = new ArrayList<>();
        unconditional = false;
    }

    /**
     * Creates a new instance with a unconditional circuit
     *
     * @param name unconditional circuit
     */
    public InsightSimple(String name) {
        this();
        add(name);
    }

    /**
     * Adds a circuit
     *
     * @param factory   the factory th creatre the circuit
     * @param condition the condition to use this circuit
     * @return this for chained calls
     */
    public InsightSimple add(InsightFactory factory, Condition condition) {
        if (unconditional)
            throw new RuntimeException("there is already a unconditional circuit");
        entries.add(new Entry(factory, condition));
        if (condition == null)
            unconditional = true;
        return this;
    }

    /**
     * Adds a unconditional factory
     *
     * @param factory the factory to create a circuit
     * @return this for chained calls
     */
    public InsightSimple add(InsightFactory factory) {
        return add(factory, null);
    }

    /**
     * Adds a circuit
     *
     * @param name      the name of the circuit
     * @param condition the condition to use this circuit
     * @return this for chained calls
     */
    public InsightSimple add(String name, Condition condition) {
        add(new InsightFactoryLoad(name), condition);
        return this;
    }

    /**
     * Adds a unconditional circuit
     *
     * @param name the name of the circuit
     * @return this for chained calls
     */
    public InsightSimple add(String name) {
        return add(name, null);
    }

    @Override
    public Circuit createInsight(VisualElement ve, ElementLibrary library) {
        for (Entry entry : entries)
            if (entry.isMet(ve))
                return entry.factory.createInsight(ve, library);
        return null;
    }

    private static Circuit invertInputs(Circuit circuit, InverterConfig ic, ShapeFactory shapeFactory) {
        List<VisualElement> list = circuit.getElements(v -> v.equalsDescription(In.DESCRIPTION) || v.equalsDescription(Clock.DESCRIPTION));
        for (VisualElement in : list) {
            String label = in.getElementAttributes().getLabel();
            if (ic.contains(label))
                addInverter(in, circuit, shapeFactory);
        }
        return circuit;
    }

    private static void addInverter(VisualElement in, Circuit circuit, ShapeFactory shapeFactory) {
        Vector pos = in.getPos();
        in.setPos(new Vector(pos.x - SIZE * 3, pos.y));
        circuit.add(new VisualElement(Not.DESCRIPTION.getName())
                .setAttribute(Keys.BITS, in.getElementAttributes().getBits())
                .setPos(new Vector(pos.x - SIZE * 2, pos.y))
                .setShapeFactory(shapeFactory));
        circuit.add(new Wire(new Vector(pos.x - SIZE * 3, pos.y), new Vector(pos.x - SIZE * 2, pos.y)));
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
        boolean isMet(ElementAttributes ve);
    }

    private static final class Entry {
        private final Condition condition;
        private final InsightFactory factory;

        private Entry(InsightFactory factory, Condition condition) {
            this.factory = factory;
            this.condition = condition;
        }

        public boolean isMet(VisualElement ve) {
            if (condition == null)
                return true;
            else
                return condition.isMet(ve.getElementAttributes());
        }
    }

    private static final class InsightFactoryLoad implements InsightFactory {
        private final String name;

        private InsightFactoryLoad(String name) {
            this.name = name;
        }

        @Override
        public Circuit createInsight(VisualElement ve, ElementLibrary library) {
            InputStream in = ClassLoader.getSystemResourceAsStream("insight/simple/" + name);
            if (in == null)
                throw new NullPointerException("file " + name + " not found!");
            try {
                return invertInputs(Circuit.loadCircuit(in, library.getShapeFactory()), ve.getElementAttributes().get(Keys.INVERTER_CONFIG), library.getShapeFactory());
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}
