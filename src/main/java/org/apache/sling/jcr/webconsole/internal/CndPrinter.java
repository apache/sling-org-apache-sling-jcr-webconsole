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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.jcr.NamespaceRegistry;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeTypeIterator;

import org.apache.felix.inventory.Format;
import org.apache.felix.inventory.InventoryPrinter;
import org.apache.jackrabbit.commons.cnd.CompactNodeTypeDefWriter;
import org.apache.sling.jcr.api.SlingRepository;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(property = { InventoryPrinter.NAME + "=JCR CND", InventoryPrinter.FORMAT + "=TEXT",
        InventoryPrinter.TITLE + "=JCR Compact Node Type and Name Space Definition (CND)" })
public class CndPrinter implements InventoryPrinter {

    @Reference
    private SlingRepository slingRepository;

    @Override
    public void print(PrintWriter printWriter, Format format, boolean isZip) {
        Session session = null;
        try {
            session = slingRepository.loginAdministrative(null);
            final CompactNodeTypeDefWriter cnd = new CompactNodeTypeDefWriter(printWriter, session, true);
            List<String> prefixes = Arrays.asList(session.getWorkspace().getNamespaceRegistry().getPrefixes());
            Collections.sort(prefixes);
            for (String prefix : prefixes) {
                if (!prefix.equals(NamespaceRegistry.PREFIX_EMPTY)) {
                    cnd.writeNamespaceDeclaration(prefix);
                }
            }
            NodeTypeIterator ntIterator = session.getWorkspace().getNodeTypeManager().getAllNodeTypes();
            while (ntIterator.hasNext()) {
                cnd.write(ntIterator.nextNodeType());
            }
            cnd.close();
        } catch (RepositoryException | IOException e) {
            printWriter.println("Unable to output CND.");
            e.printStackTrace(printWriter);
        } finally {
            if (session != null) {
                session.logout();
            }
        }
    }

}
