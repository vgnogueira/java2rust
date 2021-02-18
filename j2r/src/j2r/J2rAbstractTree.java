/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package j2r;

import java.util.Iterator;
import java.util.List;

public class J2rAbstractTree extends Java9BaseVisitor<Integer> {

    private String className;
    
    @Override public Integer visitCompilationUnit(Java9Parser.CompilationUnitContext ctx) {
        System.out.println(ctx.getText());
        return visitChildren(ctx); 
    }
    
    @Override public Integer visitOrdinaryCompilation(Java9Parser.OrdinaryCompilationContext ctx) { 
        System.out.println(ctx.getText());
        return visitChildren(ctx); 
    }
    
    @Override public Integer visitTypeDeclaration(Java9Parser.TypeDeclarationContext ctx) {
        System.out.println(ctx.getText());
        return visitChildren(ctx); 
    }

    @Override public Integer visitClassDeclaration(Java9Parser.ClassDeclarationContext ctx) { 
        System.out.println(ctx.getText());
        return visitChildren(ctx); 
    }

    @Override public Integer visitNormalClassDeclaration(Java9Parser.NormalClassDeclarationContext ctx) {
        //where the real class starts
        this.className = ctx.identifier().getText();
        System.out.println(ctx.getText());
        return visitClassBody(ctx.classBody()); 
    }    
    
    @Override public Integer visitClassBody(Java9Parser.ClassBodyContext ctx) { 
        
        for (Java9Parser.ClassBodyDeclarationContext member: ctx.classBodyDeclaration()) {
            if (member.constructorDeclaration()!=null) {
                addConstructor(member.constructorDeclaration());
            } else if (member.classMemberDeclaration()!=null) {
                if (member.classMemberDeclaration().fieldDeclaration()!=null) {
                    addField(member.classMemberDeclaration().fieldDeclaration());
                } else if (member.classMemberDeclaration().methodDeclaration() != null) {
                    addClassMethod(member.classMemberDeclaration().methodDeclaration());
                }
            } else {
                System.out.println("CLASS SOMETHING ELSE");
            }      
        }
        
//        System.out.println(ctx.getText());
//        return visitChildren(ctx);
        return 1;
    }

    private void addConstructor(Java9Parser.ConstructorDeclarationContext constructorDeclaration) {
        //TODO: LISTA DE PARAMETROS + CORPO DO CONSTRUTOR
        System.out.println("TODO: CONSTRUTOR" + constructorDeclaration.constructorDeclarator().formalParameterList().getText());
        System.out.println("TODO: CONSTRUTOR" + constructorDeclaration.constructorBody().getText());
        Object constructorBlock = getBlockStatements(constructorDeclaration.constructorBody().blockStatements());
    }

    private void addField(Java9Parser.FieldDeclarationContext fieldDeclaration) {
        String fieldType = fieldDeclaration.unannType().getText();
        
        for (Java9Parser.VariableDeclaratorContext campo: fieldDeclaration.variableDeclaratorList().variableDeclarator()) {
            System.out.println("TODO: " + campo.getText() + ": " + fieldType);
        }
    }

    private void addClassMethod(Java9Parser.MethodDeclarationContext methodDeclaration) {
        List<Java9Parser.MethodModifierContext> methodModifer = methodDeclaration.methodModifier();
        String methodName = methodDeclaration.methodHeader().methodDeclarator().identifier().getText();
        Object methodParamList = getformalParameterList(methodDeclaration.methodHeader().methodDeclarator().formalParameterList());
        String methodType = methodDeclaration.methodHeader().result().getText();
        Object methodBlock = getBlockStatements(methodDeclaration.methodBody().block().blockStatements());
        
        System.out.println("TODO: fn " + methodName + "() -> " + methodType + " {" + methodDeclaration.methodBody().block().blockStatements().getText() + " }");
    }

    private Object getBlockStatements(Java9Parser.BlockStatementsContext blockStatements) {
        //blockStatements.
        return null;
    }

    private Object getformalParameterList(Java9Parser.FormalParameterListContext formalParameterList) {
//        for (Java9Parser.FormalParameterContext fp: formalParameterList.formalParameters().formalParameter()) {
//            String tipo = fp.unannType().getText();
//            String nome = fp.variableDeclaratorId().getText();
//        }
        return null;
    }



        
}
