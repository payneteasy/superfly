package com.payneteasy.superfly.aop;

import junit.framework.TestCase;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.SourceLocation;
import org.aspectj.runtime.internal.AroundClosure;

public class ExceptionConversionAspectTest extends TestCase {
	
	private ExceptionConversionAspect aspect;
	
	public void setUp() {
		aspect = new ExceptionConversionAspect();
	}
	
	public void testNoNonconvertibles() {
		// first, no non-convertible classes
		try {
			aspect.invoke(new ProceedingJoinPointAdapter() {
				@Override
				public Object proceed() throws Throwable {
					throw new ConvertibleException();
				}
			});
		} catch (Throwable e) {
			assertOnlyRuntimeExceptions(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void testMissNonconvertibles() {
		// now, no-convertible classes exist, but we don't hit them
		aspect.setNonConvertibleClasses(new Class[]{NonConvertibleException.class});
		try {
			aspect.invoke(new ProceedingJoinPointAdapter() {
				@Override
				public Object proceed() throws Throwable {
					throw new ConvertibleException();
				}
			});
		} catch (Throwable e) {
			assertOnlyRuntimeExceptions(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void testConvertion() {
		// now, no-convertible classes exist, but we don't hit them
		aspect.setNonConvertibleClasses(new Class[]{NonConvertibleException.class});
		try {
			aspect.invoke(new ProceedingJoinPointAdapter() {
				@Override
				public Object proceed() throws Throwable {
					throw new ConvertibleException();
				}
			});
		} catch (Throwable e) {
			assertOnlyRuntimeExceptions(e);
		}
		
		// now, non-convertible is thrown
		try {
			aspect.invoke(new ProceedingJoinPointAdapter() {
				@Override
				public Object proceed() throws Throwable {
					throw new SubNonConvertibleException();
				}
			});
		} catch (Throwable e) {
			assertEquals(SubNonConvertibleException.class, e.getClass());
		}
		
		// now, non-convertible is thrown
		try {
			aspect.invoke(new ProceedingJoinPointAdapter() {
				@Override
				public Object proceed() throws Throwable {
					throw new SubNonConvertibleException(new NonConvertibleException());
				}
			});
		} catch (Throwable e) {
			assertEquals(SubNonConvertibleException.class, e.getClass());
			assertOnlyRuntimeExceptions(e.getCause());
		}
	}

	protected void assertOnlyRuntimeExceptions(Throwable e) {
		while (e != null) {
			assertEquals(RuntimeException.class, e.getClass());
			e = e.getCause();
		}
	}

	private abstract static class ProceedingJoinPointAdapter implements
			ProceedingJoinPoint {
		public String toShortString() {
			return null;
		}

		public String toLongString() {
			return null;
		}

		public Object getThis() {
			return null;
		}

		public Object getTarget() {
			return null;
		}

		public StaticPart getStaticPart() {
			return null;
		}

		public SourceLocation getSourceLocation() {
			return null;
		}

		public Signature getSignature() {
			return null;
		}

		public String getKind() {
			return null;
		}

		public Object[] getArgs() {
			return null;
		}

		public void set$AroundClosure(AroundClosure arc) {
		}

		public Object proceed(Object[] args) throws Throwable {
			return null;
		}

		public Object proceed() throws Throwable {
			return null;
		}
	}
	
	public static class ConvertibleException extends Exception {
	}

	public static class NonConvertibleException extends Exception {
		public NonConvertibleException() {
			super();
		}

		public NonConvertibleException(String message, Throwable cause) {
			super(message, cause);
		}

		public NonConvertibleException(String message) {
			super(message);
		}

		public NonConvertibleException(Throwable cause) {
			super(cause);
		}
	}

	public static class SubNonConvertibleException extends NonConvertibleException {
		public SubNonConvertibleException() {
			super();
		}

		public SubNonConvertibleException(String message, Throwable cause) {
			super(message, cause);
		}

		public SubNonConvertibleException(String message) {
			super(message);
		}

		public SubNonConvertibleException(Throwable cause) {
			super(cause);
		}
	}

}
