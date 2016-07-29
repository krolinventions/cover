package com.realitysink.cover.parser;

import java.io.IOException;
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
import com.realitysink.cover.nodes.CoverReference;
import com.realitysink.cover.nodes.CoverScope;
import com.realitysink.cover.nodes.CoverType;
import com.realitysink.cover.nodes.CoverType.BasicType;
import com.realitysink.cover.nodes.CoverTypedExpressionNode;
import com.realitysink.cover.nodes.SLRootNode;
import com.realitysink.cover.nodes.SLStatementNode;
import com.realitysink.cover.nodes.call.SLInvokeNode;
import com.realitysink.cover.nodes.controlflow.SLBlockNode;
import com.realitysink.cover.nodes.controlflow.SLFunctionBodyNode;
import com.realitysink.cover.nodes.controlflow.SLIfNode;
import com.realitysink.cover.nodes.controlflow.SLReturnNode;
import com.realitysink.cover.nodes.controlflow.SLWhileNode;
import com.realitysink.cover.nodes.expression.ArrayLiteralNode;
import com.realitysink.cover.nodes.expression.CoverAddDoubleNodeGen;
import com.realitysink.cover.nodes.expression.CoverAddLongNode;
import com.realitysink.cover.nodes.expression.CoverAddLongNodeGen;
import com.realitysink.cover.nodes.expression.CoverDivDoubleNodeGen;
import com.realitysink.cover.nodes.expression.CoverDivLongNodeGen;
import com.realitysink.cover.nodes.expression.CoverDoubleLiteralNode;
import com.realitysink.cover.nodes.expression.CoverFunctionLiteralNode;
import com.realitysink.cover.nodes.expression.CoverLessOrEqualDoubleNodeGen;
import com.realitysink.cover.nodes.expression.CoverLessOrEqualLongNodeGen;
import com.realitysink.cover.nodes.expression.CoverLessThanDoubleNodeGen;
import com.realitysink.cover.nodes.expression.CoverLessThanLongNodeGen;
import com.realitysink.cover.nodes.expression.CoverModDoubleNodeGen;
import com.realitysink.cover.nodes.expression.CoverModLongNodeGen;
import com.realitysink.cover.nodes.expression.CoverMulDoubleNodeGen;
import com.realitysink.cover.nodes.expression.CoverMulLongNodeGen;
import com.realitysink.cover.nodes.expression.CoverSubDoubleNodeGen;
import com.realitysink.cover.nodes.expression.CoverSubLongNodeGen;
import com.realitysink.cover.nodes.expression.SLBinaryAndNodeGen;
import com.realitysink.cover.nodes.expression.SLBinaryNotNodeGen;
import com.realitysink.cover.nodes.expression.SLBinaryOrNodeGen;
import com.realitysink.cover.nodes.expression.SLBinaryShiftLeftNodeGen;
import com.realitysink.cover.nodes.expression.SLBinaryShiftRightNodeGen;
import com.realitysink.cover.nodes.expression.SLEqualNodeGen;
import com.realitysink.cover.nodes.expression.SLForceBooleanNodeGen;
import com.realitysink.cover.nodes.expression.SLLogicalAndNodeGen;
import com.realitysink.cover.nodes.expression.SLLongLiteralNode;
import com.realitysink.cover.nodes.expression.SLStringLiteralNode;
import com.realitysink.cover.nodes.expression.SLSubNodeGen;
import com.realitysink.cover.nodes.local.CoverNewArrayNode;
import com.realitysink.cover.nodes.local.CoverReadArrayVariableNode;
import com.realitysink.cover.nodes.local.CoverReadArrayVariableNodeGen;
import com.realitysink.cover.nodes.local.CoverReadDoubleArrayValueNodeGen;
import com.realitysink.cover.nodes.local.CoverReadDoubleVariableNodeGen;
import com.realitysink.cover.nodes.local.CoverReadLongArgumentNodeGen;
import com.realitysink.cover.nodes.local.CoverReadLongArrayValueNodeGen;
import com.realitysink.cover.nodes.local.CoverReadLongVariableNodeGen;
import com.realitysink.cover.nodes.local.CoverWriteDoubleArrayElementNodeGen;
import com.realitysink.cover.nodes.local.CoverWriteDoubleNodeGen;
import com.realitysink.cover.nodes.local.CoverWriteDoubleVariableNodeGen;
import com.realitysink.cover.nodes.local.CoverWriteLongArrayElementNodeGen;
import com.realitysink.cover.nodes.local.CoverWriteLongNodeGen;
import com.realitysink.cover.nodes.local.CoverWriteLongVariableNodeGen;
import com.realitysink.cover.nodes.local.CreateLocalDoubleArrayNode;
import com.realitysink.cover.nodes.local.CreateLocalLongArrayNode;
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
import org.eclipse.cdt.core.model.ILanguage;
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
    final CoverScope fileScope;
    
    public CoverParser(Source source, CoverScope scope) {
        this.source = source;
        this.fileScope = scope;
    }

    public void parse() throws CoreException {
        parseRaw();
        if (fileScope.findReference("main") != null) {
            // add init function
            CoverParser parser;
            try {
                parser = new CoverParser(Source.fromFileName(System.getProperty("user.dir") + "/runtime/init.cover"), fileScope);
            } catch (IOException e) {
                throw new CoverParseException(null, "could not include _init", e);
            }
            parser.parseRaw();
        }
    }
    
    private void parseRaw() throws CoreException {
        System.err.println("Parsing " + source.getPath());

        FileContent fileContent = FileContent.createForExternalFileLocation(source.getPath());

        Map<String, String> definedSymbols = new HashMap<String, String>();
        String[] includePaths = new String[] {System.getProperty("user.dir") + "/runtime/include"};
        IScannerInfo info = new ScannerInfo(definedSymbols, includePaths);
        IParserLogService log = new DefaultLogService();

        IncludeFileContentProvider fileContentProvider =  new SavedFilesProvider() {
            @Override
            public InternalFileContent getContentForInclusion(String path, IMacroDictionary macroDictionary) {
                // we somehow don't get the contents of included headers parsed, so act on them here
                Source includedSource;
                try {
                    includedSource = Source.fromFileName(path);
                } catch (IOException e) {
                    throw new CoverParseException(null, "could not read included file " + path, e);
                }
                return new InternalFileContent(path, new CharArray(includedSource.getCode()));
            }
        };

        int opts = ILanguage.OPTION_IS_SOURCE_UNIT;
        IASTTranslationUnit translationUnit = GPPLanguage.getDefault().getASTTranslationUnit(fileContent, info,
                fileContentProvider, null, opts, log);

        IASTPreprocessorIncludeStatement[] includes = translationUnit.getIncludeDirectives();
        for (IASTPreprocessorIncludeStatement include : includes) {
            System.err.println("include - " + include.getName());
        }
        
        // RootNode
        for (IASTNode node : translationUnit.getChildren()) {
            processStatement(fileScope, node);
        }
    }

    private void printTruffleNodes(Node n2, int level) {
        String spaces = "";
        for (int i = 0; i < level; i++)
            spaces += "  ";
        if (n2 instanceof CoverTypedExpressionNode) {
            System.err.println(spaces + n2.getClass().getName() + " " + ((CoverTypedExpressionNode)n2).getType());
        } else {
            System.err.println(spaces + n2.getClass().getName());
        }
        for (Node n : n2.getChildren()) {
            printTruffleNodes(n, level + 1);
        }
    }

    private SLStatementNode processStatement(CoverScope scope, IASTNode node) {
        //info(node, "processStatement for " + node.getClass().getSimpleName());
        SLStatementNode result;
        if (node instanceof CPPASTFunctionDefinition) {
            result = processFunctionDefinition(scope, (CPPASTFunctionDefinition) node);
        } else if (node instanceof CPPASTExpressionStatement) {
            CPPASTExpressionStatement x = (CPPASTExpressionStatement) node;
            result =  processExpression(scope, x.getExpression(), null);
        } else if (node instanceof CPPASTDeclarationStatement) {
            result =  processDeclarationStatement(scope, (CPPASTDeclarationStatement) node);
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
            result =  processDeclaration(scope, (CPPASTSimpleDeclaration) node);
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
            return new CoverNopExpression();
        } else if (declSpecifier instanceof CPPASTSimpleDeclSpecifier) {
            CPPASTSimpleDeclSpecifier d = (CPPASTSimpleDeclSpecifier) declSpecifier;
            String oldType = "";
            if (d.isLong()) {
                oldType = "long";
            } else {
                throw new CoverParseException(node, "unsupported typedef type");
            }
            IASTDeclarator[] declarators = node.getDeclarators();
            for (IASTDeclarator declarator : declarators) {
                String newType = declarator.getName().toString();
                scope.addTypeDef(oldType, newType);
            }
            return new CoverNopExpression();
        } else {
            throw new CoverParseException(node, "unknown declSpecifier: " + declSpecifier.getClass().getSimpleName());
        }
    }

    private SLStatementNode processIfStatement(CoverScope scope, CPPASTIfStatement node) {
        CoverScope ifScope = new CoverScope(scope);
        CoverScope thenScope = new CoverScope(ifScope);
        CoverScope elseScope = new CoverScope(ifScope);
        CoverTypedExpressionNode conditionNode = SLForceBooleanNodeGen.create(SLForceBooleanNodeGen.create(processExpression(ifScope, node.getConditionExpression(), null)));
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
        CoverTypedExpressionNode conditionNode = SLForceBooleanNodeGen.create(processExpression(conditionScope, node.getCondition(), null));
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
        CoverTypedExpressionNode conditionNode = processExpression(conditionScope, condition, null);
        CoverTypedExpressionNode iterationNode = processExpression(iterationScope, iteration, null);
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
        CoverTypedExpressionNode conditionNode = SLForceBooleanNodeGen.create(processExpression(scope, node.getCondition(), null));
        SLStatementNode bodyNode = processStatement(scope, node.getBody());
        final SLWhileNode whileNode = new SLWhileNode(conditionNode, bodyNode);
        return whileNode;
    }

    private CoverTypedExpressionNode processExpression(CoverScope scope, IASTExpression expression, String requestedType) {
        if (expression == null) {
            throw new CoverParseException(null, "null expression");
        }
        CoverTypedExpressionNode result;
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
            result = processUnary(scope, (CPPASTUnaryExpression) expression);
        } else if (expression instanceof CPPASTCastExpression) {
            warn(expression, "ignoring cast");
            result = processExpression(scope, ((CPPASTCastExpression)expression).getOperand(), null);
        } else {
            throw new CoverParseException(expression, "unknown expression type " + expression.getClass().getSimpleName());
        }
        result.setSourceSection(createSourceSectionForNode("expression", expression));
        return result;
    }

    private CoverTypedExpressionNode processArraySubscriptExpression(CoverScope scope,
            CPPASTArraySubscriptExpression expression) {
        ICPPASTExpression array = expression.getArrayExpression();
        IASTExpression subscript = expression.getSubscriptExpression();
        
        CoverReference ref = scope.findReference(array.getRawSignature()); // FIXME
        if (ref == null) {
            throw new CoverParseException(expression, "identifier not found");
        }
        if (ref.getType().getBasicType() != BasicType.ARRAY) {
            throw new CoverParseException(expression, "does not reference an array");
        }
        if (ref.getType().getTypeOfArrayContents().getBasicType() == BasicType.LONG) {
            return CoverReadLongArrayValueNodeGen.create(CoverReadArrayVariableNodeGen.create(ref.getFrameSlot()), processExpression(scope, subscript, null));
        } else if (ref.getType().getTypeOfArrayContents().getBasicType() == BasicType.DOUBLE) {
            return CoverReadDoubleArrayValueNodeGen.create(CoverReadArrayVariableNodeGen.create(ref.getFrameSlot()), processExpression(scope, subscript, null));
        } else {
            throw new CoverParseException(expression, "unsupported array type " + ref.getType().getTypeOfArrayContents().getBasicType());
        }
    }

    private CoverTypedExpressionNode processBinaryExpression(CoverScope scope, CPPASTBinaryExpression expression) {
        int operator = expression.getOperator();
        CoverTypedExpressionNode result;
        if (operator == CPPASTBinaryExpression.op_lessThan) {
            CoverTypedExpressionNode leftNode = processExpression(scope, expression.getOperand1(), null);
            CoverTypedExpressionNode rightNode = processExpression(scope, expression.getOperand2(), null);
            result = createLessThanNode(expression, leftNode, rightNode);
        } else if (operator == CPPASTBinaryExpression.op_lessEqual) {
            CoverTypedExpressionNode leftNode = processExpression(scope, expression.getOperand1(), null);
            CoverTypedExpressionNode rightNode = processExpression(scope, expression.getOperand2(), null);
            result = createLessOrEqualNode(expression, leftNode, rightNode);
        } else if (operator == CPPASTBinaryExpression.op_equals) {
            CoverTypedExpressionNode leftNode = processExpression(scope, expression.getOperand1(), null);
            CoverTypedExpressionNode rightNode = processExpression(scope, expression.getOperand2(), null);
            result = SLEqualNodeGen.create(leftNode, rightNode);
        } else if (operator == CPPASTBinaryExpression.op_greaterThan) {
            // FIXME: this messes with evaluation order!
            CoverTypedExpressionNode leftNode = processExpression(scope, expression.getOperand1(), null);
            CoverTypedExpressionNode rightNode = processExpression(scope, expression.getOperand2(), null);
            result = createLessThanNode(expression, rightNode, leftNode);
        } else if (operator == CPPASTBinaryExpression.op_plusAssign) {
            CoverTypedExpressionNode change = processExpression(scope, expression.getOperand2(), null);
            CoverTypedExpressionNode source = processExpression(scope, expression.getOperand1(), null);
            result = createWriteVariableNode(scope, expression.getOperand1(), createAddNode(expression, source, change));
        } else if (operator == CPPASTBinaryExpression.op_assign) {
            CoverTypedExpressionNode value = processExpression(scope, expression.getOperand2(), null);
            result = createWriteVariableNode(scope, expression.getOperand1(), value);
        } else if (operator == CPPASTBinaryExpression.op_multiply) {
            CoverTypedExpressionNode leftNode = processExpression(scope, expression.getOperand1(), null);
            CoverTypedExpressionNode rightNode = processExpression(scope, expression.getOperand2(), null);
            result = createMulNode(expression, leftNode, rightNode);
        } else if (operator == CPPASTBinaryExpression.op_divide) {
            CoverTypedExpressionNode leftNode = processExpression(scope, expression.getOperand1(), null);
            CoverTypedExpressionNode rightNode = processExpression(scope, expression.getOperand2(), null);
            result = createDivNode(expression, leftNode, rightNode);
        } else if (operator == CPPASTBinaryExpression.op_plus) {
            CoverTypedExpressionNode leftNode = processExpression(scope, expression.getOperand1(), null);
            CoverTypedExpressionNode rightNode = processExpression(scope, expression.getOperand2(), null);
            result = createAddNode(expression, leftNode, rightNode);
        } else if (operator == CPPASTBinaryExpression.op_minus) {
            CoverTypedExpressionNode leftNode = processExpression(scope, expression.getOperand1(), null);
            CoverTypedExpressionNode rightNode = processExpression(scope, expression.getOperand2(), null);
            result = createSubNode(expression, leftNode, rightNode);
        } else if (operator == CPPASTBinaryExpression.op_logicalAnd) {
            CoverTypedExpressionNode leftNode = processExpression(scope, expression.getOperand1(), null);
            CoverTypedExpressionNode rightNode = processExpression(scope, expression.getOperand2(), null);
            result = SLLogicalAndNodeGen.create(leftNode, rightNode);
        } else if (operator == CPPASTBinaryExpression.op_binaryAndAssign) {
            CoverTypedExpressionNode change = processExpression(scope, expression.getOperand2(), null);
            CoverTypedExpressionNode source = processExpression(scope, expression.getOperand1(), null);
            result = createWriteVariableNode(scope, expression.getOperand1(), SLBinaryAndNodeGen.create(source, change));
        } else if (operator == CPPASTBinaryExpression.op_shiftRightAssign) {
            CoverTypedExpressionNode change = processExpression(scope, expression.getOperand2(), null);
            CoverTypedExpressionNode source = processExpression(scope, expression.getOperand1(), null);
            result = createWriteVariableNode(scope, expression.getOperand1(), SLBinaryShiftRightNodeGen.create(source, change));
        } else if (operator == CPPASTBinaryExpression.op_shiftLeftAssign) {
            CoverTypedExpressionNode change = processExpression(scope, expression.getOperand2(), null);
            CoverTypedExpressionNode source = processExpression(scope, expression.getOperand1(), null);
            result = createWriteVariableNode(scope, expression.getOperand1(), SLBinaryShiftLeftNodeGen.create(source, change));
        } else if (operator == CPPASTBinaryExpression.op_binaryOrAssign) {
            CoverTypedExpressionNode change = processExpression(scope, expression.getOperand2(), null);
            CoverTypedExpressionNode source = processExpression(scope, expression.getOperand1(), null);
            result = createWriteVariableNode(scope, expression.getOperand1(), SLBinaryOrNodeGen.create(source, change));
        } else if (operator == CPPASTBinaryExpression.op_modulo) {
            CoverTypedExpressionNode leftNode = processExpression(scope, expression.getOperand1(), null);
            CoverTypedExpressionNode rightNode = processExpression(scope, expression.getOperand2(), null);
            result = createModNode(expression, leftNode, rightNode);
        } else {
            throw new CoverParseException(expression, "unknown operator type " + operator);
        }
        result.setSourceSection(createSourceSectionForNode("binary", expression));
        return result;
    }

    private CoverTypedExpressionNode createSubNode(CPPASTBinaryExpression expression, CoverTypedExpressionNode leftNode, CoverTypedExpressionNode rightNode) {
        CoverType newType = leftNode.getType().combine(expression, rightNode.getType());
        if (newType.equals(CoverType.LONG)) {
            return CoverSubLongNodeGen.create(leftNode, rightNode);
        } else if (newType.equals(CoverType.DOUBLE)) {
            return CoverSubDoubleNodeGen.create(leftNode, rightNode);
        } else {
            throw new CoverParseException(expression, "cannot multiply type " + newType);
        }
    }

    private CoverTypedExpressionNode createMulNode(CPPASTBinaryExpression expression,
            CoverTypedExpressionNode leftNode, CoverTypedExpressionNode rightNode) {
        CoverType newType = leftNode.getType().combine(expression, rightNode.getType());
        if (newType.equals(CoverType.LONG)) {
            return CoverMulLongNodeGen.create(leftNode, rightNode);
        } else if (newType.equals(CoverType.DOUBLE)) {
            return CoverMulDoubleNodeGen.create(leftNode, rightNode);
        } else {
            throw new CoverParseException(expression, "cannot multiply type " + newType);
        }
    }

    private CoverTypedExpressionNode createModNode(CPPASTBinaryExpression expression,
            CoverTypedExpressionNode leftNode, CoverTypedExpressionNode rightNode) {
        CoverType newType = leftNode.getType().combine(expression, rightNode.getType());
        if (newType.equals(CoverType.LONG)) {
            return CoverModLongNodeGen.create(leftNode, rightNode);
        } else if (newType.equals(CoverType.DOUBLE)) {
            return CoverModDoubleNodeGen.create(leftNode, rightNode);
        } else {
            throw new CoverParseException(expression, "cannot mod type " + newType);
        }
    }

    private CoverTypedExpressionNode createLessOrEqualNode(CPPASTBinaryExpression expression,
            CoverTypedExpressionNode leftNode, CoverTypedExpressionNode rightNode) {
        CoverType newType = leftNode.getType().combine(expression, rightNode.getType());
        if (newType.equals(CoverType.DOUBLE)) {
            return CoverLessOrEqualDoubleNodeGen.create(leftNode, rightNode);
        } else if (newType.equals(CoverType.LONG)) {
            return CoverLessOrEqualLongNodeGen.create(leftNode, rightNode);
        } else {
            throw new CoverParseException(expression, "cannot add type " + newType);
        }
    }

    private CoverTypedExpressionNode createLessThanNode(CPPASTBinaryExpression expression,
            CoverTypedExpressionNode leftNode, CoverTypedExpressionNode rightNode) {
        CoverType newType = leftNode.getType().combine(expression, rightNode.getType());
        if (newType.equals(CoverType.DOUBLE)) {
            return CoverLessThanDoubleNodeGen.create(leftNode, rightNode);
        } else if (newType.equals(CoverType.LONG)) {
            return CoverLessThanLongNodeGen.create(leftNode, rightNode);
        } else {
            throw new CoverParseException(expression, "cannot add type " + newType);
        }
    }

    private CoverTypedExpressionNode createDivNode(CPPASTBinaryExpression node,
            CoverTypedExpressionNode left, CoverTypedExpressionNode right) {
        CoverType newType = left.getType().combine(node, right.getType());
        if (newType.equals(CoverType.DOUBLE)) {
            return CoverDivDoubleNodeGen.create(left, right);
        } else if (newType.equals(CoverType.LONG)) {
            return CoverDivLongNodeGen.create(left, right);
        } else {
            throw new CoverParseException(node, "cannot add type " + newType);
        }
    }

    private CoverTypedExpressionNode createAddNode(CPPASTBinaryExpression node, CoverTypedExpressionNode left,
            CoverTypedExpressionNode right) {
        CoverType newType = left.getType().combine(node, right.getType());
        if (newType.equals(CoverType.DOUBLE)) {
            return CoverAddDoubleNodeGen.create(left, right);
        } else if (newType.equals(CoverType.LONG)) {
            return CoverAddLongNodeGen.create(left, right);
        } else {
            throw new CoverParseException(node, "cannot add type " + newType);
        }
    }

    private CoverTypedExpressionNode createWriteVariableNode(CoverScope scope, IASTExpression node, CoverTypedExpressionNode value) {
        // We parse the "left" expression, and then we add an assignment
        // Types of assignments:
        // LocalVariable (frameSlot)
        // ArrayMember (Object, index)
        // ObjectMember (TODO)
        
        if (node instanceof CPPASTIdExpression) {
            CPPASTIdExpression x = (CPPASTIdExpression) node;
            CoverReference ref = scope.findReference(x.getName().toString());
            if (ref == null) {
                throw new CoverParseException(node, "not found");
            }
            return createSimpleAssignmentNode(node, ref, value);
        } else if (node instanceof CPPASTArraySubscriptExpression) {
            CPPASTArraySubscriptExpression x = (CPPASTArraySubscriptExpression) node;
            ICPPASTExpression array = x.getArrayExpression();
            IASTExpression argument = (IASTExpression) x.getArgument();
            CoverTypedExpressionNode indexExpression = processExpression(scope, argument, null);
            
            CoverReference ref = scope.findReference(array.getRawSignature());
            if (ref == null) throw new CoverParseException(node, "not found");
            FrameSlot frameSlot = ref.getFrameSlot();
            if (frameSlot == null) throw new CoverParseException(node, "no frameslot");
            if (ref.getType().getBasicType() != BasicType.ARRAY)
                throw new CoverParseException(node, "is not an array");
            CoverReadArrayVariableNode arrayExpression = CoverReadArrayVariableNodeGen.create(frameSlot);
            BasicType elementType = ref.getType().getTypeOfArrayContents().getBasicType();
            if (elementType == BasicType.LONG) {
                return CoverWriteLongArrayElementNodeGen.create(arrayExpression, indexExpression, value);
            } else if (elementType == BasicType.DOUBLE) {
                return CoverWriteDoubleArrayElementNodeGen.create(arrayExpression, indexExpression, value);
            } else {
                throw new CoverParseException(node, "unsupported array type for assignment " + elementType);
            }
             
        }
        throw new CoverParseException(node, "unknown destination type: " + node.getClass().getSimpleName());
    }

    private CoverTypedExpressionNode createSimpleAssignmentNode(IASTNode node, CoverReference ref,
            CoverTypedExpressionNode value) {
        if (!ref.getType().canAccept(value.getType())) {
            throw new CoverParseException(node, "cannot assign "+value.getType()+" to " + ref.getType());
        }
        if (ref.getType().getBasicType() == BasicType.LONG) {
            return CoverWriteLongNodeGen.create(value, ref.getFrameSlot());
        } else if (ref.getType().getBasicType() == BasicType.DOUBLE) {
            return CoverWriteDoubleNodeGen.create(value, ref.getFrameSlot());
        } else {
            throw new CoverParseException(node, "unsupported variable write");
        }
    }

    private CoverTypedExpressionNode processUnary(CoverScope scope, CPPASTUnaryExpression node) {
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
        CoverAddLongNode addNode = CoverAddLongNodeGen.create(processExpression(scope, node.getOperand(), null), new SLLongLiteralNode(change));
        return createWriteVariableNode(scope, node.getOperand(), addNode);
    }

    private SLStatementNode processDeclarationStatement(CoverScope scope, CPPASTDeclarationStatement node) {
        CPPASTSimpleDeclaration s = (CPPASTSimpleDeclaration) node.getDeclaration();
        return processDeclaration(scope, s);
    }

    // FIXME: use processType in this function
    private SLStatementNode processDeclaration(CoverScope scope, CPPASTSimpleDeclaration node) {
        IASTDeclSpecifier declSpecifier = node.getDeclSpecifier();
        IASTDeclarator[] declarators = node.getDeclarators();
        List<SLStatementNode> nodes = new ArrayList<SLStatementNode>();
        for (int i=0;i<declarators.length;i++) {
            IASTDeclarator declarator = declarators[i];
            String name = declarator.getName().toString();
            
            // throw away "const"
            String rawTypeName = declSpecifier.getRawSignature();
            String parts[] = rawTypeName.split(" ");
            if (parts.length > 1) {
                if (parts[0].equals("typedef")) {
                    // retry as typedef!
                    return processTypedef(scope, node);
                }
                if (!parts[0].equals("const")) {
                    throw new CoverParseException(node, "unknown declaration type: " + parts[0]);
                } else {
                    warn(node, "ignoring const");
                }
            }
            String typeName = scope.typedefTranslate(parts[parts.length-1]);
            
            if (declarator instanceof CPPASTArrayDeclarator) {
                // we don't support initializers yet, so keep it empty
                CPPASTArrayDeclarator arrayDeclarator = (CPPASTArrayDeclarator) declarator;
                CoverTypedExpressionNode size = processExpression(scope, arrayDeclarator.getArrayModifiers()[0].getConstantExpression(), null);
                if (declSpecifier instanceof CPPASTSimpleDeclSpecifier) {
                    CoverType type;
                    if ("int".equals(typeName)) {
                        type = new CoverType(BasicType.LONG);
                    } else if ("long".equals(typeName)) {
                        type = new CoverType(BasicType.LONG);
                    } else if ("char".equals(typeName)) {
                        type = new CoverType(BasicType.LONG);
                    } else if ("double".equals(typeName)) {
                        type = new CoverType(BasicType.DOUBLE);
                    } else {
                        throw new CoverParseException(node, "unsupported array type: " + typeName);
                    }
                    System.err.println(name+" declared as array of " + type.getBasicType());
                    CoverType arrayType = new CoverType(BasicType.ARRAY).setArrayType(type);
                    CoverReference ref = scope.define(node, name, arrayType);
                    if (type.getBasicType() == BasicType.DOUBLE) {
                        nodes.add(new CreateLocalDoubleArrayNode(ref.getFrameSlot(), size));
                    } else if (type.getBasicType() == BasicType.LONG) {
                        nodes.add(new CreateLocalLongArrayNode(ref.getFrameSlot(), size));
                    } else {
                        throw new CoverParseException(node, "unsupported array type " + type.getBasicType());
                    }
                } else {
                    throw new CoverParseException(node, "unsupported array declaration type: " + declSpecifier.getClass().getSimpleName());
                }
            } else if (declarator instanceof CPPASTDeclarator) {
                CPPASTDeclarator d = (CPPASTDeclarator) declarators[i];
                if (declSpecifier instanceof CPPASTNamedTypeSpecifier) {
                    CPPASTNamedTypeSpecifier namedTypeSpecifier = (CPPASTNamedTypeSpecifier) declSpecifier;
                    typeName = scope.typedefTranslate(namedTypeSpecifier.getName().toString());
                }
                IASTPointerOperator[] pointerOperators = d.getPointerOperators();
                CoverType type;
                if (pointerOperators.length > 0) {
                    warn(d, "pointer found: setting type to object, but should probably do smarter things");
                    type = new CoverType(BasicType.OBJECT);
                } else if ("int".equals(typeName)) {
                    type = new CoverType(BasicType.LONG);
                } else if ("long".equals(typeName)) {
                    type = new CoverType(BasicType.LONG);
                } else if ("char".equals(typeName)) {
                    type = new CoverType(BasicType.LONG);
                } else if ("double".equals(typeName)) {
                    type = new CoverType(BasicType.DOUBLE);
                } else {
                    throw new CoverParseException(node, "unsupported type: " + typeName);
                }
                //System.err.println(name+" declared as " + frameSlot.getKind());
                CoverReference ref = scope.define(node, name, type);
                CPPASTEqualsInitializer initializer = (CPPASTEqualsInitializer) d.getInitializer();
                if (initializer != null) {
                    CoverTypedExpressionNode expression = processExpression(scope, (IASTExpression) initializer.getInitializerClause(), typeName);
                    nodes.add(createSimpleAssignmentNode(node, ref, expression));
                } else {
                    // FIXME: initialize according to type
                    nodes.add(createSimpleAssignmentNode(node, ref, new SLLongLiteralNode(0)));
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

    private CoverTypedExpressionNode processLiteral(CoverScope scope, CPPASTLiteralExpression y) {
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

    private CoverTypedExpressionNode processFunctionCall(CoverScope scope, IASTNode node, String requestedType) {
        CPPASTFunctionCallExpression functionCall = (CPPASTFunctionCallExpression) node;
        String rawName = functionCall.getFunctionNameExpression().getRawSignature();

        List<CoverTypedExpressionNode> coverArguments = new ArrayList<>();
        for (IASTInitializerClause x : functionCall.getArguments()) {
            if (x instanceof IASTExpression) {
                coverArguments.add(processExpression(scope, (IASTExpression) x, null));
            } else {
                throw new CoverParseException(node, "Unknown function argument type: " + x.getClass());
            }
        }
        CoverTypedExpressionNode[] argumentArray = coverArguments.toArray(new CoverTypedExpressionNode[coverArguments.size()]);
        
        if ("puts".equals(rawName)) {
            NodeFactory<SLPrintlnBuiltin> printlnBuiltinFactory = SLPrintlnBuiltinFactory.getInstance();
            return printlnBuiltinFactory.createNode(argumentArray, CoverLanguage.INSTANCE.findContext());
        } else if ("printf".equals(rawName)) {
            return new CoverPrintfBuiltin(argumentArray);
        } else if ("fwrite".equals(rawName)) {
            return CoverFWriteBuiltinNodeGen.create(argumentArray[0], argumentArray[1], argumentArray[2], argumentArray[3]);
        } else if ("putc".equals(rawName)) {
            return CoverPutcBuiltinNodeGen.create(argumentArray[0], argumentArray[1]);
        } else if ("malloc".equals(rawName)) {
            info(node, "inserting malloc for type " + requestedType);
            return new CoverNewArrayNode(requestedType, argumentArray[0]);
        } else if ("free".equals(rawName)) {
            return new CoverNopExpression();
        } else {
            CoverTypedExpressionNode function = processExpression(scope, functionCall.getFunctionNameExpression(), null);
            return new SLInvokeNode(function, argumentArray);
        }
    }

    private CoverTypedExpressionNode processId(CoverScope scope, CPPASTIdExpression id) {
        String name = id.getName().getRawSignature();
        CoverReference ref = scope.findReference(name);
        if (ref != null) {
            if (ref.getFrameSlot() != null) {
                if (ref.getType().getBasicType().equals(BasicType.LONG)) {
                    return CoverReadLongVariableNodeGen.create(ref.getFrameSlot());
                } else if (ref.getType().getBasicType().equals(BasicType.DOUBLE)) {
                    return CoverReadDoubleVariableNodeGen.create(ref.getFrameSlot());
                } else if (ref.getType().getBasicType().equals(BasicType.ARRAY)) {
                    return CoverReadArrayVariableNodeGen.create(ref.getFrameSlot());
                } else {
                    throw new CoverParseException(id, "unsupported variable read " + ref.getType());
                }
            } else if (ref.getFunction() != null){
                return new CoverFunctionLiteralNode(ref.getFunction());
            } else {
                throw new CoverParseException(id, "not a variable or function");
            }
        } else {
            throw new CoverParseException(id, "not found in local scope");
        }
    }

    private CoverTypedExpressionNode processFunctionDefinition(CoverScope scope, CPPASTFunctionDefinition node) {
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
        CPPASTFunctionDeclarator declarator = (CPPASTFunctionDeclarator) node.getDeclarator();
        ICPPASTParameterDeclaration[] parameters = declarator.getParameters();
        SLStatementNode[] readArgumentsStatements = new SLStatementNode[parameters.length];
        for (int i = 0;i<parameters.length;i++) {
            ICPPASTParameterDeclaration parameter = parameters[i];
            String name = parameter.getDeclarator().getName().getRawSignature();
            CoverType type = processType(scope, node, parameter.getDeclSpecifier());
            CoverReference ref = newScope.define(node, name, type);
            
            // copy to local var in the prologue
            final CoverTypedExpressionNode readArg;
            if (type.getBasicType() == BasicType.LONG) {
                readArg = CoverReadLongArgumentNodeGen.create(i);
            } else {
                throw new CoverParseException(node, "unsupported argument type");
            }
            CoverTypedExpressionNode assignment = createSimpleAssignmentNode(node, ref, readArg);
            readArgumentsStatements[i] = assignment;
        }
        
        SLBlockNode readArgumentsNode = new SLBlockNode(readArgumentsStatements);
        
        IASTStatement s = node.getBody();
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
        
        CoverReference reference = scope.define(node, functionName, new CoverType(BasicType.FUNCTION));
        reference.setFunction(function);
        return new CoverNopExpression();
    }

    private CoverType processType(CoverScope scope, CPPASTFunctionDefinition node, IASTDeclSpecifier declSpecifier) {
        String rawTypeName = declSpecifier.getRawSignature();
        String parts[] = rawTypeName.split(" ");
        if (parts.length > 1) {
            if (parts[0].equals("typedef")) {
                throw new CoverParseException(node, "type definition is actually a typedef!");
            }
            if (!parts[0].equals("const")) {
                throw new CoverParseException(node, "unknown declaration type: " + parts[0]);
            } else {
                warn(node, "ignoring const");
            }
        }
        String untranslatedTypeName = scope.typedefTranslate(parts[parts.length-1]); // FIXME: make this a real lookup!
        String typeName = scope.typedefTranslate(untranslatedTypeName);
        switch (typeName) {
        case "char": return new CoverType(BasicType.LONG);
        case "int": return new CoverType(BasicType.LONG);
        case "long": return new CoverType(BasicType.LONG);
        case "double": return new CoverType(BasicType.DOUBLE);
        default: throw new CoverParseException(node, "unknown basic type " + typeName);
        }
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
