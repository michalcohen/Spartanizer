package il.ac.technion.cs.ssdl.spartan.refactoring;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class SpartanizationFactory {

	public static void initialize() {
		final BasicSpartanization redundantEquality = new BasicSpartanization(new RedundantEqualityRefactoring(), "Redundant Equality", "Convert reduntant comparison to boolean constant");
		spartanizations.put(redundantEquality.toString(),
				redundantEquality);
		final BasicSpartanization convertToTernary = new BasicSpartanization(new ConvertToTernaryRefactoring(), "Convert to Ternary", "Convert condition to ternary expression");
		spartanizations.put(convertToTernary.toString(),
				convertToTernary);
		final BasicSpartanization shortestBranchFirst = new BasicSpartanization(new ShortestBranchRefactoring(), "Shortest Branch", "Shortest branch in condition first");
		spartanizations.put(shortestBranchFirst.toString(),
				shortestBranchFirst);
		final BasicSpartanization inlineSinlgeUse = new BasicSpartanization(new InlineSingleUseRefactoring(), "Inline Single Use", "Inline single use of variable");
		spartanizations.put(inlineSinlgeUse.toString(),
				inlineSinlgeUse);
		final BasicSpartanization forwardDeclaration = new BasicSpartanization(new ForwardDeclarationRefactoring(), "Forward Declaration", "Forward declaration of variable to first use");
		spartanizations.put(forwardDeclaration.toString(),
				forwardDeclaration);
	}

	public static BasicSpartanization getSpartanizationByName(
			final String SpartanizationName) {
		return spartanizations.get(SpartanizationName);
	}

	public static Collection<BasicSpartanization> getAllSpartanizations() {
		return spartanizations.values();
	}

	private static final Map<String, BasicSpartanization> spartanizations = new HashMap<>();
}
