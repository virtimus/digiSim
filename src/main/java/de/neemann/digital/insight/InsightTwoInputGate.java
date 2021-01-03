/*
 * Copyright (c) 2021 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.insight;

import de.neemann.digital.core.*;
import de.neemann.digital.core.element.*;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.library.ElementNotFoundException;
import de.neemann.digital.draw.model.InverterConfig;

import java.io.IOException;
import java.io.InputStream;

/**
 * Insight factory used to create insights for two input combinatorial gates.
 */
public final class InsightTwoInputGate implements InsightFactory {
    /**
     * The instance to use
     */
    public static final InsightFactory INSTANCE = new InsightTwoInputGate();

    private static final String[] NAMES = new String[]{
            null,
            "ins5.dig",
            "ins6.dig",
            null,
            "ins9.dig",
            null,
            "ins7.dig",
            "ins1.dig",
            "ins2.dig",
            "ins8.dig",
            null,
            "ins3.dig",
            null,
            "ins10.dig",
            "ins4.dig",
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


            String name = NAMES[index];
            if (name == null)
                return null;

            InputStream in = ClassLoader.getSystemResourceAsStream("insight/twoInputs/" + name);
            return Circuit.loadCircuit(in, library.getShapeFactory());
        } catch (ElementNotFoundException | NodeException | PinException | IOException e) {
            return null;
        }
    }

    private long invert(ObservableValue in, InverterConfig ic, int val) {
        val = val & 1;
        if (ic.contains(in.getName()))
            return 1 - val;
        return val;
    }

}
