/*
 * Carrot2 Project
 * Copyright (C) 2002-2005, Dawid Weiss
 * Portions (C) Contributors listed in carrot2.CONTRIBUTORS file.
 * All rights reserved.
 *
 * Refer to the full license file "carrot2.LICENSE"
 * in the root folder of the CVS checkout or at:
 * http://www.cs.put.poznan.pl/dweiss/carrot2.LICENSE
 */
package com.stachoodev.carrot.odp.index;

import java.io.*;
import java.util.*;

import org.dom4j.io.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

import com.stachoodev.carrot.odp.*;
import com.stachoodev.util.common.*;

/**
 * Builds a {@link CatidPrimaryTopicIndexBuilder}based on the ODP Topic's
 * <code>catid</code> attribute. Indices created by this class are instances
 * of {@link CatidPrimaryTopicIndexBuilder}.
 * 
 * Content files (one file contains one topic along with all its external pages)
 * are laid out in a hierarchical structure of file system directories
 * corresponding to topic 'paths' in the original ODP structure, e.g.
 * Top/World/Poland/Komputery. The maximum depth of the file system directory
 * structure can be specified beyond which all topics will be saved in a flat
 * list of files (file name is the topics <code>catid</code>). As ODP topic
 * 'paths' can contain problematic UTF8 characters, each element of the path is
 * mapped to an integer number.
 * 
 * This index builder is <b>not </b> thread-safe.
 * 
 * TODO: storing topics in separate zip files was a BAD idea. Re-implement this
 * based on random access files, maybe JDK1.4 channels
 * 
 * @author Stanislaw Osinski
 * @version $Revision$
 */
