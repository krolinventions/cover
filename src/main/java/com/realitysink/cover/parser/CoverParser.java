package com.realitysink.cover.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.dsl.NodeFactory;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.FrameSlotKind;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.source.Source;
import com.realitysink.cover.CoverLanguage;
import com.realitysink.cover.builtins.CoverPrintfBuiltin;
import com.realitysink.cover.builtins.SLPrintlnBuiltin;
import com.realitysink.cover.builtins.SLPrintlnBuiltinFactory;
import com.realitysink.cover.nodes.SLExpressionNode;
import com.realitysink.cover.nodes.SLRootNode;
import com.realitysink.cover.nodes.SLStatementNode;
import com.realitysink.cover.nodes.call.SLInvokeNode;
import com.realitysink.cover.nodes.controlflow.SLBlockNode;
import com.realitysink.cover.nodes.controlflow.SLFunctionBodyNode;
import com.realitysink.cover.nodes.controlflow.SLReturnNode;
import com.realitysink.cover.nodes.controlflow.SLWhileNode;
import com.realitysink.cover.nodes.expression.CoverFunctionLiteralNode;
import com.realitysink.cover.nodes.expression.SLAddNode;
import com.realitysink.cover.nodes.expression.SLAddNodeGen;
import com.realitysink.cover.nodes.expression.SLEqualNodeGen;
import com.realitysink.cover.nodes.expression.SLFunctionLiteralNode;
import com.realitysink.cover.nodes.expression.SLLessThanNodeGen;
import com.realitysink.cover.nodes.expression.SLLongLiteralNode;
import com.realitysink.cover.nodes.expression.SLStringLiteralNode;
import com.realitysink.cover.nodes.local.CoverWriteLocalVariableNodeNoEval;
import com.realitysink.cover.nodes.local.SLReadArgumentNode;
import com.realitysink.cover.nodes.local.SLReadLocalVariableNodeGen;
import com.realitysink.cover.nodes.local.SLWriteLocalVariableNodeGen;
import com.realitysink.cover.runtime.SLFunction;

import org.eclipse.cdt.core.dom.ast.ExpansionOverlapsBoundaryException;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage;
import org.eclipse.cdt.core.parser.DefaultLogService;
import org.eclipse.cdt.core.parser.FileContent;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IncludeFileContentProvider;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTBinaryExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTCompoundStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTDeclarationStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTEqualsInitializer;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTExpressionStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionCallExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionDefinition;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTIdExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTLiteralExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTParameterDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTReturnStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTSimpleDeclSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTSimpleDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTTranslationUnit;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTUnaryExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTWhileStatement;
import org.eclipse.core.runtime.CoreException;

public class CoverParser {
    private static final class NopStatement extends SLStatementNode {
        @Override
        public void executeVoid(VirtualFrame frame) {
            // ignore
        }
    }

    public static Map<String, SLRootNode> parseSource(Source source) throws CoreException {
        Map<String, SLRootNode> result = new HashMap<String, SLRootNode>();

        FileContent fileContent = FileContent.createForExternalFileLocation(source.getPath());

        Map<String, String> definedSymbols = new HashMap<String, String>();
        String[] includePaths = new String[0];
        IScannerInfo info = new ScannerInfo(definedSymbols, includePaths);
        IParserLogService log = new DefaultLogService();

        IncludeFileContentProvider emptyIncludes = IncludeFileContentProvider.getEmptyFilesProvider();

        int opts = 8;
        IASTTranslationUnit translationUnit = GPPLanguage.getDefault().getASTTranslationUnit(fileContent, info,
                emptyIncludes, null, opts, log);

        IASTPreprocessorIncludeStatement[] includes = translationUnit.getIncludeDirectives();
        for (IASTPreprocessorIncludeStatement include : includes) {
            System.out.println("include - " + include.getName());
        }

        printTree(translationUnit, 1);

        // RootNode
        final FrameDescriptor frameDescriptor = new FrameDescriptor();
        List<SLStatementNode> statements = new ArrayList<SLStatementNode>();
        for (IASTNode node : translationUnit.getChildren()) {
            statements.add(processStatement(frameDescriptor, node));
        }

        // hack in a call to main
        statements.add(new SLInvokeNode(new CoverFunctionLiteralNode("main"), new SLExpressionNode[0]));

        SLBlockNode blockNode = new SLBlockNode(statements.toArray(new SLStatementNode[statements.size()]));
        FrameSlot[] arguments = new FrameSlot[0];
        final SLFunctionBodyNode functionBodyNode = new SLFunctionBodyNode(blockNode);
        final SLRootNode rootNode = new SLRootNode(frameDescriptor, functionBodyNode, null, "_file");
        result.put("_file", rootNode);

        printTruffleNodes(rootNode, 0);

        return result;
    }

