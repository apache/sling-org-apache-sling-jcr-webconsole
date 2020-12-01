/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.sling.jcr.webconsole.internal;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.jcr.NamespaceRegistry;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.felix.inventory.Format;
import org.apache.felix.inventory.InventoryPrinter;
import org.apache.sling.jcr.api.SlingRepository;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * A Felix WebConsole ConfigurationPrinter which outputs the current JCR
 * namespace mappings.
 */
@Component(property = { InventoryPrinter.NAME + "=JCR Namespaces", InventoryPrinter.FORMAT  + "=TEXT"})
public class NamespacesPrinter implements InventoryPrinter {

    @Reference
    private SlingRepository slingRepository;

    /**
     * Output a list of namespace prefixes and URIs from the NamespaceRegistry.
     */
    @Override
    public void print(PrintWriter printWriter, Format format, boolean isZip) {
        Session session = null;
        try {
            session = slingRepository.loginAdministrative(null);
            NamespaceRegistry reg = session.getWorkspace().getNamespaceRegistry();
            List<String> globalPrefixes = Arrays.asList(reg.getPrefixes());
            List<String> localPrefixes = Arrays.asList(session.getNamespacePrefixes());
            Collections.sort(localPrefixes);
            for (String prefix : localPrefixes) {
                if (prefix.length() > 0) {
                    printWriter.printf("%10s = %s", prefix, session.getNamespaceURI(prefix));
                    if (globalPrefixes.contains(prefix)) {
                        printWriter.print(" [global]");
                    } else {
                        printWriter.print(" [local]");
                    }
                    printWriter.println();
                }
            }
        } catch (RepositoryException e) {
            printWriter.println("Unable to output namespace mappings.");
            e.printStackTrace(printWriter);
        } finally {
            if (session != null) {
                session.logout();
            }
        }
    }

}
