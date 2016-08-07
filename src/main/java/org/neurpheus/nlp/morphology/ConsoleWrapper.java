/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.neurpheus.nlp.morphology;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import javax.swing.JTextPane;

/**
 *
 * @author jstrychowski
 */
public class ConsoleWrapper extends PrintStream {
    
    private PrintStream out;
    private JTextPane textComponent;
    
    public ConsoleWrapper(PrintStream baseOut, JTextPane textPane) {
        super(new ByteArrayOutputStream());
        out = baseOut;
        textComponent = textPane;
    }
    
    public void flush() {
        out.flush();
    }
    
    public boolean checkError() {
        return out.checkError();
    }
    
    

}
