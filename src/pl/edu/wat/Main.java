package pl.edu.wat;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import javax.tools.*;

import java.io.*;
import java.util.*;

public class Main {

	public static void main(String[] args) throws IOException {
		final String fileName = "Class.java";
		final String alteredFileName = "src\\ClassAltered.java";
		CompilationUnit cu;
		try (FileInputStream in = new FileInputStream(fileName)) {
			cu = JavaParser.parse(in);
		}

		new Rewriter().visit(cu, null);

		cu.getClassByName("Class").get().setName("ClassAltered");

		try (FileWriter output = new FileWriter(new File(alteredFileName), false)) {
			output.write(cu.toString());
		}

		File[] files = { new File(alteredFileName) };
		String[] options = { "-d", "out//production//Synthesis" };

		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
		try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null)) {
			Iterable<? extends JavaFileObject> compilationUnits = fileManager
					.getJavaFileObjectsFromFiles(Arrays.asList(files));
			compiler.getTask(null, fileManager, diagnostics, Arrays.asList(options), null, compilationUnits).call();
			diagnostics.getDiagnostics().forEach(d -> System.out.println(d.getMessage(null)));
		}
	}

	private static class Rewriter extends VoidVisitorAdapter<Void> {

		@Override
		public void visit(MethodCallExpr method, Void arg) {

			NodeList<Expression> args = method.getArguments();

			SimpleName name = method.getName();

			for (int i = 1; i < args.size(); i++) {
				if (name.toString().equals("FOR")) {

					NodeList<Expression> initialization = new NodeList<Expression>();
					initialization.add(new VariableDeclarationExpr(
							new VariableDeclarator(new PrimitiveType(PrimitiveType.Primitive.INT),
									args.get(0).toString(), new IntegerLiteralExpr(args.get(1).toString()))));

					BinaryExpr.Operator operator;
					Expression op = args.get(3);
					if (op.isStringLiteralExpr() == false)
						throw new IllegalArgumentException("Argument is of an illegal type");
					switch (((LiteralStringValueExpr) op).getValue()) {
					case "<":
						operator = BinaryExpr.Operator.LESS;
						break;
					case "<=":
						operator = BinaryExpr.Operator.LESS_EQUALS;
						break;
					case ">":
						operator = BinaryExpr.Operator.GREATER;
						break;
					case ">=":
						operator = BinaryExpr.Operator.GREATER_EQUALS;
						break;
					default:
						throw new IllegalArgumentException("Argument is of an illegal format");
					}
					BinaryExpr compare = new BinaryExpr(args.get(0), args.get(2), operator);

					NodeList<Expression> update = new NodeList<Expression>();
					UnaryExpr.Operator operator2;
					Expression op2 = args.get(4);
					if (op2.isStringLiteralExpr() == false)
						throw new IllegalArgumentException("Argument is of an illegal type");
					switch (((LiteralStringValueExpr) op2).getValue()) {
					case "+":
						operator2 = UnaryExpr.Operator.POSTFIX_INCREMENT;
						break;
					case "-":
						operator2 = UnaryExpr.Operator.POSTFIX_DECREMENT;
						break;
					default:
						throw new IllegalArgumentException("Argument is of an illegal format");
					}
					update.add(new UnaryExpr(new NameExpr(args.get(0).toString()), operator2));

					BlockStmt body = new BlockStmt();

					ForStmt forStmt = new ForStmt(initialization, compare, update, body);

					method.getParentNode().get().replace(forStmt);
				}
			}
		}
	}
}