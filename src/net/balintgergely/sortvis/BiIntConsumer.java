package net.balintgergely.sortvis;
/**
 * 
 * A @FunctionalInterface that takes two int values and returns no result.
 * 
 * @author balintgergely
 *
 */
@FunctionalInterface
public interface BiIntConsumer{
	public void accept(int left,int right);
}
