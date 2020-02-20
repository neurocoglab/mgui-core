package mgui.io.domestic.datasources;

/***************************************************************
 * Options for exporting data from a {@code DataSourceItem} to a Microsoft Excel format file.
 * 
 * @see ExportDataTableExcelWriter
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class ExportDataTableExcelOptions extends ExportDataTableOptions {

	public static enum Format{
		Xls,
		Xlsx;
	}
	
	public Format excel_format = Format.Xlsx;
	public boolean has_header = true;
	public String sheet_name = "Sheet1";
	public boolean enforce_extensions = true;
	
}
