// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.dataquality.statistics.frequency;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.talend.dataquality.statistics.frequency.pattern.DatePatternRecognition;
import org.talend.dataquality.statistics.frequency.pattern.EastAsiaCharPatternRecognition;
import org.talend.dataquality.statistics.frequency.pattern.TimePatternRecognition;
import org.talend.dataquality.statistics.quality.DataTypeQualityAnalyzer;
import org.talend.datascience.common.inference.type.DataType;
import org.talend.datascience.common.inference.type.DatetimePatternManager;

public class PatternFrequencyAnalyzerTest {

    PatternFrequencyAnalyzer patternFreqAnalyzer = null;

    @Before
    public void setUp() throws Exception {
        patternFreqAnalyzer = new PatternFrequencyAnalyzer();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testAsciiAndAsiaChars() {
        PatternFrequencyAnalyzer analzyer = new PatternFrequencyAnalyzer();

        String patternString1 = analzyer.getValuePattern("abcd1234ィゥェ中国");
        Assert.assertEquals("aaaa9999ィゥェ中国", patternString1);

        String patternString2 = analzyer.getValuePattern("");
        Assert.assertEquals("", patternString2);

        String patternString3 = analzyer.getValuePattern(null);
        Assert.assertNull(patternString3);

        String patternString4 = analzyer.getValuePattern("2008-01-01");
        Assert.assertEquals("yyyy-M-d", patternString4);

        String patternString5 = analzyer.getValuePattern("2008-1月-01");
        Assert.assertEquals("9999-9月-99", patternString5);

    }

    @Test
    public void testRecognitionInjectAndRemoval() {
        PatternFrequencyAnalyzer analzyer = new PatternFrequencyAnalyzer();
        // Add the Easten Asia recognition
        analzyer.injectRecognizer(new EastAsiaCharPatternRecognition());
        String patternString2 = analzyer.getValuePattern("abcd1234ゟ");
        Assert.assertEquals("aaaa9999H", patternString2);
        // No East Asia recognition.
        analzyer.removeRecognizer(EastAsiaCharPatternRecognition.LEVEL);
        String patternString1 = analzyer.getValuePattern("abcd1234ゟ");
        Assert.assertEquals("aaaa9999ゟ", patternString1);

        // No date and time recognition
        analzyer.removeRecognizer(DatePatternRecognition.LEVEL);
        analzyer.removeRecognizer(TimePatternRecognition.LEVEL);
        String datePattern = analzyer.getValuePattern("2003-12-20");
        Assert.assertEquals("9999-99-99", datePattern);
        String timePattern = analzyer.getValuePattern("12:00:00");
        Assert.assertEquals("99:99:99", timePattern);
        // Add date recognition
        analzyer.injectRecognizer(new DatePatternRecognition());
        String datePattern1 = analzyer.getValuePattern("2003-12-20");
        Assert.assertEquals("yyyy-M-d", datePattern1);
        analzyer.injectRecognizer(new TimePatternRecognition());
        String timePattern1 = analzyer.getValuePattern("12:00:00");
        Assert.assertEquals("H:m:s", timePattern1);
    }

    @Test
    public void testAnalyzeFreqWithEastAsiaChar() {
        PatternFrequencyAnalyzer analyzerWithAsiaChars = new PatternFrequencyAnalyzer();
        analyzerWithAsiaChars.injectRecognizer(new EastAsiaCharPatternRecognition());
        String[] data = new String[] { "John", "", "2015-08-20", "2012-02-12", "2003年", "2004年", "2001年" };
        analyzerWithAsiaChars.init();
        for (String value : data) {
            analyzerWithAsiaChars.analyze(value);
        }
        analyzerWithAsiaChars.end();
        Map<String, Long> freqTable = analyzerWithAsiaChars.getResult().get(0).getTopK(10);
        Iterator<Entry<String, Long>> entrySet = freqTable.entrySet().iterator();
        int idx = 0;
        boolean isAtLeastOneAsssert = false;
        while (entrySet.hasNext()) {
            Entry<String, Long> e = entrySet.next();
            if (idx == 0) {
                Assert.assertEquals("9999C", e.getKey());
                Assert.assertEquals(3, e.getValue(), 0);
                isAtLeastOneAsssert = true;
            } else if (idx == 1) {
                Assert.assertEquals("yyyy-M-d", e.getKey());
                Assert.assertEquals(2, e.getValue(), 0);
                isAtLeastOneAsssert = true;
            }
            idx++;
        }
        Assert.assertTrue(isAtLeastOneAsssert);
    }

    @Test
    public void testAnalyze() {
        String[] data = new String[] { "John", "", "123Code", "111", "Zhao", "2015-08-20", "2012-02-12", "12/2/99", "Hois",
                "2001年" };
        for (String value : data) {
            patternFreqAnalyzer.analyze(value);
        }
        Map<String, Long> freqTable = patternFreqAnalyzer.getResult().get(0).getTopK(10);
        Iterator<Entry<String, Long>> entrySet = freqTable.entrySet().iterator();
        int idx = 0;
        boolean isAtLeastOneAsssert = false;
        while (entrySet.hasNext()) {
            Entry<String, Long> e = entrySet.next();
            if (idx == 0) {
                Assert.assertEquals("Aaaa", e.getKey());
                Assert.assertEquals(3, e.getValue(), 0);
                isAtLeastOneAsssert = true;
            } else if (idx == 1) {
                Assert.assertEquals("yyyy-M-d", e.getKey());
                Assert.assertEquals(2, e.getValue(), 0);
                isAtLeastOneAsssert = true;
            }
            if (e.getKey().equals("999Aaaa")) {
                Assert.assertEquals(1, e.getValue(), 0);
                isAtLeastOneAsssert = true;
            }
            idx++;
        }
        Assert.assertTrue(isAtLeastOneAsssert);
    }

    @Test
    public void testAnalyzerTwoColumns() {

        String[][] data = new String[][] { { "John", "filx" }, { "", "a" }, { "123Code", "3649273" }, { "111", "100" },
                { "Zhao", "silL" }, { "2015-08-20", "2015-08-21" }, { "2012-02-12", "2022-9-12" }, { "12/2/99", "12/2/99" },
                { "Hois", "*^2lii" }, { "2001年", "4445-" } };
        for (String[] value : data) {
            patternFreqAnalyzer.analyze(value);
        }
        Map<String, Long> freqTable = patternFreqAnalyzer.getResult().get(0).getTopK(10);
        Map<String, Long> freqTable2 = patternFreqAnalyzer.getResult().get(1).getTopK(10);
        Iterator<Entry<String, Long>> entrySet = freqTable.entrySet().iterator();
        Iterator<Entry<String, Long>> entrySet2 = freqTable2.entrySet().iterator();
        int idx = 0;
        boolean isAtLeastOneAsssert = false;
        while (entrySet.hasNext()) {
            Entry<String, Long> e = entrySet.next();
            if (idx == 0) {
                Assert.assertEquals("Aaaa", e.getKey());
                Assert.assertEquals(3, e.getValue(), 0);
                isAtLeastOneAsssert = true;
            } else if (idx == 1) {
                Assert.assertEquals("yyyy-M-d", e.getKey());
                Assert.assertEquals(2, e.getValue(), 0);
                isAtLeastOneAsssert = true;
            }
            if (e.getKey().equals("999Aaaa")) {
                Assert.assertEquals(1, e.getValue(), 0);
                isAtLeastOneAsssert = true;
            }
            idx++;
        }
        Assert.assertTrue(isAtLeastOneAsssert);

        isAtLeastOneAsssert = false;
        while (entrySet2.hasNext()) {
            Entry<String, Long> e = entrySet2.next();
            if (idx == 0) {
                Assert.assertEquals("yyyy-M-d", e.getKey());
                Assert.assertEquals(2, e.getValue(), 0);
                isAtLeastOneAsssert = true;
            }
            if (e.getKey().equals("9999999")) {
                Assert.assertEquals(1, e.getValue(), 0);
                isAtLeastOneAsssert = true;
            }
            if (e.getKey().equals("a")) {
                Assert.assertEquals(1, e.getValue(), 0);
                isAtLeastOneAsssert = true;
            }
            if (e.getKey().equals("d/M/yy")) {
                Assert.assertEquals(1, e.getValue(), 0);
                isAtLeastOneAsssert = true;
            }
            idx++;
        }
        Assert.assertTrue(isAtLeastOneAsssert);

    }

    @Test
    public void testCustomizedDatePattern() {
        PatternFrequencyAnalyzer patternAnalyzer = new PatternFrequencyAnalyzer();
        String[] data = new String[] { "11/19/07 2:54", "7/6/09 16:46", "2015-08-20", "2012-02-12", "2/8/15 15:57",
                "4/15/11 4:24", "2001年" };
        patternAnalyzer.init();
        for (String value : data) {
            patternAnalyzer.analyze(value);
        }
        patternAnalyzer.end();
        Map<String, Long> freqTable = patternAnalyzer.getResult().get(0).getTopK(10);
        Iterator<Entry<String, Long>> entrySet = freqTable.entrySet().iterator();
        int idx = 0;
        boolean isAtLeastOneAsssert = false;
        while (entrySet.hasNext()) {
            Entry<String, Long> e = entrySet.next();
            if (idx == 0) {
                Assert.assertEquals("9/9/99 99:99", e.getKey());
                Assert.assertEquals(2, e.getValue(), 0);
                isAtLeastOneAsssert = true;
            }
            idx++;
        }

        // Set customized pattern and analyze again
        DatetimePatternManager.getInstance().addCustomizedDatePattern("M/d/yy H:m");
        patternAnalyzer.init();
        for (String value : data) {
            patternAnalyzer.analyze(value);
        }
        patternAnalyzer.end();
        freqTable = patternAnalyzer.getResult().get(0).getTopK(10);
        entrySet = freqTable.entrySet().iterator();
        idx = 0;
        isAtLeastOneAsssert = false;
        while (entrySet.hasNext()) {
            Entry<String, Long> e = entrySet.next();
            if (idx == 0) {
                Assert.assertEquals("M/d/yy H:m", e.getKey());
                Assert.assertEquals(4, e.getValue(), 0);
                isAtLeastOneAsssert = true;
            }
            idx++;
        }
        // Add value quality analyzer to have list of valid date. some date matches patterns from the file, some matches
        // them in memory user set.
        DataTypeQualityAnalyzer qualityAnalyzer = new DataTypeQualityAnalyzer(DataType.Type.DATE);
        qualityAnalyzer.init();
        // 2-8-15 15:57 is not at date with pattern available,"2012-02-12" is a date match pattern from file, the others
        // match pattern set as in memory
        data = new String[] { "11/19/07 2:54", "7/6/09 16:46", "2/8/15 15:57", "2-8-15 15:57", "2012-02-12" };
        for (String value : data) {
            qualityAnalyzer.analyze(value);
        }
        qualityAnalyzer.end();
        qualityAnalyzer.getResult().get(0).getValidCount();
        Assert.assertEquals(5, qualityAnalyzer.getResult().get(0).getCount(), 0); // Count
        Assert.assertEquals(4, qualityAnalyzer.getResult().get(0).getValidCount()); // Valid Count
        // Invalid values
        Assert.assertTrue(qualityAnalyzer.getResult().get(0).getInvalidValues().size() == 1);
        Assert.assertEquals("2-8-15 15:57", qualityAnalyzer.getResult().get(0).getInvalidValues().toArray()[0]);

        // Add new customized pattern , create new quality analyzer , check again all dates should be valid if all
        // patterns provided.
        DataTypeQualityAnalyzer qualityAnalyzer2 = new DataTypeQualityAnalyzer(DataType.Type.DATE);
        // Cannot recognize "2-8-15 15:57" before add customized pattern
        qualityAnalyzer2.init();
        for (String value : data) {
            qualityAnalyzer2.analyze(value);
        }
        qualityAnalyzer2.end();
        Assert.assertEquals("2-8-15 15:57", qualityAnalyzer2.getResult().get(0).getInvalidValues().toArray()[0]);
        DatetimePatternManager.getInstance().addCustomizedDatePattern("M-d-yy H:m");// Add the pattern match it.
        qualityAnalyzer2.init();
        for (String value : data) {
            qualityAnalyzer2.analyze(value);
        }
        qualityAnalyzer2.end();
        Assert.assertEquals(5, qualityAnalyzer2.getResult().get(0).getCount()); // Count
        Assert.assertEquals(5, qualityAnalyzer2.getResult().get(0).getValidCount()); // Valid Count , all match.


        Assert.assertTrue(isAtLeastOneAsssert);
    }

}
