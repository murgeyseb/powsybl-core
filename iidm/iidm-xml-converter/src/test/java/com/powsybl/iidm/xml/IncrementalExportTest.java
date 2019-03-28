/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.datasource.*;
import com.powsybl.iidm.IidmImportExportType;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.HvdcTestNetwork;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author Chamseddine BENHAMED  <chamseddine.benhamed at rte-france.com>
 */

public class IncrementalExportTest extends AbstractConverterTest {
    private Network getEurostagLfNetwork() {
        XMLImporter importer = new XMLImporter(new InMemoryPlatformConfig(fileSystem));
        ReadOnlyDataSource dataSource = new ResourceDataSource("eurostag-tutorial1-lf", new ResourceSet("/", "eurostag-tutorial1-lf.xml"));
        Network network = importer.importData(dataSource, new Properties());
        return network;
    }

    public void incrementalExport(Network network, String prefix) throws IOException {
        Properties properties;
        properties = new Properties();
        properties.put(XMLExporter.ANONYMISED, "false");
        properties.put(XMLExporter.IMPORT_EXPORT_TYPE, String.valueOf(IidmImportExportType.INCREMENTAL_IIDM));
        MemDataSource dataSource = new MemDataSource();
        new XMLExporter().export(network, properties, dataSource);

        try (InputStream is = new ByteArrayInputStream(dataSource.getData("-CONTROL.xiidm"))) {
            compareXml(getClass().getResourceAsStream("/" + prefix + "-CONTROL.xiidm"), is);
        }
        try (InputStream is = new ByteArrayInputStream(dataSource.getData("-STATE.xiidm"))) {
            compareXml(getClass().getResourceAsStream("/" + prefix + "-STATE.xiidm"), is);
        }
        try (InputStream is = new ByteArrayInputStream(dataSource.getData("-TOPO.xiidm"))) {
            compareXml(getClass().getResourceAsStream("/" + prefix + "-TOPO.xiidm"), is);
        }
    }

    @Test
    public void exportNetworkWithLoadFlow() throws IOException {
        Network network = getEurostagLfNetwork();
        incrementalExport(network, "Incremental");
    }

    @Test
    public void exportHvdcTestNetworkLcc() throws IOException {
        Network networkLcc = HvdcTestNetwork.createLcc();
        incrementalExport(networkLcc, "lcc");
    }

    @Test
    public void exportHvdcTestNetworkVsc() throws IOException {
        Network networkVsc = HvdcTestNetwork.createVsc();
        incrementalExport(networkVsc, "vsc");
    }

}
