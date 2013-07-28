package il.ac.technion.cs.ssdl.spartan.builder;

import il.ac.technion.cs.ssdl.spartan.refactoring.All;
import il.ac.technion.cs.ssdl.spartan.refactoring.BaseSpartanization;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator;

/**
 * a quickfix generator for spartanization refactoring
 * 
 * @author Boris van Sosin <code><boris.van.sosin [at] gmail.com></code>
 * @since 2013/07/01
 */
public class QuickFixer implements IMarkerResolutionGenerator {
  @Override public IMarkerResolution[] getResolutions(final IMarker m) {
    try {
      final BaseSpartanization s = All.get((String) m.getAttribute(Builder.SPARTANIZATION_TYPE_KEY));
      return new IMarkerResolution[] { s.getFix(), s.getFixWithPreview() };
    } catch (final CoreException _) {
      return new IMarkerResolution[] {};
    }
  }
}
