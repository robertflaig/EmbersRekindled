package teamroots.embers.api.upgrades;

import net.minecraft.util.Direction;

import java.util.List;

public interface IUpgradeProxy {
    void collectUpgrades(List<IUpgradeProvider> upgrades);
    boolean isSocket(Direction facing);
    boolean isProvider(Direction facing);
}
