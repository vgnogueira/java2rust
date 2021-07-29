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

    private String className;
    private List<VarDecl> fields = new ArrayList<>();
    private List<MethodDecl> metodos = new ArrayList<>();
    private ProgramBuffer rs;
    
    private List<VarDecl> getParametros(Java9Parser.FormalParameterListContext formalParameterList) {
        //TODO:
        return new ArrayList<>();
    }

    public class Node {
        Node parentNode;
        String nodeText;
        String nodeType;
        List<Node> children = new ArrayList<>();
    }

    public class VarDecl {
        String nome;
        String tipo;
        String init;

        private VarDecl(String nome, String tipo, String init) {
            this.nome=nome;this.tipo=tipo;this.init=init;
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
        for (ParseTree campo: ctx.variableDeclaratorList().children) {
            fields.add(new VarDecl(campo.getText(), ctx.unannType().getText(), ""));
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

            rs.append("struct ").append(className).append(" {\n");


            rs.append("}\n\n");

            rs.append("impl ").append(className).append(" {\n");

            generateMethods();

            rs.append("}\n\n");

            System.out.println(rs.toString());

        
        return rs.toString();
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
                        rs.append(p.unannType().getText());
                    }
                }
                if (m.parametros.lastFormalParameter()!=null) {
                    Java9Parser.FormalParameterContext p = m.parametros.lastFormalParameter().formalParameter();
                    rs.append(", ");
                    rs.append(p.variableDeclaratorId().identifier().getText());
                    rs.append(": ");
                    rs.append(p.unannType().getText());
                }
            }
            rs.append(") -> ").append(m.tipo);
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
            rs.append("let ").append(v.variableDeclaratorId().getText()).append(": ").append(tipo);
            if (v.variableInitializer()!=null) {
                rs.append(" = ");
                generateExpression(v.variableInitializer().expression());
            }
            rs.append(";\n");
        }
        
        
        //rs.append("//LocalVariableDeclarationStatementContext ").append(st.getText()).append("\n");
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
            rs.append("//StatementContext ").append(st.getText()).append("\n");
        
        return true;
    }    
    
    
    private boolean generateForStatement(Java9Parser.ForStatementContext forStatement) {
        if (forStatement==null) {
            return false;
        }
        rs.append("//forStatement ").append(forStatement.getText()).append("\n");
        
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
        rs.append("else");
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
        rs.append("//labeledStatement ").append(labeledStatement.statement().getText()).append("\n");
        
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
        rs.append("//statementWithoutTrailingSubstatement ").append(st.getText()).append("\n");
        
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
        rs.append("//AssertStatementContext ").append(st.getText()).append("\n");
        
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
        rs.append("//ContinueStatementContext ").append(st.getText()).append("\n");
        
        return true;
    }

    private boolean generateDoStatement(Java9Parser.DoStatementContext st) {
        if (st==null) {
            return false;
        }
        rs.append("//DoStatementContext ").append(st.getText()).append("\n");
        
        return true;
    }

    private boolean generateEmptyStatement(Java9Parser.EmptyStatementContext st) {
        if (st==null) {
            return false;
        }
        rs.append("//EmptyStatementContext ").append(st.getText()).append("\n");
        
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
        rs.append("//ExpressionStatementContext ").append(st.getText()).append("\n");
        
        return true;
    }

    private boolean generateAssignment(Java9Parser.AssignmentContext st) {
        if (st==null) {
            return false;
        }
        
        rs.append(st.leftHandSide().getText());
        rs.append(" ").append(st.assignmentOperator().getText()).append(" ");
        generateExpression(st.expression());
        rs.append(";\n");

        return true;
    }

    private boolean generatePostDecrementExpression(Java9Parser.PostDecrementExpressionContext st) {
        if (st==null) {
            return false;
        }
        
        rs.append("//PostDecrementExpressionContext ").append(st.getText()).append("\n");
        
        return true;
    }

    private boolean generatePostIncrementExpression(Java9Parser.PostIncrementExpressionContext st) {
        if (st==null) {
            return false;
        }
        
        rs.append("//PostIncrementExpressionContext ").append(st.getText()).append("\n");
        
        return true;
    }

    private boolean generateMethodInvocation(Java9Parser.MethodInvocationContext st) {
        if (st==null) {
            return false;
        }
        
        //methodName '(' argumentList? ')'
        if (st.methodName()!=null) {
            rs.append(st.methodName().getText());
            rs.append("(");
            generateArgumentList(st.argumentList());
            rs.append(")");
            
            return true;
        }
	
        //typeName '.' typeArguments? identifier '(' argumentList? ')'
        if (st.typeName()!=null&&st.SUPER()==null) {
            rs.append(st.typeName().getText());
            rs.append(".");
            if (st.typeArguments()!=null) {
                rs.append("//MethodInvocationContext - PANIC ").append(st.typeArguments().getText()).append("\n");
            }
            rs.append(st.identifier().getText());
            rs.append("(");
            generateArgumentList(st.argumentList());
            rs.append(")");                    
            return true; 
        }
        
        //typeName '.' 'super' '.' typeArguments? identifier '(' argumentList? ')'
        if (st.typeName()!=null&&st.SUPER()!=null) {
            rs.append(st.typeName().getText());
            rs.append(".super.");
            if (st.typeArguments()!=null) {
                rs.append("//MethodInvocationContext - PANIC ").append(st.typeArguments().getText()).append("\n");
            }
            rs.append(st.identifier().getText());
            rs.append("(");
            generateArgumentList(st.argumentList());
            rs.append(")");                    
            return true; 
        }
        
        //expressionName '.' typeArguments? identifier '(' argumentList? ')'
        if (st.expressionName()!=null) {
            rs.append(st.expressionName().getText());
            rs.append(".");
            if (st.typeArguments()!=null) {
                rs.append("//MethodInvocationContext - PANIC ").append(st.typeArguments().getText()).append("\n");
            }
            rs.append(st.identifier().getText());
            rs.append("(");
            generateArgumentList(st.argumentList());
            rs.append(")");                    
            return true; 
        }        
        
        //primary '.' typeArguments? identifier '(' argumentList? ')'
        if (st.primary()!=null) {
            rs.append(st.primary().getText());
            rs.append(".");
            if (st.typeArguments()!=null) {
                rs.append("//MethodInvocationContext - PANIC ").append(st.typeArguments().getText()).append("\n");
            }
            rs.append(st.identifier().getText());
            rs.append("(");
            generateArgumentList(st.argumentList());
            rs.append(")");                    
            return true; 
        }         
        
        //'super' '.' typeArguments? identifier '(' argumentList? ')'
        if (st.SUPER()!=null) {
            rs.append("super.");
            if (st.typeArguments()!=null) {
                rs.append("//MethodInvocationContext - PANIC ").append(st.typeArguments().getText()).append("\n");
            }
            rs.append(st.identifier().getText());
            rs.append("(");
            generateArgumentList(st.argumentList());
            rs.append(")");                    
            return true; 
        }        
        
        
        rs.append("//MethodInvocationContext ").append(st.getText()).append("\n");
        
        return true;
    }

    private boolean generateClassInstanceCreationExpression(Java9Parser.ClassInstanceCreationExpressionContext st) {
        if (st==null) {
            return false;
        }
        
        rs.append("//ClassInstanceCreationExpressionContext ").append(st.getText()).append("\n");
        
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
        
        rs.append("//SwitchLabelContext ").append(st.getText()).append("\n");
        
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
        rs.append("//BlockStatementContext ").append(st.getText()).append("\n");

        return true;
    }

    
    private boolean generateSynchronizedStatement(Java9Parser.SynchronizedStatementContext st) {
        if (st==null) {
            return false;
        }
        rs.append("//SynchronizedStatementContext ").append(st.getText()).append("\n");
        
        return true;
    }

    private boolean generateThrowStatement(Java9Parser.ThrowStatementContext st) {
        if (st==null) {
            return false;
        }
        rs.append("//ThrowStatementContext ").append(st.getText()).append("\n");
        
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
        
        
        if (generateAssignmentExpression (st.assignmentExpression())) {}
        else if (generateLambdaExpression (st.lambdaExpression())) {}
        else
            rs.append("//ExpressionContext ").append(st.getText()).append("\n");
        
        return true;
    }    
    
    private boolean generateAssignmentExpression(Java9Parser.AssignmentExpressionContext st) {
        if (st==null) {
            return false;
        }
        
        if (generateAssignment(st.assignment())) {} 
        else if (generateConditionalExpression(st.conditionalExpression())) {}
        else 
            rs.append("//AssignmentExpressionContext ").append(st.getText()).append("\n");
            
        return true;
    }

    private boolean generateLambdaExpression(Java9Parser.LambdaExpressionContext st) {
        if (st==null) {
            return false;
        }
        rs.append("//LambdaExpressionContext ").append(st.getText()).append("\n");
        
        return true;
    }    

    private boolean generateConditionalExpression(Java9Parser.ConditionalExpressionContext st) {
        if (st==null) {
            return false;
        }
        
        if (generateConditionalExpression(st.conditionalExpression())) {}
        else if (generateConditionalOrExpression(st.conditionalOrExpression())) {}
        else if (generateExpression(st.expression())) {} 
        else if (generateLambdaExpression(st.lambdaExpression())) {} 
        else
            rs.append("//ConditionalExpressionContext ").append(st.getText()).append("\n");
        
        return true;
    }

    private boolean generateConditionalOrExpression(Java9Parser.ConditionalOrExpressionContext st) {
        if (st==null) {
            return false;
        }
        
        if (generateConditionalAndExpression(st.conditionalAndExpression())) {}
        else if (generateConditionalOrExpression(st.conditionalOrExpression())) {}
        else         
            rs.append("//ConditionalOrExpressionContext ").append(st.getText()).append("\n");
        
        return true;
    }

    private boolean generateConditionalAndExpression(Java9Parser.ConditionalAndExpressionContext st) {
        if (st==null) {
            return false;
        }
        
        if (generateConditionalAndExpression(st.conditionalAndExpression())) {}
        else if (generateInclusiveOrExpression(st.inclusiveOrExpression())) {}
        else 
            rs.append("//ConditionalAndExpressionContext ").append(st.getText()).append("\n");
        
        return true;
    }

    private boolean generateInclusiveOrExpression(Java9Parser.InclusiveOrExpressionContext st) {
        if (st==null) {
            return false;
        }
        
        if (generateExclusiveOrExpression(st.exclusiveOrExpression())) {}
        else if (generateInclusiveOrExpression(st.inclusiveOrExpression())) {}
        else
            rs.append("//InclusiveOrExpressionContext ").append(st.getText()).append("\n");
        
        return true;
    }
    
    private boolean generateExclusiveOrExpression(Java9Parser.ExclusiveOrExpressionContext st) {
        if (st==null) {
            return false;
        }
        
        if (generateAndExpression(st.andExpression())) {}
        else if (generateExclusiveOrExpression(st.exclusiveOrExpression())) {}
        else 
            rs.append("//ExclusiveOrExpressionContext ").append(st.getText()).append("\n");
        
        return true;
    }    

    private boolean generateAndExpression(Java9Parser.AndExpressionContext st) {
        if (st==null) {
            return false;
        }
        
        if (generateAndExpression(st.andExpression())) {}
        else if (generateEqualityExpression(st.equalityExpression())) {}
        else
        rs.append("//AndExpressionContext ").append(st.getText()).append("\n");
        
        return true;
    }

    private boolean generateEqualityExpression(Java9Parser.EqualityExpressionContext st) {
        if (st==null) {
            return false;
        }
        
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
        
        if (generateEqualityExpression(st.equalityExpression())) {}
        else if (generateRelationalExpression(st.relationalExpression())) {}
        else        
        rs.append("//EqualityExpressionContext ").append(st.getText()).append("\n");
        
        return true;
    }
    
    private boolean generateRelationalExpression(Java9Parser.RelationalExpressionContext st) {
        if (st==null) {
            return false;
        }
        
        if (st.INSTANCEOF()!=null) {
            generateRelationalExpression(st.relationalExpression());
            rs.append(" instanceof ");
            generateShiftExpression(st.shiftExpression());
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
        
        if (generateReferenceType(st.referenceType())) {}
        else if (generateRelationalExpression(st.relationalExpression())) {}
        else if (generateShiftExpression(st.shiftExpression())) {}
        else
        rs.append("//RelationalExpressionContext ").append(st.getText()).append("\n");
        
        return true;
    }    

    private boolean generateReferenceType(Java9Parser.ReferenceTypeContext st) {
        if (st==null) {
            return false;
        }
        
        rs.append("//ReferenceTypeContext ").append(st.getText()).append("\n");
        
        return true;
    }

    private boolean generateShiftExpression(Java9Parser.ShiftExpressionContext st) {
        if (st==null) {
            return false;
        }
        
        if (generateAdditiveExpression(st.additiveExpression())) {}
        else if (generateShiftExpression(st.shiftExpression())) {}
        else 
        rs.append("//ShiftExpressionContext ").append(st.getText()).append("\n");
        
        return true;
    }

    private boolean generateAdditiveExpression(Java9Parser.AdditiveExpressionContext st) {
        if (st==null) {
            return false;
        }
        
        if (generateAdditiveExpression(st.additiveExpression())) {}
        else if (generateMultiplicativeExpression(st.multiplicativeExpression())) {}
        else
        rs.append("//AdditiveExpressionContext ").append(st.getText()).append("\n");
        
        return true;
    }

    private boolean generateMultiplicativeExpression(Java9Parser.MultiplicativeExpressionContext st) {
        if (st==null) {
            return false;
        }
        
        if (generateMultiplicativeExpression(st.multiplicativeExpression())) {}
        else if (generateUnaryExpression(st.unaryExpression())) {}
        else
        rs.append("//MultiplicativeExpressionContext ").append(st.getText()).append("\n");
        
        return true;
    }

    private boolean generateUnaryExpression(Java9Parser.UnaryExpressionContext st) {
        if (st==null) {
            return false;
        }
        
        if (generatePreDecrementExpression(st.preDecrementExpression())) {}
        else if (generatePreIncrementExpression(st.preIncrementExpression())) {}
        else if (generateUnaryExpression(st.unaryExpression())) {}
        else if (generateUnaryExpressionNotPlusMinus(st.unaryExpressionNotPlusMinus())) {}
        else 
        rs.append("//UnaryExpressionContext ").append(st.getText()).append("\n");
        
        return true;
    }

    private boolean generatePreDecrementExpression(Java9Parser.PreDecrementExpressionContext st) {
        if (st==null) {
            return false;
        }
        
        rs.append("//PreDecrementExpressionContext ").append(st.getText()).append("\n");
        
        return true;
    }

    private boolean generatePreIncrementExpression(Java9Parser.PreIncrementExpressionContext st) {
        if (st==null) {
            return false;
        }
        
        rs.append("//PreIncrementExpressionContext ").append(st.getText()).append("\n");
        
        return true;
    }

    private boolean generateUnaryExpressionNotPlusMinus(Java9Parser.UnaryExpressionNotPlusMinusContext st) {
        if (st==null) {
            return false;
        }
        
        if (generateCastExpression(st.castExpression())) {}
        else if (generatePostfixExpression(st.postfixExpression())) {}
        else if (generateUnaryExpression(st.unaryExpression())) {}
        else
        rs.append("//UnaryExpressionNotPlusMinusContext ").append(st.getText()).append("\n");
        
        return true;
    }
    
    private boolean generateCastExpression(Java9Parser.CastExpressionContext st) {
        if (st==null) {
            return false;
        }
        
        if (generateUnaryExpression(st.unaryExpression())) {}
        else if (generateUnaryExpressionNotPlusMinus(st.unaryExpressionNotPlusMinus())) {}
        else if (generateLambdaExpression(st.lambdaExpression())) {}
        else 
        rs.append("//CastExpressionContext ").append(st.getText()).append("\n");
        
        return true;
    }

    private boolean generatePostfixExpression(Java9Parser.PostfixExpressionContext st) {
        if (st==null) {
            return false;
        }

        if (st.primary()!=null) {
            rs.append(st.primary().getText());
        }        
            
            
        if (st.expressionName()!=null) {
            rs.append(st.expressionName().identifier().getText());
        }

        for (Java9Parser.PostDecrementExpression_lf_postfixExpressionContext e: st.postDecrementExpression_lf_postfixExpression()) {
            rs.append("//generatePostfixExpression->panic1 !" + e.getText());
        }

        for (Java9Parser.PostIncrementExpression_lf_postfixExpressionContext e: st.postIncrementExpression_lf_postfixExpression()) {
            rs.append("//generatePostfixExpression->panic2 !" + e.getText());
        }
        
        
        return true;
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

    
}
