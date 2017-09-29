/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.entsoe.cases;

import com.powsybl.cases.CaseRepository;
import com.powsybl.cases.CaseRepositoryFactory;
import com.powsybl.computation.ComputationManager;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class EntsoeCaseRepositoryFactory implements CaseRepositoryFactory {
    @Override
    public CaseRepository create(ComputationManager computationManager) {
        return EntsoeCaseRepository.create(computationManager);
    }
}
