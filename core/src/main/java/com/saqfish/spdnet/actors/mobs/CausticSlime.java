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

package com.saqfish.spdnet.actors.mobs;

import com.saqfish.spdnet.Dungeon;
import com.saqfish.spdnet.actors.Char;
import com.saqfish.spdnet.actors.buffs.Buff;
import com.saqfish.spdnet.actors.buffs.Ooze;
import com.saqfish.spdnet.items.quest.GooBlob;
import com.saqfish.spdnet.sprites.CausticSlimeSprite;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public class CausticSlime extends Slime {
	
	{
		spriteClass = CausticSlimeSprite.class;
		
		properties.add(Property.ACIDIC);
	}
	
	@Override
	public int attackProc( Char enemy, int damage ) {
		if (Random.Int( 2 ) == 0) {
			Buff.affect( enemy, Ooze.class ).set( Ooze.DURATION );
			enemy.sprite.burst( 0x000000, 5 );
		}
		
		return super.attackProc( enemy, damage );
	}
	
	@Override
	public void rollToDropLoot() {
		if (Dungeon.hero.lvl > maxLvl + 2) return;
		
		super.rollToDropLoot();
		
		int ofs;
		do {
			ofs = PathFinder.NEIGHBOURS8[Random.Int(8)];
		} while (Dungeon.level.solid[pos + ofs] && !Dungeon.level.passable[pos + ofs]);
		Dungeon.level.drop( new GooBlob(), pos + ofs ).sprite.drop( pos );
	}
}
