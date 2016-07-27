package com.realitysink.cover.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.dsl.NodeFactory;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.FrameSlotKind;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.source.SourceSection;
import com.realitysink.cover.CoverLanguage;
import com.realitysink.cover.builtins.CoverFWriteBuiltinNodeGen;
import com.realitysink.cover.builtins.CoverPrintfBuiltin;
import com.realitysink.cover.builtins.CoverPutcBuiltinNodeGen;
import com.realitysink.cover.builtins.SLPrintlnBuiltin;
import com.realitysink.cover.builtins.SLPrintlnBuiltinFactory;
import com.realitysink.cover.nodes.CoverNopExpression;
import com.realitysink.cover.nodes.CoverScope;
import com.realitysink.cover.nodes.SLExpressionNode;
import com.realitysink.cover.nodes.SLRootNode;
import com.realitysink.cover.nodes.SLStatementNode;
import com.realitysink.cover.nodes.call.SLInvokeNode;
import com.realitysink.cover.nodes.controlflow.SLBlockNode;
import com.realitysink.cover.nodes.controlflow.SLFunctionBodyNode;
import com.realitysink.cover.nodes.controlflow.SLIfNode;
import com.realitysink.cover.nodes.controlflow.SLReturnNode;
import com.realitysink.cover.nodes.controlflow.SLWhileNode;
import com.realitysink.cover.nodes.expression.CoverDoubleLiteralNode;
import com.realitysink.cover.nodes.expression.CoverFunctionLiteralNode;
import com.realitysink.cover.nodes.expression.SLAddNode;
import com.realitysink.cover.nodes.expression.SLAddNodeGen;
import com.realitysink.cover.nodes.expression.SLBinaryAndNodeGen;
import com.realitysink.cover.nodes.expression.SLBinaryNotNodeGen;
import com.realitysink.cover.nodes.expression.SLBinaryOrNodeGen;
import com.realitysink.cover.nodes.expression.SLBinaryShiftLeftNodeGen;
import com.realitysink.cover.nodes.expression.SLBinaryShiftRightNodeGen;
import com.realitysink.cover.nodes.expression.SLDivNodeGen;
import com.realitysink.cover.nodes.expression.SLEqualNodeGen;
import com.realitysink.cover.nodes.expression.SLForceBooleanNodeGen;
import com.realitysink.cover.nodes.expression.SLLessOrEqualNodeGen;
import com.realitysink.cover.nodes.expression.SLLessThanNodeGen;
import com.realitysink.cover.nodes.expression.SLLogicalAndNodeGen;
import com.realitysink.cover.nodes.expression.SLLongLiteralNode;
import com.realitysink.cover.nodes.expression.SLModNodeGen;
import com.realitysink.cover.nodes.expression.SLMulNodeGen;
import com.realitysink.cover.nodes.expression.SLStringLiteralNode;
import com.realitysink.cover.nodes.expression.SLSubNodeGen;
import com.realitysink.cover.nodes.local.CoverNewArrayNode;
import com.realitysink.cover.nodes.local.ArrayReferenceLiteralNode;
import com.realitysink.cover.nodes.local.CoverReadArrayValueNodeGen;
import com.realitysink.cover.nodes.local.CoverWriteVariableNodeGen;
import com.realitysink.cover.nodes.local.CreateLocalDoubleArrayNode;
import com.realitysink.cover.nodes.local.CreateLocalLongArrayNode;
import com.realitysink.cover.nodes.local.CoverFrameSlotLiteral;
import com.realitysink.cover.nodes.local.SLReadArgumentNode;
import com.realitysink.cover.nodes.local.SLReadLocalVariableNodeGen;
import com.realitysink.cover.nodes.local.SLWriteLocalVariableNodeGen;
import com.realitysink.cover.runtime.SLFunction;

import org.eclipse.cdt.core.dom.ast.ExpansionOverlapsBoundaryException;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage;
import org.eclipse.cdt.core.parser.DefaultLogService;
import org.eclipse.cdt.core.parser.FileContent;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IncludeFileContentProvider;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTArrayDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTArraySubscriptExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTBinaryExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTCastExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTCompoundStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTDeclarationStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTDoStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTEqualsInitializer;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTExpressionStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTForStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionCallExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionDefinition;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTIdExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTIfStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTLiteralExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTNamedTypeSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTReturnStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTSimpleDeclSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTSimpleDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTTranslationUnit;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTUnaryExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTWhileStatement;
import org.eclipse.cdt.internal.core.parser.IMacroDictionary;
import org.eclipse.cdt.internal.core.parser.SavedFilesProvider;
import org.eclipse.cdt.internal.core.parser.scanner.CharArray;
import org.eclipse.cdt.internal.core.parser.scanner.InternalFileContent;
import org.eclipse.core.runtime.CoreException;

public class CoverParser {
    private Source source;
    
