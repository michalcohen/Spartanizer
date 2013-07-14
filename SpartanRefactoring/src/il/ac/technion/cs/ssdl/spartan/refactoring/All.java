package il.ac.technion.cs.ssdl.spartan.refactoring;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Boris van Sosin
 * 
 * @since 2013/07/01
 */
public enum All {
  ;
  private static final Map<String, SpartanRefactoring> all = new HashMap<String, SpartanRefactoring>();
  
  private static void put(final SpartanRefactoring s) {
    all.put(s.toString(), s);
  }
  
  static {
    put(new RedundantEqualityRefactoring());
    put(new ConvertToTernaryRefactoring());
    put(new ShortestBranchRefactoring());
    put(new InlineSingleUseRefactoring());
    put(new ForwardDeclarationRefactoring());
    put(new ChangeReturnToDollarRefactoring());
  }
  
  /**
   * @param name
   *          the name of the spartanization
   * @return an instance of the spartanization
   */
  public static SpartanRefactoring get(final String name) {
    return all.get(name);
  }
  
  /**
   * @return all the registered spartanization refactoring objects
   */
  public static Iterable<SpartanRefactoring> all() {
    return all.values();
  }
}
