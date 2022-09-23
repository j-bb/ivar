/*
   Copyright (c) 2004-2020, Jean-Baptiste BRIAUD. All Rights Reserved.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License
 */
package ivar.ivarc;

import ivar.common.AbstractObject;
import ivar.common.logger.FullLogger;
import ivar.common.logger.LoggerDefaultImplementation;
import ivar.helper.CollectionFactory;
import ivar.helper.oo.ClassHelper;
import java.io.File;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
public class BasicGrammarFoundationTest extends AbstractObject {

    public static final String acceptedTestFolder = "test-grammar/accepted";
    public static final String rejectedTestFolder = "test-grammar/rejected";
    public static final File acceptedTestFolderFile = new File(acceptedTestFolder);
    public static final File rejectedTestFolderFile = new File(rejectedTestFolder);
    // Map of "test name", Set of file name"
    public static final Map<String, Set<String>> testOK = CollectionFactory.newMapWithInsertionOrderPreserved();
    public static final Map<String, Set<String>> testKO = CollectionFactory.newMapWithInsertionOrderPreserved();
    private int globalDysimetry = 0;

    public BasicGrammarFoundationTest() {
        System.out.println("BasicGrammarFoundationTest constructor");
    }

//    public void mainTest() {
//        System.out.println("BasicGrammarFoundationTest.main ");
//
//        System.out.println();
//        beforeAll();
//        BasicGrammarFoundationTest test = new BasicGrammarFoundationTest();
//        test.checkAcceptedGrammar();
//        test.checkRejectedGrammar();
//        test.summary();
//    }
    @BeforeAll
    public static void beforeAll() {
        checkTestFolders(acceptedTestFolderFile);
        checkTestFolders(rejectedTestFolderFile);
    }

    private static void checkTestFolders(final File testFolder) {
        if (testFolder == null) {

            throw new RuntimeException("Test folder is null");
        }
        if (!testFolder.exists()) {
            throw new RuntimeException("Test folder doesn't exists " + testFolder.getPath());
        }
        if (!testFolder.isDirectory()) {
            throw new RuntimeException("Test Folder is not a folder " + testFolder.getPath());
        }
        if (!testFolder.canRead()) {
            throw new RuntimeException("Cannot read test folder " + testFolder.getPath());
        }
    }

    @AfterAll
    public void summary() {
        getLogger().flush();
        emptyLine();
        emptyLine();
        info("########################################");
        emptyLine();

        boolean ok = true;
        beginBlock("########## " + ClassHelper.getShortName(this.getClass()) + " detailed sumary ##########");

        int[] filesNumber = summaryForSet("accepted");
        if (filesNumber[1] != 0) {
            compileError("=> NO GO HOUSTON. It's a no go. There was wrong files in the accepted grammar test");
            ok = false;
        } else {
            compileInfo("=> Green ligh Houston. It's a go. All files are accapted in the accepted grammar test");

            ok = ok && true;
        }

        emptyLine();

        filesNumber = summaryForSet("rejected");
        if (filesNumber[0] != 0) {
            compileError("=> NO GO HOUSTON. It's a no go. There was good files in the rejected grammar test");
            ok = false;
        } else {
            compileInfo("=> Green ligh Houston. It's a go. All files are wrong in the rejected grammar test");
            ok = ok && true;
        }
        int totalTest = 0;
        for (final String testKeys : testOK.keySet()) {
            totalTest += testOK.get(testKeys).size();
            totalTest += testKO.get(testKeys).size();
        }
        endBlock("########## " + ClassHelper.getShortName(this.getClass()) + " detailed sumary. ##########");
        emptyLine();
        info("==>> Final result ########## " + ClassHelper.getShortName(this.getClass()) + " : ran " + totalTest + " test. GLOBAL STATUS is " + (ok ? "OK, green light Houston" : "KO, I repeat KO, roger that Houston."));
        if (globalDysimetry > 0) {
            info("==>> Houston, we detected " + globalDysimetry + " global dysimetry. Check for beginBlock / endBlock dysimetry on code.");
        } else {
            info("==>> Global dysimetry check: OK.");
        }
    }