    public CoverParser(Source source) {
        this.source = source;
    }

    public Map<String, SLRootNode> parse() throws CoreException {
        final CoverScope scope = new CoverScope(null);
        
        Map<String, SLRootNode> result = new HashMap<String, SLRootNode>();

        FileContent fileContent = FileContent.createForExternalFileLocation(source.getPath());

        Map<String, String> definedSymbols = new HashMap<String, String>();
        String[] includePaths = new String[] {"/<builtin>"};
        IScannerInfo info = new ScannerInfo(definedSymbols, includePaths);
        IParserLogService log = new DefaultLogService();

        // FIXME: this doesn't work yet!
        IncludeFileContentProvider fileContentProvider =  new SavedFilesProvider() {
            @Override
            public InternalFileContent getContentForInclusion(String path, IMacroDictionary macroDictionary) {
                if (path.equals("/<builtin>/stdio.h")) {
                    System.err.println("including " + path);
                    return new InternalFileContent(path, new CharArray("typedef long intptr_t; typedef long uint8_t; int stdout=0;"));
                } else {
                    return new InternalFileContent(path, new CharArray(""));
                }
            }
        };

        int opts = 8;
        IASTTranslationUnit translationUnit = GPPLanguage.getDefault().getASTTranslationUnit(fileContent, info,
                fileContentProvider, null, opts, log);

        IASTPreprocessorIncludeStatement[] includes = translationUnit.getIncludeDirectives();
        for (IASTPreprocessorIncludeStatement include : includes) {
            System.err.println("include - " + include.getName());
        }
        
        scope.addTypeDef("long", "intptr_t");
        scope.addTypeDef("long", "uint8_t"); // FIXME

        // RootNode
        List<SLStatementNode> statements = new ArrayList<SLStatementNode>();
        for (IASTNode node : translationUnit.getChildren()) {
            statements.add(processStatement(scope, node));
        }

        // hack in a call to main
        statements.add(new SLInvokeNode(new CoverFunctionLiteralNode(scope.findFunction(null, "main")), new SLExpressionNode[0]));

        SLBlockNode blockNode = new SLBlockNode(statements.toArray(new SLStatementNode[statements.size()]));
        blockNode.setSourceSection(source.createSection("_file", 1));
        final SLFunctionBodyNode functionBodyNode = new SLFunctionBodyNode(blockNode);
        functionBodyNode.setSourceSection(source.createSection("_file", 1));
        final SLRootNode rootNode = new SLRootNode(scope.getFrameDescriptor(), functionBodyNode, source.createSection("_file", 1), "_file");
        result.put("_file", rootNode);
        printTruffleNodes(rootNode, 0);

        return result;
    }

    private void printTruffleNodes(Node n2, int level) {
        String spaces = "";
        for (int i = 0; i < level; i++)
            spaces += "  ";
        System.err.println(spaces + n2.getClass().getName());
        for (Node n : n2.getChildren()) {
            printTruffleNodes(n, level + 1);
        }
    }

    private SLStatementNode processStatement(CoverScope scope, IASTNode node) {
        System.err.println("processStatement for " + node.getClass().getSimpleName());
        SLStatementNode result;
        if (node instanceof CPPASTFunctionDefinition) {
            result = processFunctionDefinition(scope, (CPPASTFunctionDefinition) node);
        } else if (node instanceof CPPASTExpressionStatement) {
            CPPASTExpressionStatement x = (CPPASTExpressionStatement) node;
            result =  processExpression(scope, x.getExpression(), null);
        } else if (node instanceof CPPASTDeclarationStatement) {
            result =  processVariableDeclaration(scope, (CPPASTDeclarationStatement) node);
        } else if (node instanceof CPPASTWhileStatement) {
            result =  processWhile(scope, (CPPASTWhileStatement) node);
        } else if (node instanceof CPPASTDoStatement) {
            result =  processDo(scope, (CPPASTDoStatement) node);
        } else if (node instanceof CPPASTCompoundStatement) {
            result =  processCompoundStatement(scope, (CPPASTCompoundStatement) node);
        } else if (node instanceof CPPASTReturnStatement) {
            result =  processReturn(scope, (CPPASTReturnStatement) node);
        } else if (node instanceof CPPASTBinaryExpression) {
            result =  processBinaryExpression(scope, (CPPASTBinaryExpression) node);
        } else if (node instanceof CPPASTForStatement) {
            result =  processForStatement(scope, (CPPASTForStatement) node);
        } else if (node instanceof CPPASTIfStatement) {
            result =  processIfStatement(scope, (CPPASTIfStatement) node);
        } else if (node instanceof CPPASTSimpleDeclaration) {
            result =  processTypedef(scope, (CPPASTSimpleDeclaration) node);
        } else {
            printTree(node, 1);
            throw new CoverParseException(node, "unknown statement type: " + node.getClass().getSimpleName());
        }
        if (result.getSourceSection() == null) {
            result.setSourceSection(createSourceSectionForNode("statement", node));
        }
        return result;
    }

