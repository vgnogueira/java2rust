/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package j2r;

import java.util.Iterator;

public class J2rFull extends Java9BaseVisitor<Integer> {

    SourceBuffer createStruct;
    SourceBuffer construtorX;
    SourceBuffer struct;
    SourceBuffer impl;
    
    SourceBuffer currentBuffer;
    private String nomeClasse;
    
    public J2rFull() {
        createStruct = new SourceBuffer();
        construtorX = new SourceBuffer();
        struct = new SourceBuffer();
        impl = new SourceBuffer();
        
        currentBuffer = impl;
        
    }

    @Override 
    public Integer visitLiteral(Java9Parser.LiteralContext ctx) { 
        if (ctx.StringLiteral()!=null) { currentBuffer.append("String::from(" + ctx.getText() + ")"); return 0;}
        
        currentBuffer.append(ctx.getText());
        return 0;
    }

    @Override public Integer visitExpressionName(Java9Parser.ExpressionNameContext ctx) {
            if (ctx.ambiguousName()!=null) {
                visitAmbiguousName(ctx.ambiguousName());
                currentBuffer.append(".");
            }
            visitIdentifier(ctx.identifier());
            
            return 0; 
        }

    @Override 
    public Integer visitPackageDeclaration(Java9Parser.PackageDeclarationContext ctx) {
        //struct.append("mod " + ctx.packageName().getText().replaceAll("[.]", "::") + ";").nl().nl();

        return 0;
    }

    @Override 
    public Integer visitSingleStaticImportDeclaration(Java9Parser.SingleStaticImportDeclarationContext ctx) { 
        struct.append("use crate::" + ctx.typeName().getText().replaceAll("[.]", "::") + ";").nl();

        return 0; 
    }
        
    @Override public Integer visitTypeImportOnDemandDeclaration(Java9Parser.TypeImportOnDemandDeclarationContext ctx) { 
        struct.append("use crate::" + ctx.packageOrTypeName().getText().replaceAll("[.]", "::") + "; /* visitTypeImportOnDemandDeclaration */").nl();

        return 0; 
    }    
    
    
    @Override 
    public Integer visitSingleTypeImportDeclaration(Java9Parser.SingleTypeImportDeclarationContext ctx) { 
        struct.append("use crate::" + ctx.typeName().getText().replaceAll("[.]", "::") + ";").nl();

        return 0; 
    }

    @Override 
    public Integer visitNormalClassDeclaration(Java9Parser.NormalClassDeclarationContext ctx) {
        this.nomeClasse = ctx.identifier().getText();
        
        struct.nl().append("#[derive(Debug)]");
        struct.nl().append("pub struct " + ctx.identifier().getText() + " {").nl();
        
        
        currentBuffer.nl().append("impl " + ctx.identifier().getText() + " {").nl();
        currentBuffer.indentacao++;
        visitClassBody(ctx.classBody());
        struct.append("}").nl();
        
        currentBuffer.indentacao--;
        currentBuffer.append("}").nl();

        return 0; 
    }

    @Override 
    public Integer visitFieldDeclaration(Java9Parser.FieldDeclarationContext ctx) {
            String tipo = rustType(ctx.unannType().getText());
            
            Iterator<Java9Parser.VariableDeclaratorContext> ilistaVariaveis = ctx.variableDeclaratorList().variableDeclarator().iterator();
            while (ilistaVariaveis.hasNext()) {
                Java9Parser.VariableDeclaratorContext variavel = ilistaVariaveis.next();
                
                String nome = variavel.variableDeclaratorId().identifier().getText();
                
                struct.append("    " + nome + ": " + tipo + ",").nl();
                
                currentBuffer = createStruct;
                
                if (variavel.variableInitializer()==null) {
                    createStruct.append("    " + nome + ": " + "/* valor default */" + ",").nl();
                } else {
                    createStruct.append("let " + nome + ": ");
                    visitVariableInitializer(variavel.variableInitializer());
                    createStruct.append(",").nl();
                }
                
                currentBuffer = impl;
                
            }
            return 0;
        }

        @Override 
        public Integer visitMethodHeader(Java9Parser.MethodHeaderContext ctx) {
            String tipo = rustType(ctx.result().getText());
            String nome = ctx.methodDeclarator().identifier().getText();
            
            currentBuffer.nl().append("pub fn " + nome + "(");
            Java9Parser.FormalParameterListContext params = ctx.methodDeclarator().formalParameterList();
            if (params!=null) {
                currentBuffer.append ("&mut self, ");
                visitFormalParameterList(params);
            } else {
                currentBuffer.append ("&mut self");
            }
            currentBuffer.append (") ");
            if (!tipo.equals("void")) {
                currentBuffer.append ("-> " + tipo + " ");
            }
            
            return 0;
        }

        @Override public Integer visitFormalParameterList(Java9Parser.FormalParameterListContext ctx) { 
            Java9Parser.FormalParametersContext params = ctx.formalParameters();
            Java9Parser.LastFormalParameterContext last = ctx.lastFormalParameter();
            
            if (params!=null) {
            Iterator<Java9Parser.FormalParameterContext> iLista = params.formalParameter().iterator();
                while (iLista.hasNext()) {
                    Java9Parser.FormalParameterContext param = iLista.next();
                    visitFormalParameter(param);
                    currentBuffer.append(", ");
                }
            }
            
            if (last!=null) {
               visitFormalParameter(last.formalParameter()); 
            }
            
            return 0; 
        
        }

        @Override 
        public Integer visitFormalParameter(Java9Parser.FormalParameterContext ctx) { 
            String tipo = rustType(ctx.unannType().getText());
            String nome = ctx.variableDeclaratorId().getText();
            currentBuffer.append(nome + ": " + tipo);
            
            return 0; 
        }

	@Override public Integer visitEnumDeclaration(Java9Parser.EnumDeclarationContext ctx) { 
            //TODO: visitEnumDeclaration
            return 0; 
        }

        @Override 
        public Integer visitBlock(Java9Parser.BlockContext ctx) { 
            currentBuffer.append("{").nl();
            currentBuffer.indentacao++;
            if (ctx.blockStatements()!=null) { 
                visitBlockStatements(ctx.blockStatements());
            } else {
                currentBuffer.append("/* TODO: VERIFICAR EMPTY BLOCK!!! */").nl();
            }
            currentBuffer.indentacao--;
            currentBuffer.append("}").nl();
            return 0;
        }

	@Override public Integer visitLocalVariableDeclarationStatement(Java9Parser.LocalVariableDeclarationStatementContext ctx) { 
            Java9Parser.LocalVariableDeclarationContext declaration = ctx.localVariableDeclaration();
            
            String tipo = rustType(declaration.unannType().getText());
            
            Iterator<Java9Parser.VariableDeclaratorContext> iLista = declaration.variableDeclaratorList().variableDeclarator().iterator();
            
            while (iLista.hasNext()) {
                Java9Parser.VariableDeclaratorContext variavel = iLista.next();
                
                currentBuffer.append("let " + variavel.variableDeclaratorId().getText() + ": " + tipo);
                
                if (variavel.variableInitializer()!=null) {
                    currentBuffer.append(" = ");
                    visitVariableInitializer(variavel.variableInitializer());
                }
                
                currentBuffer.append(";").nl();
            }
            return 0; 
        }

