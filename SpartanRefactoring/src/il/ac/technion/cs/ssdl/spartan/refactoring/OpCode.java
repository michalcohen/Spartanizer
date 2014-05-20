package il.ac.technion.cs.ssdl.spartan.refactoring;

public enum OpCode {
	PLUS, GT {
		@Override OpCode invert() {
			return LT;
		}
	},
	LT {
		@Override OpCode invert() {
			return GT;
		}
	}, TIMES;
	OpCode invert() {
		return this;
	}
}
