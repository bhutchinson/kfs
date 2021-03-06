/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 * 
 * Copyright 2005-2014 The Kuali Foundation
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kuali.kfs.sys.batch;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.core.api.config.property.ConfigurationService;
import org.kuali.rice.core.api.util.KeyValue;
import org.kuali.rice.kns.service.DataDictionaryService;
import org.kuali.rice.krad.keyvalues.KeyValuesFinder;

public class BatchFileUtils {
    public static List<File> retrieveBatchFileLookupRootDirectories() {
        ConfigurationService kualiConfigurationService = SpringContext.getBean(ConfigurationService.class);
        List<File> directories = new ArrayList<File>();
        String configProperty = kualiConfigurationService.getPropertyValueAsString(KFSConstants.BATCH_FILE_LOOKUP_ROOT_DIRECTORIES);

        String[] directoryNames = StringUtils.split(configProperty, ";");
        for (String directoryName : directoryNames) {
            File rootDirectory = new File(directoryName).getAbsoluteFile();
            directories.add(rootDirectory);
        }

        // sanity check: make sure directories are set up so that they will not present problems for pathRelativeToRootDirectory and
        // resolvePathToAbsolutePath methods
        for (int i = 0; i < directories.size(); i++) {
            for (int j = i + 1; j < directories.size(); j++) {
                File directoryI = directories.get(i);
                File directoryJ = directories.get(j);

                if (isPrefixOfAnother(directoryI.getAbsolutePath(), directoryJ.getAbsolutePath())) {
                    throw new RuntimeException("Cannot have any two directories in config property batch.file.lookup.root.directories that have absolute paths that are prefix of another");
                }
                if (isPrefixOfAnother(directoryI.getName(), directoryJ.getName())) {
                    throw new RuntimeException("Cannot have any two directories in config property batch.file.lookup.root.directories that have names that are prefix of another");
                }
            }
        }
        return directories;
    }

    private static boolean isPrefixOfAnother(String str1, String str2) {
        return str1.startsWith(str2) || str2.startsWith(str1);
    }

    /**
     * returns a path relative to the appropriate lookup root directory, while including the name of the root directory for example,
     * if the parameter is "c:\opt\staging\gl\somefile.txt" and the roots are "c:\opt\reports;c:\opt\staging", it will return
     * "staging\gl\somefile.txt" (the system-specific path separator will be used). If there are multiple matching roots, then the
     * first one to be matched will take precedence
     * 
     * @param absolutePath an absolute path for a file/directory
     */
    public static String pathRelativeToRootDirectory(String absolutePath) {
        for (File rootDirectory : retrieveBatchFileLookupRootDirectories()) {
            if (absolutePath.startsWith(rootDirectory.getAbsolutePath())) {
                return StringUtils.replaceOnce(absolutePath, rootDirectory.getAbsolutePath(), rootDirectory.getName());
            }
        }
        throw new RuntimeException("Unable to find appropriate root directory)");
    }


    /**
     * @param path a path string that was generated by {@link #pathRelativeToRootDirectory(String)}
     * @return an absolute path, including the root directory
     */
    public static String resolvePathToAbsolutePath(String path) {
        for (File rootDirectory : retrieveBatchFileLookupRootDirectories()) {
            if (path.startsWith(rootDirectory.getName())) {
                return new File(StringUtils.replaceOnce(path, rootDirectory.getName(), rootDirectory.getAbsolutePath())).getAbsolutePath();
            }
        }
        throw new RuntimeException("Cannot resolve to absolute path");
    }

    public static boolean isDirectoryAccessible(String directory) {
        List<String> pathNames = null;

        Class<? extends KeyValuesFinder> keyValuesFinderClass = SpringContext.getBean(DataDictionaryService.class).getAttributeValuesFinderClass(BatchFile.class, "path");
        try {
            if (keyValuesFinderClass != null) {
                KeyValuesFinder valuesGenerator = keyValuesFinderClass.newInstance();
                pathNames = new ArrayList<String>();

                List<KeyValue> keyValues = valuesGenerator.getKeyValues();
                for (KeyValue keyValue : keyValues) {
                    pathNames.add(new File(resolvePathToAbsolutePath((String) keyValue.getKey())).getAbsolutePath());
                }
            }
        }
        catch (IllegalAccessException e) {
            throw new RuntimeException("can't instiantiate class " + keyValuesFinderClass, e);
        }
        catch (InstantiationException e) {
            throw new RuntimeException("can't instiantiate class " + keyValuesFinderClass, e);
        }

        File directoryAbsolute = new File(directory).getAbsoluteFile();
        String directoryAbsolutePath = directoryAbsolute.getAbsolutePath();
        if (pathNames != null) {
            if (!pathNames.contains(directoryAbsolutePath)) {
                return false;
            }
        }

        List<File> rootDirectories = retrieveBatchFileLookupRootDirectories();
        for (File rootDirectory : rootDirectories) {
            if (isSuperDirectoryOf(rootDirectory, directoryAbsolute)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isSuperDirectoryOf(File superDirectory, File directory) {
        superDirectory = superDirectory.getAbsoluteFile();

        while (directory != null) {
            directory = directory.getAbsoluteFile();
            if (directory.equals(superDirectory)) {
                return true;
            }
            directory = directory.getParentFile();
        }
        return false;
    }
}
