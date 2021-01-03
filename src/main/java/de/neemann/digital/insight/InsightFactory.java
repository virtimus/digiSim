/*
 * Copyright (c) 2021 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.insight;

import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.draw.library.ElementLibrary;

/**
 * Used to create an insight of a given element
 */
public interface InsightFactory {

    /**
     * Creates an insight circuit or null if not possible
     *
     * @param ve      the visual element
     * @param library the library used
     * @return the insight circuit
     */
    Circuit createInsight(VisualElement ve, ElementLibrary library);
}
