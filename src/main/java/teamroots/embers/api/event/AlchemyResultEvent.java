package teamroots.embers.api.event;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import teamroots.embers.api.alchemy.AlchemyResult;

//import ItemStack;

public class AlchemyResultEvent extends UpgradeEvent {
    AlchemyResult result;
    boolean consumeIngredients;
    boolean isFailure;
    ItemStack failureStack;

    public AlchemyResultEvent(BlockEntity tile, AlchemyResult result, boolean consumeIngredients, boolean isFailure, ItemStack failureStack) {
        super(tile);
        this.result = result;
        this.consumeIngredients = consumeIngredients;
        this.isFailure = isFailure;
        this.failureStack = failureStack;
    }

    public boolean shouldConsumeIngredients() {
        return consumeIngredients;
    }

    public void setConsumeIngredients(boolean consumeIngredients) {
        this.consumeIngredients = consumeIngredients;
    }

    public AlchemyResult getResult() {
        return result;
    }

    public void setResult(AlchemyResult result) {
        this.result = result;
    }

    public ItemStack getFailureStack() {
        return failureStack;
    }

    public void setFailureStack(ItemStack failureStack) {
        this.failureStack = failureStack;
    }

    public boolean isFailure() {
        return isFailure;
    }

    public void setFailure(boolean failure) {
        isFailure = failure;
    }
}
