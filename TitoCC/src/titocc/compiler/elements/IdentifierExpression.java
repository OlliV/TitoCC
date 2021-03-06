package titocc.compiler.elements;

import java.io.IOException;
import titocc.compiler.Assembler;
import titocc.compiler.Lvalue;
import titocc.compiler.Registers;
import titocc.compiler.Scope;
import titocc.compiler.Symbol;
import titocc.compiler.types.ArrayType;
import titocc.compiler.types.CType;
import titocc.compiler.types.FunctionType;
import titocc.tokenizer.IdentifierToken;
import titocc.tokenizer.SyntaxException;
import titocc.tokenizer.Token;
import titocc.tokenizer.TokenStream;

/**
 * Expression that evaluates a named variable, parameter or function.
 *
 * <p> EBNF definition:
 *
 * <br> IDENTIFIER_EXPRESSION = IDENTIFIER
 */
public class IdentifierExpression extends Expression
{
	private String identifier;

	/**
	 * Construcs an IdentifierExpression.
	 *
	 * @param identifier name of the object or function
	 * @param line starting line number of the identifier expression
	 * @param column starting column/character of the identifier expression
	 */
	public IdentifierExpression(String identifier, int line, int column)
	{
		super(line, column);
		this.identifier = identifier;
	}

	/**
	 * Returns the identifier.
	 *
	 * @return the identifier
	 */
	public String getIdentifier()
	{
		return identifier;
	}

	@Override
	public void compile(Assembler asm, Scope scope, Registers regs)
			throws SyntaxException, IOException
	{
		Symbol symbol = findSymbol(scope);
		if (!symbol.getType().isObject())
			throw new SyntaxException("Identifier \"" + identifier + "\" is not an object.", getLine(), getColumn());

		// Load value to first register (or address if we have an array).
		if (symbol.getType() instanceof ArrayType)
			asm.emit("load", regs.get(0).toString(), "=" + symbol.getReference());
		else
			asm.emit("load", regs.get(0).toString(), symbol.getReference());
	}

	@Override
	public Lvalue compileAsLvalue(Assembler asm, Scope scope, Registers regs)
			throws SyntaxException, IOException
	{
		Symbol symbol = findSymbol(scope);
		if (!symbol.getType().isObject())
			throw new SyntaxException("Identifier \"" + identifier + "\" is not an object.", getLine(), getColumn());

		return new Lvalue(regs.get(0), symbol.getReference());
	}

	@Override
	public Function getFunction(Scope scope) throws SyntaxException
	{
		Symbol symbol = findSymbol(scope);
		if (!(symbol.getType() instanceof FunctionType))
			throw new SyntaxException("Identifier \"" + identifier + "\" is not a function.", getLine(), getColumn());

		return (Function) symbol;
	}

	@Override
	public CType getType(Scope scope) throws SyntaxException
	{
		return findSymbol(scope).getType();
	}

	@Override
	public String toString()
	{
		return "(ID_EXPR " + identifier + ")";
	}

	private Symbol findSymbol(Scope scope) throws SyntaxException
	{
		Symbol symbol = scope.find(identifier);
		if (symbol == null)
			throw new SyntaxException("Undeclared identifier \"" + identifier + "\".", getLine(), getColumn());
		return symbol;
	}

	/**
	 * Attempts to parse an identifier expression from token stream. If parsing
	 * fails the stream is reset to its initial position.
	 *
	 * @param tokens source token stream
	 * @return Expression object or null if tokens don't form a valid expression
	 */
	public static IdentifierExpression parse(TokenStream tokens)
	{
		int line = tokens.getLine(), column = tokens.getColumn();
		tokens.pushMark();
		IdentifierExpression idExpr = null;

		Token token = tokens.read();
		if (token instanceof IdentifierToken)
			idExpr = new IdentifierExpression(token.toString(), line, column);

		tokens.popMark(idExpr == null);
		return idExpr;
	}
}
