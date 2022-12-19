/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package j2r;

import j2r.Java9Parser.BlockStatementContext;
import java.util.ArrayList;
import java.util.List;
import org.antlr.v4.runtime.tree.ParseTree;

public class J2rAbstractTree extends Java9BaseVisitor<Integer> {

    private String className = "no_name";
    private final List<MethodDecl> metodos = new ArrayList<>();
    private ProgramBuffer rs;
    private final List<VarDecl> fields = new ArrayList<>();

    public class Node {
        Node parentNode;
        String nodeText;
        String nodeType;
        List<Node> children = new ArrayList<>();
    }

    public class VarDecl {
        String tipo;
        Java9Parser.VariableDeclaratorContext campo;
        private final String nome;

        private VarDecl(String nome, String tipo, Java9Parser.VariableDeclaratorContext campo) {
            this.nome = nome;
            this.tipo = tipo;
            this.campo = campo;
        }
    }
    
    public class MethodDecl {
        String nome;
        String tipo;
        private Java9Parser.BlockContext bloco;
        private Java9Parser.FormalParameterListContext parametros;
    }
    
    @Override public Integer visitNormalClassDeclaration(Java9Parser.NormalClassDeclarationContext ctx) {
        //where the real class starts
        this.className = ctx.identifier().getText();
        return visitClassBody(ctx.classBody()); 
    }    
    
    @Override public Integer visitFieldDeclaration(Java9Parser.FieldDeclarationContext ctx) { 
        for (Java9Parser.VariableDeclaratorContext campo: ctx.variableDeclaratorList().variableDeclarator()) {
            
            fields.add(new VarDecl(campo.variableDeclaratorId().identifier().getText(), ctx.unannType().getText(), campo));
        }
        
        return 1;
    }

    @Override public Integer visitMethodDeclaration(Java9Parser.MethodDeclarationContext ctx) { 
        MethodDecl m = new MethodDecl();
        m.nome = ctx.methodHeader().methodDeclarator().identifier().getText();
        //m.tipo = ctx.methodHeader().result().unannType().getText();
        m.tipo = ctx.methodHeader().result().getText();
        m.parametros = ctx.methodHeader().methodDeclarator().formalParameterList();
        m.bloco = ctx.methodBody().block();
        
        metodos.add(m);
        
        return 1;
    }    

    public String generate() {
            rs = new ProgramBuffer();

            rs.append(
"#![allow(non_snake_case)]\n" +
"#![allow(dead_code)]\n" +
"#![allow(unused_variables)]\n" +
"#![allow(unused_imports)]\n" +
"\n");
            rs.append("use crate::fastdialer::dao::FastDialerDB::FastDialerDB;").append("\n");
            rs.append("use crate::java_compat::ResultSet::ResultSet;").append("\n");
            rs.append("use crate::java_compat::JavaString::JavaString;").append("\n");
            rs.append("");
            
//"use crate::java_compat::ModJavaString;\n" +
//"use crate::java_compat::ModJavaString::JavaString;\n" +
//"use crate::java_compat::ModSystem::System;\n" +
//"use crate::java_compat::ModResultSet::ResultSet;\n" +
//"use crate::java_compat::ModThread::Thread;\n" 
//            );
//            rs.append("\n\n\n");
//
//            rs.append("use crate::fastdialer::dao::AcaoDAO::AcaoDAO;\n" +
//"use crate::fastdialer::dao::AgendaDAO::AgendaDAO;\n" +
//"use crate::fastdialer::dao::AsrDAO::AsrDAO;\n" +
//"use crate::fastdialer::dao::BlacklistDAO::BlacklistDAO;\n" +
//"use crate::fastdialer::dao::CallRelatedDAO::CallRelatedDAO;\n" +
//"use crate::fastdialer::dao::CampanhaDAO::CampanhaDAO;\n" +
//"use crate::fastdialer::dao::ChatDAO::ChatDAO;\n" +
//"use crate::fastdialer::dao::ChatMessageDAO::ChatMessageDAO;\n" +
//"use crate::fastdialer::dao::DAOFactory::DAOFactory;\n" +
//"use crate::fastdialer::dao::DbExternoDAO::DbExternoDAO;\n" +
//"use crate::fastdialer::dao::EventoDAO::EventoDAO;\n" +
//"use crate::fastdialer::dao::FastdialerExternoDAO::FastdialerExternoDAO;\n" +
//"use crate::fastdialer::dao::ListaTelDAO::ListaTelDAO;\n" +
//"use crate::fastdialer::dao::LogDAO::LogDAO;\n" +
//"use crate::fastdialer::dao::MailingDetalheDAO::MailingDetalheDAO;\n" +
//"use crate::fastdialer::dao::MonitorPbxDAO::MonitorPbxDAO;\n" +
//"use crate::fastdialer::dao::NotificacaoDAO::NotificacaoDAO;\n" +
//"use crate::fastdialer::dao::OngoingDAO::OngoingDAO;\n" +
//"use crate::fastdialer::dao::OperadorDAO::OperadorDAO;\n" +
//"use crate::fastdialer::dao::ParametroDAO::ParametroDAO;\n" +
//"use crate::fastdialer::dao::PredCallRecordDAO::PredCallRecordDAO;\n" +
//"use crate::fastdialer::dao::RamalDAO::RamalDAO;\n" +
//"use crate::fastdialer::dao::RotaDAO::RotaDAO;\n" +
//"use crate::fastdialer::dao::TabulacaoDAO::TabulacaoDAO;\n" +
//"use crate::fastdialer::dao::TrunkDAO::TrunkDAO;\n");

            rs.append("\n\n\n");
            
            rs.append("pub struct ").append(className).append(" {\n");
            rs.nivelIn();
            
            for (VarDecl f: fields) {
                rs.append(f.campo.variableDeclaratorId().getText());
                rs.append(": ");
                rs.append(rustType(f.tipo));
                        
                if (f.campo.variableInitializer()!=null) {
                    rs.append(" /* = ");
                    generateExpression(f.campo.variableInitializer().expression());
                    rs.append(" */");
                }
                rs.append(",\n");
            }
            rs.nivelOut();
            rs.append("}\n\n");

            //CONSTRUTOR BASICO
            rs.append("pub fn new_").append(className).append("() -> ").append(className).append(" {\n");
            rs.nivelIn();
            rs.append(className).append(" {\n");
            rs.nivelIn();
            
            for (VarDecl f: fields) {
                rs.append(f.campo.variableDeclaratorId().getText());
                rs.append(": ");
                        
                if (f.campo.variableInitializer()!=null) {
                    generateExpression(f.campo.variableInitializer().expression());
                } else {
                    switch (f.tipo) {
                        case "long": rs.append("0"); break;
                        case "boolean": rs.append("false"); break;
                        case "String": rs.append("\"\".to_string()"); break;
                        default: rs.append(" ()");
                    }
                }
                rs.append(" /* ").append(f.tipo).append(" */");
                rs.append(",\n");
            }
            rs.nivelOut();
            rs.append("}\n\n");            
            rs.nivelOut();
            rs.append("}\n\n");
            
            //IMPLEMENTACAO
            rs.append("impl ").append(className).append(" {\n");

            generateMethods();

            rs.append("}\n\n");

        //ALGUNS "AJUSTES"
        String rsfinal = rs.toString().replaceAll("while true", "loop");
            
        rsfinal = rsfinal.replaceAll("String\\.format", "format!");
        rsfinal = rsfinal.replaceAll("db\\.", "self.db.");
        
        rsfinal = rsfinal.replaceAll("self.db.query\\(sql\\)", "self.db.query(&sql, &[])");
        rsfinal = rsfinal.replaceAll("self.db.executeVoid\\(sql\\)", "self.db.executeVoid(&sql, &[])");
        rsfinal = rsfinal.replaceAll("self.db.queryNextOrNull\\(sql\\)", "self.db.queryNextOrNull(&sql, &[])");
        
        rsfinal = rsfinal.replaceAll("%d", "{}");
        rsfinal = rsfinal.replaceAll("%s", "{}");        
        rsfinal = rsfinal.replaceAll("null", "None"); 
        
        System.out.println(rsfinal);

        
        return rsfinal;
    }

