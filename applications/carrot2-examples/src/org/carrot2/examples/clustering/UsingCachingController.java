
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

package org.carrot2.examples.clustering;

import java.util.HashMap;
import java.util.Map;

import org.carrot2.clustering.lingo.LingoClusteringAlgorithm;
import org.carrot2.core.*;
import org.carrot2.core.attribute.AttributeNames;
import org.carrot2.source.microsoft.MicrosoftLiveDocumentSource;
import org.carrot2.util.attribute.AttributeUtils;

/**
 * This example shows how to set up and use {@link CachingController}, a production-grade
 * controller for running your clustering queries. This example assumes you are familiar
 * with {@link ClusteringDataFromDocumentSources} and {@link ClusteringDocumentList}
 * examples.
 */
public class UsingCachingController
{
    @SuppressWarnings(
    {
        "unused", "unchecked"
    })
    public static void main(String [] args)
    {
        /*
         * Caching controller caches instances of components in between requests and
         * reuses these instances when possible. An internal pool of instances is created
         * and can be configured arbitrarily to suit your needs (either an unbounded pool
         * or a bounded pool). Also, if requested, caching controller can cache the
         * results produced by components, e.g. search results fetched from a search
         * engine and/or clusters produced by a clustering algorithm.
         */

        /*
         * Caching controller is always <b>thread-safe</b>, that is every processing takes
         * place on an independent set of component instances by a single thread.
         */

        /*
         * The first step is to create the caching controller. You need only one caching
         * controller instance per application life cycle. We'd like to store the results
         * fetched from any document source in the controller's cache, so that if the same
         * query is issued again, the controller will first attempt to retrieve the
         * results from cache, and only if not found in cache, contact the document
         * source. Also, we'd like to cache clusters generated by the Lingo clustering
         * algorithm.
         */
        CachingController controller = new CachingController(DocumentSource.class,
            LingoClusteringAlgorithm.class);

        /*
         * Before using the caching controller, you must initialize it. On initialization
         * you can pass a global set of attributes (both @Init and
         * @Processing ones) that will override the component's defaults.
         */

        /*
         * Let's globally override the default number of results requested from the search
         * engine and provide application id for the MSN document source (please use your
         * own appid for production).
         */
        Map<String, Object> globalAttributes = new HashMap<String, Object>();
        globalAttributes.put(AttributeNames.RESULTS, 50);
        globalAttributes.put(AttributeUtils.getKey(MicrosoftLiveDocumentSource.class,
            "appid"), MicrosoftLiveDocumentSource.CARROTSEARCH_APPID);
        controller.init(globalAttributes);

        /*
         * The controller is now ready to perform queries. To show that the documents from
         * the document input are cached, we will perform the same query a few times and
         * print processing times to the output. The first two queries should take
         * significantly longer than subsequent queries (even taking into account JVM
         * warm-ups etc.).
         */
        System.out.println("Query times: ");
        for (int i = 0; i < 10; i++)
        {
            Map<String, Object> attributes = new HashMap<String, Object>();
            attributes.put(AttributeNames.QUERY, "data mining");

            // If you want to override the number of results provided on initialization,
            // just pass a new value in processing time attribute map
            if (i % 2 == 0)
            {
                attributes.put(AttributeNames.RESULTS, 100);
            }

            final long start = System.currentTimeMillis();
            ProcessingResult result = controller.process(attributes,
                MicrosoftLiveDocumentSource.class, LingoClusteringAlgorithm.class);
            final long duration = System.currentTimeMillis() - start;

            System.out.println(String.format("\t%+10.3f", duration / 1000.0f));
        }
    }
}
