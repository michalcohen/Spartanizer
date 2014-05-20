package il.ac.technion.cs.ssdl.spartan.refactoring;

public enum OpCode {
	PLUS, TIMES, GT {
		@Override OpCode invert() {
			return LT;
		}
	},
	LT {
		@Override OpCode invert() {
			return GT;
		}
	};
	OpCode invert() {
		return this;
	}
}
