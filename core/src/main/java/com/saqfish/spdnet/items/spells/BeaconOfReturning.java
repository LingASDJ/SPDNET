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

package com.saqfish.spdnet.items.spells;

import com.saqfish.spdnet.Assets;
import com.saqfish.spdnet.Dungeon;
import com.saqfish.spdnet.actors.Actor;
import com.saqfish.spdnet.actors.Char;
import com.saqfish.spdnet.actors.buffs.Buff;
import com.saqfish.spdnet.actors.hero.Hero;
import com.saqfish.spdnet.actors.mobs.Mob;
import com.saqfish.spdnet.effects.Pushing;
import com.saqfish.spdnet.items.artifacts.TimekeepersHourglass;
import com.saqfish.spdnet.items.scrolls.ScrollOfTeleportation;
import com.saqfish.spdnet.items.scrolls.exotic.ScrollOfPassage;
import com.saqfish.spdnet.messages.Messages;
import com.saqfish.spdnet.plants.Swiftthistle;
import com.saqfish.spdnet.scenes.GameScene;
import com.saqfish.spdnet.scenes.InterlevelScene;
import com.saqfish.spdnet.sprites.ItemSprite;
import com.saqfish.spdnet.sprites.ItemSpriteSheet;
import com.saqfish.spdnet.utils.GLog;
import com.saqfish.spdnet.windows.WndOptions;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class BeaconOfReturning extends Spell {
	
	{
		image = ItemSpriteSheet.RETURN_BEACON;
	}
	
	public int returnDepth	= -1;
	public int returnBranch	= 0;
	public int returnPos;
	
	@Override
	protected void onCast(final Hero hero) {
		
		if (returnDepth == -1){
			setBeacon(hero);
		} else {
			GameScene.show(new WndOptions(new ItemSprite(this),
					Messages.titleCase(name()),
					Messages.get(BeaconOfReturning.class, "wnd_body"),
					Messages.get(BeaconOfReturning.class, "wnd_set"),
					Messages.get(BeaconOfReturning.class, "wnd_return")){
				@Override
				protected void onSelect(int index) {
					if (index == 0){
						setBeacon(hero);
					} else if (index == 1){
						returnBeacon(hero);
					}
				}
			});
			
		}
	}
	
	//we reset return depth when beacons are dropped to prevent
	//having two stacks of beacons with different return locations
	
	@Override
	protected void onThrow(int cell) {
		returnDepth = -1;
		super.onThrow(cell);
	}
	
	@Override
	public void doDrop(Hero hero) {
		returnDepth = -1;
		super.doDrop(hero);
	}
	
	private void setBeacon(Hero hero ){
		returnDepth = Dungeon.depth;
		returnBranch = Dungeon.branch;
		returnPos = hero.pos;
		
		hero.spend( 1f );
		hero.busy();
		
		GLog.i( Messages.get(this, "set") );
		
		hero.sprite.operate( hero.pos );
		Sample.INSTANCE.play( Assets.Sounds.BEACON );
		updateQuickslot();
	}
	
	private void returnBeacon( Hero hero ){

		if (returnDepth == Dungeon.depth && returnBranch == Dungeon.branch) {

			Char existing = Actor.findChar(returnPos);
			if (existing != null && existing != hero){
				Char toPush = !Char.hasProp(existing, Char.Property.IMMOVABLE) ? hero : existing;

				ArrayList<Integer> candidates = new ArrayList<>();
				for (int n : PathFinder.NEIGHBOURS8) {
					int cell = returnPos + n;
					if (!Dungeon.level.solid[cell] && Actor.findChar( cell ) == null
							&& (!Char.hasProp(toPush, Char.Property.LARGE) || Dungeon.level.openSpace[cell])) {
						candidates.add( cell );
					}
				}
				Random.shuffle(candidates);

				if (!candidates.isEmpty()){
					if (toPush == hero){
						returnPos = candidates.get(0);
					} else {
						Actor.addDelayed( new Pushing( toPush, toPush.pos, candidates.get(0) ), -1 );
						toPush.pos = candidates.get(0);
						Dungeon.level.occupyCell(toPush);
					}
				} else {
					GLog.w( Messages.get(ScrollOfTeleportation.class, "no_tele") );
					return;
				}
			}

			if (!ScrollOfTeleportation.teleportToLocation(hero, returnPos)){
				return;
			}

		} else {

			if (!Dungeon.interfloorTeleportAllowed()) {
				GLog.w( Messages.get(this, "preventing") );
				return;
			}

			TimekeepersHourglass.timeFreeze timeFreeze = Dungeon.hero.buff(TimekeepersHourglass.timeFreeze.class);
			if (timeFreeze != null) timeFreeze.disarmPressedTraps();
			Swiftthistle.TimeBubble timeBubble = Dungeon.hero.buff(Swiftthistle.TimeBubble.class);
			if (timeBubble != null) timeBubble.disarmPressedTraps();
			
			InterlevelScene.mode = InterlevelScene.Mode.RETURN;
			InterlevelScene.returnDepth = returnDepth;
			InterlevelScene.returnBranch = returnBranch;
			InterlevelScene.returnPos = returnPos;
			Game.switchScene( InterlevelScene.class );
		}
		hero.spendAndNext( 1f );
		detach(hero.belongings.backpack);
	}
	
	@Override
	public String desc() {
		String desc = super.desc();
		if (returnDepth != -1){
			desc += "\n\n" + Messages.get(this, "desc_set", returnDepth);
		}
		return desc;
	}
	
	private static final ItemSprite.Glowing WHITE = new ItemSprite.Glowing( 0xFFFFFF );
	
	@Override
	public ItemSprite.Glowing glowing() {
		return returnDepth != -1 ? WHITE : null;
	}
	
	private static final String DEPTH	= "depth";
	private static final String POS		= "pos";
	
	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( DEPTH, returnDepth );
		if (returnDepth != -1) {
			bundle.put( POS, returnPos );
		}
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle(bundle);
		returnDepth	= bundle.getInt( DEPTH );
		returnPos	= bundle.getInt( POS );
	}
	
	@Override
	public int value() {
		//prices of ingredients, divided by output quantity
		return Math.round(quantity * ((50 + 40) / 5f));
	}
	
	public static class Recipe extends com.saqfish.spdnet.items.Recipe.SimpleRecipe {
		
		{
			inputs =  new Class[]{ScrollOfPassage.class, ArcaneCatalyst.class};
			inQuantity = new int[]{1, 1};
			
			cost = 6;
			
			output = BeaconOfReturning.class;
			outQuantity = 5;
		}
		
	}
}