	@Override 
        public Integer visitLocalVariableDeclaration(Java9Parser.LocalVariableDeclarationContext ctx) { 
            String tipo = rustType(ctx.unannType().getText());
            
            Iterator<Java9Parser.VariableDeclaratorContext> iLista = ctx.variableDeclaratorList().variableDeclarator().iterator();
            int count=0;
            while (iLista.hasNext()) {
                count++;
                Java9Parser.VariableDeclaratorContext variavel = iLista.next();
                String nome = variavel.variableDeclaratorId().identifier().getText();
                
                if (count>1) {
                    currentBuffer.append(", ");
                }
                
                currentBuffer.append("let mut " + nome + ": " + tipo);
                
                if (variavel.variableInitializer()!=null) {
                    currentBuffer.append(" = ");
                    visitVariableInitializer(variavel.variableInitializer());
                }
                
                currentBuffer.append(";").nl();
                
            }
            return 0; 
        }

        /*TODO: 	|	emptyStatement
	|	expressionStatement
	|	assertStatement
	|	switchStatement
	|	doStatement
	|	continueStatement
	|	returnStatement
	|	synchronizedStatement
	|	throwStatement
	|	 */
        
//statementNoShortIf
//	:	statementWithoutTrailingSubstatement
//	|	labeledStatementNoShortIf
//	|	ifThenElseStatementNoShortIf
//	|	whileStatementNoShortIf
//	|	forStatementNoShortIf
//	;        
        
        
        @Override 
        public Integer visitExpressionStatement(Java9Parser.ExpressionStatementContext ctx) { 
            visitStatementExpression(ctx.statementExpression());
            currentBuffer.append(";").nl();
            return 0; 
        }

        @Override public Integer visitIfThenStatement(Java9Parser.IfThenStatementContext ctx) { 
            currentBuffer.append("if ");
            visitExpression(ctx.expression());
            currentBuffer.append(" { ").nl();
            currentBuffer.indentacao++;
            visitStatement(ctx.statement());
            currentBuffer.indentacao--;
            currentBuffer.append("} ").nl();
            return 0;
        }

	@Override public Integer visitIfThenElseStatement(Java9Parser.IfThenElseStatementContext ctx) {
            currentBuffer.append(" if ");
            visitExpression(ctx.expression());
            currentBuffer.append(" { ").nl();
            currentBuffer.indentacao++;
            visitStatementNoShortIf(ctx.statementNoShortIf());
            currentBuffer.indentacao--;
            currentBuffer.append("} ");            
            currentBuffer.append(" else ");
            visitStatement(ctx.statement());
            return 0;
        }