    private static void printTruffleNodes(Node n2, int level) {
        String spaces = "";
        for (int i = 0; i < level; i++)
            spaces += "  ";
        System.err.println(spaces + n2.getClass().getName());
        for (Node n : n2.getChildren()) {
            printTruffleNodes(n, level + 1);
        }
    }

    private static SLStatementNode processStatement(FrameDescriptor frameDescriptor, IASTNode node) {
        System.err.println("processStatement for " + node.getClass().getSimpleName());
        if (node instanceof CPPASTFunctionDefinition) {
            return processFunctionDefinition(frameDescriptor, (CPPASTFunctionDefinition) node);
        } else if (node instanceof CPPASTExpressionStatement) {
            CPPASTExpressionStatement x = (CPPASTExpressionStatement) node;
            return processExpression(frameDescriptor, x.getExpression());
        } else if (node instanceof CPPASTDeclarationStatement) {
            return processDeclaration(frameDescriptor, (CPPASTDeclarationStatement) node);
        } else if (node instanceof CPPASTWhileStatement) {
            return processWhile(frameDescriptor, (CPPASTWhileStatement) node);
        } else if (node instanceof CPPASTCompoundStatement) {
            return processCompoundStatement(frameDescriptor, (CPPASTCompoundStatement) node);
        } else if (node instanceof CPPASTReturnStatement) {
            return processReturn(frameDescriptor, (CPPASTReturnStatement) node);
        } else if (node instanceof CPPASTBinaryExpression) {
            return processBinaryExpression(frameDescriptor, (CPPASTBinaryExpression) node);
        } else {
            throw new CoverParseException(node, "unknown statement type: " + node.getClass().getSimpleName());
        }
    }

    private static SLStatementNode processReturn(FrameDescriptor frameDescriptor, CPPASTReturnStatement node) {
        IASTExpression returnValue = node.getReturnValue();
        final SLReturnNode returnNode = new SLReturnNode(processExpression(frameDescriptor, returnValue));
        return returnNode;
    }

    private static SLStatementNode processWhile(FrameDescriptor frameDescriptor, CPPASTWhileStatement node) {
        /*
       -CPPASTWhileStatement (offset: 27,47) -> while
         -CPPASTBinaryExpression (offset: 34,6) -> i < 10
           -CPPASTIdExpression (offset: 34,1) -> i
             -CPPASTName (offset: 34,1) -> i
           -CPPASTLiteralExpression (offset: 38,2) -> 10
         -CPPASTCompoundStatement (offset: 42,32) -> {
         */
        SLExpressionNode conditionNode = processExpression(frameDescriptor, node.getCondition());
        SLStatementNode bodyNode = processStatement(frameDescriptor, node.getBody());
        final SLWhileNode whileNode = new SLWhileNode(conditionNode, bodyNode);
        return whileNode;
    }

    private static SLExpressionNode processExpression(FrameDescriptor frameDescriptor, IASTExpression expression) {
        if (expression instanceof CPPASTBinaryExpression) {
            return processBinaryExpression(frameDescriptor, (CPPASTBinaryExpression) expression);
        } else if (expression instanceof CPPASTLiteralExpression) {
            return processLiteral(frameDescriptor, (CPPASTLiteralExpression) expression);
        } else if (expression instanceof CPPASTIdExpression) {
            return processId(frameDescriptor, (CPPASTIdExpression) expression);                    
        } else if (expression instanceof CPPASTFunctionCallExpression) {
            return processFunctionCall(frameDescriptor, expression);
        } else if (expression instanceof CPPASTUnaryExpression) {
            return (SLExpressionNode) processUnary(frameDescriptor, (CPPASTUnaryExpression) expression);
        }
        throw new CoverParseException(expression, "unknown expression type " + expression.getClass().getSimpleName());
    }

