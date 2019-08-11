package net.balintgergely.sortvis;

public interface DisplayInterface {
	/**
	 * Called on a tick from the sorter.
	 */
	public void tick();
	/**
	 * Called on starts and stops. 
	 * @param start If true, a task was started. Otherwise, a task just stopped.
	 */
	public void running(boolean start);
	/**
	 * Returns the selected sorter.
	 */
	public Object getSelectedNode();
	/**
	 * Presses the specified note in the Synthesizer.
	 */
	public void noteOn(int note);
	/**
	 * Releases the specified note in the Synthesizer.
	 */
	public void noteOff(int note);
	/**
	 * Releases all notes in the Synthesizer.
	 */
	public void notesOff();
	/**
	 * Notification that the array has been reallocated.
	 */
	public void arrayChanged();
}
