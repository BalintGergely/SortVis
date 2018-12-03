package g.sort.vis;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.tree.TreeNode;
/**
 * A basic thread safe TreeNode implementation
 * 
 * @param <E>
 */
public class Node<E> implements TreeNode,Iterable<Node<E>>{
	public final Node<? super E> parent;
	public final E element;
	private final Vector<Node<E>> children;
	public Node(E element){
		this.parent = null;
		this.element = element;
		children = new Vector<>();
	}
	private Node(Node<? super E> pr,E element){
		this.parent = pr;
		this.element = element;
		children = new Vector<>();
	}
	@SuppressWarnings("unchecked")
	public <A extends E> Node<A> createChildAt(int index,A element){
		Node<A> n = new Node<>(this,element);
		children.add(index, (Node<E>)n);
		return n;
	}
	@SuppressWarnings("unchecked")
	public <A extends E> Node<A> createChild(A element){
		Node<A> n = new Node<>(this,element);
		children.add((Node<E>)n);
		return n;
	}
	@Override
	public Node<E> getChildAt(int childIndex) {
		return children.get(childIndex);
	}
	@Override
	public int getChildCount() {
		return children.size();
	}
	@Override
	public TreeNode getParent() {
		return parent;
	}
	@Override
	public int getIndex(TreeNode node) {
		return children.indexOf(node);
	}
	@Override
	public boolean getAllowsChildren() {
		return true;
	}
	@Override
	public boolean isLeaf() {
		return children.isEmpty();
	}
	@Override
	public Enumeration<? extends TreeNode> children() {
		return Collections.enumeration(children);
	}
	public String toString(){
		return element.toString();
	}
	public static class NodeIterator<E> implements Iterator<Node<E>>{
		Node<E> node;
		private Iterator<Node<E>> itr;
		private NodeIterator<E> sub;
		int index;
		public NodeIterator(Node<E> nd){
			itr = nd.children.iterator();
		}
		@Override
		public boolean hasNext() {
			if(sub == null || !sub.hasNext()){
				return itr.hasNext();
			}
			return sub.hasNext();
		}
		@Override
		public Node<E> next() {
			if(sub == null || !sub.hasNext()){
				Node<E> n = itr.next();
				sub = n.children.isEmpty() ? null : new NodeIterator<>(n);
				return n;
			}
			return sub.next();
		}
	}
	@Override
	public NodeIterator<E> iterator() {
		return new NodeIterator<E>(this);
	}
}