    private void generateMethods() {
        rs.nivelIn();
        for (MethodDecl m: metodos) {
            rs.append("\n");
            //identa();

            rs.append("fn ").append(m.nome).append("(&self");
            if (m.parametros!=null) {
                if (m.parametros.formalParameters()!=null) {
                    for (Java9Parser.FormalParameterContext p: m.parametros.formalParameters().formalParameter()) {
                        rs.append(", ");
                        rs.append(p.variableDeclaratorId().identifier().getText());
                        rs.append(": ");
                        rs.append(rustTypeParam(p.unannType().getText()));
                    }
                }
                if (m.parametros.lastFormalParameter()!=null) {
                    Java9Parser.FormalParameterContext p = m.parametros.lastFormalParameter().formalParameter();
                    rs.append(", ");
                    rs.append(p.variableDeclaratorId().identifier().getText());
                    rs.append(": ");
                    rs.append(rustTypeParam(p.unannType().getText()));
                }
            }
            rs.append(")");
            if (!m.tipo.equals("void")) {
                rs.append(" -> ").append(rustType(m.tipo));
            }
            
            generateBlocox(m.bloco);
        }
        rs.nivelOut();
    }

    private boolean generateBlocox(Java9Parser.BlockContext bloco) {
        if (bloco==null) {
            return false;
        }
        
        rs.append(" {\n");
        rs.nivelIn();
//        try {
            if (bloco.blockStatements()!=null) {
                for (ParseTree b: bloco.blockStatements().children) {
                    BlockStatementContext bc = (BlockStatementContext) b;
                    if (bc.localVariableDeclarationStatement()!=null) {
                        generateLocalVariableDeclarationStatement(bc.localVariableDeclarationStatement());
                    }
                    if (bc.statement()!=null) {
                        Java9Parser.StatementContext st = bc.statement();
                        generateStatement(st);
                    }
                }
            } else {
                rs.append("/* BLOCO VAZIO */\n");
            }
//        }
//        catch (NullPointerException e) {
//            rs.append("//==ERRO NullPointerException " + bloco.getText());
//            System.out.println(bloco.getText());
//            System.out.println(e);
//        }
        rs.nivelOut();
        //identa();
        rs.append("}\n");

        return true;
    }

    private boolean generateLocalVariableDeclarationStatement(Java9Parser.LocalVariableDeclarationStatementContext st) {
        if (st==null) {
            return false;
        }
        
        String tipo = st.localVariableDeclaration().unannType().getText();
        
        List<Java9Parser.VariableDeclaratorContext> lista = st.localVariableDeclaration().variableDeclaratorList().variableDeclarator();
        for (Java9Parser.VariableDeclaratorContext v: lista) {
            //identa();
            rs.append("let ").append(v.variableDeclaratorId().getText()).append(": ").append(rustType(tipo));
            if (v.variableInitializer()!=null) {
                rs.append(" = ");
                generateExpression(v.variableInitializer().expression());
            }
            rs.append(";\n");
        }
        
        
        //rs.append("//==LocalVariableDeclarationStatementContext ").append(st.getText()).append("\n");
        return true;
    }    
    
    
    private boolean generateStatement(Java9Parser.StatementContext st) {
        if (st==null) {
            return false;
        }
        
        if (generateForStatement(st.forStatement())) {}
        else if (generateIfThenElseStatement(st.ifThenElseStatement())) {}
        else if (generateIfStatement(st.ifThenStatement())) {}
        else if (generateLabeledStatment(st.labeledStatement())) {}
        else if (generateStatementWithoutTrailingSubstatement(st.statementWithoutTrailingSubstatement())) {}
        else if (generateWhileStatement(st.whileStatement())) {}
        else
            rs.append("//==StatementContext ").append(st.getText()).append("\n");
        
        return true;
    }    
    
    
    private boolean generateForStatement(Java9Parser.ForStatementContext st) {
        if (st==null) {
            return false;
        }
        
        if (st.basicForStatement()!=null) {
            //basicForStatement
            //	:	'for' '(' forInit? ';' expression? ';' forUpdate? ')' statement
            //	;
            
            Java9Parser.BasicForStatementContext bf = st.basicForStatement();
            rs.append("/* ").append(bf.getText()).append(" */\n");
            rs.append("for ");
            if (bf.forInit()!=null) rs.append(bf.forInit().getText());
            rs.append(";");
            if (bf.expression()!=null) rs.append(bf.expression().getText());
            rs.append(";");
            if (bf.forUpdate()!=null) rs.append(bf.forUpdate().getText());
            generateStatement(bf.statement());
            
            return true;
        }
        if (st.enhancedForStatement()!=null) {
            //enhancedForStatement
            //	:	'for' '(' variableModifier* unannType variableDeclaratorId ':' expression ')' statement
            //	;
            Java9Parser.EnhancedForStatementContext ef = st.enhancedForStatement();
            rs.append("/* ").append(ef.getText()).append(" */\n");
            rs.append("for ");
            rs.append(ef.variableDeclaratorId().identifier().getText());
            rs.append(" in ");
            generateExpression(ef.expression());

            generateStatement(ef.statement());
            
            return true;
        }
        
        rs.append("//==forStatement ").append(st.getText()).append("\n");
        
        return true;
    }

