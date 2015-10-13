package org.talend.dataquality.statistics.text;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.talend.datascience.common.inference.Analyzer;

public class TextLengthAnalyzerTest {

    TextLengthAnalyzer analyzer = new TextLengthAnalyzer();

    @Before
    public void setUp() throws Exception {
        analyzer.init();
    }

    @After
    public void tearDown() throws Exception {
        analyzer.end();
    }

    @Test
    public void testAnalyze() {
        String[] data = new String[] { "Brayan", "Ava", " ", "" };
        for (String value : data) {
            analyzer.analyze(value);
        }
        TextLengthStatistics stats = analyzer.getResult().get(0);
        // Min
        Assert.assertEquals(0, stats.getMinTextLength(), 0);
        Assert.assertEquals(3, stats.getMinTextLengthIgnoreBlank(), 0);
        // Max
        Assert.assertEquals(6, stats.getMaxTextLength(), 0);
        Assert.assertEquals(6, stats.getMaxTextLengthIgnoreBlank(), 0);
        // Avg
        Assert.assertEquals(2.5, stats.getAvgTextLength(), 0);
        Assert.assertEquals(4.5, stats.getAvgTextLengthIgnoreBlank(), 0);

    }

    @Test
    public void testAnalyzeWithNullValue() {
        String[] data = new String[] { "          ", "Brayan", "Ava", " ", null };
        for (String value : data) {
            analyzer.analyze(value);
        }
        TextLengthStatistics stats = analyzer.getResult().get(0);
        // Min
        Assert.assertEquals(1, stats.getMinTextLength(), 0);
        Assert.assertEquals(3, stats.getMinTextLengthIgnoreBlank(), 0);
        // Max
        Assert.assertEquals(10, stats.getMaxTextLength(), 0);
        Assert.assertEquals(6, stats.getMaxTextLengthIgnoreBlank(), 0);
        // Avg
        Assert.assertEquals(5, stats.getAvgTextLength(), 0);
        Assert.assertEquals(4.5, stats.getAvgTextLengthIgnoreBlank(), 0);

    }

    @Test
    public void testMerge() {
        String[] data = new String[] { "          ", "Brayan", "Ava", " ", null };
        String[] data2 = new String[] { "          ", "Brayan", "Ava", " ", null };
        Analyzer<TextLengthStatistics> analyzer1 = new TextLengthAnalyzer();
        Runnable r1 = new Runnable() {

            @Override
            public void run() {
                analyzer1.init();
                for (String record : data) {
                    analyzer1.analyze(record);
                }
                analyzer1.end();
            };
        };
        try {
            analyzer1.close();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        Analyzer<TextLengthStatistics> analyzer2 = new TextLengthAnalyzer();
        Runnable r2 = new Runnable() {

            @Override
            public void run() {
                analyzer2.init();
                for (String record : data2) {
                    analyzer2.analyze(record);
                }
                analyzer2.end();
            };
        };
        List<Thread> workers = new ArrayList<>();
        workers.add(new Thread(r1));
        workers.add(new Thread(r2));
        for (Thread worker : workers) {
            worker.start();
        }
        for (Thread worker : workers) {
            try {
                worker.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        Analyzer<TextLengthStatistics> mergedAnalyzer = analyzer1.merge(analyzer2);
        TextLengthStatistics stats = mergedAnalyzer.getResult().get(0);
        // Min
        Assert.assertEquals(1, stats.getMinTextLength(), 0);
        Assert.assertEquals(3, stats.getMinTextLengthIgnoreBlank(), 0);
        // Max
        Assert.assertEquals(10, stats.getMaxTextLength(), 0);
        Assert.assertEquals(6, stats.getMaxTextLengthIgnoreBlank(), 0);
        // Avg
        Assert.assertEquals(5, stats.getAvgTextLength(), 0);
        Assert.assertEquals(4.5, stats.getAvgTextLengthIgnoreBlank(), 0);

    }

    @Test
    public void testEmpties() {
        String[] data = new String[] { "  gmail.", "  " };
        for (String value : data) {
            analyzer.analyze(value);
        }
        TextLengthStatistics stats = analyzer.getResult().get(0);
        Assert.assertEquals(5, stats.getAvgTextLength(), 0);
        Assert.assertEquals(8, stats.getAvgTextLengthIgnoreBlank(), 0);
    }
}
