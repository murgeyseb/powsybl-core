/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.elements;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.model.PowerFlow;
import com.powsybl.iidm.network.EnergySource;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.GeneratorAdder;
import com.powsybl.triplestore.api.PropertyBag;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class SynchronousMachineConversion extends AbstractReactiveLimitsOwnerConversion {

    public SynchronousMachineConversion(PropertyBag sm, Context context) {
        super("SynchronousMachine", sm, context);
    }

    @Override
    public void convert() {
        double minP = p.asDouble("minP", -Double.MAX_VALUE);
        double maxP = p.asDouble("maxP", Double.MAX_VALUE);
        double ratedS = p.asDouble("ratedS");
        ratedS = ratedS > 0 ? ratedS : Double.NaN;
        String generatingUnitType = p.getLocal("generatingUnitType");
        PowerFlow f = powerFlow();

        // Default targetP from initial P defined in EQ GeneratingUnit
        double targetP = p.asDouble("initialP", 0);
        double targetQ = 0;
        // Flow values may come from Terminal or Equipment (SSH RotatingMachine)
        if (f.defined()) {
            targetP = -f.p();
            targetQ = -f.q();
        }

        GeneratorAdder adder = voltageLevel().newGenerator();
        context.regulatingControlMapping().setRegulatingControl(iidmId(), p, adder, voltageLevel());
        adder.setMinP(minP)
                .setMaxP(maxP)
                .setTargetP(targetP)
                .setTargetQ(targetQ)
                .setEnergySource(fromGeneratingUnitType(generatingUnitType))
                .setRatedS(ratedS);
        identify(adder);
        connect(adder);
        Generator g = adder.add();
        convertedTerminals(g.getTerminal());
        convertReactiveLimits(g);
    }

    private static EnergySource fromGeneratingUnitType(String gut) {
        EnergySource es = EnergySource.OTHER;
        if (gut.contains("HydroGeneratingUnit")) {
            es = EnergySource.HYDRO;
        } else if (gut.contains("NuclearGeneratingUnit")) {
            es = EnergySource.NUCLEAR;
        } else if (gut.contains("ThermalGeneratingUnit")) {
            es = EnergySource.THERMAL;
        } else if (gut.contains("WindGeneratingUnit")) {
            es = EnergySource.WIND;
        }
        return es;
    }
}
