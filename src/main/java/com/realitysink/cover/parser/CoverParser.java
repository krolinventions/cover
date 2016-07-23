package com.realitysink.cover.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.oracle.truffle.api.dsl.NodeFactory;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.source.Source;
import com.realitysink.cover.CoverLanguage;
import com.realitysink.cover.builtins.SLPrintlnBuiltin;
import com.realitysink.cover.builtins.SLPrintlnBuiltinFactory;
import com.realitysink.cover.nodes.SLExpressionNode;
import com.realitysink.cover.nodes.SLRootNode;
import com.realitysink.cover.nodes.SLStatementNode;
import com.realitysink.cover.nodes.controlflow.SLBlockNode;
import com.realitysink.cover.nodes.controlflow.SLFunctionBodyNode;
import com.realitysink.cover.nodes.expression.SLStringLiteralNode;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.ExpansionOverlapsBoundaryException;
import org.eclipse.cdt.core.dom.ast.IASTAttribute;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisibilityLabel;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage;
import org.eclipse.cdt.core.parser.DefaultLogService;
import org.eclipse.cdt.core.parser.FileContent;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IncludeFileContentProvider;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTCompoundStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTExpressionStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionCallExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionDefinition;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTLiteralExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTName;
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
    	
		FileContent fileContent = FileContent
				.createForExternalFileLocation(source.getPath());

		Map definedSymbols = new HashMap();
		String[] includePaths = new String[0];
		IScannerInfo info = new ScannerInfo(definedSymbols, includePaths);
		IParserLogService log = new DefaultLogService();

		IncludeFileContentProvider emptyIncludes = IncludeFileContentProvider.getEmptyFilesProvider();

		int opts = 8;
		IASTTranslationUnit translationUnit = GPPLanguage.getDefault()
				.getASTTranslationUnit(fileContent, info, emptyIncludes, null, opts, log);

		IASTPreprocessorIncludeStatement[] includes = translationUnit.getIncludeDirectives();
		for (IASTPreprocessorIncludeStatement include : includes) {
			System.out.println("include - " + include.getName());
		}

		printTree(translationUnit, 1);

		System.out
				.println("-----------------------------------------------------");
		System.out
				.println("-----------------------------------------------------");
		System.out
				.println("-----------------------------------------------------");

		ASTVisitor visitor = new ASTVisitor() {
			public int visit(IASTName name) {
				if ((name.getParent() instanceof CPPASTFunctionDeclarator)) {
					System.out.println("IASTName: "
							+ name.getClass().getSimpleName() + "("
							+ name.getRawSignature() + ") - > parent: "
							+ name.getParent().getClass().getSimpleName());
					System.out.println("-- isVisible: "
							+ CoverParser.isVisible(name));
				}

				return 3;
			}

			public int visit(IASTDeclaration declaration) {
				System.out.println("declaration: " + declaration + " ->  "
						+ declaration.getRawSignature());

				if ((declaration instanceof IASTSimpleDeclaration)) {
					IASTSimpleDeclaration ast = (IASTSimpleDeclaration) declaration;
					try {
						System.out
								.println("--- type: " + ast.getSyntax()
										+ " (childs: "
										+ ast.getChildren().length + ")");
						IASTNode typedef = ast.getChildren().length == 1 ? ast
								.getChildren()[0] : ast.getChildren()[1];
						System.out.println("------- typedef: " + typedef);
						IASTNode[] children = typedef.getChildren();
						if ((children != null) && (children.length > 0))
							System.out.println("------- typedef-name: "
									+ children[0].getRawSignature());
					} catch (ExpansionOverlapsBoundaryException e) {
						e.printStackTrace();
					}

					IASTDeclarator[] declarators = ast.getDeclarators();
					for (IASTDeclarator iastDeclarator : declarators) {
						System.out.println("iastDeclarator > "
								+ iastDeclarator.getName());
					}

					IASTAttribute[] attributes = ast.getAttributes();
					for (IASTAttribute iastAttribute : attributes) {
						System.out.println("iastAttribute > " + iastAttribute);
					}

				}

				if ((declaration instanceof IASTFunctionDefinition)) {
					IASTFunctionDefinition ast = (IASTFunctionDefinition) declaration;
					IScope scope = ast.getScope();
					try {
						System.out.println("### function() - Parent = "
								+ scope.getParent().getScopeName());
						System.out.println("### function() - Syntax = "
								+ ast.getSyntax());
					} catch (DOMException e) {
						e.printStackTrace();
					} catch (ExpansionOverlapsBoundaryException e) {
						e.printStackTrace();
					}
					ICPPASTFunctionDeclarator typedef = (ICPPASTFunctionDeclarator) ast
							.getDeclarator();
					System.out.println("------- typedef: " + typedef.getName());
				}

				return 3;
			}

			public int visit(IASTTypeId typeId) {
				System.out.println("typeId: " + typeId.getRawSignature());
				return 3;
			}

			public int visit(IASTStatement statement) {
				System.out.println("statement: " + statement.getRawSignature());
				return 3;
			}

			public int visit(IASTAttribute attribute) {
				return 3;
			}
		};
		visitor.shouldVisitNames = true;
		visitor.shouldVisitDeclarations = false;

		visitor.shouldVisitDeclarators = true;
		visitor.shouldVisitAttributes = true;
		visitor.shouldVisitStatements = false;
		visitor.shouldVisitTypeIds = true;

		translationUnit.accept(visitor);
		// RootNode
		final FrameDescriptor frameDescriptor = new FrameDescriptor();
		List<SLStatementNode> statements = new ArrayList();
		for (IASTNode node : translationUnit.getChildren()) {
			statements.add(processStatement(frameDescriptor, node));
		}
		SLBlockNode blockNode = new SLBlockNode(statements.toArray(new SLStatementNode[statements.size()]));
		final SLFunctionBodyNode functionBodyNode = new SLFunctionBodyNode(blockNode);
		final SLRootNode rootNode = new SLRootNode(frameDescriptor, functionBodyNode, null, "file");
		result.put("main", rootNode);
		return result;
	}
    
    private static SLStatementNode processStatement(FrameDescriptor frameDescriptor, IASTNode node) {
    	System.err.println("processStatement for " + node.getClass().getSimpleName());
		if (node instanceof CPPASTFunctionDefinition) {
			CPPASTFunctionDefinition functionDefintion = (CPPASTFunctionDefinition) node;
			FrameDescriptor newFrame = new FrameDescriptor();
			IASTStatement s = functionDefintion.getBody();
			IASTCompoundStatement compound = (IASTCompoundStatement) s;
			List<SLRootNode> bodyNodes = new ArrayList<SLRootNode>();
			List<SLStatementNode> statements = new ArrayList();
			for (IASTStatement statement : compound.getStatements()) {
				statements.add(processStatement(newFrame, statement));
			}
			SLBlockNode blockNode = new SLBlockNode(statements.toArray(new SLStatementNode[statements.size()]));
	        final SLFunctionBodyNode functionBodyNode = new SLFunctionBodyNode(blockNode);
	        //functionBodyNode.setSourceSection(functionSrc);
			String functionName = functionDefintion.getDeclarator().getName().toString();
			System.err.println("Function name: " + functionName);
	        return functionBodyNode;
		} else if (node instanceof CPPASTExpressionStatement) {
			// only support function calls for now...
			return processStatement(frameDescriptor, node.getChildren()[0]);
		} else if (node instanceof CPPASTFunctionCallExpression) {
			CPPASTFunctionCallExpression functionCall = (CPPASTFunctionCallExpression) node;
			String name = functionCall.getFunctionNameExpression().getRawSignature();
			if ("puts".equals(name)) {
				NodeFactory<SLPrintlnBuiltin> p = SLPrintlnBuiltinFactory.getInstance();
				System.err.println("arguments for println");
				for (List<Class<?>> s : p.getNodeSignatures()) {
					for (Class<?> c : s) {
						System.err.println("  argument: " + c.getName());
					}
				}
				System.err.println("done");
				CPPASTLiteralExpression e = (CPPASTLiteralExpression) functionCall.getArguments()[0];
				String literal = e.getRawSignature();
				SLStringLiteralNode expression = new SLStringLiteralNode(literal.substring(1, literal.length()-1));
				return p.createNode(new SLExpressionNode[]{ expression }, CoverLanguage.INSTANCE.findContext());
			} else {
				System.err.println("Ignoring unknown function call " + name);
				return new NopStatement();
			}
		} else {
			System.err.println("Unknown node type: " + node.getClass().getSimpleName());
			return new NopStatement();
		}
	}

	static IASTNode getFromChildren(IASTNode[] children, Class<? extends IASTNode> clazz) {
    	for (IASTNode child : children) {
    		if (child.getClass().equals(clazz)) {
    			return child;
    		}
    	}
    	return null; // let it burn
    }
    
	private static Map<String, SLRootNode> convertToNodes(FrameDescriptor frameDescriptor, IASTNode astNode, int index) {
		Map<String, SLRootNode> result = new HashMap<String, SLRootNode>();
		
		IASTNode[] children = astNode.getChildren();
		
		

		if ((astNode instanceof CPPASTTranslationUnit)) {
			for (IASTNode iastNode : children) {
				result.putAll(convertToNodes(frameDescriptor, iastNode, index + 1));
			}
		}
	
		return result;
	}
	
	private static void printTree(IASTNode node, int index) {
		IASTNode[] children = node.getChildren();

		boolean printContents = true;

		if ((node instanceof CPPASTTranslationUnit)) {
			printContents = false;
		}

		String offset = "";
		try {
			offset = node.getSyntax() != null ? " (offset: "
					+ node.getFileLocation().getNodeOffset() + ","
					+ node.getFileLocation().getNodeLength() + ")" : "";
			printContents = node.getFileLocation().getNodeLength() < 30;
		} catch (ExpansionOverlapsBoundaryException e) {
			e.printStackTrace();
		} catch (UnsupportedOperationException e) {
			offset = "UnsupportedOperationException";
		}

		System.out.println(String.format(
				new StringBuilder("%1$").append(index * 2).append("s")
						.toString(), new Object[] { "-" })
				+ node.getClass().getSimpleName()
				+ offset
				+ " -> "
				+ (printContents ? node.getRawSignature().replaceAll("\n",
						" \\ ") : node.getRawSignature().subSequence(0, 5)));

		for (IASTNode iastNode : children)
			printTree(iastNode, index + 1);
	}

	public static boolean isVisible(IASTNode current) {
		IASTNode declator = current.getParent().getParent();
		IASTNode[] children = declator.getChildren();

		for (IASTNode iastNode : children) {
			if ((iastNode instanceof ICPPASTVisibilityLabel)) {
				return 1 == ((ICPPASTVisibilityLabel) iastNode).getVisibility();
			}
		}

		return false;
	}  
}
