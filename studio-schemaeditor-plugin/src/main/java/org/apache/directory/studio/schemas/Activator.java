/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *  
 *    http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License. 
 *  
 */

package org.apache.directory.studio.schemas;


import org.apache.directory.studio.schemas.model.SchemaPool;
import org.apache.directory.studio.schemas.view.views.SchemaCodeScanner;
import org.apache.directory.studio.schemas.view.views.SchemaTextAttributeProvider;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;


/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin
{

    /** The plug-in ID */
    public static final String PLUGIN_ID = "org.apache.directory.studio.schemas"; //$NON-NLS-1$

    /** The shared instance */
    private static Activator plugin;

    /** the Schema Code Scanner */
    private static ITokenScanner schemaCodeScanner;

    /** The Schema Text Attribute Provider */
    private static SchemaTextAttributeProvider schemaTextAttributeProvider;

    /** The Schema Pool */
    private SchemaPool schemaPool;


    /**
     * The constructor
     */
    public Activator()
    {
        plugin = this;
    }


    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
     */
    public void start( BundleContext context ) throws Exception
    {
        super.start( context );

        // Loading the Schema Pool
        schemaPool = SchemaPool.getInstance();

        // Initialiazing the Preferences Listener
        initPreferencesListener();

    }


    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
     */
    public void stop( BundleContext context ) throws Exception
    {
        // Saving workspace configuration
        schemaPool.saveUserSchemasPaths();

        super.stop( context );
        plugin = null;
    }


    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static Activator getDefault()
    {
        return plugin;
    }


    /**
     * Returns the Schema Code Scanner.
     *
     * @return
     *      the Schema Code Scanner
     */
    public static ITokenScanner getSchemaCodeScanner()
    {
        if ( schemaCodeScanner == null )
        {
            schemaCodeScanner = new SchemaCodeScanner( getSchemaTextAttributeProvider() );
        }

        return schemaCodeScanner;
    }


    /**
     * Returns the Schema Text Attribute Provider.
     *
     * @return
     *     the Schema Text Attribute Provider 
     */
    private static SchemaTextAttributeProvider getSchemaTextAttributeProvider()
    {
        if ( schemaTextAttributeProvider == null )
        {
            schemaTextAttributeProvider = new SchemaTextAttributeProvider();
        }

        return schemaTextAttributeProvider;
    }


    /**
     * Initializes the listener on the preferences store.
     */
    private void initPreferencesListener()
    {
        Activator.getDefault().getPreferenceStore().addPropertyChangeListener( new IPropertyChangeListener()
        {
            public void propertyChange( PropertyChangeEvent event )
            {
                if ( PluginConstants.PREFS_SCHEMAS_EDITOR_SPECIFIC_CORE == event.getProperty() )
                {
                    // Reloading the SchemaPool
                    schemaPool.reload();
                }
            }
        } );
    }
}
