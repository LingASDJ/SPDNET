/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2022 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.saqfish.spdnet.actors.mobs.npcs;

import com.saqfish.spdnet.Dungeon;
import com.saqfish.spdnet.ShatteredPixelDungeon;
import com.saqfish.spdnet.Statistics;
import com.saqfish.spdnet.actors.Char;
import com.saqfish.spdnet.actors.buffs.AscensionChallenge;
import com.saqfish.spdnet.actors.buffs.Buff;
import com.saqfish.spdnet.effects.CellEmitter;
import com.saqfish.spdnet.effects.particles.ElmoParticle;
import com.saqfish.spdnet.items.Heap;
import com.saqfish.spdnet.items.Item;
import com.saqfish.spdnet.items.armor.Armor;
import com.saqfish.spdnet.journal.Notes;
import com.saqfish.spdnet.messages.Messages;
import com.saqfish.spdnet.scenes.GameScene;
import com.saqfish.spdnet.sprites.ShopkeeperSprite;
import com.saqfish.spdnet.windows.WndBag;
import com.saqfish.spdnet.windows.WndTradeItem;
import com.watabou.noosa.Game;
import com.watabou.utils.Callback;

public class Shopkeeper extends NPC {

	{
		spriteClass = ShopkeeperSprite.class;

		properties.add(Property.IMMOVABLE);
	}
	
	@Override
	protected boolean act() {

		if (Dungeon.level.heroFOV[pos]){
			Notes.add(Notes.Landmark.SHOP);
		}

		/*if (Statistics.highestAscent < 20 && Dungeon.hero.buff(AscensionChallenge.class) != null){
			flee();
			return true;
		}*/

		sprite.turnTo( pos, Dungeon.hero.pos );
		spend( TICK );
		return super.act();
	}
	
	@Override
	public void damage( int dmg, Object src ) {
		flee();
	}
	
	@Override
	public void add( Buff buff ) {
		flee();
	}
	
	public void flee() {
		destroy();

		Notes.remove(Notes.Landmark.SHOP);

		if (sprite != null) {
			sprite.killAndErase();
			CellEmitter.get(pos).burst(ElmoParticle.FACTORY, 6);
		}
	}
	
	@Override
	public void destroy() {
		super.destroy();
		for (Heap heap: Dungeon.level.heaps.valueList()) {
			if (heap.type == Heap.Type.FOR_SALE) {
				if (ShatteredPixelDungeon.scene() instanceof GameScene) {
					CellEmitter.get(heap.pos).burst(ElmoParticle.FACTORY, 4);
				}
				if (heap.size() == 1) {
					heap.destroy();
				} else {
					heap.items.remove(heap.size()-1);
					heap.type = Heap.Type.HEAP;
				}
			}
		}
	}
	
	@Override
	public boolean reset() {
		return true;
	}

	//shopkeepers are greedy!
	public static int sellPrice(Item item){
		return item.value() * 5 * (Dungeon.depth / 5 + 1);
	}
	
	public static WndBag sell() {
		return GameScene.selectItem( itemSelector );
	}

	public static boolean canSell(Item item){
		if (item.value() <= 0)                                              return false;
		if (item.unique && !item.stackable)                                 return false;
		if (item instanceof Armor && ((Armor) item).checkSeal() != null)    return false;
		if (item.isEquipped(Dungeon.hero) && item.cursed)                   return false;
		return true;
	}

	private static WndBag.ItemSelector itemSelector = new WndBag.ItemSelector() {
		@Override
		public String textPrompt() {
			return Messages.get(Shopkeeper.class, "sell");
		}

		@Override
		public boolean itemSelectable(Item item) {
			return Shopkeeper.canSell(item);
		}

		@Override
		public void onSelect( Item item ) {
			if (item != null) {
				WndBag parentWnd = sell();
				GameScene.show( new WndTradeItem( item, parentWnd ) );
			}
		}
	};

	@Override
	public boolean interact(Char c) {
		if (c != Dungeon.hero) {
			return true;
		}
		Game.runOnRenderThread(new Callback() {
			@Override
			public void call() {
				sell();
			}
		});
		return true;
	}
}
