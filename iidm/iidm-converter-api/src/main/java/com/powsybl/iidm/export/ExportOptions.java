/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.export;

import com.powsybl.iidm.network.TopologyLevel;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public class ExportOptions {

    private boolean withBranchSV = true;

    private boolean indent = true;

    private boolean onlyMainCc = false;

    private boolean anonymized = false;

    private boolean skipExtensions = false;

    private TopologyLevel topologyLevel = TopologyLevel.NODE_BREAKER;

    private boolean throwExceptionIfExtensionNotFound = false;

    private boolean separateBaseAndExtensions = false;

    private boolean oneFilePerExtensionType = false;

    private List<String> extensions = Arrays.asList("ALL");

    public ExportOptions() {
    }

    public ExportOptions(boolean withBranchSV, boolean indent, boolean onlyMainCc, TopologyLevel topologyLevel, boolean throwExceptionIfExtensionNotFound, List<String> extensions) {
        this.withBranchSV = withBranchSV;
        this.indent = indent;
        this.onlyMainCc = onlyMainCc;
        this.topologyLevel = Objects.requireNonNull(topologyLevel);
        this.throwExceptionIfExtensionNotFound = throwExceptionIfExtensionNotFound;
        this.extensions = extensions;
    }

    public boolean isOneFilePerExtensionType() {
        return oneFilePerExtensionType;
    }

    public boolean isWithBranchSV() {
        return withBranchSV;
    }

    public ExportOptions setWithBranchSV(boolean withBranchSV) {
        this.withBranchSV = withBranchSV;
        return this;
    }

    public boolean isIndent() {
        return indent;
    }

    public ExportOptions setIndent(boolean indent) {
        this.indent = indent;
        return this;
    }

    public boolean isSeparateBaseAndExtensions() {
        return separateBaseAndExtensions;
    }

    public ExportOptions setSeparateBaseAndExtensions(boolean separateBaseAndExtensions) {
        this.separateBaseAndExtensions = separateBaseAndExtensions;
        return this;
    }

    public ExportOptions setOneFilePerExtensionType(boolean oneFilePerExtensionType) {
        this.oneFilePerExtensionType = oneFilePerExtensionType;
        return this;
    }

    public boolean isOnlyMainCc() {
        return onlyMainCc;
    }

    public ExportOptions setOnlyMainCc(boolean onlyMainCc) {
        this.onlyMainCc = onlyMainCc;
        return this;
    }

    public boolean isAnonymized() {
        return anonymized;
    }

    public ExportOptions setAnonymized(boolean anonymized) {
        this.anonymized = anonymized;
        return this;
    }

    public boolean isSkipExtensions() {
        return skipExtensions;
    }

    public ExportOptions setSkipExtensions(boolean skipExtensions) {
        this.skipExtensions = skipExtensions;
        return this;
    }

    public TopologyLevel getTopologyLevel() {
        return topologyLevel;
    }

    public ExportOptions setTopologyLevel(TopologyLevel topologyLevel) {
        this.topologyLevel = Objects.requireNonNull(topologyLevel);
        return this;
    }

    public ExportOptions setExtensions(List<String> extensions) {
        this.extensions = extensions;
        return this;
    }

    public boolean isThrowExceptionIfExtensionNotFound() {
        return throwExceptionIfExtensionNotFound;
    }

    public ExportOptions setThrowExceptionIfExtensionNotFound(boolean throwException) {
        this.throwExceptionIfExtensionNotFound = throwException;
        return this;
    }

    public  boolean isInExtensionsList(String str) {
        return extensions.contains(str);
    }

    public  boolean isALL() {
        return extensions.size() == 1 && extensions.get(0).equals("ALL");
    }

    public boolean isExtensionsEmpty() {
        return extensions.isEmpty();
    }
}
