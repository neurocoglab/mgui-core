package mgui.interfaces;

/*********************************
 * 
 * Acts as a clipboard for the current {@linkplain InterfaceSession}.
 * 
 * @author lpzatr
 *
 */
public class Clipboard {
	
	protected Item current = null;
	
	public Clipboard() {
		
	}
	
	/*************************
	 * 
	 * Sets the current content object, overwritten any existing one.
	 * 
	 * @param obj
	 */
	public void setContent(Item content) {
		current = content;
	}
	
	/*************************
	 * 
	 * Returns the current content, or {@code null} is this clipboard is empty.
	 * 
	 * @return
	 */
	public Item getContent() {
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
	
	public static class Item {
		
		Object value;
		String type;
		
		public Item(Object value, String type) {
			this.value = value;
			this.type = type;
		}
		
		public Object getValue() {
			return value;
		}
		
		public String getType() {
			return type;
		}
		
		
	}
	

}
