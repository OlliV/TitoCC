package titocc.compiler.elements;

import java.io.IOException;
import java.util.Arrays;
import java.util.Stack;
import titocc.compiler.Assembler;
import titocc.compiler.InternalCompilerException;
import titocc.compiler.Register;
import titocc.compiler.Scope;
import titocc.tokenizer.SyntaxException;
import titocc.tokenizer.TokenStream;

/**
 * Expression formed by a binary operator and two operands.
 */
public class BinaryExpression extends Expression
{
	static final String[][] binaryOperators = {
		{"||"},
		{"&&"},
		{"|"},
		{"^"},
		{"&"},
		{"=="},
		{"!="},
		{"<", "<=", ">", ">="},
		{"<<", ">>"},
		{"+", "-"},
		{"*", "/", "%"}
	};
	private String operator;
	private Expression left, right;

	public BinaryExpression(String operator, Expression left, Expression right,
			int line, int column)
	{
		super(line, column);
		this.operator = operator;
		this.left = left;
		this.right = right;
	}

	public String getOperator()
	{
		return operator;
	}

	public Expression getLeft()
	{
		return left;
	}

	public Expression getRight()
	{
		return right;
	}

	@Override
	public void compile(Assembler asm, Scope scope, Stack<Register> registers)
			throws IOException, SyntaxException
	{
		Register pushedRegister = pushRegister(asm, registers);

		// Evaluate left expression and store it in the first available register.
		left.compile(asm, scope, registers);
		Register leftRegister = registers.pop();

		// Evaluate right expression and store it in the next register.
		// With logical or/and don't evaluate yet because of the short circuit
		// Evaluation.
		if(!operator.equals("||") && !operator.equals("&&"))
			right.compile(asm, scope, registers);

		// Evaluate the operation and store the result in the left register.
		compileOperator(asm, scope, registers, leftRegister, registers.peek());

		registers.push(leftRegister);

		// Pop registers.
		popRegister(asm, registers, pushedRegister);
	}

