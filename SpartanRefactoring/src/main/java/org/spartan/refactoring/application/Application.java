package org.spartan.refactoring.application;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

/**
 * An {@link IApplication} extension entry point, allowing execution of this
 * plug-in from a terminal
 *
 * @author Daniel Mittelman <code><mittelmania [at] gmail.com></code>
 * @since 2015/09/19
 */
public class Application implements IApplication {
  @Override public Object start(final IApplicationContext arg0) throws Exception {
    final BufferedReader inpReader = new BufferedReader(new InputStreamReader(System.in));
    String line;
    while ((line = inpReader.readLine()) != null) {
      // Echo the input until "exit" (placeholder code)
      System.out.println(line);
      if (line.equals("exit"))
        break;
    }
    return IApplication.EXIT_OK;
  }
  @Override public void stop() {
    // TODO Auto-generated method stub
  }
}
