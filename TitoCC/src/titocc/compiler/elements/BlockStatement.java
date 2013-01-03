package titocc.compiler.elements;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import titocc.compiler.Assembler;
import titocc.compiler.Register;
import titocc.compiler.Scope;
import titocc.tokenizer.SyntaxException;
import titocc.tokenizer.TokenStream;

/**
 *List of statements surrounded by {} braces.
 */
public class BlockStatement extends Statement
{
	private List<Statement> statements;

	public BlockStatement(List<Statement> statements, int line, int column)
	{
		super(line, column);
		this.statements = statements;
	}

	public List<Statement> getStatements()
	{
		return statements;
	}

	@Override
	public void compile(Assembler asm, Scope scope, Stack<Register> registers)
			throws IOException, SyntaxException
	{
		//Create new scope for the block.
		Scope blockScope = new Scope(scope, "");
		scope.addSubScope(blockScope);

		// Compile statements
		for (Statement st : statements)
			st.compile(asm, blockScope, registers);
	}

	@Override
	public String toString()
	{
		String str = "(BLK_ST";
		for (Statement st : statements)
			str += " " + st;
		return str + ")";
	}

	public static BlockStatement parse(TokenStream tokens)
	{
		int line = tokens.getLine(), column = tokens.getColumn();
		tokens.pushMark();
		BlockStatement blockStatement = null;

		if (tokens.read().toString().equals("{")) {
			List<Statement> statements = new LinkedList<Statement>();

			Statement statement = Statement.parse(tokens);
			while (statement != null) {
				statements.add(statement);
				statement = Statement.parse(tokens);
			}

			if (tokens.read().toString().equals("}"))
				blockStatement = new BlockStatement(statements, line, column);
		}

		tokens.popMark(blockStatement == null);
		return blockStatement;
	}
}
