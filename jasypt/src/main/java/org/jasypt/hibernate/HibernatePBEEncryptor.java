/*
 * =============================================================================
 * 
 *   Copyright (c) 2007, The JASYPT team (http://www.jasypt.org)
 * 
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * 
 * =============================================================================
 */
package org.jasypt.hibernate;

import org.jasypt.encryption.pbe.PBEStringEncryptor;
import org.jasypt.exceptions.EncryptionInitializationException;

/**
 * <p>
 * Placeholder class for <tt>PBEStringEncryptor</tt> objects which are
 * eligible for use from Hibernate. 
 * </p>
 * <p>
 * This class acts as a wrapper on a <tt>PBEStringEncryptor</tt>, allowing
 * to be set a <b>registered name</b> (see {@link #setRegisteredName(String)})
 * and performing the needed registry operations against the 
 * {@link HibernatePBEEncryptorRegistry}.
 * </p>
 * <p>
 * This class is mainly intended for use from Spring Framework or some other
 * IoC container (if you are not using a container of this kind, please see 
 * {@link HibernatePBEEncryptorRegistry}). The steps to be performed are 
 * the following:
 * <ol>
 *   <li>Create an object of this class (declaring it).</li>
 *   <li>Set its <tt>registeredName</tt> and <tt>encryptor</tt> properties.</li>
 *   <li>Declare a <i>typedef</i> in a Hibernate mapping giving its
 *       <tt>encryptorRegisteredName</tt> parameter the same value specified
 *       to this object in <tt>registeredName</tt>.</li>
 * </ol>
 * </p>
 * <p>
 * This in a Spring config file would look like:
 * </p>
 * <p>
 * <pre> 
 *  &lt;bean id="strongEncryptor"
 *    class="org.jasypt.encryption.pbe.StandardPBEStringEncryptor">
 *    &lt;property name="algorithm">
 *        &lt;value>PBEWithMD5AndTripleDES&lt;/value>
 *    &lt;/property>
 *    &lt;property name="password">
 *        &lt;value>jasypt&lt;/value>
 *    &lt;/property>
 *  &lt;/bean>
 *  
 *  &lt;bean id="hibernateEncryptor"
 *    class="org.jasypt.hibernate.HibernatePBEEncryptor">
 *    &lt;property name="registeredName">
 *        &lt;value><b>strongHibernateEncryptor</b>&lt;/value>
 *    &lt;/property>
 *    &lt;property name="encryptor">
 *        &lt;ref bean="strongEncryptor" />
 *    &lt;/property>
 *  &lt;/bean>
 * </pre>
 * </p>
 * <p>
 * And then in the Hibernate mapping file:
 * </p>
 * <p>
 * <pre>
 *    &lt;typedef name="encrypted" class="org.jasypt.hibernate.EncryptedTextType">
 *      &lt;param name="encryptorRegisteredName"><b>strongHibernateEncryptor</b>&lt;/param>
 *    &lt;/typedef>
 * </pre>
 * </p>
 * <p>
 * An important thing to note is that, when using <tt>HibernatePBEEncryptor</tt>
 * objects this way to wrap <tt>PBEStringEncryptor</tt>s, <u>it is not
 * necessary to deal with {@link HibernatePBEEncryptorRegistry}</u>, 
 * because <tt>HibernatePBEEncryptor</tt> objects get automatically registered
 * in the encryptor registry when their {@link #setRegisteredName(String)}
 * method is called.
 * </p>
 * 
 * @since 1.0
 * @deprecated Replaced by 
 *           {@link org.jasypt.hibernate.encryptor.HibernatePBEStringEncryptor}
 *           and will be removed in version 1.3.
 * 
 * @author Daniel Fern&aacute;ndez Garrido
 * 
 */
public class HibernatePBEEncryptor {


    private String registeredName = null;
    private PBEStringEncryptor encryptor = null;
    
    
    
    /**
     * Creates a new instance of <tt>HibernatePBEEncryptor</tt> 
     */
    public HibernatePBEEncryptor() {
        super();
    }


    /*
     * For internal use only, by the Registry, when a PBEStringEncryptor
     * is registered programmatically.
     */
    HibernatePBEEncryptor(String registeredName, PBEStringEncryptor encryptor) {
        this.registeredName = registeredName;
        this.encryptor = encryptor;
    }


    /**
     * Sets the registered name of the encryptor and adds it to the registry.
     * 
     * @param registeredName the name with which the encryptor will be
     *                       registered.
     */
    public void setRegisteredName(String registeredName) {
        if (this.registeredName != null) {
            // It had another name before, we have to clean
            HibernatePBEEncryptorRegistry.getInstance().
                    unregisterHibernatePBEEncryptor(this.registeredName);
        }
        this.registeredName = registeredName;
        HibernatePBEEncryptorRegistry.getInstance().
                registerHibernatePBEEncryptor(this);
        
    }
    

    /**
     * Returns the encryptor which this object wraps.
     * 
     * @return the encryptor.
     */
    public PBEStringEncryptor getEncryptor() {
        return encryptor;
    }
    

    /**
     * Returns the name with which the wrapped encryptor is registered at
     * the registry.
     * 
     * @return the registered name.
     */
    public String getRegisteredName() {
        return registeredName;
    }
    
    
    /**
     * Sets the <tt>PBEStringEncryptor</tt> to be held (wrapped) by this
     * object.
     * 
     * @param encryptor the encryptor.
     */
    public void setEncryptor(PBEStringEncryptor encryptor) {
        this.encryptor = encryptor;
    }


    /**
     * Encrypts a message, delegating to wrapped encryptor.
     * 
     * @param message the message to be encrypted.
     * @return the encryption result.
     */
    public String encrypt(String message) {
        if (this.encryptor == null) {
            throw new EncryptionInitializationException(
                    "Encryptor has not been set into Hibernate wrapper");
        }
        return encryptor.encrypt(message);
    }

    
    /**
     * Decypts a message, delegating to wrapped encryptor
     * 
     * @param encryptedMessage the message to be decrypted.
     * @return the result of decryption.
     */
    public String decrypt(String encryptedMessage) {
        if (this.encryptor == null) {
            throw new EncryptionInitializationException(
                    "Encryptor has not been set into Hibernate wrapper");
        }
        return encryptor.decrypt(encryptedMessage);
    }
    
    
}