    private boolean generateIfThenElseStatement(Java9Parser.IfThenElseStatementContext st) {
        if (st==null) {
            return false;
        }
        
        //identa();
        rs.append("if ");
        generateExpression(st.expression());
        generateStatementWithoutTrailingSubstatement(st.statementNoShortIf().statementWithoutTrailingSubstatement());
        //identa();
        rs.append("else ");
        generateStatement(st.statement());
        return true;
    }

    private boolean generateIfStatement(Java9Parser.IfThenStatementContext st) {
        if (st==null) {
            return false;
        }
        
        //identa();
        rs.append("if ");
        generateExpression(st.expression());
        generateStatement(st.statement());
        
        return true;
    }

    private boolean generateLabeledStatment(Java9Parser.LabeledStatementContext labeledStatement) {
        if (labeledStatement==null) {
            return false;
        }
        rs.append("//==labeledStatement ").append(labeledStatement.statement().getText()).append("\n");
        
        return true;
    }

    private boolean generateStatementWithoutTrailingSubstatement(Java9Parser.StatementWithoutTrailingSubstatementContext st) {
        if (st==null) {
            return false;
        }
        
        if (generateAssertStatement(st.assertStatement())) {}
        else if (generateBlocox(st.block())) {}
        else if (generateBreakStatement( st.breakStatement())) {}
        else if (generateContinueStatement( st.continueStatement())) {}
        else if (generateDoStatement( st.doStatement())) {}
        else if (generateEmptyStatement( st.emptyStatement())) {}
        else if (generateExpressionStatement( st.expressionStatement())) {}
        else if (generateReturnStatement( st.returnStatement())) {}
        else if (generateSwitchStatement( st.switchStatement())) {}
        else if (generateSynchronizedStatement( st.synchronizedStatement())) {}
        else if (generateThrowStatement( st.throwStatement())) {}
        else if (generateTryStatement( st.tryStatement())) {}
        else 
        rs.append("//==statementWithoutTrailingSubstatement ").append(st.getText()).append("\n");
        
        return true;
    }

    private boolean generateWhileStatement(Java9Parser.WhileStatementContext st) {
        if (st==null) {
            return false;
        }
        
        //identa();
        rs.append("while ");
        generateExpression(st.expression());
        generateStatement(st.statement());
        
        return true;
    }


    private boolean generateAssertStatement(Java9Parser.AssertStatementContext st) {
        if (st==null) {
            return false;
        }
        rs.append("//==AssertStatementContext ").append(st.getText()).append("\n");
        
        return true;
    }

    private boolean generateBreakStatement(Java9Parser.BreakStatementContext st) {
        if (st==null) {
            return false;
        }
        rs.append("break;\n");
        
        return true;
    }

    private boolean generateContinueStatement(Java9Parser.ContinueStatementContext st) {
        if (st==null) {
            return false;
        }
        rs.append("continue;\n");
        
        return true;
    }

    private boolean generateDoStatement(Java9Parser.DoStatementContext st) {
        if (st==null) {
            return false;
        }
        rs.append("//==DoStatementContext ").append(st.getText()).append("\n");
        
        return true;
    }

    private boolean generateEmptyStatement(Java9Parser.EmptyStatementContext st) {
        if (st==null) {
            return false;
        }
        rs.append("//==EmptyStatementContext ").append(st.getText()).append("\n");
        
        return true;
    }

    private boolean generateExpressionStatement(Java9Parser.ExpressionStatementContext st) {
        if (st==null) {
            return false;
        }
        
//statementExpression
//	:	assignment
//	|	preIncrementExpression
//	|	preDecrementExpression
//	|	postIncrementExpression
//	|	postDecrementExpression
//	|	methodInvocation
//	|	classInstanceCreationExpression
//	;
        Java9Parser.StatementExpressionContext se = st.statementExpression();
        
        if (generateAssignment(se.assignment())) {}
        else if (generatePostDecrementExpression(se.postDecrementExpression())) {}
        else if (generatePostIncrementExpression(se.postIncrementExpression())) {}
        else if (generatePreDecrementExpression(se.preDecrementExpression())) {}
        else if (generatePreIncrementExpression(se.preIncrementExpression())) {}
        else if (generateMethodInvocation(se.methodInvocation())) {}
        else if (generateClassInstanceCreationExpression(se.classInstanceCreationExpression())) {}
        else
        rs.append("//==ExpressionStatementContext ").append(st.getText()).append("\n");
        
        return true;
    }

    private boolean generateAssignment(Java9Parser.AssignmentContext st) {
        if (st==null) {
            return false;
        }
        
        generateSelfField(st.leftHandSide().getText());
        rs.append(" ").append(st.assignmentOperator().getText()).append(" ");
        generateExpression(st.expression());
        rs.append(";\n");

        return true;
    }

    private boolean generatePostDecrementExpression(Java9Parser.PostDecrementExpressionContext st) {
        if (st==null) {
            return false;
        }
        
        rs.append("//==PostDecrementExpressionContext ").append(st.getText()).append("\n");
        
        return true;
    }

    private boolean generatePostIncrementExpression(Java9Parser.PostIncrementExpressionContext st) {
        if (st==null) {
            return false;
        }
        
        rs.append("//==PostIncrementExpressionContext ").append(st.getText()).append("\n");
        
        return true;
    }

    private boolean generateSelfMethodInvocation(String methodName) {
        for (MethodDecl p: this.metodos) {
            if (p.nome.equals(methodName)) {
                rs.append("self.");
                rs.append(methodName);
                return true;
            }
        }
        rs.append(methodName);
        return false;
    }
    
    private boolean generateSelfField(String fieldName) {
        for (VarDecl p: this.fields) {
            if (p.nome.equals(fieldName)) {
                rs.append("self.");
                rs.append(fieldName);
                return true;
            }
        }
        rs.append(fieldName);
        return false;
    }    
    
