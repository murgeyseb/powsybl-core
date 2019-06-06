/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.compress;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.zip.GZIPInputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 * @deprecated Use {@link ZipPackager} instead.
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
@Deprecated
public final class ZipHelper {

    /**
     * If the file is in .gz(detected by last 3 characters) format, the method decompresses .gz file first.
     *
     * @param baseDir   the base directory contaions files to zip
     * @param fileNames the files to be added in zip
     * @return bytes in zip format
     * @deprecated Use {@link ZipPackager#archiveFilesToZipBytes(Path, List)} instead.
     */
    @Deprecated
    public static byte[] archiveFilesToZipBytes(Path baseDir, List<String> fileNames) {
        return ZipPackager.archiveFilesToZipBytes(baseDir, fileNames);
    }

    public static byte[] archiveFilesToZipBytes(Path workingDir, String... fileNames) {
        return archiveFilesToZipBytes(workingDir, Arrays.asList(fileNames));
    }

    /**
     * Generates a zip file's bytes
     * @param bytesByName map's {@literal key} as entry name, {@literal value} as content in the zip file
     * @return a zip file in bytes
     */
    public static byte[] archiveBytesByNameToZipBytes(Map<String, byte[]> bytesByName) {
        Objects.requireNonNull(bytesByName);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos)) {
            bytesByName.forEach((name, bytes) -> {
                ZipArchiveEntry entry = new ZipArchiveEntry(name);
                try {
                    zos.putArchiveEntry(entry);
                    try (InputStream inputStream = new ByteArrayInputStream(bytes)) {
                        IOUtils.copy(inputStream, zos);
                    }
                    zos.closeArchiveEntry();
                } catch (IOException e) {
                    // ignored and continue
                }
            });
            zos.flush();
            zos.finish();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        try {
            baos.close();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return baos.toByteArray();
    }

    private ZipHelper() {
    }
}
