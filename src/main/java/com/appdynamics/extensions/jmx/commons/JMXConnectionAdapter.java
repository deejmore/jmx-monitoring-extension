/*
 *   Copyright 2018. AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.jmx.commons;


import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import javax.management.*;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JMXConnectionAdapter {

    private final JMXServiceURL serviceUrl;
    private final String username;
    private final String password;
    private String keystore = "";
    private String keystorePassword = "";
    

    
    private JMXConnectionAdapter(String host, int port, String username, String password) throws MalformedURLException {
        this.serviceUrl = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + host + ":" + port + "/jmxrmi");
        this.username = username;
        this.password = password;
    }
    
    private JMXConnectionAdapter(String host, int port, String username, String password, String keystore, String keystorePassword) throws MalformedURLException {
        this.serviceUrl = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + host + ":" + port + "/jmxrmi");
        this.username = username;
        this.password = password;
        this.keystore = keystore;
        this.keystorePassword = keystorePassword;
    }

    private JMXConnectionAdapter(String serviceUrl, String username, String password) throws MalformedURLException {
        this.serviceUrl = new JMXServiceURL(serviceUrl);
        this.username = username;
        this.password = password;
    }
   
    private JMXConnectionAdapter(String serviceUrl, String username, String password, String keystore, String keystorePassword) throws MalformedURLException {
        this.serviceUrl = new JMXServiceURL(serviceUrl);
        this.username = username;
        this.password = password;
        this.keystore = keystore;
        this.keystorePassword = keystorePassword;
    }
    

    public static JMXConnectionAdapter create(String serviceUrl, String host, int port, String username, String password, String keystore, String keystorePassword) throws MalformedURLException {
        if (Strings.isNullOrEmpty(serviceUrl)) {
        	return new JMXConnectionAdapter(host, port, username, password, keystore, keystorePassword);
        } else {
            return new JMXConnectionAdapter(serviceUrl, username, password, keystore, keystorePassword);
        }
    }

    public JMXConnector open() throws IOException {
        JMXConnector jmxConnector;
        final Map<String, Object> env = new HashMap<String, Object>();
        if (!Strings.isNullOrEmpty(username)) {
        	    env.put(JMXConnector.CREDENTIALS, new String[]{username, password, keystore, keystorePassword});
        	    jmxConnector = JMXConnectorFactory.connect(serviceUrl, env);
        } else {
            jmxConnector = JMXConnectorFactory.connect(serviceUrl);
        }
        if (jmxConnector == null) {
            throw new IOException("Unable to connect to Mbean server");
        }
        return jmxConnector;
    }

    public void close(JMXConnector jmxConnector) throws IOException {
        if (jmxConnector != null) {
            jmxConnector.close();
        }
    }

    public Set<ObjectInstance> queryMBeans(JMXConnector jmxConnection, ObjectName objectName) throws IOException {
        MBeanServerConnection connection = jmxConnection.getMBeanServerConnection();
        return connection.queryMBeans(objectName, null);
    }

    public List<String> getReadableAttributeNames(JMXConnector jmxConnection, ObjectInstance instance) throws IntrospectionException, ReflectionException, InstanceNotFoundException, IOException {
        MBeanServerConnection connection = jmxConnection.getMBeanServerConnection();
        List<String> attrNames = Lists.newArrayList();
        MBeanAttributeInfo[] attributes = connection.getMBeanInfo(instance.getObjectName()).getAttributes();
        for (MBeanAttributeInfo attr : attributes) {
            if (attr.isReadable()) {
                attrNames.add(attr.getName());
            }
        }
        return attrNames;
    }

    public List<Attribute> getAttributes(JMXConnector jmxConnection, ObjectName objectName, String[] strings) throws IOException, ReflectionException, InstanceNotFoundException {
        MBeanServerConnection connection = jmxConnection.getMBeanServerConnection();
        AttributeList list = connection.getAttributes(objectName, strings);
        if (list != null) {
            return list.asList();
        }
        return Lists.newArrayList();
    }


    boolean matchAttributeName(Attribute attribute, String matchedWith) {
        return attribute.getName().equalsIgnoreCase(matchedWith);
    }
}
