package il.ac.technion.cs.ssdl.spartan.refactoring;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Boris van Sosin
 * 
 * @since 2013/07/01
 */
public enum SpartanizationFactory {
  ;
  private static final Map<String, BasicSpartanization> all = new HashMap<String, BasicSpartanization>();
  
  private static void put(final BasicSpartanization s) {
    all.put(s.toString(), s);
  }
  
  static {
    put(new BasicSpartanization(new RedundantEqualityRefactoring(), "Redundant Equality",
        "Convert reduntant comparison to boolean constant"));
    put(new BasicSpartanization(new ConvertToTernaryRefactoring(), "Convert to Ternary", "Convert condition to ternary expression"));
    put(new BasicSpartanization(new ShortestBranchRefactoring(), "Shortest Branch", "Shortest branch in condition first"));
    put(new BasicSpartanization(new InlineSingleUseRefactoring(), "Inline Single Use", "Inline single use of variable"));
    put(new BasicSpartanization(new ForwardDeclarationRefactoring(), "Forward Declaration",
        "Forward declaration of variable to first use"));
    put(new BasicSpartanization(new ChangeReturnToDollarRefactoring(), "Change Return Variable to $", "Change return variable to $"));
  }
  
  /**
   * @param SpartanizationName
   *          the name of the spartanization
   * @return an instance of the spartanization
   */
  public static BasicSpartanization getSpartanizationByName(final String SpartanizationName) {
    return all.get(SpartanizationName);
  }
  
  /**
   * @return all the registered spartanization refactoring objects
   */
  public static Iterable<BasicSpartanization> all() {
    return all.values();
  }
}