    private SLStatementNode processTypedef(CoverScope scope, CPPASTSimpleDeclaration node) {
        IASTDeclSpecifier declSpecifier = node.getDeclSpecifier();
        if (declSpecifier instanceof CPPASTNamedTypeSpecifier) {
            CPPASTNamedTypeSpecifier d = (CPPASTNamedTypeSpecifier) declSpecifier;
            String oldType = d.getName().toString();
            
            IASTDeclarator[] declarators = node.getDeclarators();
            for (IASTDeclarator declarator : declarators) {
                String newType = declarator.getName().toString();
                scope.addTypeDef(oldType, newType);
            }
        }
        return new CoverNopExpression();
    }

    private SLStatementNode processIfStatement(CoverScope scope, CPPASTIfStatement node) {
        CoverScope ifScope = new CoverScope(scope);
        CoverScope thenScope = new CoverScope(ifScope);
        CoverScope elseScope = new CoverScope(ifScope);
        SLExpressionNode conditionNode = SLForceBooleanNodeGen.create(SLForceBooleanNodeGen.create(processExpression(ifScope, node.getConditionExpression(), null)));
        SLStatementNode thenPartNode = processStatement(thenScope, node.getThenClause());
        SLStatementNode elsePartNode = null;
        if (node.getElseClause() != null) {
            elsePartNode = processStatement(elseScope, node.getElseClause());
        }
        return new SLIfNode(conditionNode, thenPartNode, elsePartNode);
    }

    private SLStatementNode processDo(CoverScope scope, CPPASTDoStatement node) {
        CoverScope scope1 = new CoverScope(scope);
        CoverScope scope2 = new CoverScope(scope);
        CoverScope conditionScope = new CoverScope(scope);
        // a do {} while() loop is just a while loop with the body prepended
        SLExpressionNode conditionNode = SLForceBooleanNodeGen.create(processExpression(conditionScope, node.getCondition(), null));
        SLStatementNode bodyNode1 = processStatement(scope1, node.getBody());
        SLStatementNode bodyNode2 = processStatement(scope2, node.getBody());
        final SLWhileNode whileNode = new SLWhileNode(conditionNode, bodyNode2);
        SLBlockNode blockNode = new SLBlockNode(new SLStatementNode[]{bodyNode1, whileNode});
        return blockNode;
    }

    private SLStatementNode processForStatement(CoverScope scope, CPPASTForStatement node) {
        /*
           -CPPASTForStatement (offset: 15,50) -> for (
             -CPPASTDeclarationStatement (offset: 20,8) -> int i=0;
               -CPPASTSimpleDeclaration (offset: 20,8) -> int i=0;
                 -CPPASTSimpleDeclSpecifier (offset: 20,3) -> int
                 -CPPASTDeclarator (offset: 24,3) -> i=0
                   -CPPASTName (offset: 24,1) -> i
                   -CPPASTEqualsInitializer (offset: 25,2) -> =0
                     -CPPASTLiteralExpression (offset: 26,1) -> 0
             -CPPASTBinaryExpression (offset: 28,4) -> i<12
               -CPPASTIdExpression (offset: 28,1) -> i
                 -CPPASTName (offset: 28,1) -> i
               -CPPASTLiteralExpression (offset: 30,2) -> 12
             -CPPASTUnaryExpression (offset: 33,3) -> i++
               -CPPASTIdExpression (offset: 33,1) -> i
                 -CPPASTName (offset: 33,1) -> i
             -CPPASTCompoundStatement (offset: 38,27) -> {          printf("i=%d\n", i);    }
         */
        CoverScope initializerScope = new CoverScope(scope);
        CoverScope conditionScope = new CoverScope(initializerScope);
        CoverScope iterationScope = new CoverScope(conditionScope);
        CoverScope bodyScope = new CoverScope(iterationScope);        
        IASTStatement initializer = node.getInitializerStatement();
        IASTExpression condition = node.getConditionExpression();
        IASTExpression iteration = node.getIterationExpression();
        SLStatementNode initializerNode = processStatement(initializerScope, initializer);
        SLExpressionNode conditionNode = processExpression(conditionScope, condition, null);
        SLExpressionNode iterationNode = processExpression(iterationScope, iteration, null);
        SLStatementNode bodyNode = processStatement(bodyScope, node.getBody());
        /*
         * We turn this:
         *   for (int i=0;i<x;i++) {}
         * into:
         *   {
         *     int i = 0;
         *     while (i < x) {
         *       {...}
         *       i++;
         *     }
         *   }
         */
        SLStatementNode[] loopNodes = new SLStatementNode[] {bodyNode, iterationNode};
        SLBlockNode loopBlock = new SLBlockNode(loopNodes);
        
        // TODO: start a new scope, as currently the loop variable will escape!
        
        final SLWhileNode whileNode = new SLWhileNode(conditionNode, loopBlock);

        SLStatementNode[] setupNodes = new SLStatementNode[] {initializerNode, whileNode}; 
        SLBlockNode setupBlock = new SLBlockNode(setupNodes);
        
        return setupBlock;        
    }

