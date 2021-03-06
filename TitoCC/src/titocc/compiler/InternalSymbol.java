package titocc.compiler;

import titocc.compiler.types.CType;

/**
 * Class for symbols that are only used internally by the compiler, like code
 * positions inside functions etc.
 */
public class InternalSymbol implements Symbol
{
	/**
	 * Name of the symbol, including prefix "__".
	 */
	private final String name;
	/**
	 * Globally unique name.
	 */
	private final String globallyUniqueName;
	/**
	 * Suffix to add to the reference. (for example "(fp)")
	 */
	private final String referenceSuffix;
	/**
	 * Type of the symbol.
	 */
	private final CType type;

	/**
	 * Constructs a new internal symbol. The actual name of the symbol is
	 * prefixed with double underscore, because C standard reserves those
	 * identifiers for implementation.
	 *
	 * @param name name of the symbol
	 * @param scope scope this symbol belongs to
	 * @param referenceSuffix suffix that is added to global name to get the
	 * reference ("(fp)" can be used for stack frame variables)
	 * @param type type of the symbol
	 */
	public InternalSymbol(String name, Scope scope, String referenceSuffix, CType type)
	{
		this.name = "__" + name;
		this.globallyUniqueName = scope.makeGloballyUniqueName(name);
		this.referenceSuffix = referenceSuffix;
		this.type = type;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public String getGlobalName()
	{
		return globallyUniqueName;
	}

	@Override
	public String getReference()
	{
		return globallyUniqueName + referenceSuffix;
	}

	@Override
	public CType getType()
	{
		return type;
	}
}
