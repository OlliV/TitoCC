package titocc.compiler.elements;

import java.io.IOException;
import java.util.Stack;
import titocc.compiler.Assembler;
import titocc.compiler.Register;
import titocc.compiler.Scope;
import titocc.tokenizer.SyntaxException;
import titocc.tokenizer.TokenStream;

/**
 * Function call expression.
 *
 * <p> EBNF definition:
 *
 * <br> FUNCTION_CALL_EXPRESSION = POSTFIX_EXPRESSION ARGUMENT_LIST
 */
public class FunctionCallExpression extends Expression
{
	private Expression function;
	private ArgumentList argumentList;

	/**
	 * Constructs a function call expression.
	 *
	 * @param function expression that will be evaluated as the function
	 * @param argumentList list of arguments passed to the function
	 * @param line starting line number of the function call expression
	 * @param column starting column/character of the function call expression
	 */
	public FunctionCallExpression(Expression function, ArgumentList argumentList,
			int line, int column)
	{
		super(line, column);
		this.function = function;
		this.argumentList = argumentList;
	}

	/**
	 * Returns the function expression.
	 *
	 * @return the function
	 */
	public Expression getFunction()
	{
		return function;
	}

	/**
	 * Returns the argument list.
	 *
	 * @return the argument list
	 */
	public ArgumentList getArgumentList()
	{
		return argumentList;
	}

	@Override
	public void compile(Assembler asm, Scope scope, Stack<Register> registers)
			throws SyntaxException, IOException
	{
		compile(asm, scope, registers, true);
	}

	@Override
	public void compileAsVoid(Assembler asm, Scope scope, Stack<Register> registers)
			throws SyntaxException, IOException
	{
		compile(asm, scope, registers, false);
	}

	private void compile(Assembler asm, Scope scope, Stack<Register> registers,
			boolean returnValueRequired) throws SyntaxException, IOException
	{
		Function func = validateFunction(scope);

		if (func.getReturnType().getName().equals("void")) {
			if (returnValueRequired)
				throw new SyntaxException("Void return value used in an expression.", getLine(), getColumn());
		} else {
			// Reserve space for return value.
			asm.emit("add", "sp", "=1");
		}

		// Push arguments to stack.
		argumentList.compile(asm, scope, registers);

		// Make the call.
		asm.emit("call", "sp", func.getReference());

		// Read the return value.
		if (!func.getReturnType().getName().equals("void"))
			asm.emit("pop", "sp", registers.peek().toString());
	}

	private Function validateFunction(Scope scope) throws SyntaxException
	{
		Function func = function.getFunction(scope);
		if (func == null)
			throw new SyntaxException("Expression is not a function.", getLine(), getColumn());
		if (func.getParameterCount() != argumentList.getArguments().size())
			throw new SyntaxException("Number of arguments doesn't match the number of parameters.", getLine(), getColumn());
		return func;
	}

	@Override
	public String toString()
	{
		return "(FCALL_EXPR " + function + " " + argumentList + ")";
	}

	/**
	 * Attempts to parse a function call expression from token stream, given the
	 * first operand of the expression. If parsing fails the stream is reset to
	 * its initial position.
	 *
	 * @param firstOperand preparsed function expression
	 * @param tokens source token stream
	 * @return FunctionCallExpression object or null if tokens don't form a
	 * valid function call expression
	 */
	public static FunctionCallExpression parse(Expression firstOperand, TokenStream tokens)
	{
		FunctionCallExpression expr = null;

		ArgumentList argList = ArgumentList.parse(tokens);
		if (argList != null) {
			expr = new FunctionCallExpression(firstOperand, argList,
					firstOperand.getLine(), firstOperand.getColumn());
		}

		return expr;
	}
}
