/*
 * Carrot2 project.
 *
 * Copyright (C) 2002-2008, Dawid Weiss, Stanisław Osiński.
 * Portions (C) Contributors listed in "carrot2.CONTRIBUTORS" file.
 * All rights reserved.
 *
 * Refer to the full license file "carrot2.LICENSE"
 * in the root folder of the repository checkout or at:
 * http://www.carrot2.org/carrot2.LICENSE
 */

package org.carrot2.text.preprocessing.filter;

import org.carrot2.core.attribute.Processing;
import org.carrot2.text.preprocessing.PreprocessingContext;
import org.carrot2.util.attribute.*;
import org.carrot2.util.attribute.constraint.DoubleRange;

/**
 * A filter that removes "incomplete" labels.
 * <p>
 * See <a href="http://project.carrot2.org/publications/osinski-2003-lingo.pdf">this
 * document</a>, page 31 for a definition of a complete phrase.
 */
@Bindable(prefix = "CompleteLabelFilter")
public class CompleteLabelFilter implements ILabelFilter
{
    /**
     * Remove truncated phrases. Try to remove "incomplete" cluster labels, e.g. prefer
     * "Conference on Data" to "Conference on Data Mining".
     * 
     * @level Basic
     * @group Label filtering
     * @label Remove truncated phrases
     */
    @Input
    @Processing
    @Attribute
    public boolean enabled = true;

    /**
     * Truncated label threshold. Determines the strength of the truncated label filter.
     * The lowest value means strongest truncated labels elimination, which may lead to
     * overlong cluster labels and many unclustered documents. The highest value
     * effectively disables the filter, which may result in short or truncated labels.
     * 
     * @level Advanced
     * @group Phrase extraction
     * @label Truncated label threshold
     */
    @Input
    @Processing
    @Attribute
    @DoubleRange(min = 0.0, max = 1.0)
    public double labelOverrideThreshold = 0.65;

    /**
     * Left complete label filter.
     */
    private LeftCompleteLabelFilter leftCompleteLabelFilter = new LeftCompleteLabelFilter();

    /**
     * Right complete label filter.
     */
    private RightCompleteLabelFilter rightCompleteLabelFilter = new RightCompleteLabelFilter();

    /**
     * Marks incomplete labels.
     */
    public void filter(PreprocessingContext context, boolean [] acceptedStems,
        boolean [] acceptedPhrases)
    {
        if (!enabled)
        {
            return;
        }

        leftCompleteLabelFilter.filter(context, acceptedStems, acceptedPhrases,
            labelOverrideThreshold);
        rightCompleteLabelFilter.filter(context, acceptedStems, acceptedPhrases,
            labelOverrideThreshold);
    }

    public boolean isEnabled()
    {
        return enabled;
    }
}