	@Override public Integer visitIfThenElseStatementNoShortIf(Java9Parser.IfThenElseStatementNoShortIfContext ctx) { currentBuffer.append("/* visitIfThenElseStatementNoShortIf ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

	@Override public Integer visitAssertStatement(Java9Parser.AssertStatementContext ctx) { currentBuffer.append("/* visitAssertStatement ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

        @Override 
        public Integer visitSwitchStatement(Java9Parser.SwitchStatementContext ctx) {
            currentBuffer.append("match ");
            visitExpression(ctx.expression());
            currentBuffer.append(" {").nl();
            currentBuffer.indentacao+=2;
            visitSwitchBlock(ctx.switchBlock());
            currentBuffer.indentacao-=2;
            currentBuffer.nl().append("}").nl();
            

            return 0; 
        
        }

	@Override 
        public Integer visitSwitchBlockStatementGroup(Java9Parser.SwitchBlockStatementGroupContext ctx) { 

            Iterator<Java9Parser.SwitchLabelContext> iLista = ctx.switchLabels().switchLabel().iterator();
            int count=0;
            while (iLista.hasNext()) {
                Java9Parser.SwitchLabelContext cas = iLista.next();
                
                count++;
                
                if (count>1) {
                    currentBuffer.append(" | ");
                }
                
                if (cas.constantExpression()!=null) visitConstantExpression(cas.constantExpression());
                
                if (cas.enumConstantName()!=null) visitEnumConstantName(cas.enumConstantName());
                
                if (cas.DEFAULT()!=null) currentBuffer.append(" _ ");
                
            }
            
            currentBuffer.append(" => {").nl();
            currentBuffer.indentacao += 5;
            
            visitBlockStatements(ctx.blockStatements());
            
            currentBuffer.indentacao -= 5;
            currentBuffer.append("},").nl();
            
            return 0; 
        }

        @Override public Integer visitSwitchLabels(Java9Parser.SwitchLabelsContext ctx) { currentBuffer.append("/* visitSwitchLabels ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

        @Override public Integer visitSwitchLabel(Java9Parser.SwitchLabelContext ctx) { currentBuffer.append("/* visitSwitchLabel ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

        @Override public Integer visitEnumConstantName(Java9Parser.EnumConstantNameContext ctx) { currentBuffer.append("/* visitEnumConstantName ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

	@Override 
        public Integer visitWhileStatement(Java9Parser.WhileStatementContext ctx) { 
            currentBuffer.append("while ");
            visitExpression(ctx.expression());
            currentBuffer.append(" ");
            visitStatement(ctx.statement());
            return 0; 
        }

	@Override public Integer visitWhileStatementNoShortIf(Java9Parser.WhileStatementNoShortIfContext ctx)  { 
            currentBuffer.append("while ");
            visitExpression(ctx.expression());
            currentBuffer.append(" ");
            visitStatementNoShortIf(ctx.statementNoShortIf());
            return 0; 
        }

	@Override 
        public Integer visitBreakStatement(Java9Parser.BreakStatementContext ctx) {
            currentBuffer.append("break;").nl();
            
            return 0; 
        }

        public Integer visitContinueStatement(Java9Parser.ContinueStatementContext ctx) { 
            currentBuffer.append("continue;").nl();
            
            return 0; 
        }        
        
        @Override public Integer visitReturnStatement(Java9Parser.ReturnStatementContext ctx) { 
            if (ctx.expression()==null) {
                currentBuffer.append("return;").nl();
            } else {
                currentBuffer.append("return ");
                visitChildren(ctx.expression());
                currentBuffer.append(";").nl();
            }
            return 0; 
        }
        
        @Override 
        public Integer visitTryStatement(Java9Parser.TryStatementContext ctx) {
            if (ctx.tryWithResourcesStatement()!=null) {
                visitTryWithResourcesStatement(ctx.tryWithResourcesStatement());
                return 0;
            }
            
            currentBuffer.append("/* try block */").nl();
            visitBlock(ctx.block());
            
            if (ctx.finally_()!=null) {
                currentBuffer.append("/* finally block */").nl();
                visitFinally_(ctx.finally_());
            }

            return 0;
        }
        
	@Override public Integer visitClassInstanceCreationExpression(Java9Parser.ClassInstanceCreationExpressionContext ctx) { currentBuffer.append("/* visitClassInstanceCreationExpression ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

        @Override public Integer visitClassInstanceCreationExpression_lf_primary(Java9Parser.ClassInstanceCreationExpression_lf_primaryContext ctx) { currentBuffer.append("/* visitClassInstanceCreationExpression_lf_primary ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

        @Override public Integer visitClassInstanceCreationExpression_lfno_primary(Java9Parser.ClassInstanceCreationExpression_lfno_primaryContext ctx) { 
            currentBuffer.append(ctx.identifier(0).getText());
            currentBuffer.append("::new(");
            visitArgumentList(ctx.argumentList());
            currentBuffer.append(")");
            return 0; 
        }        
        
        @Override 
        public Integer visitArrayCreationExpression(Java9Parser.ArrayCreationExpressionContext ctx) { 
            currentBuffer.append("/* visitArrayCreationExpression ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");
            
            //visitChildren(ctx);
            return 0; 
        }        
        
        
        
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.</p>
	 */
	@Override public Integer visitCatches(Java9Parser.CatchesContext ctx) { currentBuffer.append("/* visitCatches ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.</p>
	 */
	@Override public Integer visitCatchClause(Java9Parser.CatchClauseContext ctx) { currentBuffer.append("/* visitCatchClause ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.</p>
	 */
	@Override public Integer visitCatchFormalParameter(Java9Parser.CatchFormalParameterContext ctx) { currentBuffer.append("/* visitCatchFormalParameter ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.</p>
	 */
	@Override public Integer visitCatchType(Java9Parser.CatchTypeContext ctx) { currentBuffer.append("/* visitCatchType ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.</p>
	 */
	@Override public Integer visitFinally_(Java9Parser.Finally_Context ctx) { currentBuffer.append("/* visitFinally_ ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.</p>
	 */
	@Override 
        public Integer visitTryWithResourcesStatement(Java9Parser.TryWithResourcesStatementContext ctx) {
            currentBuffer.append("/* TryWithResourcesStatement - START */").nl();
            currentBuffer.append("{").nl();
            currentBuffer.indentacao++;
            
            Iterator<Java9Parser.ResourceContext> iResource = ctx.resourceSpecification().resourceList().resource().iterator();
            while (iResource.hasNext()) {
                Java9Parser.ResourceContext resource = iResource.next();
                
                String tipo = resource.unannType().getText();
                String nome = resource.variableDeclaratorId().getText();
                
                currentBuffer.append("let ").append(nome).append(": ").append(tipo).append(" = ");
                visitExpression(resource.expression());
                currentBuffer.append(";").nl();
                
            }
            
            visitBlock(ctx.block());
//            currentBuffer.append(ctx.getText());
//            currentBuffer.append(" */");
//            currentBuffer.nl();
//            visitChildren(ctx);
            
            currentBuffer.indentacao--;
            currentBuffer.append("}").nl();
            currentBuffer.append("/* TryWithResourcesStatement - END */").nl();
            return 0; 
        }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.</p>
	 */
	@Override public Integer visitResourceSpecification(Java9Parser.ResourceSpecificationContext ctx) { currentBuffer.append("/* visitResourceSpecification ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.</p>
	 */
	@Override public Integer visitResourceList(Java9Parser.ResourceListContext ctx) { currentBuffer.append("/* visitResourceList ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.</p>
	 */
	@Override public Integer visitResource(Java9Parser.ResourceContext ctx) { currentBuffer.append("/* visitResource ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.</p>
	 */
	@Override public Integer visitVariableAccess(Java9Parser.VariableAccessContext ctx) { currentBuffer.append("/* visitVariableAccess ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

	@Override public Integer visitPrimaryNoNewArray(Java9Parser.PrimaryNoNewArrayContext ctx) { currentBuffer.append("/* visitPrimaryNoNewArray ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.</p>
	 */
	@Override public Integer visitPrimaryNoNewArray_lf_arrayAccess(Java9Parser.PrimaryNoNewArray_lf_arrayAccessContext ctx) { currentBuffer.append("/* visitPrimaryNoNewArray_lf_arrayAccess ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.</p>
	 */
	@Override public Integer visitPrimaryNoNewArray_lfno_arrayAccess(Java9Parser.PrimaryNoNewArray_lfno_arrayAccessContext ctx) { currentBuffer.append("/* visitPrimaryNoNewArray_lfno_arrayAccess ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.</p>
	 */
	@Override public Integer visitPrimaryNoNewArray_lf_primary(Java9Parser.PrimaryNoNewArray_lf_primaryContext ctx) { 
            //TODO: ????
            visitChildren(ctx);return 0; 
        }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.</p>
	 */
	@Override public Integer visitPrimaryNoNewArray_lf_primary_lf_arrayAccess_lf_primary(Java9Parser.PrimaryNoNewArray_lf_primary_lf_arrayAccess_lf_primaryContext ctx) { currentBuffer.append("/* visitPrimaryNoNewArray_lf_primary_lf_arrayAccess_lf_primary ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.</p>
	 */
	@Override public Integer visitPrimaryNoNewArray_lf_primary_lfno_arrayAccess_lf_primary(Java9Parser.PrimaryNoNewArray_lf_primary_lfno_arrayAccess_lf_primaryContext ctx) { currentBuffer.append("/* visitPrimaryNoNewArray_lf_primary_lfno_arrayAccess_lf_primary ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.</p>
	 */
	@Override public Integer visitPrimaryNoNewArray_lfno_primary(Java9Parser.PrimaryNoNewArray_lfno_primaryContext ctx) { 
            if (ctx.literal()!=null) {
                visitLiteral(ctx.literal());
                return 0;
            }
            
            //TODO: OS DEMAIS visitPrimaryNoNewArray_lfno_primary
            //currentBuffer.append("/* visitPrimaryNoNewArray_lfno_primary ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();;return 0; 
            visitChildren(ctx);
            return 0;
        }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.</p>
	 */
	@Override public Integer visitPrimaryNoNewArray_lfno_primary_lf_arrayAccess_lfno_primary(Java9Parser.PrimaryNoNewArray_lfno_primary_lf_arrayAccess_lfno_primaryContext ctx) { currentBuffer.append("/* visitPrimaryNoNewArray_lfno_primary_lf_arrayAccess_lfno_primary ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.</p>
	 */
	@Override public Integer visitPrimaryNoNewArray_lfno_primary_lfno_arrayAccess_lfno_primary(Java9Parser.PrimaryNoNewArray_lfno_primary_lfno_arrayAccess_lfno_primaryContext ctx) { currentBuffer.append("/* visitPrimaryNoNewArray_lfno_primary_lfno_arrayAccess_lfno_primary ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.</p>
	 */
	@Override public Integer visitClassLiteral(Java9Parser.ClassLiteralContext ctx) { currentBuffer.append("/* visitClassLiteral ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.</p>
	 */

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.</p>
	 */
	@Override public Integer visitTypeArgumentsOrDiamond(Java9Parser.TypeArgumentsOrDiamondContext ctx) { currentBuffer.append("/* visitTypeArgumentsOrDiamond ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.</p>
	 */
	@Override public Integer visitFieldAccess(Java9Parser.FieldAccessContext ctx) { currentBuffer.append("/* visitFieldAccess ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.</p>
	 */
	@Override public Integer visitFieldAccess_lf_primary(Java9Parser.FieldAccess_lf_primaryContext ctx) { currentBuffer.append("/* visitFieldAccess_lf_primary ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.</p>
	 */
	@Override public Integer visitFieldAccess_lfno_primary(Java9Parser.FieldAccess_lfno_primaryContext ctx) { currentBuffer.append("/* visitFieldAccess_lfno_primary ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.</p>
	 */
	@Override public Integer visitArrayAccess(Java9Parser.ArrayAccessContext ctx) { currentBuffer.append("/* visitArrayAccess ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.</p>
	 */
	@Override public Integer visitArrayAccess_lf_primary(Java9Parser.ArrayAccess_lf_primaryContext ctx) { currentBuffer.append("/* visitArrayAccess_lf_primary ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.</p>
	 */
	@Override public Integer visitArrayAccess_lfno_primary(Java9Parser.ArrayAccess_lfno_primaryContext ctx) { currentBuffer.append("/* visitArrayAccess_lfno_primary ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.</p>
	 */
	@Override public Integer visitMethodInvocation(Java9Parser.MethodInvocationContext ctx) { 
            if (ctx.methodName()!=null) {
                currentBuffer.append(ctx.methodName().getText());
                currentBuffer.append("(");
                if (ctx.argumentList()!=null) {
                   visitArgumentList(ctx.argumentList()); //TODO
                }
                currentBuffer.append(")");
                return 0;
            }
            
            if (ctx.typeName() != null || ctx.expressionName() != null || ctx.primary() != null) {
                if (ctx.typeName() != null) {
                    currentBuffer.append(ctx.typeName().getText() + ".");
                } else if (ctx.expressionName() != null) {
                    currentBuffer.append(ctx.expressionName().getText() + ".");
                } else {
                    visitPrimary(ctx.primary());
                    currentBuffer.append(".");
                }
                if (ctx.typeArguments()!=null) {
                    Iterator<Java9Parser.TypeArgumentContext> iArgs = ctx.typeArguments().typeArgumentList().typeArgument().iterator();
                    if (iArgs.hasNext()) {
                        Java9Parser.TypeArgumentContext arg = iArgs.next();
                        currentBuffer.append(arg.getText() + ".");
                    }
                }
                currentBuffer.append(ctx.identifier().getText());
                currentBuffer.append("(");
                if (ctx.argumentList()!=null) {
                   visitArgumentList(ctx.argumentList()); //TODO
                }
                currentBuffer.append(")");
                return 0;                
            }
            
            //TODO: OUTROS TIPOS DE METHOD INVOCATION!!!
            currentBuffer.append("/* visitMethodInvocation ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; 
        }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.</p>
	 */
	@Override 
        public Integer visitMethodInvocation_lf_primary(Java9Parser.MethodInvocation_lf_primaryContext ctx) { 
            currentBuffer.append(".");
            if (ctx.typeArguments()!=null) {
                visitTypeArguments(ctx.typeArguments());
            }
            visitIdentifier(ctx.identifier());
            currentBuffer.append("(");
            if (ctx.argumentList()!=null) {
                visitArgumentList(ctx.argumentList());
            }
            currentBuffer.append(")");
            
            return 0;
        }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.</p>
	 */
	@Override public Integer visitMethodInvocation_lfno_primary(Java9Parser.MethodInvocation_lfno_primaryContext ctx) {
            if (ctx.methodName()!=null) {
                currentBuffer.append(ctx.methodName().getText());
                currentBuffer.append("(");
                if (ctx.argumentList()!=null) {
                   currentBuffer.append(ctx.argumentList().getText()); //TODO
                }
                currentBuffer.append(")");
                return 0;
            }
            
            if (ctx.typeName() != null || ctx.expressionName() != null) {
                if (ctx.typeName() != null) {
                    currentBuffer.append(ctx.typeName().getText() + ".");
                } else if (ctx.expressionName() != null) {
                    currentBuffer.append(ctx.expressionName().getText() + ".");
                }
                if (ctx.typeArguments()!=null) {
                    Iterator<Java9Parser.TypeArgumentContext> iArgs = ctx.typeArguments().typeArgumentList().typeArgument().iterator();
                    if (iArgs.hasNext()) {
                        Java9Parser.TypeArgumentContext arg = iArgs.next();
                        currentBuffer.append(arg.getText() + ".");
                    }
                }
                currentBuffer.append(ctx.identifier().getText());
                currentBuffer.append("(");
                if (ctx.argumentList()!=null) {
                   currentBuffer.append(ctx.argumentList().getText()); //TODO
                }
                currentBuffer.append(")");
                return 0;                
            }
            
            //TODO: OUTROS TIPOS DE METHOD INVOCATION!!!
            currentBuffer.append("/* visitMethodInvocation_lfno_primary ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; 
        }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.</p>
	 */
	@Override 
        public Integer visitArgumentList(Java9Parser.ArgumentListContext ctx) {
            if (ctx==null) return 0;
            
            Iterator<Java9Parser.ExpressionContext> iLista = ctx.expression().iterator();
            
            int count=0;
            while (iLista.hasNext()) {
                Java9Parser.ExpressionContext e = iLista.next();
                count++;
                
                if (count>1) currentBuffer.append(", ");
                
                visitExpression(e);
            }
            
            return 0;
        }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.</p>
	 */
	@Override public Integer visitMethodReference(Java9Parser.MethodReferenceContext ctx) { currentBuffer.append("/* visitMethodReference ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.</p>
	 */
	@Override public Integer visitMethodReference_lf_primary(Java9Parser.MethodReference_lf_primaryContext ctx) { currentBuffer.append("/* visitMethodReference_lf_primary ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.</p>
	 */
	@Override public Integer visitMethodReference_lfno_primary(Java9Parser.MethodReference_lfno_primaryContext ctx) { currentBuffer.append("/* visitMethodReference_lfno_primary ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.</p>
	 */
	
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.</p>
	 */
	@Override public Integer visitDimExprs(Java9Parser.DimExprsContext ctx) { currentBuffer.append("/* visitDimExprs ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.</p>
	 */
	@Override public Integer visitDimExpr(Java9Parser.DimExprContext ctx) { currentBuffer.append("/* visitDimExpr ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

	@Override public Integer visitLambdaExpression(Java9Parser.LambdaExpressionContext ctx) { currentBuffer.append("/* visitLambdaExpression ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.</p>
	 */
	@Override public Integer visitLambdaParameters(Java9Parser.LambdaParametersContext ctx) { currentBuffer.append("/* visitLambdaParameters ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.</p>
	 */
	@Override public Integer visitInferredFormalParameterList(Java9Parser.InferredFormalParameterListContext ctx) { currentBuffer.append("/* visitInferredFormalParameterList ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.</p>
	 */
	@Override public Integer visitLambdaBody(Java9Parser.LambdaBodyContext ctx) { currentBuffer.append("/* visitLambdaBody ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

	@Override public Integer visitAssignment(Java9Parser.AssignmentContext ctx) { 
            currentBuffer.append(ctx.leftHandSide().getText());
            currentBuffer.append(" = ");
            currentBuffer.append(ctx.expression().getText());
            currentBuffer.append(";").nl();
            return 0;
        }

	@Override public Integer visitAssignmentOperator(Java9Parser.AssignmentOperatorContext ctx) { 
            currentBuffer.append(" = ");
            return 0;
        }

        @Override 
        public Integer visitConditionalExpression(Java9Parser.ConditionalExpressionContext ctx) { 
            
            
            //TODO: ternario => bool ? x : y
            if (ctx.expression()!=null) {
                currentBuffer.append(ctx.getText());
                return 0;
            }
        
            visitConditionalOrExpression(ctx.conditionalOrExpression());
            return 0;
        }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.</p>
	 */
	@Override 
        public Integer visitConditionalOrExpression(Java9Parser.ConditionalOrExpressionContext ctx) { 
            if (ctx.conditionalOrExpression()!=null) {
                visitConditionalOrExpression(ctx.conditionalOrExpression());
                currentBuffer.append (" || ");
            }
            
            visitConditionalAndExpression(ctx.conditionalAndExpression());
            
            return 0;
        }

	@Override 
        public Integer visitConditionalAndExpression(Java9Parser.ConditionalAndExpressionContext ctx) { 
            if (ctx.conditionalAndExpression()!=null) {
                visitConditionalAndExpression(ctx.conditionalAndExpression());
                currentBuffer.append (" && ");
            }
            
            visitInclusiveOrExpression(ctx.inclusiveOrExpression());
            return 0;
        }

	@Override 
        public Integer visitInclusiveOrExpression(Java9Parser.InclusiveOrExpressionContext ctx) { 
            if (ctx.inclusiveOrExpression()!=null) {
                visitInclusiveOrExpression(ctx.inclusiveOrExpression());
                currentBuffer.append (" | ");
            }
            
            visitExclusiveOrExpression(ctx.exclusiveOrExpression());
            return 0;        
        }

	@Override 
        public Integer visitExclusiveOrExpression(Java9Parser.ExclusiveOrExpressionContext ctx) { 
            if (ctx.exclusiveOrExpression()!=null) {
                visitExclusiveOrExpression(ctx.exclusiveOrExpression());
                currentBuffer.append (" ^ ");
            }
            
            visitAndExpression(ctx.andExpression());
            return 0; 
        }

	@Override public Integer visitAndExpression(Java9Parser.AndExpressionContext ctx) { 
            if (ctx.andExpression()!=null) {
                visitAndExpression(ctx.andExpression());
                currentBuffer.append (" & ");
            }
            
            visitEqualityExpression(ctx.equalityExpression());
            return 0;         
        }

	@Override 
        public Integer visitEqualityExpression(Java9Parser.EqualityExpressionContext ctx) { 
            if (ctx.equalityExpression()!=null) {
                visitEqualityExpression(ctx.equalityExpression());
                
                if (ctx.EQUAL()!=null) {
                    currentBuffer.append (" == ");
                } else {
                    currentBuffer.append (" != ");
                }
            }
            
            visitRelationalExpression(ctx.relationalExpression());
            return 0;                
        }

	@Override public Integer visitRelationalExpression(Java9Parser.RelationalExpressionContext ctx) { 
        
            if (ctx.relationalExpression()!=null) {
                visitRelationalExpression(ctx.relationalExpression());
                
                if (ctx.INSTANCEOF()!=null) {
                    currentBuffer.append(" instanceof");
                    visitReferenceType(ctx.referenceType());
                    return 0;
                }
                
                if (ctx.GE()!=null) currentBuffer.append (" >= ");
                if (ctx.GT()!=null) currentBuffer.append (" > ");
                if (ctx.LE()!=null) currentBuffer.append (" <= ");
                if (ctx.LT()!=null) currentBuffer.append (" < ");
            }
            
            visitShiftExpression(ctx.shiftExpression());
            return 0;        
        
        
        }

        @Override 
        public Integer visitShiftExpression(Java9Parser.ShiftExpressionContext ctx) { 
            if (ctx.shiftExpression()!=null) {
                visitShiftExpression(ctx.shiftExpression());
                
                if (ctx.LT()!=null) {
                    currentBuffer.append(" << ");
                } else {
                    if (ctx.LT().size()==2) {
                        currentBuffer.append(" >> ");
                    } else {
                        currentBuffer.append(" >>> ");
                    }
                }
            }
            
            visitAdditiveExpression(ctx.additiveExpression());
            
            return 0;
            
        }

	@Override 
        public Integer visitAdditiveExpression(Java9Parser.AdditiveExpressionContext ctx) { 
            if (ctx.additiveExpression()!=null) {
                visitAdditiveExpression(ctx.additiveExpression());
                
                if (ctx.ADD()!=null) {
                    currentBuffer.append(" + ");
                } else {
                    currentBuffer.append(" - ");
                }
            }
            
            visitMultiplicativeExpression(ctx.multiplicativeExpression());                
                
            return 0;
        }

	@Override 
        public Integer visitMultiplicativeExpression(Java9Parser.MultiplicativeExpressionContext ctx) { 

            if (ctx.multiplicativeExpression()!=null) {
                visitMultiplicativeExpression(ctx.multiplicativeExpression());
                
                if (ctx.DIV()!=null) { currentBuffer.append(" / "); }
                if (ctx.MUL()!=null) { currentBuffer.append(" * "); }
                if (ctx.MOD()!=null) { currentBuffer.append(" % "); }

            }
            
            visitUnaryExpression(ctx.unaryExpression());                
                
            return 0;            
        
        }

	@Override 
        public Integer visitUnaryExpression(Java9Parser.UnaryExpressionContext ctx) { 

            if (ctx.preIncrementExpression()!=null) {
                visitPreIncrementExpression(ctx.preIncrementExpression());
                return 0;
            }
            
            if (ctx.preDecrementExpression()!=null) {
                visitPreDecrementExpression(ctx.preDecrementExpression());
                return 0;
            }            
            
            if (ctx.ADD()!=null) {
                currentBuffer.append(" + ");
                visitUnaryExpression(ctx.unaryExpression());
                return 0;
            }
            
            if (ctx.SUB()!=null) {
                currentBuffer.append(" - ");
                visitUnaryExpression(ctx.unaryExpression());
                return 0;
            }            
            
            if (ctx.unaryExpressionNotPlusMinus()!=null) {
                visitUnaryExpressionNotPlusMinus(ctx.unaryExpressionNotPlusMinus());
                return 0;
            }
            
            return 0;
        }

	@Override 
        public Integer visitPreIncrementExpression(Java9Parser.PreIncrementExpressionContext ctx) { 
            currentBuffer.append(" ++");
            visitUnaryExpression(ctx.unaryExpression());
            return 0;
        }

	@Override 
        public Integer visitPreDecrementExpression(Java9Parser.PreDecrementExpressionContext ctx) { 
            currentBuffer.append(" --");
            visitUnaryExpression(ctx.unaryExpression());
            return 0;
        }

	@Override 
        public Integer visitUnaryExpressionNotPlusMinus(Java9Parser.UnaryExpressionNotPlusMinusContext ctx) { 
            if (ctx.postfixExpression()!=null) {
                visitPostfixExpression(ctx.postfixExpression());
                return 0;
            }
            
            if (ctx.TILDE()!=null) {
                currentBuffer.append("~");
                visitUnaryExpression(ctx.unaryExpression());
                return 0;
            }

            if (ctx.BANG()!=null) {
                currentBuffer.append("!");
                visitUnaryExpression(ctx.unaryExpression());
                return 0;
            }

            if (ctx.castExpression()!=null) {
                visitCastExpression(ctx.castExpression());
                return 0;
            }            
            
            return 0;
        }

	@Override public Integer visitPostIncrementExpression(Java9Parser.PostIncrementExpressionContext ctx) { 
            visitPostfixExpression(ctx.postfixExpression());
            currentBuffer.append("++ ");
            return 0;
        }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.</p>
	 */
	@Override public Integer visitPostIncrementExpression_lf_postfixExpression(Java9Parser.PostIncrementExpression_lf_postfixExpressionContext ctx) { 
            currentBuffer.append("++ ");
            return 0; 
        }

	@Override public Integer visitPostDecrementExpression(Java9Parser.PostDecrementExpressionContext ctx) { 
            visitPostfixExpression(ctx.postfixExpression());
            currentBuffer.append("-- ");
            return 0;
        }

	@Override 
        public Integer visitPostDecrementExpression_lf_postfixExpression(Java9Parser.PostDecrementExpression_lf_postfixExpressionContext ctx) { 
            currentBuffer.append("-- ");
            return 0; 
        }            

	@Override 
        public Integer visitCastExpression(Java9Parser.CastExpressionContext ctx) { 
            //TODO: CAST
            
            if (ctx.unaryExpression()!=null) visitUnaryExpression(ctx.unaryExpression());
            
            if (ctx.unaryExpressionNotPlusMinus()!=null) visitUnaryExpressionNotPlusMinus(ctx.unaryExpressionNotPlusMinus());
            
            if (ctx.lambdaExpression()!=null) visitLambdaExpression(ctx.lambdaExpression());
            
            currentBuffer.append ("/* CAST NAO IMPLEMENTADO!!! */");
            
            return 0;
            
        }

    @Override 
    public Integer visitIdentifier(Java9Parser.IdentifierContext ctx) { 
        currentBuffer.append(ctx.getText());
        return 0; 
    }

    @Override 
    public Integer visitVariableInitializer(Java9Parser.VariableInitializerContext ctx) {
        if (ctx.expression()!=null) {
            visitExpression(ctx.expression());
        }
        
        if (ctx.arrayInitializer()!=null) {
            visitArrayInitializer(ctx.arrayInitializer());
        }
        
        return 0;
    }

    @Override public Integer visitAnnotation(Java9Parser.AnnotationContext ctx) { return 0; }

    @Override 
    public Integer visitBasicForStatement(Java9Parser.BasicForStatementContext ctx) {
        
        //for looping infinito
        if (ctx.forInit()==null && ctx.expression() == null && ctx.forUpdate() == null) {
            currentBuffer.append("while true /* for (;;) */").nl();
            visitStatement(ctx.statement());
            return 0;
        }
        
        currentBuffer.nl().append("/* FOR - BEGIN */").nl();
        currentBuffer.append("{").nl();
        currentBuffer.indentacao++;
        
        
        
        if (ctx.forInit()!=null) {
            visitForInit(ctx.forInit());
        }
        
        currentBuffer.append("let mut __first_for_guard: bool = false;").nl();
        currentBuffer.append("while true {").nl();
        currentBuffer.indentacao++;
        
        currentBuffer.append("if __first_for_guard {").nl();
        currentBuffer.indentacao++;
        
        currentBuffer.indentacao--;
        currentBuffer.append("} /* __first_for_guard */").nl();
        currentBuffer.append("__first_for_guard = true;").nl();
        
        currentBuffer.append("if !(");
        if (ctx.expression()==null) {
            currentBuffer.append ("true /* looping infinito */");
        } else {
            visitExpression(ctx.expression());
        }
        currentBuffer.append(") { break; }").nl().nl();
        
        currentBuffer.indentacao++;
        visitStatement(ctx.statement());
        currentBuffer.indentacao--;        
        
        currentBuffer.indentacao--;
        currentBuffer.append("} /* while */").nl();
                
        currentBuffer.indentacao--; 
        currentBuffer.append("}").nl();
        currentBuffer.append("/* FOR - END */").nl().nl();
       
        return 0; 
    }

    @Override 
    public Integer visitEnhancedForStatement(Java9Parser.EnhancedForStatementContext ctx) { 
        String tipo = rustType(ctx.unannType().getText());
        
        currentBuffer.append("for ");
        
        currentBuffer.append(ctx.variableDeclaratorId().identifier().getText() + ": " + tipo);
        currentBuffer.append(" in ");
        visitExpression(ctx.expression());
        visitStatement(ctx.statement());
        
        return 0;
    }

    @Override public Integer visitEnhancedForStatementNoShortIf(Java9Parser.EnhancedForStatementNoShortIfContext ctx) { currentBuffer.append("/* visitEnhancedForStatementNoShortIf ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }
    
    @Override 
    public Integer visitConstructorDeclarator(Java9Parser.ConstructorDeclaratorContext ctx) {
        
        currentBuffer = construtorX;
        
        String tipo = ctx.simpleTypeName().getText().trim();
        
        currentBuffer.append("pub fn new").append(tipo).append(" (");
        
        if (ctx.formalParameterList()!=null) {
            visitFormalParameterList(ctx.formalParameterList());
        }
        
        currentBuffer.append(") -> " + tipo);
        
        
        currentBuffer = impl;
        
        return 0; 
    }    
    
@Override 
public Integer visitConstructorBody(Java9Parser.ConstructorBodyContext ctx) { 

    currentBuffer = construtorX;

    currentBuffer.append(" {").nl();
    currentBuffer.indentacao++;

    currentBuffer.append(nomeClasse);
    currentBuffer.append(" {").nl();
    currentBuffer.indentacao++;
    
    if (ctx.blockStatements()!=null) {
        visitBlockStatements(ctx.blockStatements());
    }
    
    currentBuffer.indentacao--;
    currentBuffer.append("}").nl();
    
    currentBuffer.indentacao--;
    currentBuffer.append("}").nl();

    currentBuffer = impl;
    
    return 0; 
}    
    
    
    
    private String rustType(String text) {
        switch (text) {
            case "long": return "i64";
            case "int": return "i32";
            case "boolean": return "bool";
            case "Double": return "f64";
            case "double": return "f64";
            default: return text;
        }
    }
    
    @Override public Integer visitPrimitiveType(Java9Parser.PrimitiveTypeContext ctx) { currentBuffer.append("/* visitPrimitiveType ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

    @Override public Integer visitNumericType(Java9Parser.NumericTypeContext ctx) { currentBuffer.append("/* visitNumericType ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

    @Override public Integer visitIntegralType(Java9Parser.IntegralTypeContext ctx) { currentBuffer.append("/* visitIntegralType ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

    @Override public Integer visitFloatingPointType(Java9Parser.FloatingPointTypeContext ctx) { currentBuffer.append("/* visitFloatingPointType ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

    @Override public Integer visitReferenceType(Java9Parser.ReferenceTypeContext ctx) { currentBuffer.append("/* visitReferenceType ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

    @Override public Integer visitClassOrInterfaceType(Java9Parser.ClassOrInterfaceTypeContext ctx) { currentBuffer.append("/* visitClassOrInterfaceType ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

    @Override public Integer visitClassType(Java9Parser.ClassTypeContext ctx) { currentBuffer.append("/* visitClassType ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

    @Override public Integer visitClassType_lf_classOrInterfaceType(Java9Parser.ClassType_lf_classOrInterfaceTypeContext ctx) { currentBuffer.append("/* visitClassType_lf_classOrInterfaceType ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

    @Override public Integer visitClassType_lfno_classOrInterfaceType(Java9Parser.ClassType_lfno_classOrInterfaceTypeContext ctx) { currentBuffer.append("/* visitClassType_lfno_classOrInterfaceType ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

    @Override public Integer visitInterfaceType(Java9Parser.InterfaceTypeContext ctx) { currentBuffer.append("/* visitInterfaceType ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

    @Override public Integer visitInterfaceType_lf_classOrInterfaceType(Java9Parser.InterfaceType_lf_classOrInterfaceTypeContext ctx) { currentBuffer.append("/* visitInterfaceType_lf_classOrInterfaceType ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

    @Override public Integer visitInterfaceType_lfno_classOrInterfaceType(Java9Parser.InterfaceType_lfno_classOrInterfaceTypeContext ctx) { currentBuffer.append("/* visitInterfaceType_lfno_classOrInterfaceType ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

    @Override public Integer visitTypeVariable(Java9Parser.TypeVariableContext ctx) { currentBuffer.append("/* visitTypeVariable ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

    @Override public Integer visitArrayType(Java9Parser.ArrayTypeContext ctx) { currentBuffer.append("/* visitArrayType ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

    @Override public Integer visitDims(Java9Parser.DimsContext ctx) { currentBuffer.append("/* visitDims ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

    @Override public Integer visitTypeParameter(Java9Parser.TypeParameterContext ctx) { currentBuffer.append("/* visitTypeParameter ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

    @Override public Integer visitTypeParameterModifier(Java9Parser.TypeParameterModifierContext ctx) { currentBuffer.append("/* visitTypeParameterModifier ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

    @Override public Integer visitTypeBound(Java9Parser.TypeBoundContext ctx) { currentBuffer.append("/* visitTypeBound ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

    @Override public Integer visitAdditionalBound(Java9Parser.AdditionalBoundContext ctx) { currentBuffer.append("/* visitAdditionalBound ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

    @Override public Integer visitTypeArguments(Java9Parser.TypeArgumentsContext ctx) { currentBuffer.append("/* visitTypeArguments ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

    @Override public Integer visitTypeArgumentList(Java9Parser.TypeArgumentListContext ctx) { currentBuffer.append("/* visitTypeArgumentList ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

    @Override public Integer visitTypeArgument(Java9Parser.TypeArgumentContext ctx) { currentBuffer.append("/* visitTypeArgument ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

    @Override public Integer visitWildcard(Java9Parser.WildcardContext ctx) { currentBuffer.append("/* visitWildcard ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

    @Override public Integer visitWildcardBounds(Java9Parser.WildcardBoundsContext ctx) { currentBuffer.append("/* visitWildcardBounds ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

    @Override public Integer visitModuleName(Java9Parser.ModuleNameContext ctx) { currentBuffer.append("/* visitModuleName ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

    @Override public Integer visitPackageName(Java9Parser.PackageNameContext ctx) { currentBuffer.append("/* visitPackageName ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

    @Override public Integer visitTypeName(Java9Parser.TypeNameContext ctx) { currentBuffer.append("/* visitTypeName ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

    @Override public Integer visitPackageOrTypeName(Java9Parser.PackageOrTypeNameContext ctx) { currentBuffer.append("/* visitPackageOrTypeName ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }
    
    @Override public Integer visitMethodName(Java9Parser.MethodNameContext ctx) { currentBuffer.append("/* visitMethodName ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

    @Override public Integer visitModularCompilation(Java9Parser.ModularCompilationContext ctx) { currentBuffer.append("/* visitModularCompilation ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

    @Override public Integer visitPackageModifier(Java9Parser.PackageModifierContext ctx) { currentBuffer.append("/* visitPackageModifier ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

    @Override public Integer visitStaticImportOnDemandDeclaration(Java9Parser.StaticImportOnDemandDeclarationContext ctx) { currentBuffer.append("/* visitStaticImportOnDemandDeclaration ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

    @Override public Integer visitModuleDeclaration(Java9Parser.ModuleDeclarationContext ctx) { currentBuffer.append("/* visitModuleDeclaration ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

    @Override public Integer visitModuleDirective(Java9Parser.ModuleDirectiveContext ctx) { currentBuffer.append("/* visitModuleDirective ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

    @Override public Integer visitRequiresModifier(Java9Parser.RequiresModifierContext ctx) { currentBuffer.append("/* visitRequiresModifier ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }
    
    @Override public Integer visitClassModifier(Java9Parser.ClassModifierContext ctx) { currentBuffer.append("/* visitClassModifier ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

    @Override public Integer visitTypeParameters(Java9Parser.TypeParametersContext ctx) { currentBuffer.append("/* visitTypeParameters ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

    @Override public Integer visitTypeParameterList(Java9Parser.TypeParameterListContext ctx) { currentBuffer.append("/* visitTypeParameterList ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

    @Override public Integer visitSuperclass(Java9Parser.SuperclassContext ctx) { currentBuffer.append("/* visitSuperclass ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

    @Override public Integer visitSuperinterfaces(Java9Parser.SuperinterfacesContext ctx) { currentBuffer.append("/* visitSuperinterfaces ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

    @Override public Integer visitInterfaceTypeList(Java9Parser.InterfaceTypeListContext ctx) { currentBuffer.append("/* visitInterfaceTypeList ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

        @Override public Integer visitFieldModifier(Java9Parser.FieldModifierContext ctx) { currentBuffer.append("/* visitFieldModifier ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

        @Override public Integer visitVariableDeclaratorList(Java9Parser.VariableDeclaratorListContext ctx) { currentBuffer.append("/* visitVariableDeclaratorList ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

        @Override public Integer visitVariableDeclarator(Java9Parser.VariableDeclaratorContext ctx) { currentBuffer.append("/* visitVariableDeclarator ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

        @Override public Integer visitVariableDeclaratorId(Java9Parser.VariableDeclaratorIdContext ctx) { currentBuffer.append("/* visitVariableDeclaratorId ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

        @Override public Integer visitUnannType(Java9Parser.UnannTypeContext ctx) { currentBuffer.append("/* visitUnannType ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

        @Override public Integer visitUnannPrimitiveType(Java9Parser.UnannPrimitiveTypeContext ctx) { currentBuffer.append("/* visitUnannPrimitiveType ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

        @Override public Integer visitUnannReferenceType(Java9Parser.UnannReferenceTypeContext ctx) { currentBuffer.append("/* visitUnannReferenceType ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

        @Override public Integer visitUnannClassOrInterfaceType(Java9Parser.UnannClassOrInterfaceTypeContext ctx) { currentBuffer.append("/* visitUnannClassOrInterfaceType ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

        @Override public Integer visitUnannClassType(Java9Parser.UnannClassTypeContext ctx) { currentBuffer.append("/* visitUnannClassType ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

        @Override public Integer visitUnannClassType_lf_unannClassOrInterfaceType(Java9Parser.UnannClassType_lf_unannClassOrInterfaceTypeContext ctx) { currentBuffer.append("/* vist ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

        @Override public Integer visitUnannClassType_lfno_unannClassOrInterfaceType(Java9Parser.UnannClassType_lfno_unannClassOrInterfaceTypeContext ctx) { currentBuffer.append("/* vist ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

        @Override public Integer visitUnannInterfaceType(Java9Parser.UnannInterfaceTypeContext ctx) { currentBuffer.append("/* vist ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

        @Override public Integer visitUnannInterfaceType_lf_unannClassOrInterfaceType(Java9Parser.UnannInterfaceType_lf_unannClassOrInterfaceTypeContext ctx) { currentBuffer.append("/* vist ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

        @Override public Integer visitUnannInterfaceType_lfno_unannClassOrInterfaceType(Java9Parser.UnannInterfaceType_lfno_unannClassOrInterfaceTypeContext ctx) { currentBuffer.append("/* vist ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

        @Override public Integer visitUnannTypeVariable(Java9Parser.UnannTypeVariableContext ctx) { currentBuffer.append("/* visitUnannTypeVariable ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

        @Override public Integer visitUnannArrayType(Java9Parser.UnannArrayTypeContext ctx) { currentBuffer.append("/* visitUnannArrayType ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

        @Override public Integer visitVariableModifier(Java9Parser.VariableModifierContext ctx) { currentBuffer.append("/* visitVariableModifier ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

        @Override public Integer visitLastFormalParameter(Java9Parser.LastFormalParameterContext ctx) { currentBuffer.append("/* visitLastFormalParameter ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

        @Override public Integer visitReceiverParameter(Java9Parser.ReceiverParameterContext ctx) { currentBuffer.append("/* visitReceiverParameter ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

        @Override public Integer visitThrows_(Java9Parser.Throws_Context ctx) { currentBuffer.append("/* visitThrows_ ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

        @Override public Integer visitExceptionTypeList(Java9Parser.ExceptionTypeListContext ctx) { currentBuffer.append("/* visitExceptionTypeList ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

        @Override public Integer visitExceptionType(Java9Parser.ExceptionTypeContext ctx) { currentBuffer.append("/* visitExceptionType ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

	@Override public Integer visitInstanceInitializer(Java9Parser.InstanceInitializerContext ctx) { currentBuffer.append("/* visitInstanceInitializer ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

        @Override public Integer visitStaticInitializer(Java9Parser.StaticInitializerContext ctx) { currentBuffer.append("/* visitStaticInitializer ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

        @Override public Integer visitSimpleTypeName(Java9Parser.SimpleTypeNameContext ctx) { currentBuffer.append("/* visitSimpleTypeName ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

        @Override public Integer visitExplicitConstructorInvocation(Java9Parser.ExplicitConstructorInvocationContext ctx) { currentBuffer.append("/* visitExplicitConstructorInvocation ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }
        
        @Override public Integer visitEnumBody(Java9Parser.EnumBodyContext ctx) { currentBuffer.append("/* visitEnumBody ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

        @Override public Integer visitEnumConstantList(Java9Parser.EnumConstantListContext ctx) { currentBuffer.append("/* visitEnumConstantList ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

        @Override public Integer visitEnumConstant(Java9Parser.EnumConstantContext ctx) { currentBuffer.append("/* visitEnumConstant ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

        @Override public Integer visitEnumConstantModifier(Java9Parser.EnumConstantModifierContext ctx) { currentBuffer.append("/* visitEnumConstantModifier ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

        @Override public Integer visitEnumBodyDeclarations(Java9Parser.EnumBodyDeclarationsContext ctx) { currentBuffer.append("/* visitEnumBodyDeclarations ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

        @Override public Integer visitInterfaceDeclaration(Java9Parser.InterfaceDeclarationContext ctx) { currentBuffer.append("/* visitInterfaceDeclaration ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

        @Override public Integer visitNormalInterfaceDeclaration(Java9Parser.NormalInterfaceDeclarationContext ctx) { currentBuffer.append("/* visitNormalInterfaceDeclaration ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

        @Override public Integer visitInterfaceModifier(Java9Parser.InterfaceModifierContext ctx) { currentBuffer.append("/* visitInterfaceModifier ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

        @Override public Integer visitExtendsInterfaces(Java9Parser.ExtendsInterfacesContext ctx) { currentBuffer.append("/* visitExtendsInterfaces ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

        @Override public Integer visitInterfaceBody(Java9Parser.InterfaceBodyContext ctx) { currentBuffer.append("/* visitInterfaceBody ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

        @Override public Integer visitInterfaceMemberDeclaration(Java9Parser.InterfaceMemberDeclarationContext ctx) { currentBuffer.append("/* visitInterfaceMemberDeclaration ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

        @Override public Integer visitConstantDeclaration(Java9Parser.ConstantDeclarationContext ctx) { currentBuffer.append("/* visitConstantDeclaration ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

        @Override public Integer visitConstantModifier(Java9Parser.ConstantModifierContext ctx) { currentBuffer.append("/* visitConstantModifier ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

        @Override public Integer visitInterfaceMethodDeclaration(Java9Parser.InterfaceMethodDeclarationContext ctx) { currentBuffer.append("/* visitInterfaceMethodDeclaration ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

        @Override public Integer visitInterfaceMethodModifier(Java9Parser.InterfaceMethodModifierContext ctx) { currentBuffer.append("/* visitInterfaceMethodModifier ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

        @Override public Integer visitAnnotationTypeBody(Java9Parser.AnnotationTypeBodyContext ctx) { currentBuffer.append("/* visitAnnotationTypeBody ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

        @Override public Integer visitAnnotationTypeMemberDeclaration(Java9Parser.AnnotationTypeMemberDeclarationContext ctx) { currentBuffer.append("/* visitAnnotationTypeMemberDeclaration ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

        @Override public Integer visitAnnotationTypeElementDeclaration(Java9Parser.AnnotationTypeElementDeclarationContext ctx) { currentBuffer.append("/* visitAnnotationTypeElementDeclaration ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

        @Override public Integer visitAnnotationTypeElementModifier(Java9Parser.AnnotationTypeElementModifierContext ctx) { currentBuffer.append("/* visitAnnotationTypeElementModifier ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

        @Override public Integer visitDefaultValue(Java9Parser.DefaultValueContext ctx) { currentBuffer.append("/* visitDefaultValue ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

        @Override public Integer visitNormalAnnotation(Java9Parser.NormalAnnotationContext ctx) { currentBuffer.append("/* visitNormalAnnotation ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

        @Override public Integer visitElementValuePairList(Java9Parser.ElementValuePairListContext ctx) { currentBuffer.append("/* visitElementValuePairList ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

        @Override public Integer visitElementValuePair(Java9Parser.ElementValuePairContext ctx) { currentBuffer.append("/* visitElementValuePair ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

        @Override public Integer visitElementValue(Java9Parser.ElementValueContext ctx) { currentBuffer.append("/* visitElementValue ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

        @Override public Integer visitElementValueArrayInitializer(Java9Parser.ElementValueArrayInitializerContext ctx) { currentBuffer.append("/* visitElementValueArrayInitializer ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

        @Override public Integer visitElementValueList(Java9Parser.ElementValueListContext ctx) { currentBuffer.append("/* visitElementValueList ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

        @Override public Integer visitMarkerAnnotation(Java9Parser.MarkerAnnotationContext ctx) { currentBuffer.append("/* visitMarkerAnnotation ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

        @Override public Integer visitSingleElementAnnotation(Java9Parser.SingleElementAnnotationContext ctx) { currentBuffer.append("/* visitSingleElementAnnotation ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

        @Override public Integer visitArrayInitializer(Java9Parser.ArrayInitializerContext ctx) { currentBuffer.append("/* visitArrayInitializer ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

        @Override public Integer visitVariableInitializerList(Java9Parser.VariableInitializerListContext ctx) { currentBuffer.append("/* visitVariableInitializerList ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

        @Override public Integer visitEmptyStatement(Java9Parser.EmptyStatementContext ctx) { currentBuffer.append("/* visitEmptyStatement ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

	@Override public Integer visitLabeledStatement(Java9Parser.LabeledStatementContext ctx) { currentBuffer.append("/* visitLabeledStatement ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

        @Override public Integer visitLabeledStatementNoShortIf(Java9Parser.LabeledStatementNoShortIfContext ctx) { currentBuffer.append("/* visitLabeledStatementNoShortIf ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

	@Override public Integer visitMethodDeclarator(Java9Parser.MethodDeclaratorContext ctx) { currentBuffer.append("/* visitMethodDeclarator ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

        @Override public Integer visitFormalParameters(Java9Parser.FormalParametersContext ctx) { currentBuffer.append("/* visitFormalParameters ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

        @Override public Integer visitDoStatement(Java9Parser.DoStatementContext ctx) { currentBuffer.append("/* visitDoStatement ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

        @Override public Integer visitForStatementNoShortIf(Java9Parser.ForStatementNoShortIfContext ctx) { currentBuffer.append("/* visitForStatementNoShortIf ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

        @Override public Integer visitBasicForStatementNoShortIf(Java9Parser.BasicForStatementNoShortIfContext ctx) { currentBuffer.append("/* visitBasicForStatementNoShortIf ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

        @Override public Integer visitForUpdate(Java9Parser.ForUpdateContext ctx) { currentBuffer.append("/* visitForUpdate ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

        @Override public Integer visitStatementExpressionList(Java9Parser.StatementExpressionListContext ctx) { currentBuffer.append("/* visitStatementExpressionList ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

	@Override public Integer visitThrowStatement(Java9Parser.ThrowStatementContext ctx) { currentBuffer.append("/* visitThrowStatement ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }

        @Override public Integer visitSynchronizedStatement(Java9Parser.SynchronizedStatementContext ctx) { currentBuffer.append("/* visitSynchronizedStatement ==> ");currentBuffer.append(ctx.getText());currentBuffer.append(" */");currentBuffer.nl();visitChildren(ctx);return 0; }
        
}
