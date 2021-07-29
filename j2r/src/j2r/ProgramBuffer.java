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
class ProgramBuffer {
    StringBuffer b = new StringBuffer();
    boolean identacaoPendente = false;
    private long nivel = 0;
    
    public ProgramBuffer identa() {
        for (long i=0;i<nivel;i++) {
            b.append("    ");
        }
        return this;
    }    

    public ProgramBuffer append(String valor) {
        if (identacaoPendente) {
            identa();
            identacaoPendente=false;
        }
        if (valor.contains("\n")) {
            identacaoPendente=true;
        }
        b.append(valor);
        
        
        return this;
    }

    public ProgramBuffer nivelIn() {
        nivel++;
        return this;
    }

    public ProgramBuffer nivelOut() {
        nivel--;
        return this;
    }
    
    @Override
    public String toString() {
        return b.toString();
    }
    
    
}
