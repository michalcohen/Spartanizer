package il.ac.technion.cs.ssdl.spartan.builder;

import il.ac.technion.cs.ssdl.spartan.refactoring.SpartanizationFactory;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator;

/**
 * a quickfix generator for spartanization refactoring
 * 
 * @author Boris van Sosin <boris.van.sosin@gmail.com>
 * @since 2013/07/01
 */
public class SpartanizationQuickfixer implements IMarkerResolutionGenerator {
  @Override public IMarkerResolution[] getResolutions(final IMarker m) {
    try {
      final String spartanizationName = (String) m.getAttribute(SpartaBuilder.SPARTANIZATION_TYPE_KEY);
      return new IMarkerResolution[] { SpartanizationFactory.getSpartanizationByName(spartanizationName).getFix(),
          SpartanizationFactory.getSpartanizationByName(spartanizationName).getFixWithPreview() };
    } catch (final CoreException _) {
      return new IMarkerResolution[] {};
    }
  }
}
