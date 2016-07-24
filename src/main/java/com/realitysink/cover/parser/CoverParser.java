package com.realitysink.cover.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.dsl.NodeFactory;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.FrameSlot;
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
import com.realitysink.cover.nodes.expression.CoverFunctionLiteralNode;
import com.realitysink.cover.nodes.expression.SLFunctionLiteralNode;
import com.realitysink.cover.nodes.expression.SLLongLiteralNode;
import com.realitysink.cover.nodes.expression.SLStringLiteralNode;
import com.realitysink.cover.nodes.local.CoverWriteLocalVariableNodeNoEval;
import com.realitysink.cover.nodes.local.SLWriteLocalVariableNodeGen;
import com.realitysink.cover.runtime.SLFunction;

import org.eclipse.cdt.core.dom.ast.ExpansionOverlapsBoundaryException;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage;
import org.eclipse.cdt.core.parser.DefaultLogService;
import org.eclipse.cdt.core.parser.FileContent;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IncludeFileContentProvider;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTExpressionStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionCallExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionDefinition;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTLiteralExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTTranslationUnit;
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
            CPPASTFunctionDefinition functionDefintion = (CPPASTFunctionDefinition) node;
            FrameDescriptor newFrame = new FrameDescriptor();
            IASTStatement s = functionDefintion.getBody();
            IASTCompoundStatement compound = (IASTCompoundStatement) s;
            List<SLStatementNode> statements = new ArrayList<SLStatementNode>();
            for (IASTStatement statement : compound.getStatements()) {
                statements.add(processStatement(newFrame, statement));
            }
            SLBlockNode blockNode = new SLBlockNode(statements.toArray(new SLStatementNode[statements.size()]));
            final SLFunctionBodyNode functionBodyNode = new SLFunctionBodyNode(blockNode);
            String functionName = functionDefintion.getDeclarator().getName().toString();
            System.err.println("Function name: " + functionName);

            // for int main() {} we create a main = (int)(){} assignment
            FrameSlot frameSlot = frameDescriptor.findOrAddFrameSlot(functionName);
            SLFunction function = new SLFunction(functionName);
            SLRootNode rootNode = new SLRootNode(newFrame, functionBodyNode, null, functionName);
            RootCallTarget callTarget = Truffle.getRuntime().createCallTarget(rootNode);
            function.setCallTarget(callTarget);
            final SLExpressionNode result = new CoverWriteLocalVariableNodeNoEval(function, frameSlot);
            return result;
        } else if (node instanceof CPPASTExpressionStatement) {
            // only support function calls for now...
            return processStatement(frameDescriptor, node.getChildren()[0]);
        } else if (node instanceof CPPASTFunctionCallExpression) {
            CPPASTFunctionCallExpression functionCall = (CPPASTFunctionCallExpression) node;
            String name = functionCall.getFunctionNameExpression().getRawSignature();

            if ("puts".equals(name)) {
                NodeFactory<SLPrintlnBuiltin> printlnBuiltinFactory = SLPrintlnBuiltinFactory.getInstance();
                CPPASTLiteralExpression e = (CPPASTLiteralExpression) functionCall.getArguments()[0];
                String literal = e.getRawSignature();
                SLStringLiteralNode expression = new SLStringLiteralNode(literal.substring(1, literal.length() - 1));
                return printlnBuiltinFactory.createNode(new SLExpressionNode[] { expression }, CoverLanguage.INSTANCE.findContext());
            } else if ("printf".equals(name)) {
                List<SLExpressionNode> coverArguments = new ArrayList<>();
                for (IASTInitializerClause x : functionCall.getArguments()) {
                    if (x instanceof CPPASTLiteralExpression) {
                        CPPASTLiteralExpression y = (CPPASTLiteralExpression) x;
                        if (y.getKind() == IASTLiteralExpression.lk_string_literal) {
                            String v = new String(y.getValue());
                            String noQuotes = v.substring(1, v.length() - 1).replace("\\n", "\n");
                            coverArguments.add(new SLStringLiteralNode(noQuotes));
                        } else if (y.getKind() == IASTLiteralExpression.lk_integer_constant) {
                            coverArguments.add(new SLLongLiteralNode(Integer.parseInt(new String(y.getValue()))));
                        } else {
                            System.err.println("Unknown argument for function call!");
                            coverArguments.add(null);
                        }
                    }
                }
                return new CoverPrintfBuiltin(coverArguments.toArray(new SLExpressionNode[coverArguments.size()]));
            } else {
                // FIXME: compile time checks!
                System.err.println("Invoking known function " + name);
                return new SLInvokeNode(new CoverFunctionLiteralNode(name), new SLExpressionNode[0]);
                // System.err.println("Ignoring unknown function call " + name);
                // return new NopStatement();
            }
        } else {
            System.err.println("Unknown node type: " + node.getClass().getSimpleName());
            return new NopStatement();
        }
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
