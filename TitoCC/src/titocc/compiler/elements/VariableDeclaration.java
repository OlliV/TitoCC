package titocc.compiler.elements;

import java.io.IOException;
import java.io.Writer;
import titocc.compiler.Assembler;
import titocc.compiler.Scope;
import titocc.compiler.Symbol;
import titocc.tokenizer.IdentifierToken;
import titocc.tokenizer.SyntaxException;
import titocc.tokenizer.Token;
import titocc.tokenizer.TokenStream;

public class VariableDeclaration extends Declaration implements Symbol
{
	private boolean isGlobal; // Used in the compilation phase
	private Type type;
	private String name;
	private Expression initializer;

	public VariableDeclaration(Type type, String name,
			Expression initializer, int line, int column)
	{
		super(line, column);
		this.type = type;
		this.name = name;
		this.initializer = initializer;
	}

	public Type getType()
	{
		return type;
	}

	public String getName()
	{
		return name;
	}

	public Expression getInitializer()
	{
		return initializer;
	}

	@Override
	public void compile(Assembler asm, Scope scope) throws SyntaxException, IOException
	{
		if (scope.find(name) != null)
			throw new SyntaxException("Redefinition of " + name, getLine(), getColumn());

		isGlobal = scope.isGlobal();
		if (isGlobal)
			compileGlobalVariable(asm, scope);
		else
			compileLocalVariable(asm, scope);
	}

	@Override
	public String getReference()
	{
		if (isGlobal)
			return name;
		else
			return name + "(fp)";
	}

	@Override
	public String toString()
	{
		return "(VAR_DECL " + type + " " + name + " " + initializer + ")";
	}

	private void compileGlobalVariable(Assembler asm, Scope scope) throws SyntaxException, IOException
	{
		Integer initValue = 0;
		if (initializer != null) {
			initValue = initializer.getCompileTimeValue();
			if (initValue == null)
				throw new SyntaxException("Global variable must be initialized with a compile time constant.", getLine(), getColumn());
		}

		asm.emit(name, "dc", "" + initValue);

		scope.add(this);
	}

	private void compileLocalVariable(Assembler asm, Scope scope)
	{
		throw new UnsupportedOperationException("");
	}

	public static VariableDeclaration parse(TokenStream tokens)
	{
		int line = tokens.getLine(), column = tokens.getColumn();
		tokens.pushMark();
		VariableDeclaration varDeclaration = null;

		Type type = Type.parse(tokens);

		if (type != null) {
			Token id = tokens.read();
			if (id instanceof IdentifierToken) {
				Expression init = parseInitializer(tokens);
				if (tokens.read().toString().equals(";"))
					varDeclaration = new VariableDeclaration(type, id.toString(), init, line, column);
			}
		}

		tokens.popMark(varDeclaration == null);
		return varDeclaration;
	}

	private static Expression parseInitializer(TokenStream tokens)
	{
		tokens.pushMark();
		Expression init = null;

		if (tokens.read().toString().equals("="))
			init = Expression.parse(tokens);

		tokens.popMark(init == null);
		return init;
	}
}
