package teamroots.embers.itemmod;

import teamroots.embers.api.itemmod.ModifierBase;

import teamroots.embers.api.itemmod.ModifierBase.EnumType;

public class ModifierCore extends ModifierBase {
	public ModifierCore() {
		super(EnumType.ALL,"core",0.0,false);
		this.canRemove = false;
		this.shouldRenderTooltip = false;
	}
}
