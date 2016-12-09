package tess4j;

import java.io.File;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

public class TesseractInstance {
	
	public static String doOCR(File file){
		ITesseract instance = new Tesseract(); // JNA Interface Mapping
        // ITesseract instance = new Tesseract1(); // JNA Direct Mapping

        try {
            String result = instance.doOCR(file);
            return result;
        } catch (TesseractException e) {
            System.err.println(e.getMessage());
        }
        return null;
	}

}
