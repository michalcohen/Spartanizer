package il.ac.technion.cs.ssdl.spartan.utils;

import org.eclipse.jdt.core.dom.ASTNode;

/**
 * a range which contains a spartanization suggestion. used for creating text
 * markers
 * 
 * @author Boris van Sosin <code><boris.van.sosin [at] gmail.com></code>
 */
public class Range {
	/** the beginning of the range (inclusive) */
	public final int from;
	/** the end of the range (exclusive) */
	public final int to;
	/**
	 * Instantiates from beginning and end locations
	 * 
	 * @param from
	 *          the beginning of the range (inclusive)
	 * @param to
	 *          the end of the range (exclusive)
	 */
	public Range(final int from, final int to) {
		this.from = from;
		this.to = to;
	}
	/**
	 * Instantiates from a single ASTNode
	 * 
	 * @param n
	 *          an arbitrary ASTNode
	 */
	public Range(final ASTNode n) {
		this(n.getStartPosition(), n.getStartPosition() + n.getLength());
	}
	/**
	 * Instantiates from beginning and end ASTNodes
	 * 
	 * @param from
	 *          the beginning ASTNode (inclusive)
	 * @param to
	 *          the end ASTNode (inclusive)
	 */
	public Range(final ASTNode from, final ASTNode to) {
		this(from.getStartPosition(), to.getStartPosition() + to.getLength());
	}
}