    private static SLExpressionNode processBinaryExpression(FrameDescriptor frameDescriptor, CPPASTBinaryExpression expression) {
        int operator = expression.getOperator();
        if (operator == CPPASTBinaryExpression.op_lessThan) {
            SLExpressionNode leftNode = processExpression(frameDescriptor, expression.getOperand1());
            SLExpressionNode rightNode = processExpression(frameDescriptor, expression.getOperand2());
            return SLLessThanNodeGen.create(leftNode, rightNode);
        } else if (operator == CPPASTBinaryExpression.op_equals) {
            SLExpressionNode leftNode = processExpression(frameDescriptor, expression.getOperand1());
            SLExpressionNode rightNode = processExpression(frameDescriptor, expression.getOperand2());
            return SLEqualNodeGen.create(leftNode, rightNode);
        } else if (operator == CPPASTBinaryExpression.op_greaterThan) {
            // FIXME: this messes with evaluation order!
            SLExpressionNode leftNode = processExpression(frameDescriptor, expression.getOperand1());
            SLExpressionNode rightNode = processExpression(frameDescriptor, expression.getOperand2());
            return SLEqualNodeGen.create(rightNode, leftNode);
        } else if (operator == CPPASTBinaryExpression.op_plusAssign) {
            SLExpressionNode change = processExpression(frameDescriptor, expression.getOperand2());
            CPPASTIdExpression x = (CPPASTIdExpression) expression.getOperand1();
            FrameSlot frameSlot = frameDescriptor.findOrAddFrameSlot(x.getRawSignature());
            return SLWriteLocalVariableNodeGen.create(SLAddNodeGen.create(SLReadLocalVariableNodeGen.create(frameSlot), change), frameSlot);
        } else if (operator == CPPASTBinaryExpression.op_assign) {
            CPPASTIdExpression x = (CPPASTIdExpression) expression.getOperand1();
            FrameSlot frameSlot = frameDescriptor.findOrAddFrameSlot(x.getRawSignature());
            SLExpressionNode rightNode = processExpression(frameDescriptor, expression.getOperand2());
            return SLWriteLocalVariableNodeGen.create(rightNode, frameSlot);
        } 
        throw new CoverParseException(expression, "unknown operator type " + operator);
    }

    private static SLStatementNode processUnary(FrameDescriptor frameDescriptor, CPPASTUnaryExpression node) {
        /*
             -CPPASTUnaryExpression (offset: 28,3) -> i++
               -CPPASTIdExpression (offset: 28,1) -> i
                 -CPPASTName (offset: 28,1) -> i
         */
        String name = node.getOperand().getRawSignature(); // FIXME, shortcut
        int operator = node.getOperator();
        final int change;
        if (operator == IASTUnaryExpression.op_postFixIncr || operator == IASTUnaryExpression.op_prefixIncr) {
            change = 1;
        } else if (operator == IASTUnaryExpression.op_postFixDecr || operator == IASTUnaryExpression.op_prefixDecr) {
            change = -1;
        } else {
            throw new CoverParseException(node, "Unsupported operator type " + operator);
        }
        
        // build assign(name, addnode(name, 1))
        SLAddNode addNode = SLAddNodeGen.create(processId(frameDescriptor, (CPPASTIdExpression) node.getOperand()), new SLLongLiteralNode(change));
        
        FrameSlot frameSlot = frameDescriptor.findOrAddFrameSlot(name);
        return SLWriteLocalVariableNodeGen.create(addNode, frameSlot);
    }