    private boolean generateMethodInvocation(Java9Parser.MethodInvocationContext st) {
        if (st==null) {
            return false;
        }
        
        //methodName '(' argumentList? ')'
        if (st.methodName()!=null) {
            generateSelfMethodInvocation(st.methodName().getText());
            //rs.append(st.methodName().getText());
            rs.append("(");
            generateArgumentList(st.argumentList());
            rs.append(");\n");
            
            return true;
        }
	
        //typeName '.' typeArguments? identifier '(' argumentList? ')'
        if (st.typeName()!=null&&st.SUPER()==null) {
            generateSelfMethodInvocation(st.typeName().getText());
            //rs.append(st.typeName().getText());
            rs.append(".");
            if (st.typeArguments()!=null) {
                rs.append("//==MethodInvocationContext - PANIC ").append(st.typeArguments().getText()).append("\n");
            }
            rs.append(st.identifier().getText());
            rs.append("(");
            generateArgumentList(st.argumentList());
            rs.append(");\n");                    
            return true; 
        }
        
        //typeName '.' 'super' '.' typeArguments? identifier '(' argumentList? ')'
        if (st.typeName()!=null&&st.SUPER()!=null) {
            generateSelfMethodInvocation(st.typeName().getText());
            //rs.append(st.typeName().getText());
            rs.append(".super.");
            if (st.typeArguments()!=null) {
                rs.append("//==MethodInvocationContext - PANIC ").append(st.typeArguments().getText()).append("\n");
            }
            rs.append(st.identifier().getText());
            rs.append("(");
            generateArgumentList(st.argumentList());
            rs.append(");\n");                    
            return true; 
        }
        
        //expressionName '.' typeArguments? identifier '(' argumentList? ')'
        if (st.expressionName()!=null) {
            generateSelfMethodInvocation(st.expressionName().getText());
            //rs.append(st.expressionName().getText());
            rs.append(".");
            if (st.typeArguments()!=null) {
                rs.append("//==MethodInvocationContext - PANIC ").append(st.typeArguments().getText()).append("\n");
            }
            rs.append(st.identifier().getText());
            rs.append("(");
            generateArgumentList(st.argumentList());
            rs.append(");\n");                    
            return true; 
        }        
        
        //primary '.' typeArguments? identifier '(' argumentList? ')'
        if (st.primary()!=null) {
            generateSelfMethodInvocation(st.primary().getText());
            //rs.append(st.primary().getText());
            rs.append(".");
            if (st.typeArguments()!=null) {
                rs.append("//==MethodInvocationContext - PANIC ").append(st.typeArguments().getText()).append("\n");
            }
            rs.append(st.identifier().getText());
            rs.append("(");
            generateArgumentList(st.argumentList());
            rs.append(");\n");                    
            return true; 
        }         
        
        //'super' '.' typeArguments? identifier '(' argumentList? ')'
        if (st.SUPER()!=null) {
            rs.append("super.");
            if (st.typeArguments()!=null) {
                rs.append("//==MethodInvocationContext - PANIC ").append(st.typeArguments().getText()).append("\n");
            }
            rs.append(st.identifier().getText());
            rs.append("(");
            generateArgumentList(st.argumentList());
            rs.append(");\n");                    
            return true; 
        }        
        
        
        rs.append("//==MethodInvocationContext ").append(st.getText()).append("\n");
        
        return true;
    }

    private boolean generateClassInstanceCreationExpression(Java9Parser.ClassInstanceCreationExpressionContext st) {
        if (st==null) {
            return false;
        }
        
        rs.append("//==ClassInstanceCreationExpressionContext ").append(st.getText()).append("\n");
        
        return true;
    }
    
    private boolean generateReturnStatement(Java9Parser.ReturnStatementContext st) {
        if (st==null) {
            return false;
        }
        //identa();
        rs.append("return ");
        generateExpression(st.expression());
        rs.append(";\n");
        
        return true;
    }

    private boolean generateSwitchStatement(Java9Parser.SwitchStatementContext st) {
        if (st==null) {
            return false;
        }
        
        rs.append("match ");
        generateExpression(st.expression());
        rs.append(" {\n").nivelIn();
        
        for (Java9Parser.SwitchBlockStatementGroupContext e: st.switchBlock().switchBlockStatementGroup()) {
            generateSwitchLabels(e.switchLabels());
            generateBlockStatements(e.blockStatements());
        }
        
        for (Java9Parser.SwitchLabelContext e: st.switchBlock().switchLabel()) {
            generateSwitchLabel(e);
        }

        rs.nivelOut().append("}\n");
        
        return true;
    }

    private boolean generateSwitchLabels(Java9Parser.SwitchLabelsContext st) {
        if (st==null) {
            return false;
        }
        
        for (Java9Parser.SwitchLabelContext e: st.switchLabel()) {
            generateSwitchLabel(e);
        }        
        
        return true;
    }

    private boolean generateSwitchLabel(Java9Parser.SwitchLabelContext st) {
        if (st==null) {
            return false;
        }
        
        if (st.DEFAULT()!=null) {
            rs.append("_ => ");
            return true;
        }
        
        if (st.constantExpression()!=null) {
            //TODO:
            rs.append(st.constantExpression().getText()).append(" => ");
            return true;
        }
        
        if (st.enumConstantName()!=null) {
            //TODO:
            rs.append(st.enumConstantName().getText()).append(" => ");
            return true;
        }        
        
        rs.append("//==SwitchLabelContext ").append(st.getText()).append("\n");
        
        return true;
    }  
    
    private boolean generateBlockStatements(Java9Parser.BlockStatementsContext st) {
        if (st==null) {
            return false;
        }
        
        rs.append("{\n").nivelIn().nivelIn();
        for (BlockStatementContext e: st.blockStatement()) {
            generateBlockStatement(e);
        }
        rs.nivelOut().nivelOut().append("}\n");

        return true;
    }    
    
    private boolean generateBlockStatement(BlockStatementContext st) {
        if (st==null) {
            return false;
        }

        if (generateLocalVariableDeclarationStatement(st.localVariableDeclarationStatement())) {}
        else if (generateStatement(st.statement()));
        else
        rs.append("//==BlockStatementContext ").append(st.getText()).append("\n");

        return true;
    }

    
    private boolean generateSynchronizedStatement(Java9Parser.SynchronizedStatementContext st) {
        if (st==null) {
            return false;
        }
        rs.append("//==SynchronizedStatementContext ").append(st.getText()).append("\n");
        
        return true;
    }

    private boolean generateThrowStatement(Java9Parser.ThrowStatementContext st) {
        if (st==null) {
            return false;
        }
        rs.append("panic!(");
        generateExpression(st.expression());
        rs.append(");\n");

        return true;
    }

    private boolean generateTryStatement(Java9Parser.TryStatementContext st) {
        if (st==null) {
            return false;
        }
        
        if (generateTryWithResourcesStatement(st.tryWithResourcesStatement())) {
            return true;
        }
        //identa();
        rs.append("/*TRY BLOCK START*/ ");
        generateBlocox(st.block());
        //identa();
        rs.append("/*TRY BLOCK END*/\n");
        
        return true;
    }

