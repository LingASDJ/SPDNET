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

package com.saqfish.spdnet.items.potions;

import com.saqfish.spdnet.Assets;
import com.saqfish.spdnet.Dungeon;
import com.saqfish.spdnet.actors.blobs.Blob;
import com.saqfish.spdnet.actors.buffs.BlobImmunity;
import com.saqfish.spdnet.actors.buffs.Buff;
import com.saqfish.spdnet.actors.hero.Hero;
import com.saqfish.spdnet.effects.CellEmitter;
import com.saqfish.spdnet.effects.Speck;
import com.saqfish.spdnet.effects.SpellSprite;
import com.saqfish.spdnet.messages.Messages;
import com.saqfish.spdnet.sprites.ItemSpriteSheet;
import com.saqfish.spdnet.utils.BArray;
import com.saqfish.spdnet.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PathFinder;

import java.util.ArrayList;

public class PotionOfPurity extends Potion {
	
	private static final int DISTANCE	= 3;
	
	private static ArrayList<Class> affectedBlobs;

	{
		icon = ItemSpriteSheet.Icons.POTION_PURITY;
		
		affectedBlobs = new ArrayList<>(new BlobImmunity().immunities());
	}

	@Override
	public void shatter( int cell ) {
		
		PathFinder.buildDistanceMap( cell, BArray.not( Dungeon.level.solid, null ), DISTANCE );
		
		ArrayList<Blob> blobs = new ArrayList<>();
		for (Class c : affectedBlobs){
			Blob b = Dungeon.level.blobs.get(c);
			if (b != null && b.volume > 0){
				blobs.add(b);
			}
		}
		
		for (int i=0; i < Dungeon.level.length(); i++) {
			if (PathFinder.distance[i] < Integer.MAX_VALUE) {
				
				for (Blob blob : blobs) {
					blob.clear(i);
				}
				
				if (Dungeon.level.heroFOV[i]) {
					CellEmitter.get( i ).burst( Speck.factory( Speck.DISCOVER ), 2 );
				}
				
			}
		}
		
		
		if (Dungeon.level.heroFOV[cell]) {
			splash(cell);
			Sample.INSTANCE.play(Assets.Sounds.SHATTER);

			identify();
			GLog.i(Messages.get(this, "freshness"));
		}
		
	}
	
	@Override
	public void apply( Hero hero ) {
		GLog.w( Messages.get(this, "protected") );
		Buff.prolong( hero, BlobImmunity.class, BlobImmunity.DURATION );
		SpellSprite.show(hero, SpellSprite.PURITY);
		identify();
	}
	
	@Override
	public int value() {
		return isKnown() ? 40 * quantity : super.value();
	}
}