    private static SLStatementNode processDeclaration(FrameDescriptor frameDescriptor, CPPASTDeclarationStatement node) {
        /* -CPPASTDeclarationStatement (offset: 14,10) -> int i = 0;
             -CPPASTSimpleDeclaration (offset: 14,10) -> int i = 0;
               -CPPASTSimpleDeclSpecifier (offset: 14,3) -> int
               -CPPASTDeclarator (offset: 18,5) -> i = 0
                 -CPPASTName (offset: 18,1) -> i
                 -CPPASTEqualsInitializer (offset: 20,3) -> = 0
                   -CPPASTLiteralExpression (offset: 22,1) -> 0
        */
        CPPASTSimpleDeclaration s = (CPPASTSimpleDeclaration) node.getDeclaration();
        // assume all variables are ints for now, single variable
        CPPASTDeclarator d = (CPPASTDeclarator) s.getDeclarators()[0];
        String name = d.getName().getRawSignature();
        CPPASTEqualsInitializer i = (CPPASTEqualsInitializer) d.getInitializer();
        CPPASTLiteralExpression literal = (CPPASTLiteralExpression) i.getInitializerClause();
        SLExpressionNode expression = processLiteral(frameDescriptor, literal);
        FrameSlot frameSlot = frameDescriptor.findOrAddFrameSlot(name);

        return SLWriteLocalVariableNodeGen.create(expression, frameSlot);
    }

    private static SLExpressionNode processLiteral(FrameDescriptor frameDescriptor, CPPASTLiteralExpression y) {
        if (y.getKind() == IASTLiteralExpression.lk_string_literal) {
            String v = new String(y.getValue());
            String noQuotes = v.substring(1, v.length() - 1).replace("\\n", "\n");
            return new SLStringLiteralNode(noQuotes);
        } else if (y.getKind() == IASTLiteralExpression.lk_integer_constant) {
            return new SLLongLiteralNode(Integer.parseInt(new String(y.getValue())));
        } else {
            throw new RuntimeException("Unknown argument for function call!");
        }
    }

    private static SLExpressionNode processFunctionCall(FrameDescriptor frameDescriptor, IASTNode node) {
        CPPASTFunctionCallExpression functionCall = (CPPASTFunctionCallExpression) node;
        String name = functionCall.getFunctionNameExpression().getRawSignature();

        List<SLExpressionNode> coverArguments = new ArrayList<>();
        for (IASTInitializerClause x : functionCall.getArguments()) {
            if (x instanceof IASTExpression) {
                coverArguments.add(processExpression(frameDescriptor, (IASTExpression) x));
            } else {
                throw new CoverParseException(node, "Unknown function argument type: " + x.getClass());
            }
        }
        SLExpressionNode[] argumentArray = coverArguments.toArray(new SLExpressionNode[coverArguments.size()]);
        
        if ("puts".equals(name)) {
            NodeFactory<SLPrintlnBuiltin> printlnBuiltinFactory = SLPrintlnBuiltinFactory.getInstance();
            return printlnBuiltinFactory.createNode(argumentArray, CoverLanguage.INSTANCE.findContext());
        } else if ("printf".equals(name)) {
            return new CoverPrintfBuiltin(argumentArray);
        } else {
            return new SLInvokeNode(new CoverFunctionLiteralNode(name), argumentArray);
        }
    }

    private static SLExpressionNode processId(FrameDescriptor frameDescriptor, CPPASTIdExpression id) {
        String name = id.getName().getRawSignature();
        final SLExpressionNode result;
        final FrameSlot frameSlot = frameDescriptor.findFrameSlot(name);
        if (frameSlot != null) {
            /* Read of a local variable. */
            result = SLReadLocalVariableNodeGen.create(frameSlot);
        } else {
            throw new CoverParseException(id, "ID not found in local scope");
        }
        return result; 
    }

