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

package org.apache.directory.studio.connection.ui.properties;


import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.ConnectionCorePlugin;
import org.apache.directory.studio.connection.core.ConnectionParameter;
import org.apache.directory.studio.connection.core.PasswordsKeyStoreManager;
import org.apache.directory.studio.connection.core.Utils;
import org.apache.directory.studio.connection.core.jobs.CloseConnectionsRunnable;
import org.apache.directory.studio.connection.core.jobs.StudioConnectionJob;
import org.apache.directory.studio.connection.ui.ConnectionParameterPage;
import org.apache.directory.studio.connection.ui.ConnectionParameterPageManager;
import org.apache.directory.studio.connection.ui.ConnectionParameterPageModifyListener;
import org.apache.directory.studio.connection.ui.ConnectionUIConstants;
import org.apache.directory.studio.connection.ui.PasswordsKeyStoreManagerUtils;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PropertyPage;


/**
 * The ConnectionPropertyPage displays the properties of a {@link Connection}.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ConnectionPropertyPage extends PropertyPage implements ConnectionParameterPageModifyListener
{
    /** The tab folder. */
    private TabFolder tabFolder;

    /** The tabs. */
    private TabItem[] tabs;

    /** The connection property pages. */
    private ConnectionParameterPage[] pages;


    /**
     * Creates a new instance of ConnectionPropertyPage.
     */
    public ConnectionPropertyPage()
    {
        super();
        super.noDefaultAndApplyButton();
    }


    /**
     * @see org.apache.directory.studio.connection.ui.ConnectionParameterPageModifyListener#connectionParameterPageModified()
     */
    public void connectionParameterPageModified()
    {
        validate();
    }


    /**
     * @see org.apache.directory.studio.connection.ui.ConnectionParameterPageModifyListener#getTestConnectionParameters()
     */
    public ConnectionParameter getTestConnectionParameters()
    {
        ConnectionParameter connectionParameter = new ConnectionParameter();
        for ( int i = 0; i < pages.length; i++ )
        {
            pages[i].saveParameters( connectionParameter );
        }
        return connectionParameter;
    }


    /**
     * @see org.eclipse.jface.dialogs.DialogPage#setMessage(java.lang.String)
     */
    public void setMessage( String message )
    {
        super.setMessage( message, PropertyPage.WARNING );
    }


    /**
     * @see org.eclipse.jface.preference.PreferencePage#setErrorMessage(java.lang.String)
     */
    public void setErrorMessage( String errorMessage )
    {
        super.setErrorMessage( errorMessage );
    }


    /**
     * Validates the dialog.
     */
    private void validate()
    {
        int index = tabFolder.getSelectionIndex();
        ConnectionParameterPage page = index >= 0 ? pages[tabFolder.getSelectionIndex()] : null;
        if ( page != null
            && ( page.getMessage() != null || page.getInfoMessage() != null || page.getErrorMessage() != null ) )
        {
            if ( page.getMessage() != null )
            {
                setMessage( page.getMessage() );
            }
            else if ( page.getInfoMessage() != null )
            {
                setMessage( page.getInfoMessage() );
            }
            else
            {
                setMessage( null );
            }
            setErrorMessage( page.getErrorMessage() );
            setValid( page.isValid() );
        }
        else
        {
            for ( int i = 0; i < pages.length; i++ )
            {
                if ( pages[i].getMessage() != null || pages[i].getInfoMessage() != null
                    || pages[i].getErrorMessage() != null )
                {
                    if ( page.getMessage() != null )
                    {
                        setMessage( page.getMessage() );
                    }
                    else if ( page.getInfoMessage() != null )
                    {
                        setMessage( page.getInfoMessage() );
                    }
                    else
                    {
                        setMessage( null );
                    }
                    setErrorMessage( pages[i].getErrorMessage() );
                    setValid( pages[i].isValid() );
                    return;
                }
            }

            setMessage( null );
            setErrorMessage( null );
            setValid( true );
        }
    }


    static Connection getConnection( Object element )
    {
        Connection connection = null;
        if ( element instanceof IAdaptable )
        {
            connection = ( Connection ) ( ( IAdaptable ) element ).getAdapter( Connection.class );
        }
        return connection;
    }


    /**
     * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
     */
    protected Control createContents( Composite parent )
    {
        PlatformUI.getWorkbench().getHelpSystem().setHelp( parent,
            ConnectionUIConstants.PLUGIN_ID + "." + "tools_connection_properties" ); //$NON-NLS-1$ //$NON-NLS-2$

        // Checking of the connection passwords keystore is enabled
        if ( PasswordsKeyStoreManagerUtils.isPasswordsKeystoreEnabled() )
        {
            // Getting the passwords keystore manager
            PasswordsKeyStoreManager passwordsKeyStoreManager = ConnectionCorePlugin.getDefault()
                .getPasswordsKeyStoreManager();

            // Checking if the keystore is not loaded 
            if ( !passwordsKeyStoreManager.isLoaded() )
            {
                // Asking the user to load the keystore
                if ( !PasswordsKeyStoreManagerUtils.askUserToLoadKeystore() )
                {
                    // The user failed to load the keystore and cancelled
                    Label label = BaseWidgetUtils
                        .createLabel(
                            parent,
                            Messages
                                .getString( "ConnectionPropertyPage.AccessToPasswordsKeystoreRequiredToViewProperties" ), 1 ); //$NON-NLS-1$
                    return label;
                }
            }
        }

        Connection connection = getConnection( getElement() );
        if ( connection != null )
        {
            super
                .setMessage( Messages.getString( "ConnectionPropertyPage.Connection" ) + Utils.shorten( connection.getName(), 30 ) ); //$NON-NLS-1$

            pages = ConnectionParameterPageManager.getConnectionParameterPages();

            tabFolder = new TabFolder( parent, SWT.TOP );

            tabs = new TabItem[pages.length];
            for ( int i = 0; i < pages.length; i++ )
            {
                Composite composite = new Composite( tabFolder, SWT.NONE );
                GridLayout gl = new GridLayout( 1, false );
                composite.setLayout( gl );

                pages[i].init( composite, this, connection.getConnectionParameter() );

                tabs[i] = new TabItem( tabFolder, SWT.NONE );
                tabs[i].setText( pages[i].getPageName() );
                tabs[i].setControl( composite );
            }

            return tabFolder;
        }
        else
        {
            Label label = BaseWidgetUtils.createLabel( parent, Messages
                .getString( "ConnectionPropertyPage.NoConnection" ), 1 ); //$NON-NLS-1$
            return label;
        }
    }


    /**
     * @see org.eclipse.jface.preference.PreferencePage#performOk()
     */
    public boolean performOk()
    {
        // Checking of the connection passwords keystore is enabled
        if ( PasswordsKeyStoreManagerUtils.isPasswordsKeystoreEnabled() )
        {
            // Checking if the keystore is not loaded 
            if ( !ConnectionCorePlugin.getDefault().getPasswordsKeyStoreManager().isLoaded() )
            {
                return true;
            }
        }

        // get current connection parameters
        Connection connection = ( Connection ) getConnection( getElement() );

        // save modified parameters
        boolean parametersModified = false;
        boolean reconnectionRequired = false;
        ConnectionParameter connectionParameter = new ConnectionParameter();
        connectionParameter.setId( connection.getConnectionParameter().getId() );
        for ( int i = 0; i < pages.length; i++ )
        {
            pages[i].saveParameters( connectionParameter );
            pages[i].saveDialogSettings();
            parametersModified |= pages[i].areParametersModifed();
            reconnectionRequired |= pages[i].isReconnectionRequired();
        }

        if ( parametersModified )
        {
            // update connection parameters
            connection.setConnectionParameter( connectionParameter );

            if ( reconnectionRequired )
            {
                // close connection
                new StudioConnectionJob( new CloseConnectionsRunnable( connection ) ).execute();
            }
        }

        return true;
    }
}