    private boolean generateTryWithResourcesStatement(Java9Parser.TryWithResourcesStatementContext st) {
        if (st==null) {
            return false;
        }
        
        //rs.identa();
        rs.append("/*TRY WITH RESOURCE BLOCK START*/ ");
        
        generateResourceSpecification(st.resourceSpecification());
        
        generateBlocox(st.block());
        //rs.identa();
        rs.append("/*TRY WITH RESOURCE BLOCK END*/\n");
        
        return true;
    }

    private boolean generateResourceSpecification(Java9Parser.ResourceSpecificationContext st) {
        if (st==null) {
            return false;
        }
        
        for (Java9Parser.ResourceContext r: st.resourceList().resource()) {
            if (r.unannType()!=null) {
                rs.append(r.unannType().getText());
            }
            generateExpression(r.expression());

        }
        
        return true;
    }    
    
    private boolean generateExpression(Java9Parser.ExpressionContext st) {
        if (st==null) {
            return false;
        }
        
        //expression
        //	:	lambdaExpression
        //	|	assignmentExpression
        //	;        
        
        if (generateAssignmentExpression (st.assignmentExpression())) {}
        else if (generateLambdaExpression (st.lambdaExpression())) {}
        else
            rs.append("//==ExpressionContext ").append(st.getText()).append("\n");
        
        return true;
    }    
    
    private boolean generateAssignmentExpression(Java9Parser.AssignmentExpressionContext st) {
        if (st==null) {
            return false;
        }
        
        //assignmentExpression
        //	:	conditionalExpression
        //	|	assignment
        //	;        
        
        if (generateAssignment(st.assignment())) {} 
        else if (generateConditionalExpression(st.conditionalExpression())) {}
        else 
            rs.append("//==AssignmentExpressionContext ").append(st.getText()).append("\n");
            
        return true;
    }

    private boolean generateLambdaExpression(Java9Parser.LambdaExpressionContext st) {
        if (st==null) {
            return false;
        }
        
        rs.append("|");
        if (st.lambdaParameters().formalParameterList()!=null) {
            rs.append(st.lambdaParameters().formalParameterList().getText());
            rs.append(", ");
        }
        if (st.lambdaParameters().inferredFormalParameterList()!=null) {
            rs.append(st.lambdaParameters().inferredFormalParameterList().getText());
        }
        if (st.lambdaParameters().identifier()!=null) {
            rs.append(st.lambdaParameters().identifier().getText());
        }
        
        rs.append("|");
        if (st.lambdaBody().expression()!=null) {
            generateExpression(st.lambdaBody().expression());
        }
        
        if (st.lambdaBody().block()!=null) {
            generateBlockStatements(st.lambdaBody().block().blockStatements());
        }
        rs.append(";");
        
        return true;
    }    

    private boolean generateConditionalExpression(Java9Parser.ConditionalExpressionContext st) {
        if (st==null) {
            return false;
        }
        
        //conditionalExpression
        //	:	conditionalOrExpression
        //	|	conditionalOrExpression '?' expression ':' (conditionalExpression|lambdaExpression)
        //	;        

        //condicao ternaria
        if (st.QUESTION()!=null) {
            rs.append("/* ?: */ if ");
            generateConditionalOrExpression(st.conditionalOrExpression());
            rs.append(" then ");
            generateExpression(st.expression());
            rs.append(" else ");
            generateConditionalExpression(st.conditionalExpression());
            generateLambdaExpression(st.lambdaExpression());
            rs.append(";");
            return true;
        }

        if (generateConditionalOrExpression(st.conditionalOrExpression())) {
            return true;
        }
        
        return true;
    }

    private boolean generateConditionalOrExpression(Java9Parser.ConditionalOrExpressionContext st) {
        if (st==null) {
            return false;
        }
        
        //conditionalOrExpression
        //	:	conditionalAndExpression
        //	|	conditionalOrExpression '||' conditionalAndExpression
        //	;        
        
        if (st.OR()!=null) {
            generateConditionalOrExpression(st.conditionalOrExpression());
            rs.append(" || ");
            generateConditionalAndExpression(st.conditionalAndExpression());
            return true;
        }
        
        if (generateConditionalAndExpression(st.conditionalAndExpression())) {
            return true;
        }

        rs.append("//==ConditionalOrExpressionContext ").append(st.getText()).append("\n");
        
        return true;
    }

    private boolean generateConditionalAndExpression(Java9Parser.ConditionalAndExpressionContext st) {
        if (st==null) {
            return false;
        }
        
        //conditionalAndExpression
        //	:	inclusiveOrExpression
        //	|	conditionalAndExpression '&&' inclusiveOrExpression
        //	;        
        
        if (st.AND()!=null) {
            generateConditionalAndExpression(st.conditionalAndExpression());
            rs.append(" && ");
            generateInclusiveOrExpression(st.inclusiveOrExpression());
            return true;
        }
        
        if (generateInclusiveOrExpression(st.inclusiveOrExpression())) {
            return true;
        }

        rs.append("//==ConditionalAndExpressionContext ").append(st.getText()).append("\n");
        
        return true;
    }

    private boolean generateInclusiveOrExpression(Java9Parser.InclusiveOrExpressionContext st) {
        if (st==null) {
            return false;
        }
        
        //inclusiveOrExpression
        //	:	exclusiveOrExpression
        //	|	inclusiveOrExpression '|' exclusiveOrExpression
        //	;        
        
        if (st.BITOR()!=null) {
            generateInclusiveOrExpression(st.inclusiveOrExpression());
            rs.append(" | ");
            generateExclusiveOrExpression(st.exclusiveOrExpression());
            return true;
        }
        
        if (generateExclusiveOrExpression(st.exclusiveOrExpression())) {
            return true;
        }
        
        rs.append("//==InclusiveOrExpressionContext ").append(st.getText()).append("\n");
        
        return true;
    }
    
    private boolean generateExclusiveOrExpression(Java9Parser.ExclusiveOrExpressionContext st) {
        if (st==null) {
            return false;
        }
        
        //exclusiveOrExpression
        //	:	andExpression
        //	|	exclusiveOrExpression '^' andExpression
        //	;        
        
        if (st.CARET()!=null) {
            generateExclusiveOrExpression(st.exclusiveOrExpression());
            rs.append(" ^ ");
            generateAndExpression(st.andExpression());
            return true;
        }
        
        if (generateAndExpression(st.andExpression())) {
            return true;
        }
        
        rs.append("//==ExclusiveOrExpressionContext ").append(st.getText()).append("\n");
        
        return true;
    }    