    private SLStatementNode processReturn(CoverScope scope, CPPASTReturnStatement node) {
        IASTExpression returnValue = node.getReturnValue();
        final SLReturnNode returnNode = new SLReturnNode(processExpression(scope, returnValue, null));
        return returnNode;
    }

    private SLStatementNode processWhile(CoverScope scope, CPPASTWhileStatement node) {
        /*
       -CPPASTWhileStatement (offset: 27,47) -> while
         -CPPASTBinaryExpression (offset: 34,6) -> i < 10
           -CPPASTIdExpression (offset: 34,1) -> i
             -CPPASTName (offset: 34,1) -> i
           -CPPASTLiteralExpression (offset: 38,2) -> 10
         -CPPASTCompoundStatement (offset: 42,32) -> {
         */
        SLExpressionNode conditionNode = SLForceBooleanNodeGen.create(processExpression(scope, node.getCondition(), null));
        SLStatementNode bodyNode = processStatement(scope, node.getBody());
        final SLWhileNode whileNode = new SLWhileNode(conditionNode, bodyNode);
        return whileNode;
    }

    private SLExpressionNode processExpression(CoverScope scope, IASTExpression expression, String requestedType) {
        SLExpressionNode result;
        if (expression instanceof CPPASTBinaryExpression) {
            result = processBinaryExpression(scope, (CPPASTBinaryExpression) expression);
        } else if (expression instanceof CPPASTLiteralExpression) {
            result = processLiteral(scope, (CPPASTLiteralExpression) expression);
        } else if (expression instanceof CPPASTArraySubscriptExpression) {
            result = processArraySubscriptExpression(scope, (CPPASTArraySubscriptExpression) expression);
        } else if (expression instanceof CPPASTIdExpression) {
            result = processId(scope, (CPPASTIdExpression) expression);                    
        } else if (expression instanceof CPPASTFunctionCallExpression) {
            result = processFunctionCall(scope, expression, requestedType);
        } else if (expression instanceof CPPASTUnaryExpression) {
            result = (SLExpressionNode) processUnary(scope, (CPPASTUnaryExpression) expression);
        } else if (expression instanceof CPPASTCastExpression) {
            warn(expression, "ignoring cast");
            result = processExpression(scope, ((CPPASTCastExpression)expression).getOperand(), null);
        } else {
            throw new CoverParseException(expression, "unknown expression type " + expression.getClass().getSimpleName());
        }
        result.setSourceSection(createSourceSectionForNode("expression", expression));
        return result;
    }

    private SLExpressionNode processArraySubscriptExpression(CoverScope scope,
            CPPASTArraySubscriptExpression expression) {
        ICPPASTExpression array = expression.getArrayExpression();
        IASTExpression subscript = expression.getSubscriptExpression();
        FrameSlot frameSlot = scope.findFrameSlot(expression, array.getRawSignature());
        return CoverReadArrayValueNodeGen.create(new CoverFrameSlotLiteral(frameSlot), processExpression(scope, subscript, null));
    }