public class CatidPrimaryTopicIndexBuilder extends DefaultHandler implements
    PrimaryTopicIndexBuilder, ObservableTopicIndexBuilder
{
    /** Stores properties of this indexer */
    private PropertyHelper propertyHelper;

    /** Topic serializer */
    private TopicSerializer topicSerializer;

    /** Listeners */
    private List topicIndexBuilderListeners;

    /** Currently processed ODP category */
    private MutableTopic currentTopic;

    /** Currently processed ODP reference */
    private MutableExternalPage currentExternalPage;

    /** Collects contents of text elements */
    private StringBuffer stringBuffer;

    /** Entries of this index */
    private List indexEntries;

    /** Piggyback topic index builders */
    private Collection topicIndexBuilders;

    /**
     * Creates a new CatidPrimaryTopicIndexBuilder.
     * 
     * @param dataLocationPath location in which the index data is to be stored.
     */
    public CatidPrimaryTopicIndexBuilder()
    {
        this.topicIndexBuilderListeners = new ArrayList();
        this.propertyHelper = new PropertyHelper();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.stachoodev.carrot.odp.index.PrimaryTopicIndexBuilder#create(java.io.InputStream)
     */
    public PrimaryTopicIndex create(InputStream inputStream,
        TopicSerializer topicSerializer, Collection topicIndexBuilders)
        throws IOException, ClassNotFoundException
    {
        this.topicIndexBuilders = topicIndexBuilders;
        this.topicSerializer = topicSerializer;

        // Reset fields
        indexEntries = new ArrayList();
        currentTopic = null;
        currentExternalPage = null;
        stringBuffer = null;

        // Initialize SAX
        try
        {
            XMLReader xmlReader = new SAXReader().getXMLReader();
            xmlReader.setContentHandler(this);
            xmlReader.parse(new InputSource(inputStream));
        }
        catch (SAXException e)
        {
            System.err.println("SAX parser exception: " + e.getMessage());
        }

        // Sort the entries according to the id and convert to a
        // PrimaryTopicIndex
        Collections.sort(indexEntries);
        return new SimplePrimaryTopicIndex(indexEntries);
    }

    /**
     * @param topic
     * @throws IOException
     */
    private void index(Topic topic) throws IOException
    {
        // Omit empty topics
        if (topic.getExternalPages().size() == 0)
        {
            return;
        }

        // Serialize the topic
        Location location = topicSerializer.serialize(topic);

        // Add to index entries
        indexEntries.add(new SimplePrimaryTopicIndex.IndexEntry(topic
            .getCatid(), location));

        // Let the piggyback topic index builders
        if (topicIndexBuilders != null)
        {
            for (Iterator iter = topicIndexBuilders.iterator(); iter.hasNext();)
            {
                TopicIndexBuilder topicIndexBuilder = (TopicIndexBuilder) iter
                    .next();
                topicIndexBuilder.index(topic);
            }
        }

        // Fire the event
        fireTopicIndexed();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.ContentHandler#characters(char[], int, int)
     */
    public void characters(char [] ch, int start, int length)
        throws SAXException
    {
        if (stringBuffer != null)
        {
            stringBuffer.append(ch, start, length);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.ContentHandler#startElement(java.lang.String,
     *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    public void startElement(String uri, String localName, String qName,
        Attributes attributes) throws SAXException
    {
        // Just in case localName is null
        String elementName = localName;
        if (elementName.equals(""))
        {
            elementName = qName;
        }

        // Topic element
        if (elementName.equals("Topic"))
        {
            // Index previous topic if there is any
            if (currentTopic != null)
            {
                try
                {
                    // Add currentTopic to the index
                    index(currentTopic);
                }
                catch (IOException e)
                {
                    new SAXException("Can't serialize topic", e);
                }
            }

            String id = attributes.getValue("r:id");
            if (id == null)
            {
                currentTopic = null;
                throw new SAXException("WARNING: no id found for a topic.");
            }

            currentTopic = new MutableTopic(id);
            return;
        }

        // If we were unable to parse the Topic element - don't bother to parse
        // its content and the following ExternalPages.
        if (currentTopic == null)
        {
            return;
        }

        // Topic/catid element
        if (elementName.equals("catid"))
        {
            stringBuffer = new StringBuffer();
            return;
        }

        // ExternalPage element
        if (elementName.equals("ExternalPage"))
        {
            currentExternalPage = new MutableExternalPage();
            return;
        }

        // ExternalPage/Title element
        if (elementName.equals("Title"))
        {
            stringBuffer = new StringBuffer();
            return;
        }

        // ExternalPage/Description element
        if (elementName.equals("Description"))
        {
            stringBuffer = new StringBuffer();
            return;
        }

        // We don't parse the ExternalPage/topic element as we make an
        // assumption that all ExternalPages following a Topic element belong
        // to that topic.
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.ContentHandler#endElement(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    public void endElement(String uri, String localName, String qName)
        throws SAXException
    {
        // Don't bother doing anything if there is no valid Topic
        if (currentTopic == null)
        {
            return;
        }

        // Just in case localName is null
        String elementName = localName;
        if (elementName.equals(""))
        {
            elementName = qName;
        }

        // Topic/catid element
        if (elementName.equals("catid"))
        {
            // Current stringBuffer is the contents of catid
            currentTopic.setCatid(Integer.parseInt(stringBuffer.toString()));
            return;
        }

        // ExternalPage element
        if (elementName.equals("ExternalPage"))
        {
            // Add currentExternalPage to currentTopic
            currentTopic.addExternalPage(currentExternalPage);
            return;
        }

        // ExternalPage/Title element
        if (elementName.equals("Title"))
        {
            // Current stringBuffer is the contents of Title
            currentExternalPage.setTitle(stringBuffer.toString());
            return;
        }

        // ExternalPage/Description element
        if (elementName.equals("Description"))
        {
            // Current stringBuffer is the contents of Description
            currentExternalPage.setDescription(stringBuffer.toString());
            return;
        }

        // We don't parse the ExternalPage/topic element as we make an
        // assumption that all ExternalPages following a Topic element belong
        // to that topic.
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.ContentHandler#endDocument()
     */
    public void endDocument() throws SAXException
    {
        // Serialize the last topic
        if (currentTopic != null)
        {
            try
            {
                // Add currentTopic to the index
                index(currentTopic);
            }
            catch (IOException e)
            {
                new SAXException("Can't serialize topic", e);
            }
        }
    }

    /**
     * Sets this CatidPrimaryTopicIndexBuilder's <code>topicSerializer</code>.
     * 
     * @param topicSerializer
     */
    public void setTopicSerializer(TopicSerializer topicSerializer)
    {
        this.topicSerializer = topicSerializer;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.stachoodev.carrot.odp.index.ObservableTopicIndexBuilder#addTopicIndexBuilderListener(com.stachoodev.carrot.odp.index.TopicIndexBuilderListener)
     */
    public void addTopicIndexBuilderListener(TopicIndexBuilderListener listener)
    {
        topicIndexBuilderListeners.add(listener);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.stachoodev.carrot.odp.index.ObservableTopicIndexBuilder#removeTopicIndexBuilderListener(com.stachoodev.carrot.odp.index.TopicIndexBuilderListener)
     */
    public void removeTopicIndexBuilderListener(
        TopicIndexBuilderListener listener)
    {
        topicIndexBuilderListeners.remove(listener);
    }

    /**
     * Fires the {@link TopicIndexBuilderListener#topicIndexed()}method on all
     * registered listeners.
     */
    protected void fireTopicIndexed()
    {
        for (Iterator iter = topicIndexBuilderListeners.iterator(); iter
            .hasNext();)
        {
            TopicIndexBuilderListener listener = (TopicIndexBuilderListener) iter
                .next();
            listener.topicIndexed();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.stachoodev.util.common.PropertyProvider#getDoubleProperty(java.lang.String)
     */
    public double getDoubleProperty(String propertyName, double defaultValue)
    {
        return propertyHelper.getDoubleProperty(propertyName, defaultValue);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.stachoodev.util.common.PropertyProvider#getIntProperty(java.lang.String)
     */
    public int getIntProperty(String propertyName, int defaultValue)
    {
        return propertyHelper.getIntProperty(propertyName, defaultValue);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.stachoodev.util.common.PropertyProvider#getProperty(java.lang.String)
     */
    public Object getProperty(String propertyName)
    {
        return propertyHelper.getProperty(propertyName);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.stachoodev.util.common.PropertyProvider#setDoubleProperty(java.lang.String,
     *      double)
     */
    public Object setDoubleProperty(String propertyName, double value)
    {
        return propertyHelper.setDoubleProperty(propertyName, value);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.stachoodev.util.common.PropertyProvider#setIntProperty(java.lang.String,
     *      int)
     */
    public Object setIntProperty(String propertyName, int value)
    {
        return propertyHelper.setIntProperty(propertyName, value);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.stachoodev.util.common.PropertyProvider#setProperty(java.lang.String,
     *      java.lang.Object)
     */
    public Object setProperty(String propertyName, Object property)
    {
        return propertyHelper.setProperty(propertyName, property);
    }
}