    private boolean generateAndExpression(Java9Parser.AndExpressionContext st) {
        if (st==null) {
            return false;
        }
        
        //andExpression
        //	:	equalityExpression
        //	|	andExpression '&' equalityExpression
        //	;        
        
        if (st.BITAND()!=null) {
            generateAndExpression(st.andExpression());
            rs.append(" & ");
            generateEqualityExpression(st.equalityExpression());
            return true;
        }
        
        if (generateEqualityExpression(st.equalityExpression())) {
            return true;
        }
        
        rs.append("//==AndExpressionContext ").append(st.getText()).append("\n");
        
        return true;
    }

    private boolean generateEqualityExpression(Java9Parser.EqualityExpressionContext st) {
        if (st==null) {
            return false;
        }
        
        //equalityExpression
        //        :	relationalExpression
        //        |	equalityExpression '==' relationalExpression
        //        |	equalityExpression '!=' relationalExpression
        //        ;        
        
        if (st.EQUAL()!=null) {
            generateEqualityExpression(st.equalityExpression());
            rs.append(" == ");
            generateRelationalExpression(st.relationalExpression());
            return true;
        }
        
        if (st.NOTEQUAL()!=null) {
            generateEqualityExpression(st.equalityExpression());
            rs.append(" != ");
            generateRelationalExpression(st.relationalExpression());
            return true;
        }
        
        if (generateRelationalExpression(st.relationalExpression())) {
            return true;
        }

        rs.append("//==EqualityExpressionContext ").append(st.getText()).append("\n");
        
        return true;
    }
    
    private boolean generateRelationalExpression(Java9Parser.RelationalExpressionContext st) {
        if (st==null) {
            return false;
        }
        
        //relationalExpression
        //	:	shiftExpression
        //	|	relationalExpression '<' shiftExpression
        //	|	relationalExpression '>' shiftExpression
        //	|	relationalExpression '<=' shiftExpression
        //	|	relationalExpression '>=' shiftExpression
        //	|	relationalExpression 'instanceof' referenceType
        //	;        
        
        
        if (st.INSTANCEOF()!=null) {
            generateRelationalExpression(st.relationalExpression());
            rs.append(" instanceof ");
            generateReferenceType(st.referenceType());
            return true;            
        }
        
        if (st.LT()!=null) {
            generateRelationalExpression(st.relationalExpression());
            rs.append(" < ");
            generateShiftExpression(st.shiftExpression());
            return true;
        }
        
        if (st.LE()!=null) {
            generateRelationalExpression(st.relationalExpression());
            rs.append(" <= ");
            generateShiftExpression(st.shiftExpression());
            return true;
        }        
        
        if (st.GT()!=null) {
            generateRelationalExpression(st.relationalExpression());
            rs.append(" > ");
            generateShiftExpression(st.shiftExpression());
            return true;
        }
        
        if (st.GE()!=null) {
            generateRelationalExpression(st.relationalExpression());
            rs.append(" >= ");
            generateShiftExpression(st.shiftExpression());
            return true;
        }        
        
        if (generateShiftExpression(st.shiftExpression())) {
            return true;
        }

        rs.append("//==RelationalExpressionContext ").append(st.getText()).append("\n");
        
        return true;
    }    

    private boolean generateReferenceType(Java9Parser.ReferenceTypeContext st) {
        if (st==null) {
            return false;
        }
        
        rs.append("//==ReferenceTypeContext ").append(st.getText()).append("\n");
        
        return true;
    }

    private boolean generateShiftExpression(Java9Parser.ShiftExpressionContext st) {
        if (st==null) {
            return false;
        }
        
        //shiftExpression
        //	:	additiveExpression
        //	|	shiftExpression '<' '<' additiveExpression
        //	|	shiftExpression '>' '>' additiveExpression
        //	|	shiftExpression '>' '>' '>' additiveExpression
        //	;        
        
        if (st.shiftExpression()!=null) {
            generateShiftExpression(st.shiftExpression());
            rs.append (" SHIFT !!! ");
            generateAdditiveExpression(st.additiveExpression());
            return true;
        }
        
        if (generateAdditiveExpression(st.additiveExpression())) {
            return true;
        }

        rs.append("//==ShiftExpressionContext ").append(st.getText()).append("\n");
        
        return true;
    }

    private boolean generateAdditiveExpression(Java9Parser.AdditiveExpressionContext st) {
        if (st==null) {
            return false;
        }
        
        //additiveExpression
        //	:	multiplicativeExpression
        //	|	additiveExpression '+' multiplicativeExpression
        //	|	additiveExpression '-' multiplicativeExpression
        //	;        
        
        if (st.additiveExpression()!=null) {
            generateAdditiveExpression(st.additiveExpression());
            if (st.ADD()!=null) {rs.append("+");}
            if (st.SUB()!=null) {rs.append("-");}
            generateMultiplicativeExpression(st.multiplicativeExpression());
            return true;
        }
        
        if (generateMultiplicativeExpression(st.multiplicativeExpression())) {
            return true;
        }
        
        rs.append("//==AdditiveExpressionContext ").append(st.getText()).append("\n");
        
        return true;
    }

    private boolean generateMultiplicativeExpression(Java9Parser.MultiplicativeExpressionContext st) {
        if (st==null) {
            return false;
        }
        
        //multiplicativeExpression
        //	:	unaryExpression
        //	|	multiplicativeExpression '*' unaryExpression
        //	|	multiplicativeExpression '/' unaryExpression
        //	|	multiplicativeExpression '%' unaryExpression
        //	;        
        
        if (st.multiplicativeExpression()!=null) {
            generateMultiplicativeExpression(st.multiplicativeExpression());
            if (st.DIV()!=null) { rs.append(" / "); }
            if (st.MUL()!=null) { rs.append(" * "); }
            if (st.MOD()!=null) { rs.append(" % "); }
            generateUnaryExpression(st.unaryExpression());
            return true;
        }
        
        if (generateUnaryExpression(st.unaryExpression())) {
            return true;
        }
        
        rs.append("//==MultiplicativeExpressionContext ").append(st.getText()).append("\n");
        
        return true;
    }

    private boolean generateUnaryExpression(Java9Parser.UnaryExpressionContext st) {
        if (st==null) {
            return false;
        }
        
        //unaryExpression
        //	:	preIncrementExpression
        //	|	preDecrementExpression
        //	|	'+' unaryExpression
        //	|	'-' unaryExpression
        //	|	unaryExpressionNotPlusMinus
        //	;        

        if (st.unaryExpression()!=null) {
            if (st.ADD()!=null) { rs.append(" + "); }
            if (st.SUB()!=null) { rs.append(" - "); }
            generateUnaryExpression(st.unaryExpression());
            return true;
        }
        
        if (generatePreDecrementExpression(st.preDecrementExpression())) {}
        else if (generatePreIncrementExpression(st.preIncrementExpression())) {}
        else if (generateUnaryExpressionNotPlusMinus(st.unaryExpressionNotPlusMinus())) {}
        else 
        rs.append("//==UnaryExpressionContext ").append(st.getText()).append("\n");
        
        return true;
    }