    private SLExpressionNode processBinaryExpression(CoverScope scope, CPPASTBinaryExpression expression) {
        int operator = expression.getOperator();
        SLExpressionNode result;
        if (operator == CPPASTBinaryExpression.op_lessThan) {
            SLExpressionNode leftNode = processExpression(scope, expression.getOperand1(), null);
            SLExpressionNode rightNode = processExpression(scope, expression.getOperand2(), null);
            result = SLLessThanNodeGen.create(leftNode, rightNode);
        } else if (operator == CPPASTBinaryExpression.op_lessEqual) {
            SLExpressionNode leftNode = processExpression(scope, expression.getOperand1(), null);
            SLExpressionNode rightNode = processExpression(scope, expression.getOperand2(), null);
            result = SLLessOrEqualNodeGen.create(leftNode, rightNode);
        } else if (operator == CPPASTBinaryExpression.op_equals) {
            SLExpressionNode leftNode = processExpression(scope, expression.getOperand1(), null);
            SLExpressionNode rightNode = processExpression(scope, expression.getOperand2(), null);
            result = SLEqualNodeGen.create(leftNode, rightNode);
        } else if (operator == CPPASTBinaryExpression.op_greaterThan) {
            // FIXME: this messes with evaluation order!
            SLExpressionNode leftNode = processExpression(scope, expression.getOperand1(), null);
            SLExpressionNode rightNode = processExpression(scope, expression.getOperand2(), null);
            result = SLLessThanNodeGen.create(rightNode, leftNode);
        } else if (operator == CPPASTBinaryExpression.op_plusAssign) {
            SLExpressionNode destination = processExpressionAsDestination(scope, expression.getOperand1());
            SLExpressionNode change = processExpression(scope, expression.getOperand2(), null);
            SLExpressionNode source = processExpression(scope, expression.getOperand1(), null);
            result = CoverWriteVariableNodeGen.create(destination, SLAddNodeGen.create(source, change));
        } else if (operator == CPPASTBinaryExpression.op_assign) {
            SLExpressionNode destination = processExpressionAsDestination(scope, expression.getOperand1());
            SLExpressionNode value = processExpression(scope, expression.getOperand2(), null);
            result = CoverWriteVariableNodeGen.create(destination, value);
        } else if (operator == CPPASTBinaryExpression.op_multiply) {
            SLExpressionNode leftNode = processExpression(scope, expression.getOperand1(), null);
            SLExpressionNode rightNode = processExpression(scope, expression.getOperand2(), null);
            result = SLMulNodeGen.create(leftNode, rightNode);
        } else if (operator == CPPASTBinaryExpression.op_divide) {
            SLExpressionNode leftNode = processExpression(scope, expression.getOperand1(), null);
            SLExpressionNode rightNode = processExpression(scope, expression.getOperand2(), null);
            result = SLDivNodeGen.create(leftNode, rightNode);
        } else if (operator == CPPASTBinaryExpression.op_plus) {
            SLExpressionNode leftNode = processExpression(scope, expression.getOperand1(), null);
            SLExpressionNode rightNode = processExpression(scope, expression.getOperand2(), null);
            result = SLAddNodeGen.create(leftNode, rightNode);
        } else if (operator == CPPASTBinaryExpression.op_minus) {
            SLExpressionNode leftNode = processExpression(scope, expression.getOperand1(), null);
            SLExpressionNode rightNode = processExpression(scope, expression.getOperand2(), null);
            result = SLSubNodeGen.create(leftNode, rightNode);
        } else if (operator == CPPASTBinaryExpression.op_logicalAnd) {
            SLExpressionNode leftNode = processExpression(scope, expression.getOperand1(), null);
            SLExpressionNode rightNode = processExpression(scope, expression.getOperand2(), null);
            result = SLLogicalAndNodeGen.create(leftNode, rightNode);
        } else if (operator == CPPASTBinaryExpression.op_binaryAndAssign) {
            SLExpressionNode destination = processExpressionAsDestination(scope, expression.getOperand1());
            SLExpressionNode change = processExpression(scope, expression.getOperand2(), null);
            SLExpressionNode source = processExpression(scope, expression.getOperand1(), null);
            result = CoverWriteVariableNodeGen.create(destination, SLBinaryAndNodeGen.create(source, change));
        } else if (operator == CPPASTBinaryExpression.op_shiftRightAssign) {
            SLExpressionNode destination = processExpressionAsDestination(scope, expression.getOperand1());
            SLExpressionNode change = processExpression(scope, expression.getOperand2(), null);
            SLExpressionNode source = processExpression(scope, expression.getOperand1(), null);
            result = CoverWriteVariableNodeGen.create(destination, SLBinaryShiftRightNodeGen.create(source, change));
        } else if (operator == CPPASTBinaryExpression.op_shiftLeftAssign) {
            SLExpressionNode destination = processExpressionAsDestination(scope, expression.getOperand1());
            SLExpressionNode change = processExpression(scope, expression.getOperand2(), null);
            SLExpressionNode source = processExpression(scope, expression.getOperand1(), null);
            result = CoverWriteVariableNodeGen.create(destination, SLBinaryShiftLeftNodeGen.create(source, change));
        } else if (operator == CPPASTBinaryExpression.op_binaryOrAssign) {
            SLExpressionNode destination = processExpressionAsDestination(scope, expression.getOperand1());
            SLExpressionNode change = processExpression(scope, expression.getOperand2(), null);
            SLExpressionNode source = processExpression(scope, expression.getOperand1(), null);
            result = CoverWriteVariableNodeGen.create(destination, SLBinaryOrNodeGen.create(source, change));
        } else if (operator == CPPASTBinaryExpression.op_modulo) {
            SLExpressionNode leftNode = processExpression(scope, expression.getOperand1(), null);
            SLExpressionNode rightNode = processExpression(scope, expression.getOperand2(), null);
            result = SLModNodeGen.create(leftNode, rightNode);
        } else {
            throw new CoverParseException(expression, "unknown operator type " + operator);
        }
        result.setSourceSection(createSourceSectionForNode("binary", expression));
        return result;
    }

