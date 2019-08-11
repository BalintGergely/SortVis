package net.balintgergely.sortvis;

import java.util.Collections;
import java.util.Iterator;

public abstract class ConfigurableSorter implements Sorter, Iterable<Sorter>{
	@Override
	public Iterator<Sorter> iterator(){return Collections.emptyIterator();}
	public int getNumberOptions() {return 0;}
	public String getOptionName(int index) {return null;}
	public Class<?> getOptionClass(int index){return null;}
	public Object[] getOptions(int index) {return null;}
	public void setOption(int index,Object opt){}
	public Object getOption(int index) {return null;}
}
