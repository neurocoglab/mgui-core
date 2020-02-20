package mgui.interfaces.gui;

import mgui.interfaces.ProgressUpdater;

/*********************************
 * An abstract updater with default implementations for all methods
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public abstract class AbstractProgressUpdater implements ProgressUpdater {

	protected int current_value = 0;
	
	@Override
	public void iterate() {
		current_value++;
	}

	@Override
	public void update(int t) {
		current_value = t;
	}

	@Override
	public void setMinimum(int min) {
		minimum = min;
	}

	@Override
	public void setMaximum(int max) {
		maximum = max;
	}

	protected int minimum = 0;
	
	@Override
	public int getMinimum() {
		// TODO Auto-generated method stub
		return minimum;
	}
	
	protected int maximum = 0;

	@Override
	public int getMaximum() {
		// TODO Auto-generated method stub
		return maximum;
	}

	@Override
	public void cancel() {
		is_cancelled = true;
	}
	
	protected boolean is_cancelled = false;

	@Override
	public boolean isCancelled() {
		return is_cancelled;
	}

	@Override
	public void setMode(Mode mode) {
		
	}

	@Override
	public void reset() {
		current_value = 0;
	}

	@Override
	public void register() {
		// TODO Auto-generated method stub

	}

	@Override
	public void deregister() {
		// TODO Auto-generated method stub

	}
	
	protected String message = "";

	@Override
	public void setMessage(String message) {
		this.message = message;
	}

	protected boolean is_indeterminate = false;
	
	@Override
	public void setIndeterminate(boolean b) {
		is_indeterminate = b;
	}
	
	protected boolean allow_changes = true;

	@Override
	public boolean allowChanges() {
		return allow_changes;
	}

}
