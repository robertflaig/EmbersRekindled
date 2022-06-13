package teamroots.embers.network.message;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.UUID;

public class MessageItemUpdate implements IMessage {
	public CompoundNBT tag = new CompoundNBT();
	public int slot = 0;
	public UUID id = null;
	public MessageItemUpdate(){
		//
	}
	
	public MessageItemUpdate(UUID id, int slot, CompoundNBT tag){
		this.tag = tag;
		this.id = id;
		this.slot = slot;
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		tag = ByteBufUtils.readTag(buf);
		slot = buf.readInt();
		id = new UUID(buf.readLong(),buf.readLong());
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeTag(buf, tag);
		buf.writeInt(slot);
		buf.writeLong(id.getMostSignificantBits());
		buf.writeLong(id.getLeastSignificantBits());
	}

    public static class MessageHolder implements IMessageHandler<MessageItemUpdate,IMessage>
    {
    	@SideOnly(Side.CLIENT)
        @Override
        public IMessage onMessage(final MessageItemUpdate message, final MessageContext ctx) {
    		Minecraft.getMinecraft().addScheduledTask(()-> {
	    		PlayerEntity player = Minecraft.getMinecraft().world.getPlayerEntityByUUID(message.id);
	    		if (player != null){
	    			player.inventory.getStackInSlot(message.slot).setTagCompound(message.tag);
	    		}
	    	});
    		return null;
	    }
    }
}
