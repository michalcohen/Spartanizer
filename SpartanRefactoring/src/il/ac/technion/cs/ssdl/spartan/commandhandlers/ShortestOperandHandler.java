package il.ac.technion.cs.ssdl.spartan.commandhandlers;

import il.ac.technion.cs.ssdl.spartan.refactoring.ShortestOperand;

/**
 * a handler for {@link ShortestOperand}
 * 
 * @author Boris van Sosin <code><boris.van.sosin [at] gmail.com></code>
 * @author Yossi Gil <code><yossi.gil [at] gmail.com></code> (major refactoring
 *         2013/07/11)
 * @since 2013/07/011
 */
public class ShortestOperandHandler extends BaseHandler {
	/** Instantiates this class */
	public ShortestOperandHandler() {
		super(new ShortestOperand());
	}
}
