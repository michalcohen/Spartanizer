package il.ac.technion.cs.ssdl.spartan.builder;

import il.ac.technion.cs.ssdl.spartan.refactoring.SpartanizationFactory;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator;

/**
 * @author Boris van Sosin a quickfix generator for spartanization refactoring
 */
public class SpartanizationQuickfixer implements IMarkerResolutionGenerator {
  @Override public IMarkerResolution[] getResolutions(final IMarker arg0) {
    try {
      final String spartanizationName = (String) arg0.getAttribute(SpartaBuilder.SPARTANIZATION_TYPE_KEY);
      return new IMarkerResolution[] { SpartanizationFactory.getSpartanizationByName(spartanizationName).getFix(),
          SpartanizationFactory.getSpartanizationByName(spartanizationName).getFixWithPreview() };
    } catch (CoreException _) {
      return new IMarkerResolution[] {};
    }
  }
}