    private boolean generatePreDecrementExpression(Java9Parser.PreDecrementExpressionContext st) {
        if (st==null) {
            return false;
        }
        
        rs.append(" --");
        generateUnaryExpression(st.unaryExpression());
        
        return true;
    }

    private boolean generatePreIncrementExpression(Java9Parser.PreIncrementExpressionContext st) {
        if (st==null) {
            return false;
        }
        
        rs.append(" ++");
        generateUnaryExpression(st.unaryExpression());
        
        return true;
    }

    private boolean generateUnaryExpressionNotPlusMinus(Java9Parser.UnaryExpressionNotPlusMinusContext st) {
        if (st==null) {
            return false;
        }
        
        //unaryExpressionNotPlusMinus
        //        :	postfixExpression
        //        |	'~' unaryExpression
        //        |	'!' unaryExpression
        //        |	castExpression
        //        ;
        
        if (st.unaryExpression()!=null) {
            if (st.BANG()!=null) { rs.append("!"); }
            if (st.TILDE()!=null) { rs.append("~"); }
            generateUnaryExpression(st.unaryExpression());
            return true;
        }
        
        if (generateCastExpression(st.castExpression())) {}
        else if (generatePostfixExpression(st.postfixExpression())) {}
        else
        rs.append("//==UnaryExpressionNotPlusMinusContext ").append(st.getText()).append("\n");
        
        return true;
    }
    
    private boolean generateCastExpression(Java9Parser.CastExpressionContext st) {
        if (st==null) {
            return false;
        }
        
        //castExpression
        //	:	'(' primitiveType ')' unaryExpression
        //	|	'(' referenceType additionalBound* ')' unaryExpressionNotPlusMinus
        //	|	'(' referenceType additionalBound* ')' lambdaExpression
        //	;        
        
        
        //NAO FARA O TRATAMENTO DE CAST, POIS NO RUST O SENTIDO DISTO É OUTRO
        //APENAS FARA O TRATAMENTO DAS EXPRESSOES
        if (generateUnaryExpression(st.unaryExpression())) {}
        else if (generateUnaryExpressionNotPlusMinus(st.unaryExpressionNotPlusMinus())) {}
        else if (generateLambdaExpression(st.lambdaExpression())) {}
        else 
        rs.append("//==CastExpressionContext ").append(st.getText()).append("\n");
        
        return true;
    }

    private boolean generatePostfixExpression(Java9Parser.PostfixExpressionContext st) {
        if (st==null) {
            return false;
        }

        //postfixExpression
        //	:	(	primary
        //		|	expressionName
        //		)
        //		(	postIncrementExpression_lf_postfixExpression
        //		|	postDecrementExpression_lf_postfixExpression
        //		)*
        //	;        
        
        
        generatePrimary(st.primary());
        generateExpressionName(st.expressionName());

        for (Java9Parser.PostIncrementExpression_lf_postfixExpressionContext e: st.postIncrementExpression_lf_postfixExpression()) {
            generatePostIncrementExpression_lf_postfixExpression(e);
        }        
        for (Java9Parser.PostDecrementExpression_lf_postfixExpressionContext e: st.postDecrementExpression_lf_postfixExpression()) {
            generatepostDecrementExpression_lf_postfixExpression(e);
        }
        
        return true;
    }    

    private boolean generatePrimary(Java9Parser.PrimaryContext st) {
        if (st==null) {
            return false;
        }
        //primary
        //        :	(	primaryNoNewArray_lfno_primary
        //                |	arrayCreationExpression
        //                )
        //                (	primaryNoNewArray_lf_primary
        //                )*
        //        ;
        
        if (generatePrimaryNoNewArray_lfno_primary(st.primaryNoNewArray_lfno_primary())) {
            return true;
        }
        
        //arrayCreationExpression
        //	:	'new' primitiveType dimExprs dims?
        //	|	'new' classOrInterfaceType dimExprs dims?
        //	|	'new' primitiveType dims arrayInitializer
        //	|	'new' classOrInterfaceType dims arrayInitializer
        //	;
        Java9Parser.ArrayCreationExpressionContext a = st.arrayCreationExpression();
        if (a.primitiveType()!=null) { 
            rs.append(rustType(a.primitiveType().getText()));
        }
        if (a.classOrInterfaceType()!=null) { 
            rs.append("new_");
            rs.append(a.classOrInterfaceType().getText());
        }
        if (a.dimExprs()!=null) {
            rs.append("[");
            long count = 0;
            for (Java9Parser.DimExprContext de: a.dimExprs().dimExpr()) {
                count++;
                if (count>1) { rs.append(", ");}
                generateExpression(de.expression());
            }
            rs.append("]");
        }
        if (a.dims()!=null) {
            rs.append("[]");
        }
        if (a.arrayInitializer()!=null) {
            rs.append(a.arrayInitializer().getText()).append( "/* array initializer */");
        }
            
        return true;
    }

    private boolean generatePrimaryNoNewArray_lfno_primary(Java9Parser.PrimaryNoNewArray_lfno_primaryContext st) {
        if (st==null) {
            return false;
        }
        
        //primaryNoNewArray
        //	:	literal
        //	|	classLiteral
        //	|	'this'
        //	|	typeName '.' 'this'
        //	|	'(' expression ')'
        //	|	classInstanceCreationExpression
        //	|	fieldAccess
        //	|	arrayAccess
        //	|	methodInvocation
        //	|	methodReference
        //	;        
        
        if (generateLiteral(st.literal())) { return true; }
        if (generateTypeName(st.typeName())) { return true; }
        if (generateClassInstanceCreationExpression_lfno_primary(st.classInstanceCreationExpression_lfno_primary())) { return true; }
        if (generateFieldAccess_lfno_primary(st.fieldAccess_lfno_primary())) { return true; }
        if (generateArrayAccess_lfno_primary(st.arrayAccess_lfno_primary())) { return true; }
        if (generateMethodInvocation_lfno_primary(st.methodInvocation_lfno_primary())) { return true; }
        if (generateMethodReference_lfno_primary(st.methodReference_lfno_primary())) { return true; }
        if (st.LPAREN()!=null) {
            rs.append("(");
            generateExpression(st.expression());
            rs.append(")");
            return true;
        }
        if (st.typeName()==null&&st.THIS()!=null) {
            rs.append("self");
            return true;
        }
        if (st.typeName()==null&&st.THIS()!=null) {
            generateTypeName(st.typeName());
            rs.append(".self");
            return true;
        }        
        
        rs.append("//==generatePrimaryNoNewArray_lfno_primary ").append(st.getText()).append("\n");
        
        return true;
    }    
    
