package com.realitysink.cover.parser;

import java.util.HashMap;
import java.util.Map;

import com.oracle.truffle.api.source.Source;
import com.realitysink.cover.nodes.SLRootNode;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.ExpansionOverlapsBoundaryException;
import org.eclipse.cdt.core.dom.ast.IASTAttribute;
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
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTTranslationUnit;
import org.eclipse.core.runtime.CoreException;

public class CoverParser {
    public static Map<String, SLRootNode> parseSource(Source source) throws CoreException {
    	Map<String, SLRootNode> result = new HashMap<String, SLRootNode>();
    	
		FileContent fileContent = FileContent
				.createForExternalFileLocation(source.getPath());

		Map definedSymbols = new HashMap();
		String[] includePaths = new String[0];
		IScannerInfo info = new ScannerInfo(definedSymbols, includePaths);
		IParserLogService log = new DefaultLogService();

		IncludeFileContentProvider emptyIncludes = IncludeFileContentProvider
				.getEmptyFilesProvider();

		int opts = 8;
		IASTTranslationUnit translationUnit = GPPLanguage.getDefault()
				.getASTTranslationUnit(fileContent, info, emptyIncludes, null,
						opts, log);

		IASTPreprocessorIncludeStatement[] includes = translationUnit
				.getIncludeDirectives();
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
