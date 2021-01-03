/*
 * Copyright (c) 2021 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.insight;

import de.neemann.digital.core.*;
import de.neemann.digital.core.element.*;
import de.neemann.digital.core.io.In;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.library.ElementNotFoundException;
import de.neemann.digital.draw.model.InverterConfig;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Insight factory used to create insights for two input combinatorial gates.
 */
public final class InsightTwoInputGate implements InsightFactory {
    /**
     * The instance to use
     */
    public static final InsightFactory INSTANCE = new InsightTwoInputGate();

    private static final NameEntry[] NAMES = new NameEntry[]{
            null,
            new NameEntry("ins5.dig", false),
            new NameEntry("ins6.dig", false),
            null,
            new NameEntry("ins6.dig", true),
            null,
            new NameEntry("ins7.dig", false),
            new NameEntry("ins1.dig", false),
            new NameEntry("ins2.dig", false),
            new NameEntry("ins8.dig", false),
            null,
            new NameEntry("ins3.dig", false),
            null,
            new NameEntry("ins3.dig", true),
            new NameEntry("ins4.dig", false),
            null,
    };

    private InsightTwoInputGate() {
    }

    @Override
    public Circuit createInsight(VisualElement ve, ElementLibrary library) {
        ElementAttributes attr = ve.getElementAttributes();
        if (attr.get(Keys.INPUT_COUNT) != 2 || attr.get(Keys.BITS) != 1)
            return null;

        try {
            ElementTypeDescription typeDescription = library.getElementType(ve.getElementName());
            Element n = typeDescription.createElement(ve.getElementAttributes());

            PinDescriptions inputs = typeDescription.getInputDescription(attr);
            if (inputs.size() != 2)
                return null;
            ObservableValues o = n.getOutputs();
            if (o.size() != 1)
                return null;

            ObservableValue out = o.get(0);

            InverterConfig ic = attr.get(Keys.INVERTER_CONFIG);

            ObservableValue in1 = new ObservableValue(inputs.get(0).getName(), 1);
            ObservableValue in2 = new ObservableValue(inputs.get(1).getName(), 1);

            n.setInputs(new ObservableValues(in1, in2));
            n.registerNodes(new Model());


            int index = 0;
            int mask = 1;
            for (int i = 0; i < 4; i++) {
                in1.setValue(invert(in1, ic, i >> 1));
                in2.setValue(invert(in2, ic, i));
                ((Node) n).readInputs();
                ((Node) n).writeOutputs();

                if (out.getValue() == 1) {
                    index |= mask;
                }
                mask *= 2;
            }


            NameEntry ne = NAMES[index];
            if (ne == null)
                return null;

            InputStream in = ClassLoader.getSystemResourceAsStream("insight/twoInputs/" + ne.getName());
            Circuit circuit = Circuit.loadCircuit(in, library.getShapeFactory());

            if (ne.getSwapInputs())
                swapInputs(circuit);

            return circuit;
        } catch (ElementNotFoundException | NodeException | PinException | IOException e) {
            return null;
        }
    }

    private static void swapInputs(Circuit c) {
        List<VisualElement> list = c.getElements(v -> v.equalsDescription(In.DESCRIPTION));
        String l1 = list.get(0).getElementAttributes().getLabel();
        String l2 = list.get(1).getElementAttributes().getLabel();
        list.get(0).getElementAttributes().set(Keys.LABEL, l2);
        list.get(1).getElementAttributes().set(Keys.LABEL, l1);
    }

    private long invert(ObservableValue in, InverterConfig ic, int val) {
        val = val & 1;
        if (ic.contains(in.getName()))
            return 1 - val;
        return val;
    }

    static final class NameEntry {
        private final String name;
        private final boolean swapInputs;

        NameEntry(String name, boolean swapInputs) {
            this.name = name;
            this.swapInputs = swapInputs;
        }

        public boolean getSwapInputs() {
            return swapInputs;
        }

        public String getName() {
            return name;
        }
    }

}
