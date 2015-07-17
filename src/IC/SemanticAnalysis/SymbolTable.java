package IC.SemanticAnalysis;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import IC.TypeTable.Type;

public abstract class SymbolTable {

	private String id;
	private Kind kind;
	
	/** map from String to Symbol **/
	protected final Map<String, SymbolTableRow> entries;
	protected final List<SymbolTable> childList;

	private SymbolTable parentSymbolTable;

	public SymbolTable(String id, Kind kind) {
		this.id = id;
		this.kind = kind;
		entries = new LinkedHashMap<String, SymbolTableRow>();
		childList = new ArrayList<SymbolTable>();
	}

	public String getId() {
		return this.id;
	}
	
	public Kind getKind() {
		return this.kind;
	}

	public void insert(String name, SymbolTableRow symbol) throws SemanticError {
		if (!entries.containsKey(name)) {
			entries.put(name, symbol);
		} else {
			//System.out.println("Duplicate fields name");
			throw new SemanticError("Duplicate fields name");
		}
	}
	
	public SymbolTableRow look(String symbolName) {
		if (entries.containsKey(symbolName)) {
			return entries.get(symbolName);
		} else {
			return null;
		}
	}
	
	public SymbolTableRow lookup(String symbolName) {
		
		if (entries.containsKey(symbolName)) {
			return entries.get(symbolName);
		}

		if (parentSymbolTable != null) {
			return parentSymbolTable.lookup(symbolName);
		}
		
		return null;
	}

	public void addChild(SymbolTable child) {
		childList.add(child);
	}

	public SymbolTable getParentSymbolTable() {
		return this.parentSymbolTable;
	}

	public void setParentSymbolTable(SymbolTable parentTable) {
		this.parentSymbolTable = parentTable;
	}

	public List<SymbolTable> getChildList() {
		return childList;
	}

	public Type getType() {
		return parentSymbolTable.look(id).getType();
	}
	
	public Set<Entry<String, SymbolTableRow>> getIterator() {
		return entries.entrySet();
	}
	
	public void AddUniqueTypes() {
		if (entries.size() > 0) {
			for (Entry<String, SymbolTableRow> entry : entries.entrySet()) {
				if (entry.getValue().getKind() != Kind.RET_VAR
						&& entry.getValue().getKind() != Kind.THIS) {
					//output.append("    " + entry.getValue() + "\n");
					
				}
			}
		}
	}
	
	@Override
	public String toString() {
		StringBuffer output = new StringBuffer();
		
		if (entries.size() > 0) {
			for (Entry<String, SymbolTableRow> entry : entries.entrySet()) {
				if (entry.getValue().getKind() != Kind.RET_VAR
						&& entry.getValue().getKind() != Kind.THIS)
				output.append("    " + entry.getValue() + "\n");
			}
		}

		if (childList.size() > 0) {
			output.append("Children tables: " + childList.get(0).getId());
			for (int i = 1; i < childList.size(); i++) {
				output.append(", " + childList.get(i).getId());
			}
		}
		
		output.append("\n");
		
		return output.toString();
	}
}
