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

package com.saqfish.spdnet.items.bombs;

import com.saqfish.spdnet.Dungeon;
import com.saqfish.spdnet.actors.Actor;
import com.saqfish.spdnet.actors.Char;
import com.saqfish.spdnet.actors.buffs.Blindness;
import com.saqfish.spdnet.actors.buffs.Buff;
import com.saqfish.spdnet.actors.buffs.Cripple;
import com.saqfish.spdnet.levels.Level;
import com.saqfish.spdnet.scenes.GameScene;
import com.saqfish.spdnet.sprites.ItemSpriteSheet;

public class Flashbang extends Bomb {
	
	{
		image = ItemSpriteSheet.FLASHBANG;
	}

	@Override
	public void explode(int cell) {
		super.explode(cell);

		Level l = Dungeon.level;
		for (Char ch : Actor.chars()){
			if (ch.fieldOfView != null && ch.fieldOfView[cell]){
				int power = 16 - 4*l.distance(ch.pos, cell);
				if (power > 0){
					Buff.prolong(ch, Blindness.class, power);
					Buff.prolong(ch, Cripple.class, power);
					if (ch == Dungeon.hero){
						GameScene.flash(0x80FFFFFF);
					}
				}
			}
		}
		
	}
	
	@Override
	public int value() {
		//prices of ingredients
		return quantity * (20 + 40);
	}
}
