package titocc.compiler.types;

import java.util.ArrayList;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class InvalidTypeTest
{
	private CType t;

	@Before
	public void setUp()
	{
		t = new InvalidType();
	}

	@Test
	public void hasRightProperties()
	{
		assertFalse(t.isObject());
		assertFalse(t.isScalar());
		assertFalse(t.isPointer());
		assertFalse(t.isArithmetic());
		assertFalse(t.isInteger());
		assertFalse(t.isValid());
	}

	@Test
	public void sizeIsCorrect()
	{
		assertEquals(0, t.getSize());
	}

	@Test
	public void incrementSizeIsCorrect()
	{
		assertEquals(0, t.getIncrementSize());
	}

	@Test
	public void dereferenceReturnsCorrectType()
	{
		assertTrue(t.dereference() instanceof InvalidType);
	}

	@Test
	public void decayReturnsCorrectType()
	{
		assertTrue(t.decay() instanceof InvalidType);
	}

	@Test
	public void equalsWorksCorrectly()
	{
		assertFalse(t.equals(new ArrayType(new ArrayType(new IntType(), 6), 7)));
		assertFalse(t.equals(new IntType()));
		assertFalse(t.equals(new VoidType()));
		assertFalse(t.equals(new PointerType(new ArrayType(new IntType(), 6))));
		assertFalse(t.equals(new FunctionType(new VoidType(), new ArrayList<CType>())));
		assertFalse(t.equals(new InvalidType()));
	}
}