	private void compileOperator(Assembler asm, Scope scope, Stack<Register> registers,
			Register leftReg, Register rightReg) throws IOException, SyntaxException
	{
		String jumpLabel, jumpLabel2;
		switch (operator) {
			case "||":
				// Short circuit evaluation; only evaluate RHS if LHS is false.
				jumpLabel = scope.makeGloballyUniqueName("lbl");
				jumpLabel2 = scope.makeGloballyUniqueName("lbl");
				asm.emit("jnzer", leftReg.toString(), jumpLabel);
				right.compile(asm, scope, registers);
				asm.emit("jnzer", rightReg.toString(), jumpLabel);
				asm.emit("load", leftReg.toString(), "=0");
				asm.emit("jump", leftReg.toString(), jumpLabel2);
				asm.addLabel(jumpLabel);
				asm.emit("load", leftReg.toString(), "=1");
				asm.addLabel(jumpLabel2);
				break;
			case "&&":
				// Short circuit evaluation; only evaluate RHS if LHS is true.
				jumpLabel = scope.makeGloballyUniqueName("lbl");
				jumpLabel2 = scope.makeGloballyUniqueName("lbl");
				asm.emit("jzer", leftReg.toString(), jumpLabel);
				right.compile(asm, scope, registers);
				asm.emit("jzer", rightReg.toString(), jumpLabel);
				asm.emit("load", leftReg.toString(), "=1");
				asm.emit("jump", leftReg.toString(), jumpLabel2);
				asm.addLabel(jumpLabel);
				asm.emit("load", leftReg.toString(), "=0");
				asm.addLabel(jumpLabel2);
				break;
			case "|":
				asm.emit("or", leftReg.toString(), rightReg.toString());
				break;
			case "^":
				asm.emit("xor", leftReg.toString(), rightReg.toString());
				break;
			case "&":
				asm.emit("and", leftReg.toString(), rightReg.toString());
				break;
			case "==":
				jumpLabel = scope.makeGloballyUniqueName("lbl");
				asm.emit("comp", leftReg.toString(), rightReg.toString());
				asm.emit("load", leftReg.toString(), "=1");
				asm.emit("jequ", leftReg.toString(), jumpLabel);
				asm.emit("load", leftReg.toString(), "=0");
				asm.addLabel(jumpLabel);
				break;
			case "!=":
				jumpLabel = scope.makeGloballyUniqueName("lbl");
				asm.emit("comp", leftReg.toString(), rightReg.toString());
				asm.emit("load", leftReg.toString(), "=1");
				asm.emit("jnequ", leftReg.toString(), jumpLabel);
				asm.emit("load", leftReg.toString(), "=0");
				asm.addLabel(jumpLabel);
				break;
			case "<":
				jumpLabel = scope.makeGloballyUniqueName("lbl");
				asm.emit("comp", leftReg.toString(), rightReg.toString());
				asm.emit("load", leftReg.toString(), "=1");
				asm.emit("jles", leftReg.toString(), jumpLabel);
				asm.emit("load", leftReg.toString(), "=0");
				asm.addLabel(jumpLabel);
				break;
			case "<=":
				jumpLabel = scope.makeGloballyUniqueName("lbl");
				asm.emit("comp", leftReg.toString(), rightReg.toString());
				asm.emit("load", leftReg.toString(), "=1");
				asm.emit("jngre", leftReg.toString(), jumpLabel);
				asm.emit("load", leftReg.toString(), "=0");
				asm.addLabel(jumpLabel);
				break;
			case ">":
				jumpLabel = scope.makeGloballyUniqueName("lbl");
				asm.emit("comp", leftReg.toString(), rightReg.toString());
				asm.emit("load", leftReg.toString(), "=1");
				asm.emit("jgre", leftReg.toString(), jumpLabel);
				asm.emit("load", leftReg.toString(), "=0");
				asm.addLabel(jumpLabel);
				break;
			case ">=":
				jumpLabel = scope.makeGloballyUniqueName("lbl");
				asm.emit("comp", leftReg.toString(), rightReg.toString());
				asm.emit("load", leftReg.toString(), "=1");
				asm.emit("jnles", leftReg.toString(), jumpLabel);
				asm.emit("load", leftReg.toString(), "=0");
				asm.addLabel(jumpLabel);
				break;
			case "<<":
				asm.emit("shl", leftReg.toString(), rightReg.toString());
				break;
			case ">>":
				asm.emit("shr", leftReg.toString(), rightReg.toString());
				break;
			case "+":
				asm.emit("add", leftReg.toString(), rightReg.toString());
				break;
			case "-":
				asm.emit("sub", leftReg.toString(), rightReg.toString());
				break;
			case "*":
				asm.emit("mul", leftReg.toString(), rightReg.toString());
				break;
			case "/":
				asm.emit("div", leftReg.toString(), rightReg.toString());
				break;
			case "%":
				asm.emit("mod", leftReg.toString(), rightReg.toString());
				break;
			default:
				throw new InternalCompilerException("Invalid operator in BinaryExpression.");
		}
	}

	@Override
	public Integer getCompileTimeValue()
	{
		// Compile time evaluation of binary operators could be implemented here.

		return null;
	}

	@Override
	public String toString()
	{
		return "(BIN_EXPR " + operator + " " + left + " " + right + ")";
	}

	public static Expression parse(TokenStream tokens)
	{
		return parseImpl(tokens, 0);
	}

	private static Expression parseImpl(TokenStream tokens, int priority)
	{
		if (priority == binaryOperators.length)
			return PrefixExpression.parse(tokens);

		int line = tokens.getLine(), column = tokens.getColumn();
		tokens.pushMark();
		Expression expr = parseImpl(tokens, priority + 1);

		if (expr != null)
			while (true) {
				tokens.pushMark();
				Expression right = null;
				String op = tokens.read().toString();
				if (Arrays.asList(binaryOperators[priority]).contains(op))
					right = parseImpl(tokens, priority + 1);

				tokens.popMark(right == null);
				if (right != null)
					expr = new BinaryExpression(op, expr, right, line, column);
				else
					break;
			}

		tokens.popMark(expr == null);
		return expr;
	}
}