    private SLExpressionNode processExpressionAsDestination(CoverScope scope,
            IASTExpression node) {
        if (node instanceof CPPASTIdExpression) {
            CPPASTIdExpression x = (CPPASTIdExpression) node;
            FrameSlot frameSlot = scope.findFrameSlot(node, x.getRawSignature());
            return new CoverFrameSlotLiteral(frameSlot);
        } else if (node instanceof CPPASTArraySubscriptExpression) {
            CPPASTArraySubscriptExpression x = (CPPASTArraySubscriptExpression) node;
            ICPPASTExpression array = x.getArrayExpression();
            IASTExpression argument = (IASTExpression) x.getArgument();
            FrameSlot frameSlot = scope.findFrameSlot(node, array.getRawSignature());
            if (frameSlot == null) {
                throw new CoverParseException(node, "could not find local array " + array.getRawSignature());
            }
            return new ArrayReferenceLiteralNode(frameSlot, processExpression(scope, argument, null)); 
        }
        throw new CoverParseException(node, "unknown destination type: " + node.getClass().getSimpleName());
    }

    private SLStatementNode processUnary(CoverScope scope, CPPASTUnaryExpression node) {
        int operator = node.getOperator();
        final int change;
        if (operator == IASTUnaryExpression.op_postFixIncr || operator == IASTUnaryExpression.op_prefixIncr) {
            change = 1;
        } else if (operator == IASTUnaryExpression.op_postFixDecr || operator == IASTUnaryExpression.op_prefixDecr) {
            change = -1;
        } else if (operator == IASTUnaryExpression.op_bracketedPrimary) {
            return processExpression(scope, node.getOperand(), null);
        } else if (operator == IASTUnaryExpression.op_tilde) {
            return SLBinaryNotNodeGen.create(processExpression(scope, node.getOperand(), null));
        } else {
            throw new CoverParseException(node, "Unsupported operator type " + operator);
        }
        
        // build assign(name, addnode(name, 1))
        SLAddNode addNode = SLAddNodeGen.create(processExpression(scope, node.getOperand(), null), new SLLongLiteralNode(change));
        return CoverWriteVariableNodeGen.create(processExpressionAsDestination(scope, node.getOperand()), addNode);
    }

    private SLStatementNode processVariableDeclaration(CoverScope scope, CPPASTDeclarationStatement node) {
        /* -CPPASTDeclarationStatement (offset: 14,10) -> int i = 0;
             -CPPASTSimpleDeclaration (offset: 14,10) -> int i = 0;
               -CPPASTSimpleDeclSpecifier (offset: 14,3) -> int
               -CPPASTDeclarator (offset: 18,5) -> i = 0
                 -CPPASTName (offset: 18,1) -> i
                 -CPPASTEqualsInitializer (offset: 20,3) -> = 0
                   -CPPASTLiteralExpression (offset: 22,1) -> 0
            for arrays:
               -CPPASTDeclarationStatement (offset: 36,12) -> int a[size];
                 -CPPASTSimpleDeclaration (offset: 36,12) -> int a[size];
                   -CPPASTSimpleDeclSpecifier (offset: 36,3) -> int
                   -CPPASTArrayDeclarator (offset: 40,7) -> a[size]
                     -CPPASTName (offset: 40,1) -> a
                     -CPPASTArrayModifier (offset: 41,6) -> [size]
                       -CPPASTIdExpression (offset: 42,4) -> size
                         -CPPASTName (offset: 42,4) -> size
        */
        CPPASTSimpleDeclaration s = (CPPASTSimpleDeclaration) node.getDeclaration();
        IASTDeclSpecifier declSpecifier = s.getDeclSpecifier();
        IASTDeclarator[] declarators = s.getDeclarators();
        List<SLStatementNode> nodes = new ArrayList<SLStatementNode>();
        for (int i=0;i<declarators.length;i++) {
            IASTDeclarator declarator = declarators[i];
            String name = declarator.getName().toString();
            FrameSlot frameSlot = scope.addFrameSlot(node, name);
            
            // throw away "const"
            String rawTypeName = declSpecifier.getRawSignature();
            String parts[] = rawTypeName.split(" ");
            if (parts.length > 1) {
                if (!parts[0].equals("const")) {
                    throw new CoverParseException(node, "unknown declaration type: " + parts[0]);
                } else {
                    warn(node, "ignoring const");
                }
            }
            String typeName = scope.typedefTranslate(parts[parts.length-1]);
            
            if (declarator instanceof CPPASTArrayDeclarator) {
                System.err.println(name+" declared as array");
                // we don't support initializers yet, so keep it empty
                frameSlot.setKind(FrameSlotKind.Object);
                CPPASTArrayDeclarator arrayDeclarator = (CPPASTArrayDeclarator) declarator;
                SLExpressionNode size = processExpression(scope, arrayDeclarator.getArrayModifiers()[0].getConstantExpression(), null);
                if (declSpecifier instanceof CPPASTSimpleDeclSpecifier) {
                    if ("int".equals(typeName)) {
                        nodes.add(new CreateLocalLongArrayNode(frameSlot, size)); // FIXME
                    } else if ("long".equals(typeName)) {
                        nodes.add(new CreateLocalLongArrayNode(frameSlot, size));
                    } else if ("char".equals(typeName)) {
                        nodes.add(new CreateLocalLongArrayNode(frameSlot, size)); // FIXME
                    } else if ("double".equals(typeName)) {
                        nodes.add(new CreateLocalDoubleArrayNode(frameSlot, size)); // FIXME
                    } else {
                        throw new CoverParseException(node, "unsupported array type: " + typeName);
                    }
                } else {
                    throw new CoverParseException(node, "unsupported array declaration type: " + declSpecifier.getClass().getSimpleName());
                }
            } else if (declarator instanceof CPPASTDeclarator) {
                printTree(node, 1);
                CPPASTDeclarator d = (CPPASTDeclarator) declarators[i];
                if (declSpecifier instanceof CPPASTNamedTypeSpecifier) {
                    CPPASTNamedTypeSpecifier namedTypeSpecifier = (CPPASTNamedTypeSpecifier) declSpecifier;
                    typeName = scope.typedefTranslate(namedTypeSpecifier.getName().toString());
                }
                IASTPointerOperator[] pointerOperators = d.getPointerOperators();
                if (pointerOperators.length > 0) {
                    warn(d, "pointer found: setting type to object, but should probably do smarter things");
                    frameSlot.setKind(FrameSlotKind.Object);
                } else if ("int".equals(typeName)) {
                    frameSlot.setKind(FrameSlotKind.Long);
                } else if ("long".equals(typeName)) {
                    frameSlot.setKind(FrameSlotKind.Long);
                } else if ("char".equals(typeName)) {
                    frameSlot.setKind(FrameSlotKind.Long);
                } else if ("double".equals(typeName)) {
                    frameSlot.setKind(FrameSlotKind.Double);
                } else {
                    throw new CoverParseException(node, "unsupported type: " + typeName);
                }
                //System.err.println(name+" declared as " + frameSlot.getKind());
                CPPASTEqualsInitializer initializer = (CPPASTEqualsInitializer) d.getInitializer();
                if (initializer != null) {
                    SLExpressionNode expression = processExpression(scope, (IASTExpression) initializer.getInitializerClause(), typeName);
                    nodes.add(CoverWriteVariableNodeGen.create(new CoverFrameSlotLiteral(frameSlot), expression));
                } else {
                    // FIXME: initialize according to type
                    nodes.add(CoverWriteVariableNodeGen.create(new CoverFrameSlotLiteral(frameSlot), new SLLongLiteralNode(0)));
                }
            } else {
                throw new CoverParseException(node, "unknown declarator type: " + declarators[i].getClass().getSimpleName());
            }
        }
        return new SLBlockNode(nodes.stream().toArray(SLStatementNode[]::new));
    }

