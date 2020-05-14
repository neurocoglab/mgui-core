package mgui.interfaces;

/*********************************
 * 
 * Acts as a clipboard for the current {@linkplain InterfaceSession}.
 * 
 * @author lpzatr
 *
 */
public class Clipboard {
	
	protected Object current = null;
	
	public Clipboard() {
		
	}
	
	/*************************
	 * 
	 * Sets the current content object, overwritten any existing one.
	 * 
	 * @param obj
	 */
	public void setContent(Object obj) {
		current = obj;
	}
	
	/*************************
	 * 
	 * Returns the current content, or {@code null} is this clipboard is empty.
	 * 
	 * @return
	 */
	public Object getContent() {
		return current;
	}
	
	/************************
	 * 
	 * Clears the current content
	 * 
	 */
	public void clear() {
		current = null;
	}
	
	

}
