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

import org.apache.felix.inventory.Format;
import org.apache.felix.inventory.InventoryPrinter;
import org.apache.sling.jcr.api.SlingRepository;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * A Felix WebConsole ConfigurationPrinter which outputs the current JCR
 * repository descriptors.
 */
@Component(property = { InventoryPrinter.NAME + "=JCR Descriptors", InventoryPrinter.FORMAT  + "=TEXT"})
public class DescriptorsPrinter implements InventoryPrinter {


    @Reference
    private SlingRepository slingRepository;

    /**
     * Output a list of repository descriptors.
     */
    @Override
    public void print(PrintWriter printWriter, Format format, boolean isZip) {
        final String[] descriptorKeys = slingRepository.getDescriptorKeys();
        for (final String key : descriptorKeys) {
            printWriter.printf("%s = %s%n", key, slingRepository.getDescriptor(key));
        }
    }

}
