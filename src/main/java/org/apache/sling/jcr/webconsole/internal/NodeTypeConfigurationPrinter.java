/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.sling.jcr.webconsole.internal;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.nodetype.NodeDefinition;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeIterator;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.nodetype.PropertyDefinition;
import javax.jcr.version.OnParentVersionAction;

import org.apache.felix.inventory.Format;
import org.apache.felix.inventory.InventoryPrinter;
import org.apache.sling.jcr.api.SlingRepository;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * A Felix WebConsole ConfigurationPrinter which outputs the current JCR
 * nodetypes.
 */
@Component(property = { InventoryPrinter.NAME + "=JCR Node Types", InventoryPrinter.FORMAT  + "=TEXT"})
public class NodeTypeConfigurationPrinter implements InventoryPrinter {

    @Reference
    private SlingRepository slingRepository;

    private List<NodeType> sortTypes(NodeTypeIterator it) {
        List<NodeType> types = new ArrayList<NodeType>();
        while (it.hasNext()) {
            NodeType nt = it.nextNodeType();
            types.add(nt);
        }
        Collections.sort(types, new Comparator<NodeType>(){
            public int compare(NodeType o1, NodeType o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        return types;
    }

    private void printRequiredChildTypes(PrintWriter pw, NodeDefinition child) {
        if (child.getRequiredPrimaryTypes() != null && child.getRequiredPrimaryTypes().length > 0) {
            pw.print(" (");

            boolean first = true;
            for (NodeType required : child.getRequiredPrimaryTypes()) {
                if (!first) {
                    pw.print(", ");
                }
                pw.print(required.getName());
                first = false;
            }
            pw.print(")");
        }
    }

    private void printDefaultValues(PrintWriter pw, PropertyDefinition prop) throws RepositoryException {
        if (prop.getDefaultValues() != null && prop.getDefaultValues().length > 0) {
            pw.print(" = ");

            boolean first = true;
            for (Value v : prop.getDefaultValues()) {
                if (!first) {
                    pw.print(", ");
                }
                pw.print(v.getString());
                first = false;
            }
        }
    }

    private void printConstraints(PrintWriter pw, PropertyDefinition prop) throws RepositoryException {
        if (prop.getValueConstraints() != null && prop.getValueConstraints().length > 0) {
            pw.print(" < ");
            boolean first = true;
            for (String s : prop.getValueConstraints()) {
                if (!first) {
                    pw.print(", ");
                }
                pw.print(s);
                first = false;
            }
        }
    }

    private void printSuperTypes(PrintWriter pw, NodeType nt) {
        pw.print(" > ");
        boolean first = true;
        for (NodeType st : nt.getSupertypes()) {
            if (!first) {
                pw.print(", ");
            }
            pw.print(st.getName());
            first = false;
        }
    }

    @Override
    public void print(PrintWriter pw, Format format, boolean isZip) {
        Session session = null;
        try {
            session = slingRepository.loginAdministrative(null);
            NodeTypeManager ntm = session.getWorkspace().getNodeTypeManager();
            NodeTypeIterator it = ntm.getAllNodeTypes();
            List<NodeType> sortedTypes = sortTypes(it);

            for (NodeType nt : sortedTypes) {
                pw.printf("[%s]", nt.getName());

                printSuperTypes(pw, nt);

                if (nt.hasOrderableChildNodes()) {
                    pw.print(" orderable");
                }
                if (nt.isMixin()) {
                    pw.print(" mixin");
                }
                pw.println();

                for (PropertyDefinition prop : nt.getPropertyDefinitions()) {

                    pw.printf("- %s", prop.getName());
                    printDefaultValues(pw, prop);
                    if (prop.getName().equals(nt.getPrimaryItemName())) {
                        pw.print(" primary");
                    }
                    if (prop.isMandatory()) {
                        pw.print(" mandatory");
                    }
                    if (prop.isAutoCreated()) {
                        pw.print(" autocreated");
                    }
                    if (prop.isProtected()) {
                        pw.print(" protected");
                    }
                    if (prop.isMultiple()) {
                        pw.print(" multiple");
                    }
                    pw.printf(" %s", OnParentVersionAction.nameFromValue(prop.getOnParentVersion()));
                    printConstraints(pw, prop);


                    pw.println();
                }
                for (NodeDefinition child : nt.getChildNodeDefinitions()) {

                    pw.printf("+ %s", child.getName());

                    printRequiredChildTypes(pw, child);

                    if (child.getDefaultPrimaryType() != null) {
                        pw.printf(" = %s", child.getDefaultPrimaryType().getName());
                    }

                    if (child.isMandatory()) {
                        pw.print(" mandatory");
                    }
                    if (child.isAutoCreated()) {
                        pw.print(" autocreated");
                    }
                    if (child.isProtected()) {
                        pw.print(" protected");
                    }
                    if (child.allowsSameNameSiblings()) {
                        pw.print(" multiple");
                    }
                    pw.printf(" %s", OnParentVersionAction.nameFromValue(child.getOnParentVersion()));

                    pw.println();
                }
                pw.println();
            }
        } catch (RepositoryException e) {
            pw.println("Unable to output node type definitions.");
            e.printStackTrace(pw);
        } finally {
            if (session != null) {
                session.logout();
            }
        }
    }
}