package IC.LirTranslate;

import microLIR.instructions.Reg;

import java.util.ArrayList;
import java.util.List;

public class RegistersInformation {

	private Register targetRegister;
	private List<Register> availableRegisters;
	
	public RegistersInformation() {
		availableRegisters = new ArrayList<>();
	}
	
	public void initRegisters(int num) {
		for (int i = 0; i < num; i++) {
			availableRegisters.add(new Register(i + 1, null));
		}
	}
	
	public void setTargetRegister(Register targetRegister) {
		this.targetRegister = targetRegister;
	}
	
	public Register getRegister(String value) {
		Register register = null;
		for (Register reg : availableRegisters) {
			if (reg.getValue() == null) continue;
			if (reg.getValue().equals(value)) {
				register = reg;
				break;
			}
		}

		return register;
	}

    public Register getRegister(int index) {
        Register register = availableRegisters.get(index - 1);
        return (register != null) ? register : null;
    }
	
	public Register getTargetRegister() {

		if (targetRegister == null) {
			targetRegister = getAvailableRegiser();
		}

		return targetRegister;
	}
	
	public void freeRegister(Register register) {
		register.setValue(null);
	}
	
	public Register getAvailableRegiser() {
		Register register = null;
		for (Register reg : availableRegisters) {
			if (reg.isAvailable()) {
				register = reg;
				break;
			}
		}
		
		return register;
	}

    public void freeAllRegisters() {
        availableRegisters.forEach(this::freeRegister);
    }
	
	@Override
	public String toString() {
		StringBuffer output = new StringBuffer();
		
		output.append("Registers:\n");
		for (Register reg : availableRegisters) {
			output.append(reg + "=" + reg.getValue() + "\n");
		}
		
		if (targetRegister != null) {
			output.append(targetRegister + "=" + targetRegister.getValue() + " (Target)\n");
		}
		return output.toString();
	}
}
