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
  private static final Map<String, BaseRefactoring> all = new HashMap<String, BaseRefactoring>();
  
  private static void put(final BaseRefactoring s) {
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
   * @param SpartanizationName
   *          the name of the spartanization
   * @return an instance of the spartanization
   */
  public static BaseRefactoring getSpartanizationByName(final String SpartanizationName) {
    return all.get(SpartanizationName);
  }
  
  /**
   * @return all the registered spartanization refactoring objects
   */
  public static Iterable<BaseRefactoring> all() {
    return all.values();
  }
}
