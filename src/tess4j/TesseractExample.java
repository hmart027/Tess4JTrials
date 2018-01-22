package tess4j;

import static net.sourceforge.lept4j.ILeptonica.L_CLONE;
import static net.sourceforge.tess4j.ITessAPI.TRUE;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

import image.tools.ITools;
import image.tools.IViewer;
import img.ImageManipulation;
import net.sourceforge.lept4j.Box;
import net.sourceforge.lept4j.Boxa;
import net.sourceforge.lept4j.Leptonica;
import net.sourceforge.lept4j.Pix;
import net.sourceforge.lept4j.util.LeptUtils;
import net.sourceforge.tess4j.*;
import net.sourceforge.tess4j.ITessAPI.TessBaseAPI;
import net.sourceforge.tess4j.ITessAPI.TessPageIteratorLevel;
import net.sourceforge.tess4j.ITessAPI.TessPageSegMode;

public class TesseractExample {
	
	IViewer view;
	byte[][][] img;

    private static String datapath = "/mnt/Research/Harold/software/tesseract/";
    private static String language = "eng_lstm";
    
    TessAPI api;
    TessBaseAPI handle;
    Leptonica leptInstance;
    Pix pix;
    Boxa boxes;
    
    int boxCount = 0;
    int cBox = 0;
    
    TesseractExample(){
        
        File image = new File("/home/harold/Pictures/book1.tiff");
        
//        language = "chi_sim_lstm";
//        image = new File("/home/harold/Pictures/chen1.jpg");

//        language = "Thai_lstm";
//        image = new File("/home/harold/Pictures/jar1.jpeg");
        
        img = ImageManipulation.loadImage(image.getAbsolutePath());
        view = new IViewer(ImageManipulation.getBufferedImage(img));
        view.addWindowListener(new java.awt.event.WindowAdapter(){
        	 @Override
             public void windowClosing(java.awt.event.WindowEvent e)
             {
                 onDestroy();
                 e.getWindow().dispose();
             }
        });
        
//        ITesseract instance = new Tesseract(); // JNA Interface Mapping
//        // ITesseract instance = new Tesseract1(); // JNA Direct Mapping
//
//        try {
//            String result = instance.doOCR(image);
//            System.out.println(result);
//        } catch (TesseractException e) {
//            System.err.println(e.getMessage());
//        }
        
        onCreate();
        leptInstance = Leptonica.INSTANCE;
        pix = leptInstance.pixRead(image.getPath());
        api.TessBaseAPIInit3(handle, datapath, language);
        api.TessBaseAPISetImage2(handle, pix);
        api.TessBaseAPISetPageSegMode(handle, TessPageSegMode.PSM_SINGLE_BLOCK);
        PointerByReference pixa = null;
        PointerByReference blockids = null;
        boxes = api.TessBaseAPIGetComponentImages(handle, TessPageIteratorLevel.RIL_TEXTLINE, TRUE, pixa, blockids);
//        boxes = api.TessBaseAPIGetRegions(handle, pixa); // equivalent to TessPageIteratorLevel.RIL_BLOCK
        boxCount = leptInstance.boxaGetCount(boxes);
        cBox = 0;
    	System.out.println("There are "+boxCount+" boxes");
    	
        processBox();
        
        view.addKeyListener(new java.awt.event.KeyListener(){

			public void keyTyped(KeyEvent e) {}

			public void keyPressed(KeyEvent e) {
				System.out.println(e.getKeyCode());
				switch(e.getKeyCode()){
				case 107:
					cBox++;
					if(cBox>=boxCount) cBox = boxCount-1;
					processBox();
					break;
				case 109:
					cBox--;
					if(cBox<0) cBox = 0;
					processBox();
					break;
				}
				
			}

			public void keyReleased(KeyEvent e) {}
		});
    }
    
    public void processBox(){
		Box box = leptInstance.boxaGetBox(boxes, cBox, L_CLONE);
		if (box == null) {
			return;
		}
		BufferedImage i = ImageManipulation.getBufferedImage(img);
		Graphics2D graph = (Graphics2D) i.getGraphics();
		graph.setStroke(new BasicStroke(3));
		graph.setColor(java.awt.Color.RED);
		graph.drawRect(box.x, box.y, box.w, box.h);
		view.setImage(i);

		api.TessBaseAPISetRectangle(handle, box.x, box.y, box.w, box.h);
		int conf = -1;
		Pointer utf8Text = api.TessBaseAPIGetUTF8Text(handle);
		String ocrResult = utf8Text.getString(0);
		api.TessDeleteText(utf8Text);
		conf = api.TessBaseAPIMeanTextConf(handle);
		System.out.print(String.format("Box[%d]: x=%d, y=%d, w=%d, h=%d, confidence: %d, text: %s \n", cBox, box.x,
				box.y, box.w, box.h, conf, ocrResult));
		LeptUtils.dispose(box);
    }
    
    private void onCreate(){
        // Init
        api = new TessAPIImpl().getInstance();
        handle = api.TessBaseAPICreate();
    }
    
    private void onDestroy(){
        // release Pix and Boxa resources
        LeptUtils.dispose(pix);
        LeptUtils.dispose(boxes);
        
        //Destroy
        api.TessBaseAPIDelete(handle);
    }

    public static void main(String[] args) {
    	new TesseractExample();
    }
}