package il.ac.technion.cs.ssdl.spartan.refactoring;

/**
 * Demo. To be erased
 *
 */
public enum OpCode {
	/**
	 * Demo. To be erased
	 */
	PLUS, /**
	 * Demo. To be erased
	 */
	TIMES, /**
	 * Demo. To be erased
	 */
	GT {
		@Override OpCode invert() {
			return LT;
		}
	},
	/**
	 * 
	 */
	LT {
		@Override OpCode invert() {
			return GT;
		}
	};
	OpCode invert() {
		return this;
	}
}
