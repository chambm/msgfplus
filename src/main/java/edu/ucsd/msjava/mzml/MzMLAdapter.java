package edu.ucsd.msjava.mzml;

import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;

import edu.ucsd.msjava.mzid.Constants;
import uk.ac.ebi.jmzidml.model.mzidml.CvParam;
import uk.ac.ebi.jmzml.model.mzml.CVParam;
import uk.ac.ebi.jmzml.model.mzml.SourceFile;
import uk.ac.ebi.jmzml.xml.io.MzMLObjectIterator;
import uk.ac.ebi.jmzml.xml.io.MzMLUnmarshaller;

import java.io.File;

public class MzMLAdapter {

    private final File specFile;
    private MzMLUnmarshaller unmarshaller;
    private int minMSLevel = 2;        // inclusive
    private int maxMSLevel = Integer.MAX_VALUE;        // exclusive
    private CvParam spectrumIDFormatCvParam = null;

    public MzMLAdapter(File specFile) {
        turnOffLogs();
        this.specFile = specFile;
        unmarshaller = new MzMLUnmarshaller(specFile);
    }

    /**
     * Setter to set msLevel.
     *
     * @param minMSLevel minimum msLevel to be considered (inclusive).
     * @param maxMSLevel maximum msLevel to be considered (inclusive).
     * @return this object.
     */
    public MzMLAdapter msLevel(int minMSLevel, int maxMSLevel) {
        this.minMSLevel = minMSLevel;
        this.maxMSLevel = maxMSLevel;
        return this;
    }

    public MzMLUnmarshaller getUnmarshaller() {
        return unmarshaller;
    }

    public int getMinMSLevel() {
        return minMSLevel;
    }

    public int getMaxMSLevel() {
        return maxMSLevel;
    }

    public CvParam getSpectrumIDFormatCvParam() {
        if (spectrumIDFormatCvParam != null)
            return spectrumIDFormatCvParam;

        MzMLObjectIterator<SourceFile> itr = unmarshaller.unmarshalCollectionFromXpath("/fileDescription/sourceFileList/sourceFile", SourceFile.class);
        while (itr.hasNext()) {
            SourceFile sourceFile = itr.next();
            for (CVParam param : sourceFile.getCvParam()) {
                String tempAcc = param.getAccession();
                long accNum = Long.parseLong(tempAcc.substring(tempAcc.lastIndexOf(':') + 1));
                if (accNum >= 1000768 && accNum <= 1000777
                        || accNum == 1000823 || accNum == 1000824 || accNum == 1000929
                        || accNum == 1001508 || accNum == 1001526 || accNum == 1001528
                        || accNum == 1001531 || accNum == 1001532
                        || accNum == 1001559 || accNum == 1001562
                        ) {
                    spectrumIDFormatCvParam = Constants.makeCvParam(param.getAccession(), param.getName());
                    return spectrumIDFormatCvParam;
                }
            }
        }

        System.err.println("Unsupported mzML format: " + specFile.getAbsolutePath() + " does not contain a child term of MS:1000767 (native spectrum identifier format)");
        System.err.println("Unsupported mzML format: " + specFile.getAbsolutePath());
        System.exit(-1);
        return null;
    }

    private static boolean logOff = false;

    public static void turnOffLogs() {
        if (!logOff) {
            @SuppressWarnings("unchecked")
            LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
            context.reset();
            Logger rootLogger = context.getLogger(Logger.ROOT_LOGGER_NAME);
            rootLogger.detachAndStopAllAppenders();
        }
    }

    public static void main(String argv[]) throws Exception {
        test();
    }

    public static void test() throws Exception {
        long time = System.currentTimeMillis();
        File specFile = new File(System.getProperty("user.home") + "/Research/Data/QCShew/QC_Shew_12_02_2_1Aug12_Cougar_12-06-11.mzML");
        MzMLAdapter adapter = new MzMLAdapter(specFile);
        System.out.println("Unmarshaller: " + (System.currentTimeMillis() - time) / 1000);
        System.out.println(adapter.getSpectrumIDFormatCvParam().getAccession() + " " + adapter.getSpectrumIDFormatCvParam().getName());


//		System.out.println("Supported XPath:");
//		for(String xpath : uk.ac.ebi.jmzml.xml.Constants.XML_INDEXED_XPATHS)
//			if(xpath.contains("fileDescription"))
//				System.out.println(xpath);
    }
}
