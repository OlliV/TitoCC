package titocc.compiler.elements;

import titocc.tokenizer.TokenStream;

/**
 * Abstract placeholder class for parsing primary expressions.
 *
 * <p> EBNF definition:
 *
 * <br> PRIMARY_EXPRESSION = IDENTIFIER_EXPRESSION | INTEGER_LITERAL_EXPRESSION
 * | "(" EXPRESSION ")"
 */
public abstract class PrimaryExpression extends Expression
{
	/**
	 * Not used.
	 */
	private PrimaryExpression(int line, int column)
	{
		super(line, column);
	}

	/**
	 * Attempts to parse a syntactic primary expression from token stream. If
	 * parsing fails the stream is reset to its initial position.
	 *
	 * @param tokens source token stream
	 * @return Expression object or null if tokens don't form a valid expression
	 */
	public static Expression parse(TokenStream tokens)
	{
		int line = tokens.getLine(), column = tokens.getColumn();
		tokens.pushMark();

		Expression expr = IdentifierExpression.parse(tokens);

		if (expr == null)
			expr = IntegerLiteralExpression.parse(tokens);

		if (expr == null) {
			if (tokens.read().toString().equals("(")) {
				expr = Expression.parse(tokens);
				if (expr != null && !tokens.read().toString().equals(")"))
					expr = null;
			}
		}

		tokens.popMark(expr == null);
		return expr;
	}
}
