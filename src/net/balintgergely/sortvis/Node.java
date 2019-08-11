package net.balintgergely.sortvis;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.tree.TreeNode;
/**
 * A basic thread safe TreeNode implementation
 * 
 * @param <E> The element type of this node.
 */
public class Node<E> implements TreeNode,Iterable<Node<?>>{
	public final Node<?> parent;
	public final E element;
	private final Vector<Node<?>> children;
	public Node(E element){
		this.parent = null;
		this.element = element;
		children = new Vector<>();
	}
	private Node(Node<?> pr,E element){
		this.parent = pr;
		this.element = element;
		children = new Vector<>();
	}
	public <A> Node<A> createChildAt(int index,A element1){
		Node<A> n = new Node<>(this,element1);
		children.add(index, n);
		return n;
	}
	public <A> Node<A> createChild(A element1){
		Node<A> n = new Node<>(this,element1);
		children.add(n);
		return n;
	}
	@Override
	public Node<?> getChildAt(int childIndex) {
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
	public Enumeration<? extends Node<?>> children() {
		return Collections.enumeration(children);
	}
	@Override
	public String toString(){
		return element.toString();
	}
	public static class NodeIterator implements Iterator<Node<?>>{
		Node<?> node;
		private Iterator<Node<?>> itr;
		private NodeIterator sub;
		int index;
		public NodeIterator(Node<?> nd){
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
		public Node<?> next() {
			if(sub == null || !sub.hasNext()){
				Node<?> n = itr.next();
				sub = n.children.isEmpty() ? null : new NodeIterator(n);
				return n;
			}
			return sub.next();
		}
	}
	@Override
	public NodeIterator iterator() {
		return new NodeIterator(this);
	}
}