    private void warn(IASTNode node, String message) {
        System.err.println(nodeMessage(node, "warning: " + message));
    }

    private void info(IASTNode node, String message) {
        System.err.println(nodeMessage(node, "info: " + message));
    }

    private SLExpressionNode processLiteral(CoverScope scope, CPPASTLiteralExpression y) {
        if (y.getKind() == IASTLiteralExpression.lk_string_literal) {
            String v = new String(y.getValue());
            String noQuotes = v.substring(1, v.length() - 1).replace("\\n", "\n");
            return new SLStringLiteralNode(noQuotes);
        } else if (y.getKind() == IASTLiteralExpression.lk_integer_constant) {
            String stringValue = new String(y.getValue());
            final int intValue;
            if (stringValue.startsWith("0x")) {
                intValue = Integer.parseInt(stringValue.substring(2), 16);
            } else {
                intValue = Integer.parseInt(stringValue);
            }
            return new SLLongLiteralNode(intValue);
        } else if (y.getKind() == IASTLiteralExpression.lk_float_constant) {
            return new CoverDoubleLiteralNode(Double.parseDouble(new String(y.getValue())));
        } else {
            throw new CoverParseException(y, "unsupported literal type: " + y.getKind());
        }
    }

    private SLExpressionNode processFunctionCall(CoverScope scope, IASTNode node, String requestedType) {
        CPPASTFunctionCallExpression functionCall = (CPPASTFunctionCallExpression) node;
        String name = functionCall.getFunctionNameExpression().getRawSignature();

        List<SLExpressionNode> coverArguments = new ArrayList<>();
        for (IASTInitializerClause x : functionCall.getArguments()) {
            if (x instanceof IASTExpression) {
                coverArguments.add(processExpression(scope, (IASTExpression) x, null));
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
        } else if ("fwrite".equals(name)) {
            return CoverFWriteBuiltinNodeGen.create(argumentArray[0], argumentArray[1], argumentArray[2], argumentArray[3]);
        } else if ("putc".equals(name)) {
            return CoverPutcBuiltinNodeGen.create(argumentArray[0], argumentArray[1]);
        } else if ("malloc".equals(name)) {
            info(node, "inserting malloc for type " + requestedType);
            return new CoverNewArrayNode(requestedType, argumentArray[0]);
        } else if ("free".equals(name)) {
            return new CoverNopExpression();
        } else {
            return new SLInvokeNode(new CoverFunctionLiteralNode(scope.findFunction(node, name)), argumentArray);
        }
    }

    private SLExpressionNode processId(CoverScope scope, CPPASTIdExpression id) {
        String name = id.getName().getRawSignature();
        final SLExpressionNode result;
        final FrameSlot frameSlot = scope.findFrameSlot(id, name);
        if (frameSlot != null) {
            /* Read of a local variable. */
            // System.err.println(name + " is " + frameSlot.getKind().toString() + " slot is " + System.identityHashCode(frameSlot));
            result = SLReadLocalVariableNodeGen.create(frameSlot);
        } else {
            throw new CoverParseException(id, "ID not found in local scope");
        }
        return result; 
    }

    private SLExpressionNode processFunctionDefinition(CoverScope scope, CPPASTFunctionDefinition functionDefintion) {
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
        CoverScope newScope = new CoverScope(scope);
        CPPASTFunctionDeclarator declarator = (CPPASTFunctionDeclarator) functionDefintion.getDeclarator();
        ICPPASTParameterDeclaration[] parameters = declarator.getParameters();
        FrameSlot[] argumentArray = new FrameSlot[parameters.length];
        SLStatementNode[] readArgumentsStatements = new SLStatementNode[parameters.length];
        for (int i = 0;i<parameters.length;i++) {
            ICPPASTParameterDeclaration parameter = parameters[i];
            String name = parameter.getDeclarator().getName().getRawSignature();
            String rawSignature = scope.typedefTranslate(parameter.getDeclSpecifier().getRawSignature());
            FrameSlotKind kind = FrameSlotKind.Object;
            if ("int".equals(rawSignature)) {
                kind = FrameSlotKind.Long;
            }
            FrameSlot frameSlot = newScope.addFrameSlot(functionDefintion, name);
            frameSlot.setKind(kind);
            argumentArray[i] = frameSlot;
            
            // copy to local var in the prologue
            final SLReadArgumentNode readArg = new SLReadArgumentNode(i);
            SLExpressionNode assignment = SLWriteLocalVariableNodeGen.create(readArg, frameSlot);
            readArgumentsStatements[i] = assignment;
        }
        
        SLBlockNode readArgumentsNode = new SLBlockNode(readArgumentsStatements);
        
        IASTStatement s = functionDefintion.getBody();
        SLStatementNode blockNode = processStatement(newScope, s);
        SLBlockNode wrappedBodyNode = new SLBlockNode(new SLStatementNode[] {readArgumentsNode, blockNode});
        final SLFunctionBodyNode functionBodyNode = new SLFunctionBodyNode(wrappedBodyNode);
        
        // we will now add code to read the arguments into the frame
        // load local variables from arguments
        
        String functionName = declarator.getName().toString();
        // for int main() {} we create a main = (int)(){} assignment
        SLFunction function = new SLFunction(functionName);
        SLRootNode rootNode = new SLRootNode(newScope.getFrameDescriptor(), functionBodyNode, null, functionName);
        RootCallTarget callTarget = Truffle.getRuntime().createCallTarget(rootNode);
        function.setCallTarget(callTarget);
        
        scope.addFunction(functionName, function);
        return new CoverNopExpression();
    }

    private SourceSection createSourceSectionForNode(String identifier, IASTNode expression) {
        IASTFileLocation fileLocation = expression.getFileLocation();
        int charIndex = fileLocation.getNodeOffset();
        int length = fileLocation.getNodeLength();
        return source.createSection(identifier, charIndex, length);
    }

    private SLBlockNode processCompoundStatement(CoverScope scope, IASTStatement s) {
        IASTCompoundStatement compound = (IASTCompoundStatement) s;
        List<SLStatementNode> statements = new ArrayList<SLStatementNode>();
        for (IASTStatement statement : compound.getStatements()) {
            statements.add(processStatement(scope, statement));
        }
        SLBlockNode blockNode = new SLBlockNode(statements.toArray(new SLStatementNode[statements.size()]));
        return blockNode;
    }

    static String nodeMessage(IASTNode node, String message) {
        if (node == null) {
            return "<unknown>: " + message;
        }
        IASTFileLocation f = node.getFileLocation();
        return f.getFileName() + ":" + f.getStartingLineNumber() + ": " + message + ": '" + node.getRawSignature() + "'";
    }

    public static void printTree(IASTNode node, int index) {
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

        System.err.println(String.format(new StringBuilder("%1$").append(index * 2).append("s").toString(),
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
