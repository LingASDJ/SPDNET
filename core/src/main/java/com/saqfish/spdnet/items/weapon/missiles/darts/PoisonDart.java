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

package com.saqfish.spdnet.items.weapon.missiles.darts;

import com.saqfish.spdnet.Dungeon;
import com.saqfish.spdnet.actors.Char;
import com.saqfish.spdnet.actors.buffs.Buff;
import com.saqfish.spdnet.actors.buffs.Poison;
import com.saqfish.spdnet.sprites.ItemSpriteSheet;

public class PoisonDart extends TippedDart {
	
	{
		image = ItemSpriteSheet.POISON_DART;
	}
	
	@Override
	public int proc(Char attacker, Char defender, int damage) {
		
		Buff.affect( defender, Poison.class ).set( 3 + Dungeon.scalingDepth() / 2 );
		
		return super.proc(attacker, defender, damage);
	}
}