    private static SLExpressionNode processFunctionDefinition(FrameDescriptor frameDescriptor, CPPASTFunctionDefinition functionDefintion) {
        /*
           -CPPASTFunctionDefinition (offset: 1,81) -> void 
             -CPPASTSimpleDeclSpecifier (offset: 1,4) -> void
             -CPPASTFunctionDeclarator (offset: 6,18) -> doStuff(int count)
               -CPPASTName (offset: 6,7) -> doStuff
               -CPPASTParameterDeclaration (offset: 14,9) -> int count
                 -CPPASTSimpleDeclSpecifier (offset: 14,3) -> int
                 -CPPASTDeclarator (offset: 18,5) -> count
                   -CPPASTName (offset: 18,5) -> count
             -CPPASTCompoundStatement (offset: 25,57) -> {
         */
        FrameDescriptor newFrame = new FrameDescriptor();
        CPPASTFunctionDeclarator declarator = (CPPASTFunctionDeclarator) functionDefintion.getDeclarator();
        ICPPASTParameterDeclaration[] parameters = declarator.getParameters();
        FrameSlot[] argumentArray = new FrameSlot[parameters.length];
        SLStatementNode[] readArgumentsStatements = new SLStatementNode[parameters.length];
        for (int i = 0;i<parameters.length;i++) {
            ICPPASTParameterDeclaration parameter = parameters[i];
            String name = parameter.getDeclarator().getName().getRawSignature();
            String rawSignature = parameter.getDeclSpecifier().getRawSignature();
            FrameSlotKind kind = FrameSlotKind.Object;
            if ("int".equals(rawSignature)) {
                kind = FrameSlotKind.Int;
            }
            FrameSlot frameSlot = newFrame.addFrameSlot(name, kind);
            argumentArray[i] = frameSlot;
            
            // copy to local var in the prologue
            final SLReadArgumentNode readArg = new SLReadArgumentNode(i+1);
            SLExpressionNode assignment = SLWriteLocalVariableNodeGen.create(readArg, frameSlot);
            readArgumentsStatements[i] = assignment;
        }
        
        SLBlockNode readArgumentsNode = new SLBlockNode(readArgumentsStatements);
        
        IASTStatement s = functionDefintion.getBody();
        SLStatementNode blockNode = processStatement(newFrame, s);
        SLBlockNode wrappedBodyNode = new SLBlockNode(new SLStatementNode[] {readArgumentsNode, blockNode});
        final SLFunctionBodyNode functionBodyNode = new SLFunctionBodyNode(wrappedBodyNode);
        
        // we will now add code to read the arguments into the frame
        // load local variables from arguments
        
        String functionName = declarator.getName().toString();
        // for int main() {} we create a main = (int)(){} assignment
        FrameSlot frameSlot = frameDescriptor.findOrAddFrameSlot(functionName);
        SLFunction function = new SLFunction(functionName);
        SLRootNode rootNode = new SLRootNode(newFrame, functionBodyNode, null, functionName);
        RootCallTarget callTarget = Truffle.getRuntime().createCallTarget(rootNode);
        function.setCallTarget(callTarget);
        final SLExpressionNode result = new CoverWriteLocalVariableNodeNoEval(function, frameSlot);
        return result;
    }

    private static SLBlockNode processCompoundStatement(FrameDescriptor newFrame, IASTStatement s) {
        IASTCompoundStatement compound = (IASTCompoundStatement) s;
        List<SLStatementNode> statements = new ArrayList<SLStatementNode>();
        for (IASTStatement statement : compound.getStatements()) {
            statements.add(processStatement(newFrame, statement));
        }
        SLBlockNode blockNode = new SLBlockNode(statements.toArray(new SLStatementNode[statements.size()]));
        return blockNode;
    }

    private static void printTree(IASTNode node, int index) {
        IASTNode[] children = node.getChildren();

        boolean printContents = true;

        if ((node instanceof CPPASTTranslationUnit)) {
            printContents = false;
        }

        String offset = "";
        try {
            offset = node.getSyntax() != null ? " (offset: " + node.getFileLocation().getNodeOffset() + ","
                    + node.getFileLocation().getNodeLength() + ")" : "";
            printContents = node.getFileLocation().getNodeLength() < 30;
        } catch (ExpansionOverlapsBoundaryException e) {
            e.printStackTrace();
        } catch (UnsupportedOperationException e) {
            offset = "UnsupportedOperationException";
        }

        System.out.println(String.format(new StringBuilder("%1$").append(index * 2).append("s").toString(),
                new Object[] { "-" })
                + node.getClass().getSimpleName()
                + offset
                + " -> "
                + (printContents ? node.getRawSignature().replaceAll("\n", " \\ ") : node.getRawSignature()
                        .subSequence(0, 5)));

        for (IASTNode iastNode : children)
            printTree(iastNode, index + 1);
    }
}
