package g.sort.vis;
/**
 * 
 * A @FunctionalInterface that takes two int values and returns no result.
 * 
 * @author balintgergely
 *
 */
@FunctionalInterface
public interface BiIntBooleanConsumer{
	public static final BiIntBooleanConsumer EMPTY = (int a,int b,boolean c) -> {};
	public void accept(int left,int right,boolean bool);
}
