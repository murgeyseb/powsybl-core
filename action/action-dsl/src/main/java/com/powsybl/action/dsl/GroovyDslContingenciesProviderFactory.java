/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.dsl;

import com.powsybl.commons.config.ModuleConfig;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.contingency.ContingenciesProviderFactory;

import java.nio.file.Path;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class GroovyDslContingenciesProviderFactory implements ContingenciesProviderFactory {

    @Override
    public GroovyDslContingenciesProvider create() {
        ModuleConfig config = PlatformConfig.defaultConfig().getModuleConfig("groovy-dsl-contingencies");
        Path dslFile = config.getPathProperty("dsl-file");
        return new GroovyDslContingenciesProvider(dslFile);
    }

    @Override
    public GroovyDslContingenciesProvider create(Path dslFile) {
        Objects.requireNonNull(dslFile);
        return new GroovyDslContingenciesProvider(dslFile);
    }

}
