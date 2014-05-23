package il.ac.technion.cs.ssdl.spartan.commandhandlers;

import il.ac.technion.cs.ssdl.spartan.refactoring.ShortestBranch;

/**
 * a handler for {@link ShortestBranch}
 * 
 * @author Boris van Sosin <code><boris.van.sosin [at] gmail.com></code>
 * @author Yossi Gil <code><yossi.gil [at] gmail.com></code> (major refactoring
 *         2013/07/11)
 * @since 2013/07/011
 */
public class ShortestBranchFirstHandler extends BaseHandler {
	/** Instantiates this class */
	public ShortestBranchFirstHandler() {
		super(new ShortestBranch());
	}
}
