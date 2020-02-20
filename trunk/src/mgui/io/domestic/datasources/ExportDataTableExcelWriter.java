package mgui.io.domestic.datasources;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import mgui.datasources.DataField;
import mgui.datasources.DataRecordSet;
import mgui.datasources.DataSource;
import mgui.datasources.DataSourceException;
import mgui.datasources.DataSourceItem;
import mgui.interfaces.ProgressUpdater;
import mgui.numbers.MguiBoolean;
import mgui.numbers.MguiNumber;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/*****************************************************************
 * Writes data from a data source {@link DataItem} to Microsoft Excel format.
 * Formats include the older XLS and the newer Open Office XML XLSX format.
 * 
 * <p>Depends on the <a href="http://poi.apache.org/">Apache POI Project</a>
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class ExportDataTableExcelWriter extends ExportDataTableWriter {

	@Override
	public boolean writeDataItem(DataSourceItem item, ProgressUpdater progress_bar) throws IOException {
		
		if (dataFile == null) return false;
		
		try{
			ExportDataTableExcelOptions options = (ExportDataTableExcelOptions)this.options;
			DataSource data_source = options.data_source;
			DataRecordSet record_set = data_source.getRecordSet(item);
			record_set.moveFirst();
			
			ArrayList<DataField> fields = record_set.getFields();
			
			String excel_file_str = dataFile.getAbsolutePath(); 
			boolean is_x = false;
			
			switch (options.excel_format){
				case Xlsx:
					if (options.enforce_extensions && !excel_file_str.endsWith(".xlsx")){
						if (excel_file_str.endsWith(".xls")){
							excel_file_str = excel_file_str + "x";
						}else{
							excel_file_str = excel_file_str + ".xlsx";
							}
						}
					is_x = true;
					break;
				case Xls:
					if (options.enforce_extensions && !excel_file_str.endsWith(".xls")){
						if (excel_file_str.endsWith(".xlsx")){
							excel_file_str = excel_file_str.substring(0,excel_file_str.length()-2);
						}else{
							excel_file_str = excel_file_str + ".xls";
							}
						}
					break;
				}
			
			// Create workbook with the appropriate format
			Workbook workbook = null;
			if (is_x){
				workbook = new XSSFWorkbook();
			}else{
				workbook = new HSSFWorkbook();
				}
			
			// Create new worksheet
			Sheet sheet = workbook.createSheet(options.sheet_name == null ? "Sheet1" : options.sheet_name);
			Row this_row = null;
			Cell this_cell = null;
			int current_row = 0;
			
			if (options.has_header){
				this_row = sheet.createRow(0);
				for (int i = 0; i < fields.size(); i++){
					// Write header to first row
					this_cell = this_row.createCell(i);
					this_cell.setCellValue(fields.get(i).getName());
					}
				current_row = 1;
				}
			
			int[] cell_types = getCellTypes(fields);
			
			// Write rows
			while (!record_set.EOF()){
				this_row = sheet.createRow(current_row);
				for (int i = 0; i < fields.size(); i++){
					
					Object value = fields.get(i).getValue();
					// If null, don't create a cell
					
					if (value != null){
						this_cell = this_row.createCell(i, cell_types[i]);
						switch (cell_types[i]){
							case Cell.CELL_TYPE_BOOLEAN:
								if (value instanceof MguiBoolean)
									this_cell.setCellValue(((MguiBoolean)value).getTrue());
								else
									this_cell.setCellValue((Boolean)value);
								break;
								
							case Cell.CELL_TYPE_NUMERIC:
								if (value instanceof MguiNumber)
									this_cell.setCellValue(((MguiNumber)value).getValue());
								else
									this_cell.setCellValue((double)value);
								break;
								
							default:
								String str = getStringForValue(fields.get(i).getDataType(), value);
								this_cell.setCellValue(str);
								break;
							}
						}
					
					}
					
				record_set.moveNext();
				current_row++;
				}
			
			// Write results
			FileOutputStream excel_file = new FileOutputStream(dataFile);
			workbook.write(excel_file);
			excel_file.close();
			
			return true;
			
		}catch (DataSourceException e){
			
			}
		
		return false;
	}
	
	/**************************************************
	 * Returns the Excel cell type corresponding to each field
	 * 
	 * @param fields
	 * @return
	 */
	protected int[] getCellTypes(ArrayList<DataField> fields){
		
		int[] types = new int[fields.size()];
		
		for (int i = 0; i < fields.size(); i++){
			DataField field = fields.get(i);
			types[i] = Cell.CELL_TYPE_BLANK;
			if (field.isBoolean()) types[i] = Cell.CELL_TYPE_BOOLEAN;
			if (field.isNumeric()) types[i] = Cell.CELL_TYPE_NUMERIC;
			}
		
		return types;
	}
	

}