    private boolean generateLiteral(Java9Parser.LiteralContext st) {
        if (st==null) {
            return false;
        }

        //literal
        //	:	IntegerLiteral
        //	|	FloatingPointLiteral
        //	|	BooleanLiteral
        //	|	CharacterLiteral
        //	|	StringLiteral
        //	|	NullLiteral
        //	;        
        
        if (st.StringLiteral()!=null) {
            rs.append(st.StringLiteral().getText());
            //rs.append(".to_string()");
            return true;
        }
        
        rs.append(st.getText());
        return true;
    }

    private boolean generateTypeName(Java9Parser.TypeNameContext st) {
        if (st==null) {
            return false;
        }
        rs.append(st.getText());
        return true;
    }

    private boolean generateClassInstanceCreationExpression_lfno_primary(Java9Parser.ClassInstanceCreationExpression_lfno_primaryContext st) {
        if (st==null) {
            return false;
        }
        //classInstanceCreationExpression_lfno_primary
        //	:	'new' typeArguments? annotation* identifier ('.' annotation* identifier)* typeArgumentsOrDiamond? '(' argumentList? ')' classBody?
        //	|	expressionName '.' 'new' typeArguments? annotation* identifier typeArgumentsOrDiamond? '(' argumentList? ')' classBody?
        //	;        
        
        if (st.expressionName()==null) {
            rs.append("new");
            for (Java9Parser.IdentifierContext i: st.identifier()) {
                rs.append("_");
                rs.append(i.getText());
                rs.append("(");
                generateArgumentList(st.argumentList());
                rs.append(")");
            }
            return true;
        }
        
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private boolean generateFieldAccess_lfno_primary(Java9Parser.FieldAccess_lfno_primaryContext st) {
        if (st==null) {
            return false;
        }

        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private boolean generateArrayAccess_lfno_primary(Java9Parser.ArrayAccess_lfno_primaryContext st) {
        if (st==null) {
            return false;
        }
        
        //arrayAccess_lfno_primary
        //	:	(	expressionName '[' expression ']'
        //		|	primaryNoNewArray_lfno_primary_lfno_arrayAccess_lfno_primary '[' expression ']'
        //		)
        //		(	primaryNoNewArray_lfno_primary_lf_arrayAccess_lfno_primary '[' expression ']'
        //		)*
        //	;        
        
        if (st.expressionName()!=null) {
            generateExpressionName(st.expressionName());
            rs.append("[");
            long count=0;
            for (Java9Parser.ExpressionContext e: st.expression()) {
                count++;
                if (count>1) {
                    rs.append(", ");
                }
                generateExpression(e);
            }
            rs.append("]");
            return true;
        }
        
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private boolean generateMethodInvocation_lfno_primary(Java9Parser.MethodInvocation_lfno_primaryContext st) {
        if (st==null) {
            return false;
        }
        
        //methodInvocation_lfno_primary
        //	:	methodName '(' argumentList? ')'
        //	|	typeName '.' typeArguments? identifier '(' argumentList? ')'
        //	|	expressionName '.' typeArguments? identifier '(' argumentList? ')'
        //	|	'super' '.' typeArguments? identifier '(' argumentList? ')'
        //	|	typeName '.' 'super' '.' typeArguments? identifier '(' argumentList? ')'
        //	;        
        if (st.methodName()!=null) {
            generateSelfMethodInvocation(st.getText());  
            rs.append("(");
            generateArgumentList(st.argumentList());
            rs.append(")"); 
            return true;
        }
        
        //	|	typeName '.' typeArguments? identifier '(' argumentList? ')'
        if (st.typeName()!=null && st.SUPER()==null) {
            rs.append(st.typeName().getText());  
            rs.append(".");
            rs.append(st.identifier().getText());
            rs.append("(");
            generateArgumentList(st.argumentList());
            rs.append(")");
            return true;
        }
        
        if (st.expressionName()!=null) {
            generateExpressionName(st.expressionName());
            return true;
        }
        
        if (st.SUPER()!=null&&st.typeName()==null) {
            return true;
        }        
        
        if (st.SUPER()!=null&&st.typeName()!=null) {
            return true;
        }        
        
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private boolean generateMethodReference_lfno_primary(Java9Parser.MethodReference_lfno_primaryContext st) {
        if (st==null) {
            return false;
        }
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


    
    private boolean generateExpressionName(Java9Parser.ExpressionNameContext st) {
        if (st==null) {
            return false;
        }
        //expressionName
        //	:	identifier
        //	|	ambiguousName '.' identifier
        //	;

        if (st.ambiguousName()!=null) {
            generateAmbiguousName(st.ambiguousName());
            rs.append(".");
            rs.append(st.identifier().getText());
            return true;            
        }
        
        generateSelfField(st.identifier().getText());
        
        return true;
    }

    private boolean generateAmbiguousName(Java9Parser.AmbiguousNameContext st) {
        if (st==null) {
            return false;
        }

        //ambiguousName
        //	:	identifier
        //	|	ambiguousName '.' identifier
        //	;        
        
        if (st.ambiguousName()!=null) {
            generateAmbiguousName(st.ambiguousName());
            rs.append(".");
            rs.append(st.identifier().getText());
            return true;            
        }
        
        generateSelfField(st.identifier().getText());
        
        return true;        
    }    
    
    private void generatePostIncrementExpression_lf_postfixExpression(Java9Parser.PostIncrementExpression_lf_postfixExpressionContext e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void generatepostDecrementExpression_lf_postfixExpression(Java9Parser.PostDecrementExpression_lf_postfixExpressionContext e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }    
    
    
    private boolean generateArgumentList(Java9Parser.ArgumentListContext st) {
        if (st==null) {
            return false;
        }
        
        int count = 0;
        for (Java9Parser.ExpressionContext e: st.expression()) {
            count++;
            if (count>1) {
                rs.append(", ");
            }
            generateExpression(e);
        }

        return true;
    }

    private String rustType(String javaType) {
        switch (javaType) {
            case "long": return "i64";
            case "int": return "i32";
            case "boolean": return "bool";
            case "double": return "f64";
            case "float": return "f64";
            case "void": return "()";
            case "byte": return "u8";
            default: return javaType;
        }
    }
    
    private String rustTypeParam(String javaType) {
        switch (javaType) {
            case "long": return "i64";
            case "int": return "i32";
            case "boolean": return "bool";
            case "double": return "f64";
            case "float": return "f64";
            case "void": return "()";
            case "byte": return "u8";
            default: return "&" + javaType;
        }
    }    
    
}
