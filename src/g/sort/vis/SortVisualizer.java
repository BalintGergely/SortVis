package g.sort.vis;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.IdentityHashMap;

import javax.sound.midi.Instrument;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Synthesizer;
import javax.swing.BoundedRangeModel;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonModel;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.JToggleButton.ToggleButtonModel;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

public class SortVisualizer extends JPanel implements DisplayInterface{
	private static final String PILLARS = "Pillars",DOTS = "Dots",BRICKS = "Bricks",RAYS = "Rays",RAY_DOTS = "Ray dots",RAY_LINES = "Ray lines",
								VALUE = "Value",VALUE360 = "Value 360",DISTANCE = "Distance from target",STATUS = "Status",CONSTANT = "Constant",
								ABSOLUTE_DISTANCE = "Absolute distance from target",
								CIRCULAR_DISTANCE = "Circular distance from target";
	private static final String[]	mainPick = new String[]{PILLARS,DOTS,BRICKS,RAYS,RAY_DOTS,RAY_LINES},
									colorPick = new String[]{STATUS,VALUE,VALUE360,DISTANCE,CONSTANT},
									heightPick = new String[]{VALUE,DISTANCE,ABSOLUTE_DISTANCE,CIRCULAR_DISTANCE,CONSTANT};
	public static final Color TRANSPARENT = new Color(0,true);
	private static final long serialVersionUID = 1L;
	public static void main(String[] atgs) throws Exception{
		SortVisualizer vis = new SortVisualizer();
		vis.display();
	}
	private JFrame frame;
	private ToggleButtonModel						startModel = new ToggleButtonModel();
	private TreeSelectionModel						selectedSorter = new DefaultTreeSelectionModel();
	{selectedSorter.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);}
	private ComboBoxModel<ValueSetGenerator>		valueSetModel = new DefaultComboBoxModel<>(ValueSetGenerator.values());
	private ComboBoxModel<String>					mainPickMethod = new DefaultComboBoxModel<>(mainPick),
													colorPickMethod = new DefaultComboBoxModel<>(colorPick),
													heightPickMethod = new DefaultComboBoxModel<>(heightPick);
	{
		ListDataListener lst = new ListDataListener(){
			@Override
			public void intervalAdded(ListDataEvent e) {
				updateVisual();
			}
			@Override
			public void intervalRemoved(ListDataEvent e) {
				updateVisual();
			}
			@Override
			public void contentsChanged(ListDataEvent e) {
				updateVisual();
			}
		};
		mainPickMethod.addListDataListener(lst);
		colorPickMethod.addListDataListener(lst);
		heightPickMethod.addListDataListener(lst);
	}
	private BoundedRangeModel	timeModel = new DefaultBoundedRangeModel(0, 0,-200, 500);
	private SpinnerNumberModel	arraySizeModel = new SpinnerNumberModel(128, 3, Integer.MAX_VALUE, 64),
								threadCountModel = new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1);
	private final SorterManager manager;
	private boolean stepFlag = false;
	Synthesizer synthesizer;
	MidiChannel channel;
	private Dimension createPreferredSize(){
		VisualArray array = manager.getArray();
		if(array == null){
			return new Dimension(0,0);
		}
		switch(mainPickMethod.getSelectedItem().toString()){
		case PILLARS:
		case DOTS:
		case BRICKS:return new Dimension(array.size+16,0);
		default:return new Dimension(0,0);
		}
	}
	private void updateVisual(){
		setPreferredSize(createPreferredSize());
		revalidate();
		repaint();
	}
	public void arrayChanged(){
		EventQueue.invokeLater(this::updateVisual);
	}
	@Override
	public void tick(){
		super.repaint();
		try {
			EventQueue.invokeAndWait(() -> {});
		} catch (InvocationTargetException | InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
	@Override
	@SuppressWarnings("unchecked")
	public Object getSelectedNode() {
		TreePath pt = selectedSorter.getSelectionPath();
		if(pt != null){
			Object obj = pt.getLastPathComponent();
			if(obj instanceof Node){
				return (Node<Sorter>)obj;
			}
		}
		return null;
	}
	@Override
	public void noteOn(int note) {
		if(channel != null){
			channel.noteOn(note, 100);
		}
	}
	public void noteOff(int note){
		if(channel != null){
			channel.noteOff(note);
		}
	}
	public void notesOff(){
		if(channel != null){
			channel.allNotesOff();
		}
	}
	public void running(boolean start){
		if(!(stepFlag && start)){
			startModel.setSelected(start);
		}
		stepFlag = false;
	}
	private SortVisualizer() {
		super(null,false);
		manager = new SorterManager(this);
		super.setPreferredSize(new Dimension(0,0));
		super.setBackground(Color.BLACK);
		frame = new JFrame("SortVisualizer");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JScrollPane scrollPane = new JScrollPane(this, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setPreferredSize(new Dimension(800,600));
		frame.add(scrollPane,BorderLayout.CENTER);
		JPanel sidebar = new JPanel();
		sidebar.setLayout(new BoxLayout(sidebar,BoxLayout.Y_AXIS));
		frame.add(sidebar,BorderLayout.LINE_START);
		{
			JButton abt = new JButton("About");
			abt.addActionListener((ActionEvent e) -> {
				JOptionPane.showMessageDialog(this, "Sorting Algorithm Visualizer\nBy: Bálint János Gergely\ngithub.com/BalintGergely/SortVis", "About", JOptionPane.INFORMATION_MESSAGE);
			});
			abt.setForeground(Color.GREEN.darker());
			abt.setOpaque(false);
			abt.setPreferredSize(new Dimension(128,16));
			sidebar.add(abt);
		}
		{
			JPanel drawerSubpanel = new JPanel(new GridLayout(0,2));
			drawerSubpanel.add(new JLabel("Shape:"));
			drawerSubpanel.add(new JComboBox<>(mainPickMethod));
			drawerSubpanel.add(new JLabel("Heights:"));
			drawerSubpanel.add(new JComboBox<>(heightPickMethod));
			drawerSubpanel.add(new JLabel("Colors:"));
			drawerSubpanel.add(new JComboBox<>(colorPickMethod));
			sidebar.add(drawerSubpanel);
		}
		{
			JPanel arraySubpanel = new JPanel(new GridLayout(0,2));
			sidebar.add(arraySubpanel);
			arraySubpanel.add(new JLabel("Array size"));
			arraySubpanel.add(new JSpinner(arraySizeModel));
			arraySubpanel.add(new JLabel("Value set"));
			arraySubpanel.add(new JComboBox<>(valueSetModel));
			arraySubpanel.add(new JLabel("Thread count"));
			arraySubpanel.add(new JSpinner(threadCountModel));
			threadCountModel.addChangeListener((ChangeEvent e) -> {
				manager.setThreadCount(threadCountModel.getNumber().intValue());
			});
			JCheckBox bowBox = new JCheckBox("Block on write",true),borBox = new JCheckBox("Block on read",true);
			bowBox.getModel().addChangeListener((ChangeEvent e) -> {
				manager.setWriteBlockEnabled(bowBox.isSelected());
			});
			borBox.getModel().addChangeListener((ChangeEvent e) -> {
				manager.setReadBlockEnabled(borBox.isSelected());
			});
			arraySubpanel.add(bowBox);
			arraySubpanel.add(borBox);
			JButton create = new JButton("Create");
			create.addActionListener((ActionEvent e) -> {
				manager.recreate((ValueSetGenerator)valueSetModel.getSelectedItem(), arraySizeModel.getNumber().intValue());
			});
			arraySubpanel.add(create);
			JButton shuffle = new JButton();
			shuffle.setLayout(new BorderLayout());
			shuffle.add(new JLabel("Shuffle"),BorderLayout.CENTER);
			JCheckBox instant = new JCheckBox("Instant");
			shuffle.add(instant,BorderLayout.LINE_END);
			shuffle.addActionListener((ActionEvent e) -> manager.shuffle(!instant.isSelected()));
			arraySubpanel.add(shuffle);
			startModel.addChangeListener((ChangeEvent e) -> {
				if(startModel.isSelected()){
					manager.stepUnlock();
					if(!manager.isTaskRunning()){
						manager.doSort();
					}
				}else{
					manager.stepLock();
				}
			});
			JButton reset = new JButton("Reset");
			reset.addActionListener((ActionEvent e) -> manager.reset());
			arraySubpanel.add(reset, BorderLayout.LINE_END);
			JButton check = new JButton("Check");
			check.addActionListener((ActionEvent e) -> manager.check());
			arraySubpanel.add(check, BorderLayout.LINE_END);
		}
		{
			JPanel sorterSubpanel = new JPanel(new BorderLayout());
			sidebar.add(sorterSubpanel);
			JToggleButton startButton = new JToggleButton();
			startButton.setModel(startModel);
			startButton.setLayout(new BorderLayout());
			startButton.add(new JLabel("Run"),BorderLayout.CENTER);
			JButton stepButton = new JButton("Step");
			JButton stopButton = new JButton("Stop");
			JPanel stepSub = new JPanel(new GridLayout(1,2));
			stepSub.add(stepButton);
			stepSub.add(stopButton);
			startButton.add(stepSub,BorderLayout.LINE_END);
			sorterSubpanel.add(startButton,BorderLayout.PAGE_START);
			stepButton.addActionListener((ActionEvent e) -> {
				startModel.setSelected(false);
				stepFlag = true;
				if(manager.isTaskRunning()){
					manager.stepDo();
				}else{
					manager.doSort();
				}
			});
			stopButton.addActionListener((ActionEvent e) -> {
				startModel.setSelected(false);
				manager.stop();
			});
			JTree tree = new JTree(new DefaultTreeModel(manager.root));
			tree.setSelectionModel(selectedSorter);
			sorterSubpanel.add(new JScrollPane(tree),BorderLayout.CENTER);
			CardLayout crd = new CardLayout();
			JPanel settingPanel = new JPanel(crd);
			IdentityHashMap<Object,String> ihs = new IdentityHashMap<>();
			recursiveAddConfig(ihs, manager.root, settingPanel, null);
			if(settingPanel.getComponentCount() > 0){
				sorterSubpanel.add(settingPanel,BorderLayout.PAGE_END);
				selectedSorter.addTreeSelectionListener((TreeSelectionEvent e) -> {
					TreePath pt = e.getPath();
					if(pt != null){
						@SuppressWarnings("unchecked")
						Node<Sorter> nd = (Node<Sorter>)pt.getLastPathComponent();
						String unit = ihs.get(nd.element);
						if(unit != null){
							settingPanel.setVisible(true);
							crd.show(settingPanel, unit);
						}else{
							settingPanel.setVisible(false);
						}
					}
				});
			}
		}
		JLabel timeLabel = new JLabel();
		timeLabel.setHorizontalAlignment(JLabel.LEFT);
		timeModel.addChangeListener((ChangeEvent e) -> {
			int val = timeModel.getValue();
			if(val <= 0){
				timeLabel.setText((1-val)+" op per draw");
			}else{
				timeLabel.setText(val+" ms delay");
			}
			manager.setDelayTime(val);
		});
		timeLabel.setText("0");
		sidebar.add(timeLabel);
		JSlider timeSlider = new JSlider(timeModel);
		timeSlider.setMajorTickSpacing(100);
		timeSlider.setPaintTicks(true);
		timeSlider.setPaintTrack(true);
		timeSlider.setMinorTickSpacing(10);
		timeSlider.setSnapToTicks(true);
		sidebar.add(timeSlider);
		a: try {
			synthesizer = MidiSystem.getSynthesizer();
			synthesizer.open();
			b: for(MidiChannel c : synthesizer.getChannels()){
				if(c != null){
					channel = c;
					break b;
				}
			}
			if(channel == null){
				break a;
			}
			channel.setChannelPressure(127);
			Instrument[] ins = synthesizer.getAvailableInstruments();
			JList<Instrument> insList = new JList<>(ins);
			insList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			boolean w = true;
			String start = "Saw Wave";
			{
				Calendar cld = Calendar.getInstance();
				if(cld.get(Calendar.MONTH) == 3 && cld.get(Calendar.DAY_OF_MONTH) == 1){
					start = "Piano 1";
				}
			}
			for(int i = 0;i < ins.length;i++){
				synthesizer.loadInstrument(ins[i]);
				if(w || ins[i].getName().startsWith(start)){
					insList.setSelectedIndex(i);
					channel.programChange(ins[i].getPatch().getProgram());
					w = false;
				}
			}
			if(w){
				insList.setSelectedIndex(0);
				channel.programChange(ins[0].getPatch().getProgram());
			}
			insList.addListSelectionListener((ListSelectionEvent e) -> {
				int index = e.getFirstIndex();
				if(index >= 0){
					channel.programChange(ins[index].getPatch().getProgram());
					if(!manager.isTaskRunning()){
						manager.check();
					}
				}
			});
			JScrollPane scr = new JScrollPane(insList);
			scr.setVisible(false);
			frame.add(scr, BorderLayout.LINE_END);
			JToggleButton iss = new JToggleButton("Select MIDI instrument");
			iss.addActionListener((ActionEvent e) -> {
				scr.setVisible(iss.isSelected());
				frame.revalidate();
			});
			sidebar.add(iss);
		} catch (MidiUnavailableException e) {
			e.printStackTrace();
			channel = null;
		}
		sidebar.add(Box.createGlue());
	}
	private static void recursiveAddConfig(IdentityHashMap<Object,String> smap,Node<?> node,JPanel settingPanel,String name){
		if(node.element instanceof ConfigurableSorter){
			ConfigurableSorter str = (ConfigurableSorter)node.element;
			int count = str.getNumberOptions();
			if(count > 0){
				JPanel panel = new JPanel(new GridLayout(0,2));
				name = Integer.toString(System.identityHashCode(str));
				smap.put(str, name);
				settingPanel.add(panel, name);
				for(int c = 0;c < count;c++){
					final int index = c;
					panel.add(new JLabel(str.getOptionName(index)+": "));
					Class<?> cls = str.getOptionClass(index);
					if(cls == Boolean.class){
						JCheckBox box = new JCheckBox();
						ButtonModel md = box.getModel();
						md.setSelected((Boolean)str.getOption(index));
						md.addChangeListener((ChangeEvent e) -> {
							str.setOption(index, md.isSelected());
						});
						panel.add(box);
					}else if(cls == Integer.class){
						Integer[] obj = (Integer[])str.getOptions(index);
						SpinnerNumberModel md = new SpinnerNumberModel((Integer)str.getOption(index), obj[0], obj[1], Integer.valueOf(1));
						JSpinner sp = new JSpinner(md);
						md.addChangeListener((ChangeEvent e) -> {
							str.setOption(index, md.getNumber());
						});
						panel.add(sp);
					}else{
						Object[] obj = str.getOptions(index);
						JComboBox<Object> box = new JComboBox<>(obj);
						box.addActionListener((ActionEvent e) -> {
							str.setOption(index, box.getSelectedItem());
						});
						box.setSelectedItem(str.getOption(index));
						panel.add(box);
					}
				}
			}
		}
		if(name != null) smap.put(node.element, name);
		Enumeration<? extends Node<?>> en = node.children();
		while(en.hasMoreElements()){
			recursiveAddConfig(smap, en.nextElement(), settingPanel, name);
		}
	}
	public void display() {
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
	private static ThreadLocal<LongHeap> hpTl = new ThreadLocal<>();
	public void paintComponent(Graphics gr){
		super.paintComponent(gr);
		VisualArray data = manager.getArray();
		if(data == null){
			return;
		}
		int width = getWidth(),height = getHeight();
		long time = System.nanoTime();
		String	cm = colorPickMethod.getSelectedItem().toString(),
				hm = heightPickMethod.getSelectedItem().toString();
		if(!cm.equals(STATUS)){
			int ts = data.size/50;
			if(ts < 1){
				ts = 1;
			}
			LongHeap t = hpTl.get();
			if(t == null || t.values.length != ts){
				hpTl.set(t = new LongHeap(ts));
			}
			t.size = 0;
			t.put(time);
			for(int i = 0;i < data.size;i++){
				long cd = data.getCooldown(i);
				t.put(cd);
			}
			time = t.values[0];
		}
		switch(mainPickMethod.getSelectedItem().toString()){
		case PILLARS:paintComponentPillars(data,(Graphics2D)gr,width,height,time,cm,hm);break;
		case DOTS:paintComponentDots(data,(Graphics2D)gr,width,height,time,cm,hm);break;
		case BRICKS:paintComponentBricks(data,(Graphics2D)gr,width,height,time,cm,hm);break;
		case RAYS:paintComponentRays(data,(Graphics2D)gr,width,height,time,cm,hm);break;
		case RAY_DOTS:paintComponentRayDots(data,(Graphics2D)gr,width,height,time,cm,hm);break;
		case RAY_LINES:paintComponentRayLines(data,(Graphics2D)gr,width,height,time,cm,hm);break;
		}
	}
	public Color colorOf(VisualArray data,boolean shan,int i,boolean cooldown,String method){
		switch(method){
		case CONSTANT:
			return (shan || cooldown) ? TRANSPARENT : Color.WHITE;
		case STATUS:
			int color = data.getColor(shan,i);
			return new Color(cooldown ? 
								Colors.specialBlend(0xffff0000,color) : 
								(shan ? color : Colors.specialBlend(0xffffffff, color)),true);
		case VALUE:
			if(shan || cooldown){
				return TRANSPARENT;
			}
			float range = ((data.getValue(i)-data.min)/(float)data.range)*300;
			return new Color(Colors.HSVtoRGB(range, 100, 100, 0));
		case VALUE360:
			if(shan || cooldown){
				return TRANSPARENT;
			}
			range = ((data.getValue(i)-data.min)/(float)data.range)*360;
			return new Color(Colors.HSVtoRGB(range, 100, 100, 0));
		case DISTANCE:
			if(shan || cooldown){
				return TRANSPARENT;
			}
			float hue = (data.getValue(i)-data.getSortedValue(i))*360/(float)data.range;
			return new Color(Colors.HSVtoRGB(hue, 100, 100, 0));
		default:throw new IllegalArgumentException();
		}
	}
	public float heightOf(VisualArray data,boolean shan,int i,String method){
		switch(method){
		case VALUE:return (data.getValue(shan, i)-data.min+1)/((float)data.range+1);
		case DISTANCE:return ((data.getValue(shan,i)-data.getSortedValue(i)+1))/2f/data.range+0.5f;
		case ABSOLUTE_DISTANCE:return 1-Math.abs(data.getValue(shan,i)-data.getSortedValue(i)+1)/(float)data.range;
		case CIRCULAR_DISTANCE:float val = 1-Math.abs(data.getValue(shan,i)-data.getSortedValue(i)+1)*2/(float)data.range;
								return Math.abs(val);
		case CONSTANT:return 1;
		default:throw new IllegalArgumentException();
		}
	}
	public void paintComponentPillars(VisualArray data,Graphics2D gr,int width,int height,long time,String cm,String hm){
		width -= 16;
		height -= 8;
		if(width < data.size){
			width = data.size;
		}
		float columnWidth = width/(float)data.size;
		int off = 0;
		boolean shan = false;
		while(true){
			for(int i = 0;i < data.size;i++){
				int x = Math.round(i*columnWidth);
				int w = Math.round((i+1)*columnWidth)-x;
				if(w < 1){
					w = 1;
				}else if(w > 1){
					w--;
				}
				int h = Math.round(heightOf(data,shan,i,hm)*height);
				gr.setColor(colorOf(data,shan,i,data.getCooldown(shan,i) >= time,cm));
				gr.fillRect(x+8+off, height+4-h, w, h);
			}
			if(shan){
				break;
			}else{
				shan = true;
				off = columnWidth >= 2 ? 1 : 0;
			}
		}
	}
	public void paintComponentBricks(VisualArray data,Graphics2D gr,int width,int height,long time,String cm,String hm){
		width -= 16;
		height -= 8;
		if(width < data.size){
			width = data.size;
		}
		float columnWidth = width/(float)data.size;
		int off = 0;
		boolean shan = false;
		while(true){
			for(int i = 0;i < data.size;i++){
				int x = Math.round(i*columnWidth);
				int w = Math.round((i+1)*columnWidth)-x;
				if(w < 1){
					w = 1;
				}else if(w > 1){
					w--;
				}
				int h = Math.round(heightOf(data,shan,i,hm)*height);
				gr.setColor(colorOf(data,shan,i,data.getCooldown(shan,i) >= time,cm));
				gr.fillRect(x+8+off, height+4-h, w, h);
			}
			if(shan){
				break;
			}else{
				shan = true;
				off = columnWidth >= 2 ? 1 : 0;
			}
		}
		gr.setColor(Color.BLACK);
		float mlt = height/(float)(data.range+1);
		for(int i = 0;i <= data.range;i++){
			int y = Math.round(i*mlt)+4;
			gr.drawLine(0, y, width+16, y);
		}
	}
	public void paintComponentDots(VisualArray data,Graphics2D gr,int width,int height,long time,String cm,String hm){
		width -= 16;
		height -= 8;
		if(width < data.size){
			width = data.size;
		}
		float columnWidth = width/(float)data.size;
		int off = 0;
		boolean shan = false;
		while(true){
			for(int i = 0;i < data.size;i++){
				int x = Math.round(i*columnWidth);
				int w = Math.round((i+1)*columnWidth)-x;
				if(w < 1){
					w = 1;
				}else if(w > 1){
					w--;
				}
				int h = Math.round(heightOf(data,shan,i,hm)*height);
				gr.setColor(colorOf(data,shan,i,data.getCooldown(shan,i) >= time,cm));
				gr.fillRect(x+8+off, height+4-h, w, w);
			}
			if(shan){
				break;
			}else{
				shan = true;
				off = columnWidth >= 2 ? 1 : 0;
			}
		}
	}
	public void paintComponentRays(VisualArray data,Graphics2D gr,int w,int h,long time,String cm,String hm){
		w -= 8;
		h -= 8;
		int cx = w/2+4,cy = h/2+4;
		w /= 2;
		h /= 2;
		double direction = 0;
		gr.setStroke(new BasicStroke(1.5f));
		boolean shan = false;
		while(true){
			for(int i = 0;i < data.size;i++){
				Color c = colorOf(data,shan,i,data.getCooldown(i) >= time,cm);
				double nextDir = ((i+1)*Math.PI*2/data.size);
				float a = c.getAlpha()/255f;
				if(a != 0){
					float ho = heightOf(data,shan,i,hm);
					gr.setColor(c);
					while(direction < nextDir){
						int y = (int) Math.round(Math.cos(direction)*h*ho),x = (int) Math.round(Math.sin(direction)*w*ho);
						gr.drawLine(cx, cy, cx+x, cy-y);
						direction += 0.0009765625/a;
					}
				}
				direction = nextDir;
			}
			if(shan){
				break;
			}else{
				shan = true;
			}
		}
	}
	public void paintComponentRayDots(VisualArray data,Graphics2D gr,int w,int h,long time,String cm,String hm){
		w -= 8;
		h -= 8;
		int cx = w/2+4,cy = h/2+4;
		w /= 2;
		h /= 2;
		double direction = 0;
		gr.setStroke(new BasicStroke(1.5f));
		boolean shan = false;
		while(true){
			for(int i = 0;i < data.size;i++){
				Color c = colorOf(data,shan,i,data.getCooldown(i) >= time,cm);
				double nextDir = ((i+1)*Math.PI*2/data.size);
				float a = c.getAlpha()/255f;
				if(a != 0){
					float ho = heightOf(data,shan,i,hm);
					gr.setColor(c);
					while(direction < nextDir){
						int y1 = (int) Math.round(Math.cos(direction)*(h*ho-1f)),x1 = (int) Math.round(Math.sin(direction)*(w*ho-1f));
						int y2 = (int) Math.round(Math.cos(direction)*h*ho),x2 = (int) Math.round(Math.sin(direction)*w*ho);
						gr.drawLine(cx+x1, cy-y1, cx+x2, cy-y2);
						direction += 0.0009765625/a;
					}
				}
				direction = nextDir;
			}
			if(shan){
				break;
			}else{
				shan = true;
			}
		}
	}
	public void paintComponentRayLines(VisualArray data,Graphics2D gr,int w,int h,long time,String cm,String hm){
		w -= 8;
		h -= 8;
		int cx = w/2+4,cy = h/2+4;
		w /= 2;
		h /= 2;
		double direction = 0;
		gr.setStroke(new BasicStroke(1.5f));
		boolean shan = false;
		while(true){
			for(int i = 0;i < data.size;i++){
				Color c = colorOf(data,shan,i,data.getCooldown(i) >= time,cm);
				double nextDir = ((i+1)*Math.PI*2/data.size);
				float a = c.getAlpha()/255f;
				if(a != 0){
					float ho = 1-heightOf(data,shan,i,hm)*0.9f;
					gr.setColor(c);
					while(direction < nextDir){
						int y1 = (int) Math.round(Math.cos(direction)*(h*ho-1f)),x1 = (int) Math.round(Math.sin(direction)*(w*ho-1f));
						int y2 = (int) Math.round(Math.cos(direction)*h),x2 = (int) Math.round(Math.sin(direction)*w);
						gr.drawLine(cx+x1, cy-y1, cx+x2, cy-y2);
						direction += 0.0009765625/a;
					}
				}
				direction = nextDir;
			}
			if(shan){
				break;
			}else{
				shan = true;
			}
		}
	}
}