    // Return[0] number of ok files for the test key
    // Return[1] number of ko filess for the test key
    private int[] summaryForSet(final String testKey) {
        int[] result = {0, 0};
        final Set<String> filesOK = testOK.get(testKey);
        final Set<String> filesKO = testKO.get(testKey);
        final int totalFiles = filesOK.size() + filesKO.size();

        beginBlock(testKey + " test. " + totalFiles + " total files.");

        result[0] = summaryForFile(filesOK, " OK files for " + testKey + " out of " + totalFiles);
        result[1] = summaryForFile(filesKO, " KO files for " + testKey + " out of " + totalFiles);
        endBlock();
        return result;
    }

    private int summaryForFile(final Set<String> files, final String trace) {
        int result = files.size();
        beginBlock(result + trace);
        for (final String file : files) {
            info(" - " + file);
        }
        endBlock();
        return result;
    }

    @Test
    public void acceptedGrammarTest() {
        beginBlock("Testing accepted grammar from " + acceptedTestFolderFile.getAbsolutePath());
        Set<String> filesok = CollectionFactory.newSetWithInsertionOrderPreserved();
        Set<String> filesko = CollectionFactory.newSetWithInsertionOrderPreserved();
        final File[] listFiles = acceptedTestFolderFile.listFiles();
        info("Found " + listFiles.length + " files.");
        for (File file : listFiles) {
            if (file.getName().endsWith(".ivar")) {
                checkIvarFile(this.getLogger(), file, filesok, filesko, true);
            } else {
                info("Not an Ivar file " + file.getName());
            }
        }
        testOK.put("accepted", filesok);
        testKO.put("accepted", filesko);
        endBlock();
        if (!filesko.isEmpty()) {
            compileError("Some accepted Ivar files are wrong.");
        }
        getLogger().flush();
    }

    @Test
    public void checkRejectedGrammar() {
        beginBlock("Testing rejected grammar from " + rejectedTestFolderFile.getAbsolutePath());
        Set<String> filesok = CollectionFactory.newSetWithInsertionOrderPreserved();
        Set<String> filesko = CollectionFactory.newSetWithInsertionOrderPreserved();
        final File[] listFiles = rejectedTestFolderFile.listFiles();
        info("Found " + listFiles.length + " files.");
        for (File file : listFiles) {
            if (file.getName().endsWith(".ivar")) {
                checkIvarFile(this.getLogger(), file, filesok, filesko, false);
            } else {
                info("Not an Ivar file " + file.getName());
            }
        }
        testOK.put("rejected", filesok);
        testKO.put("rejected", filesko);
        endBlock();
        if (!filesok.isEmpty()) {
            compileError("Some rejected Ivar files are right.");
        }
        getLogger().flush();
    }

    private void checkIvarFile(final FullLogger logger, final File file, final Set<String> filesok, final Set<String> filesko, boolean acceptedSection) {
        int tabBefore = logger.getTabValue();
        logger.beginBlock("Testing " + file.getName());
        logger.flush();
        final IvarcParameters params = new IvarcParameters(file.getAbsolutePath(), true, true, false);
        boolean test = true;
        try {
            Ivarc.ivarc(logger, params);
            filesok.add(file.getName());
        } catch (IvarcException e) {
            logger.error("IvarcException raised during testing of " + file.getName() + ". " + e.getMessage(), e);
            filesko.add(file.getName());
            test = false;
        }
        logger.endBlock("Testing " + file.getName() + ". Done. Test result " + test);
        int tabAfter = logger.getTabValue();
        // The following if to get back to parity when tests failed. It break into a block,
        // whiwh is normal for a rejected test,
        // and it break readability of result.
        if (tabBefore != tabAfter) {
            if (!acceptedSection) {
                logger.info("Resetting tab value to " + tabBefore + ". It was " + tabAfter);
                LoggerDefaultImplementation loggerDefaultImplementation = (LoggerDefaultImplementation) logger;
                loggerDefaultImplementation.setTabValue(tabBefore);
            } else {
                globalDysimetry++;
                logger.emptyLine();
                logger.error("********** GLobal dysimetry #" + globalDysimetry);
                logger.error("********** Tab value are wrong after testing file " + file.getAbsolutePath() + ". Before " + tabBefore + ". After " + tabAfter + ". REVIEW code for beginBlock / endBlock symetry.");
                logger.error("**********");
                logger.emptyLine();
            }
        }
        emptyLine();
        logger.flush();
    }
}
