/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package j2r;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import​ org.antlr.v4.runtime.*;
import​ org.antlr.v4.runtime.tree.*;

/**
 *
 * @author fastway
 */
public class J2r {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        compileProject("/opt/fastway/fastdialer_monitor/src/", "/opt/fastway/java2rust/fastdialer-monitor-rust/src/");
	
        //compile("/opt/fastway/fastdialer_monitor/src/fastdialer/FastDialer.java", "/opt/fastway/java2rust/fastdialer-monitor-rust/src/fastdialer/FastDialer.rs");
       
        //compileAT("/opt/fastway/fastdialer_monitor/src/fastdialer/dao/AcaoDAO.java", "/home/fastway/myprojects/java2rust/samples/Employee.rs");
        
        
        //compileAT("/opt/fastway/fastdialer_monitor/src/fastdialer/RunCampanha.java", "/home/fastway/myprojects/java2rust/samples/Employee.rs");
        
        //compileAT("/opt/fastway/fastdialer_monitor/src/fastdialer/fastagi/Agi.java", "/home/fastway/myprojects/java2rust/samples/Employee.rs");
        
        
        System.out.println("\n\n FIM DO PROCESSAMENTO!!!");
    }
    
    public static String translateAT(String javaFileName) throws FileNotFoundException, IOException {
        // create a CharStream that reads from standard input
        ANTLRInputStream input = new ANTLRInputStream(new FileInputStream(javaFileName));
        // create a lexer that feeds off of input CharStream
        Java9Lexer lexer = new Java9Lexer(input);
        // create a buffer of tokens pulled from the lexer
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        // create a parser that feeds off the tokens buffer
        Java9Parser parser = new Java9Parser(tokens);
        ParseTree tree = parser.compilationUnit(); // begin parsing at init rule

        J2rAbstractTree j = new J2rAbstractTree();
        j.visit(tree);
        ;
        
        
        
        
        return j.generate();
    }
    
    
    
//    public static String translate(String javaFileName) throws FileNotFoundException, IOException {
//        // create a CharStream that reads from standard input
//        ANTLRInputStream input = new ANTLRInputStream(new FileInputStream(javaFileName));
//        // create a lexer that feeds off of input CharStream
//        Java9Lexer lexer = new Java9Lexer(input);
//        // create a buffer of tokens pulled from the lexer
//        CommonTokenStream tokens = new CommonTokenStream(lexer);
//        // create a parser that feeds off the tokens buffer
//        Java9Parser parser = new Java9Parser(tokens);
//        ParseTree tree = parser.compilationUnit(); // begin parsing at init rule
//
//        // Create a generic parse tree walker that can trigger callbacks
//        ParseTreeWalker walker = new ParseTreeWalker();
//        // Walk the tree created during the parse, trigger callbacks
//        //walker.walk(new J2rFull(), tree);
//        //walker.walk(new Java2RustPOC(), tree);
//        
//        
//        J2rDirectTranslation j = new J2rDirectTranslation();
//        j.visit(tree);
//        
//        String programa = "#![allow(non_snake_case)]\n\n"
//                + j.struct.toString() + j.construtorX.toString() + j.impl.toString();
//        
//        return programa;
//    }

    private static void compileProject(String srcPath, String dstPath) {
        File src = new File(srcPath);
        
        //nao processao o srcPath em si, apenas o seus filhos
        for (File f: src.listFiles()) {
            if (f.isDirectory()) {
                estruturaProcessa(f, srcPath, dstPath);
            } else if (f.getName().endsWith(".java")) {
                estruturaProcessa(f, srcPath, dstPath);
            }
        }
    }

    private static void estruturaProcessa(File src, String srcPath, String dstPath) {
        //System.out.println(src.getAbsolutePath());
        if (src.isDirectory()) {
            //cria o diretorio no destino
            String dstFileName = src.getAbsolutePath().replace(srcPath, dstPath);
            File dst = new File(dstFileName);
            dst.mkdir();
            
            //cria um arquivo mod.rs dentro do diretorio
            //if (modrs.exists()) { modrs.delete(); }
            estruturaCreateFile(dstFileName + "/mod.rs","/* mod.rs " + dst.getAbsolutePath().replace(dstPath, "").replaceAll("[/]", "::")   + " */\n\n");
            //processa os filhos
            for (File f: src.listFiles()) {
                if (f.isDirectory()) {
                    estruturaAppendFile(dstFileName + "/mod.rs", "pub mod " + f.getName() + "; /* diretorio */\n");
                    estruturaProcessa(f, srcPath, dstPath);
                } else if (f.getName().endsWith(".java")) {
                    estruturaAppendFile(dstFileName + "/mod.rs", "pub mod " + f.getName().replace(".java", "") + ";\n");
                    estruturaProcessa(f, srcPath, dstPath);
                }
            }
        } else if (src.getName().endsWith(".java")) {
            try {
                String rustFileName = src.getAbsolutePath().replace(srcPath, dstPath).replace(".java", ".rs");
                System.out.println(rustFileName);
                String rustCode = translateAT(src.getAbsolutePath());
                estruturaCreateFile(rustFileName, rustCode);
            } catch (IOException ex) {
                Logger.getLogger(J2r.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private static void estruturaCreateFile(String fileName, String conteudo) {
      FileWriter myWriter = null;
        try {
            myWriter = new FileWriter(fileName);
            myWriter.write(conteudo);
            myWriter.close();
        } catch (IOException ex) {
            Logger.getLogger(J2r.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                myWriter.close();
            } catch (IOException ex) {
                Logger.getLogger(J2r.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private static void estruturaAppendFile(String fileName, String conteudo) {
        FileWriter fr = null;
        try {
            File file = new File(fileName);
            fr = new FileWriter(file, true);
            BufferedWriter br = new BufferedWriter(fr);
            br.write(conteudo);
            br.close();
            fr.close();
        } catch (IOException ex) {
            Logger.getLogger(J2r.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                fr.close();
            } catch (IOException ex) {
                Logger.getLogger(J2r.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private static void compileAT(String srcFileName, String dstFileName) {
        String src;
        try {
            src = translateAT(srcFileName);
            estruturaCreateFile(dstFileName, src);
        } catch (IOException ex) {
            Logger.getLogger(J2r.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }    
    
//    private static void compile(String srcFileName, String dstFileName) {
//        String src;
//        try {
//            src = translate(srcFileName);
//            estruturaCreateFile(dstFileName, src);
//        } catch (IOException ex) {
//            Logger.getLogger(J2r.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        
//    }
    
}
