/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package j2r;

/**
 *
 * @author fastway
 */
public class SourceBuffer {
    
    StringBuffer buffer = new StringBuffer();
    public int indentacao = 0;
    public int newLine = 1;

    public SourceBuffer nl() {
        buffer.append("\n");
        newLine = 1;
        
        return this;
    }
    
    public SourceBuffer append(String s) {
        if (newLine==1) {
            startLine();
            newLine=0;
        }
        buffer.append(s);
        return this;
    }

    public String toString() {
        return buffer.toString();
    }
    
    private void startLine() {
        for (int i=0;i<indentacao;i++) {
            buffer.append("    ");
        }
    }
    
    
}
