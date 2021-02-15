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
public class Java2RustPOC extends Java9BaseListener {

    StringBuffer struct = new StringBuffer();
    StringBuffer impl = new StringBuffer();

    boolean fieldDeclaration = false;
    private String fieldType;
    private String fieldName;

    boolean methodHeader = false;
    private String methodHeaderType;

    private String getType(String type) {
        if (type == null) {
            return "/* null */";
        }

        switch (type) {
            case "long":
                return "i64";
            case "boolean":
                return "bool";
            default:
                return type;
        }
    }

    @Override
    public void enterNormalClassDeclaration(Java9Parser.NormalClassDeclarationContext ctx) {
        struct.append("struct " + ctx.identifier().getText() + "{\n");
        impl.append("impl " + ctx.identifier().getText() + " {\n");
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * The default implementation does nothing.</p>
     */
    @Override
    public void exitNormalClassDeclaration(Java9Parser.NormalClassDeclarationContext ctx) {
        struct.append("} /* close struct */\n");
        impl.append("} /* close impl */\n");
    }

    @Override
    public void enterFieldDeclaration(Java9Parser.FieldDeclarationContext ctx) {
        fieldDeclaration = true;
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * The default implementation does nothing.</p>
     */
    @Override
    public void exitFieldDeclaration(Java9Parser.FieldDeclarationContext ctx) {
        struct.append(fieldName + ": " + getType(fieldType) + ",\n");
        fieldDeclaration = false;
    }

    @Override
    public void enterUnannType(Java9Parser.UnannTypeContext ctx) {
        this.fieldType = ctx.getText();
    }

    @Override
    public void enterVariableDeclaratorId(Java9Parser.VariableDeclaratorIdContext ctx) {
        this.fieldName = ctx.getText();
    }

    @Override
    public void exitCompilationUnit(Java9Parser.CompilationUnitContext ctx) {
        System.out.println(struct);
        System.out.println(impl);
    }

    @Override
    public void enterMethodHeader(Java9Parser.MethodHeaderContext ctx) {
        methodHeader = true;
        methodHeaderType = ctx.result().getText();
    }

    @Override
    public void exitMethodHeader(Java9Parser.MethodHeaderContext ctx) {
        impl.append("\n");
        methodHeader = false;
    }

    @Override
    public void enterMethodDeclarator(Java9Parser.MethodDeclaratorContext ctx) {
        impl.append("fn " + ctx.identifier().getText() + "( ");
    }

    @Override
    public void exitMethodDeclarator(Java9Parser.MethodDeclaratorContext ctx) {
        impl.append(") -> " + getType(methodHeaderType) + " \n");
    }

    @Override
    public void exitFormalParameter(Java9Parser.FormalParameterContext ctx) {
        impl.append(fieldName + ": " + getType(fieldType));
    }

    @Override
    public void enterBlock(Java9Parser.BlockContext ctx) {
        impl.append("{--enterBlock\n");
    }

    @Override
    public void exitBlock(Java9Parser.BlockContext ctx) {
        impl.append("\n}--exitBlock\n");
    }

    @Override
    public void enterLocalVariableDeclarationStatement(Java9Parser.LocalVariableDeclarationStatementContext ctx) {
        impl.append(ctx.getText()).append("\n");
    }

    @Override
    public void exitLocalVariableDeclarationStatement(Java9Parser.LocalVariableDeclarationStatementContext ctx) {
    }

    @Override
    public void enterClassDeclaration(Java9Parser.ClassDeclarationContext ctx) {
        impl.append(ctx.getText()).append("\n");
    }

    @Override
    public void exitClassDeclaration(Java9Parser.ClassDeclarationContext ctx) {
    }

    @Override
    public void enterStatement(Java9Parser.StatementContext ctx) {
        impl.append(ctx.getText()).append("\n");
    }

    @Override
    public void exitStatement(Java9Parser.StatementContext ctx) {
    }

}
